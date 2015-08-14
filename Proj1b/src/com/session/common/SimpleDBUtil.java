package com.session.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.session.view.Member;

/**
 * This class is responsible for handling all reads and writes from/to Simple DB 
 * @author karthik
 */
public class SimpleDBUtil {
	private AmazonSimpleDB sdb;
	private static SimpleDBUtil INSTANCE;

	private SimpleDBUtil() {
	}

	public static SimpleDBUtil getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new SimpleDBUtil();
			INSTANCE.init();
		}

		return INSTANCE;
	}

	public void init() {
		String accessKey = Constants.ACCESS_KEY;
		String secretKey = Constants.SECRET_KEY;
		
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		sdb = new AmazonSimpleDBClient(credentials);

		// If domain doesnt exist, then create it
		if (!sdb.listDomains().getDomainNames().contains(Constants.SIMPLE_DB_DOMAIN)) {
			sdb.createDomain(new CreateDomainRequest(Constants.SIMPLE_DB_DOMAIN));
		}
	}
	
	/**
	 * Register this host into Simple DB by adding a new entry with HOST_IP
	 */
	public void writeSelf() {
		List<ReplaceableItem> data = new ArrayList<ReplaceableItem>();

		data.add(new ReplaceableItem().withName(Constants.HOST_IP).withAttributes(
				new ReplaceableAttribute().withName("ServerID").withValue(Constants.HOST_IP),
				new ReplaceableAttribute().withName("Status").withValue(Member.Status.UP.toString()),
				new ReplaceableAttribute().withName("LastSeenTime").withValue(String.valueOf(System.currentTimeMillis()))));
		
		sdb.batchPutAttributes(new BatchPutAttributesRequest(Constants.SIMPLE_DB_DOMAIN, data));
	}
	
	/**
	 * Read the entire membership view of this server from SimpleDB 
	 */
	public Map<String, Member> readAll() {
		Map<String, Member> members = new HashMap<>();

		String qry = "select * from " + Constants.SIMPLE_DB_DOMAIN;
		SelectRequest selectRequest = new SelectRequest(qry);
		for (Item item : sdb.select(selectRequest).getItems()) {
			Member member = new Member(item.getName());

			for (Attribute attribute : item.getAttributes()) {
				if (attribute.getName().equals("Status")) {
					Member.Status status = attribute.getValue().equals("UP") ? Member.Status.UP : Member.Status.DOWN;
					member.setStatus(status);
				} else if (attribute.getName().equals("LastSeenTime")) {
					String lastSeenTime = attribute.getValue();
					member.setLastSeenTimeInMillis(Long.parseLong(lastSeenTime));
				}
			}

			members.put(member.getServerID(), member);
		}
		
		if(members.isEmpty())
			System.out.println("SimpleDB is empty");
		
		return members;
	}
	
	/**
	 * Write the membership view (union-merged) from the server cache into SimpleDB 
	 * @param members
	 */
	public void writeAll(Collection<Member> members) {
		List<ReplaceableItem> data = new ArrayList<ReplaceableItem>();
		
		for (Member member : members) {
			data.add(new ReplaceableItem().withName(member.getServerID()).withAttributes(
					new ReplaceableAttribute().withName("ServerID").withValue(member.getServerID()),
					new ReplaceableAttribute().withName("Status").withValue(member.getStatus().name()),
					new ReplaceableAttribute().withName("LastSeenTime").withValue(String.valueOf(member.getLastSeenTimeInMillis()))));
		}
		
		if(!data.isEmpty())
			sdb.batchPutAttributes(new BatchPutAttributesRequest(Constants.SIMPLE_DB_DOMAIN, data));
		else
			System.out.println("MembershipView empty. Not writing anything to Simple DB");
	}
	
	/**
	 * For testing purpose only ...
	 */
	public void cleanup() {
		DeleteDomainRequest qry = new DeleteDomainRequest();
		qry.setDomainName(Constants.SIMPLE_DB_DOMAIN);
		sdb.deleteDomain(qry);
		sdb.createDomain(new CreateDomainRequest(Constants.SIMPLE_DB_DOMAIN));
	}
	
	/**
	 * For testing only ...
	 * @param args
	 */
	public static void main(String[] args) {
		//SimpleDBUtil.getInstance().writeAll(members);
		//System.out.println(SimpleDBUtil.getInstance().readAll());
		SimpleDBUtil.getInstance().cleanup();
	}
}
