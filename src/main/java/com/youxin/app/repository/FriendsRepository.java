package com.youxin.app.repository;



import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.youxin.app.yx.request.Friends;


@Repository
public class FriendsRepository  extends BasicDAO<Friends, ObjectId>{
	
	@Autowired
	public FriendsRepository(@Qualifier("get") Datastore ds) {
		super(ds);
	}
	

}
