package com.session.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.session.common.Constants;
import com.session.common.SimpleDBUtil;
import com.session.rpc.RPCClient;

/**
 * This class is responsible for caching and managing Group Membership View
 * information
 * 
 * @author karthik
 */
public class MembershipViewStore {
	private final Map<String, Member> membershipView = new ConcurrentHashMap<>();

	private final ScheduledExecutorService gossipExecutorService = Executors.newSingleThreadScheduledExecutor();

	private static MembershipViewStore INSTANCE = null;

	private MembershipViewStore() {
	}

	public static MembershipViewStore getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new MembershipViewStore();
		}

		return INSTANCE;
	}

	public void initialise() {
		SimpleDBUtil.getInstance().writeSelf();
		loadViews();
		spawnExchangeViews();
		
		System.out.println("Initial Membership View: "+membershipView);
	}

	public void destroy() {
		gossipExecutorService.shutdown();
	}

	/**
	 * Background thread runs periodically and gossips with other servers (and
	 * SimpleDB) in the membership view. *
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void spawnExchangeViews() {
		gossipExecutorService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					// sleep random time to avoid any convoys
					sleepRandom();

					System.out.println("Exchanging Views Now ...");

					int indexToRandMember = (int) (Math.random() * (membershipView.size())) + 1;

					// gossip with other server
					if (indexToRandMember < membershipView.size()) {
						Member member = new ArrayList<Member>(membershipView.values()).get(indexToRandMember);
						System.out.println("Gossip initiated with Server: "+member.getServerID());

						Map<String, Member> otherMembershipView = RPCClient.getInstance().exchangeViews(member.getServerID(), membershipView);
						if (otherMembershipView == null)
							update(member.getServerID(), Member.Status.DOWN);
						else {
							update(member.getServerID(), Member.Status.UP);
							mergeViews(otherMembershipView);
						}
					}
					// gossip with SimpleDB
					else {
						System.out.println("Gossip initiated with Simple DB");
						Map<String, Member> otherMembershipView = SimpleDBUtil.getInstance().readAll();
						mergeViews(otherMembershipView);
						SimpleDBUtil.getInstance().writeAll(membershipView.values());
					}

					System.out.println("MembershipView post gossip: " + membershipView);
				} catch (Exception e) {
					System.out.println("Gossip failed: " + e);
				}
			}
		}, Constants.GOSSIP_INTERVAL_MINS, Constants.GOSSIP_INTERVAL_MINS, TimeUnit.MINUTES);
	}

	/**
	 * When a group member informs us of his view, we need to merge both views
	 * and create a new merged view
	 * 
	 * @param otherMembershipView
	 */
	public void mergeViews(Map<String, Member> otherMembershipView) {
		for (Entry<String, Member> entry : otherMembershipView.entrySet()) {
			if (get(entry.getKey()) == null)
				add(entry.getValue());
			else if (get(entry.getKey()).getLastSeenTimeInMillis() < entry.getValue().getLastSeenTimeInMillis())
				add(entry.getValue());
		}
	}

	private void sleepRandom() {
		try {
			int randDelay = (int) Math.random() * Constants.MAX_RANDOM_GOSSIP_DELAY_MILLIS;
			Thread.sleep(randDelay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load Membership View information from SimpleDB on server startup
	 */
	private void loadViews() {
		Map<String, Member> members = SimpleDBUtil.getInstance().readAll();
		membershipView.putAll(members);
		membershipView.remove(Constants.HOST_IP);
	}

	public void add(Member member) {
		if (!member.getServerID().equals(Constants.HOST_IP))
			membershipView.put(member.getServerID(), member);
	}

	public Member get(String serverID) {
		Member member = membershipView.get(serverID);
		if (member != null)
			member.updateLastSeenTime();
		return member;
	}

	public void update(String serverID, Member.Status status) {
		Member member = membershipView.get(serverID);

		if (member == null) {
			member = new Member(serverID);
			add(member);
		}

		member.setStatus(status);
		member.updateLastSeenTime();
	}

	public Map<String, Member> getMembershipView() {
		return membershipView;
	}

	/**
	 * Return all active members from the membership view in a shuffled order
	 * 
	 * @return
	 */
	public List<Member> getAllActive() {
		List<Member> activeMembers = new ArrayList<>();

		for (Member member : membershipView.values()) {
			if (member.getStatus().equals(Member.Status.UP))
				activeMembers.add(member);
		}

		Collections.shuffle(activeMembers);
		return activeMembers;
	}

	/**
	 * Return all members from the membership view
	 * 
	 * @return
	 */
	public List<Member> getAll() {
		return new ArrayList<>(membershipView.values());
	}
}
