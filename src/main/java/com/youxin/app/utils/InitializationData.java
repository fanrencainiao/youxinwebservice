package com.youxin.app.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBCollection;
import com.youxin.app.entity.Role;
import com.youxin.app.entity.User;
import com.youxin.app.service.UserService;



/** @version:（1.0） 
* @ClassName	InitializationData
* @Description: （初始化数据） 
*/
@Component
public class InitializationData  implements CommandLineRunner {
	protected Log log=LogFactory.getLog(InitializationData.class);
	
	
//	@Value("classpath:data/message.json")
//	private Resource resource;
	
	@Autowired
	@Qualifier("get")
	private Datastore dfds;
	
	@Autowired
	private UserService userService;

	
	

	@Override
	public void run(String... args) throws Exception {
		
		initSuperAdminData();
		
//		initErrorMassageData();
		
	}
	
	
	/**
       * 初始化异常信息数据
	* @throws Exception
	*/
//	private void initErrorMassageData() throws Exception{
//		if(null==resource) {
//			System.out.println("error initErrorMassageData  resource is null");
//			return;
//		}
//		DBCollection errMsgCollection = getDatastore().getCollection(ErrorMessage.class);
//		
//		if(errMsgCollection == null || errMsgCollection.count()==0) {
//			
//			BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
//			StringBuffer message = new StringBuffer();
//			String line = null;
//			while ((line = br.readLine()) != null) {
//				message.append(line);
//			}
//			String defaultString = message.toString();
//			if(!StringUtil.isEmpty(defaultString)){
//				List<ErrorMessage> errorMessages = JSONObject.parseArray(defaultString, ErrorMessage.class);
//				errorMessages.forEach(errorMessage ->{
//					getDatastore().save(errorMessage);
//				});
//				
//			}
//			log.info("\n"+">>>>>>>>>>>>>>> 异常信息数据初始化完成  <<<<<<<<<<<<<");
//		}
//	}
	
	
	/**
        * 初始化默认超级管理员数据
	*/
	private void initSuperAdminData() {

		DBCollection adminCollection = dfds.getCollection(Role.class);
		if (adminCollection == null || adminCollection.count() == 0) {
			try {
				User user = new User();
				user.setId(1000);
				user.setName("1000");
				user.setMobile("861000");
				user.setPassword(DigestUtils.md5Hex("1000"));
				user.setCreateTime(DateUtil.currentTimeSeconds());
				dfds.save(user);
//				KXMPPServiceImpl.getInstance().registerAndXmppVersion(user.getUserId() + "", user.getPassword());
			} catch (Exception e) {
				e.printStackTrace();
			}
			Role role = new Role(1000, "1000", (byte) 6, (byte) 1, 0);
			dfds.save(role);
			
			// 初始化10000号
			try {
				User u=new User();
				u.setMobile("8610000");
				u.setId(10000);
				u.setName("10000");
				u.setPassword(DigestUtils.md5Hex("10000"));
				u.setCreateTime(DateUtil.currentTimeSeconds());
				dfds.save(u);
//				KXMPPServiceImpl.getInstance().registerSystemNo("10000", DigestUtils.md5Hex("10000"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			log.info("\n" + ">>>>>>>>>>>>>>> 默认管理员数据初始化完成  <<<<<<<<<<<<<");
		}
		
		Query<User> query = dfds.createQuery(User.class);
		query.field("_id").equal(1100);
		if(query.get()==null){
			// 初始化1100号 作为金钱相关通知系统号码
			try {
				User u=new User();
				u.setMobile("861100");
				u.setId(1100);
				u.setName("1100");
				u.setPassword(DigestUtils.md5Hex("1100"));
				u.setCreateTime(DateUtil.currentTimeSeconds());
				dfds.save(u);
//				KXMPPServiceImpl.getInstance().registerSystemNo("1100", DigestUtils.md5Hex("1100"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			log.info("\n" + ">>>>>>>>>>>>>>> 默认系统通知数据初始化完成  <<<<<<<<<<<<<");
		}
		
		
	}
	
	
}
