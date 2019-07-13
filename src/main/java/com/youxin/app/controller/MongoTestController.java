package com.youxin.app.controller;

import org.mongodb.morphia.Key;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.youxin.app.utils.Result;

@RestController
@RequestMapping("/user/")
public class MongoTestController extends AbstractController{
	

//	@Autowired
//	private TestRepository tm;
//	
//	@RequestMapping("motests")
//	public Object motests(){
//		Test test=new Test();
//		test.setTname("ssmm");
//		Key<Test> tt=tm.save(test);
//		tt.getCollection();
//		tt.getClass();
//		tt.getType();
//		System.out.println(tm.find().asList().get(0).getTid());
//		log.debug(tm.find().asList().get(0).getTid());
//		return Result.success(tm.find().asList());
//	}

}
