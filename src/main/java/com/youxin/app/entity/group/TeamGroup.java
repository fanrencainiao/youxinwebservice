package com.youxin.app.entity.group;

import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import lombok.Data;

/**
 *  群组分组查询
 * @author cf
 * @date 2020年4月10日 上午10:26:25
 */
@Data
public class TeamGroup {
	@Id
	private String to;
	private String fromAccount;
	private int count;
}
