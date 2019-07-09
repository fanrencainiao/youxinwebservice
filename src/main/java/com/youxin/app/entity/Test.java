package com.youxin.app.entity;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexed;

import lombok.Data;

@Entity(value = "test", noClassnameStored = true)
@Data
public class Test{
	@Id
	private ObjectId tid;
	@Indexed
	private String tname;

}
