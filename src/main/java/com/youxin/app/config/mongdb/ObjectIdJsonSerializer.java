package com.youxin.app.config.mongdb;

import java.io.IOException;

import org.bson.types.ObjectId;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import java.lang.reflect.Type;

/**
 * mongdb Object序列化问题
 * 
 * @author cf
 *
 */
public class ObjectIdJsonSerializer implements ObjectSerializer {

	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
			throws IOException {
		SerializeWriter out = serializer.getWriter();
		if (object == null) {
			serializer.getWriter().writeNull();
			return;
		}
		out.write("\"" + ((ObjectId) object).toString() + "\"");
	}
}
