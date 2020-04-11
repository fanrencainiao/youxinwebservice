//package com.youxin.app;
//
//import static org.mongodb.morphia.aggregation.Group.grouping;
//import static org.mongodb.morphia.aggregation.Group.id;
//
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mongodb.morphia.Datastore;
//import org.mongodb.morphia.aggregation.Accumulator;
//import org.mongodb.morphia.aggregation.AggregationPipeline;
//import org.mongodb.morphia.query.Query;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.youxin.app.entity.MessageReceive;
//import com.youxin.app.entity.group.TeamGroup;
//import com.youxin.app.repository.MessageReceiveRepository;
//import com.youxin.app.yx.SDKService;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes=YouxinApplication.class)
//public class MongdbGroupTest {
//	@Autowired
//	@Qualifier("get")
//	private Datastore dfds;
//	@Autowired
//	private MessageReceiveRepository mrr;
//	@Test
//	public void teamGroupTest() {
////		Query<MessageReceive> q = mrr.createQuery();
////		q.field("convType").equal("TEAM");
////		
////		AggregationPipeline pipeline  = dfds.createAggregation(MessageReceive.class);
////		pipeline.match(q).group(id(grouping("to")));
////		pipeline.skip(0);
////		pipeline.limit(10);
////		
////		Iterator<TeamGroup> iterator  = pipeline.aggregate(TeamGroup.class);
////		 System.out.println(iterator.hasNext());
////		 Set<String> tids=new HashSet<>();
////		 while (iterator.hasNext()) {
////			 TeamGroup ug = iterator.next();
////			 System.out.println(JSON.parseObject(ug.getTo()).getString("to"));
////			
////			 tids.add(JSON.parseObject(ug.getTo()).getString("to"));
////         }
////		 System.out.println(tids.size());
////		 String[] array = tids.toArray(new String[tids.size()]);
////	
////		 com.youxin.app.yx.request.team.QueryDetail tq=new com.youxin.app.yx.request.team.QueryDetail();
////		 tq.setTid(2790523046l);
////		 JSONObject teamQuery = SDKService.teamQueryDetail(tq);
////		System.out.println(teamQuery.getJSONObject(null));
//	}
//}
