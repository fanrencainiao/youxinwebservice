package com.youxin.app.repository;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.youxin.app.entity.Config;

/**
 * 
 * @author cf
 * @date 2019年10月22日 上午10:21:25
 */
@Repository
public class ConfigRepository extends BasicDAO<Config, ObjectId>{
	@Autowired
	public ConfigRepository(@Qualifier("get") Datastore ds) {
		super(ds);
	}

}
