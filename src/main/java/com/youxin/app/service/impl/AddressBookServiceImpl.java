package com.youxin.app.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.BasicBSONObject;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.youxin.app.entity.AddressBook;
import com.youxin.app.entity.Config;
import com.youxin.app.entity.KSession;
import com.youxin.app.entity.User;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.repository.AddressBookRepository;
import com.youxin.app.service.ConfigService;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.MongoOperator;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.ThreadUtil;
import com.youxin.app.utils.supper.Callback;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.Friends;


@Service
public class AddressBookServiceImpl {
	Log log=LogFactory.getLog(getClass());
	@Autowired
	private AddressBookRepository abr;
	@Autowired
	@Qualifier("get") 
	private Datastore ds;
	@Autowired
	private UserService uservice;
	@Autowired
	private ConfigService cs;
	
	public List<AddressBook> uploadTelephone(User user,String deleteStr,String uploadStr,String uploadJsonStr){
//		public List<AddressBook> uploadTelephone(KSession session,String deleteStr,String uploadStr){
		List<AddressBook> books = null;
//		String telephone=session.getTelephone();
		if(!StringUtil.isEmpty(deleteStr))
			deleteByStrs(user.getMobile(), deleteStr);
		else if(!StringUtil.isEmpty(uploadStr))
			throw new ServiceException("旧版已弃用");
//			books =	uploadTelephone(user.getId(),user.getAccid(),user.getMobile(), uploadStr);
		else if(!StringUtil.isEmpty(uploadJsonStr)){
			books =	uploadJsonTelephone(user.getId(),user.getAccid(), user.getMobile(), uploadJsonStr);
		}
		return books;
	}
	
	/** @Description:（新版通讯录） 
	* @param userId
	* @param telephone
	* @param strs
	* @return
	**/ 
	private List<AddressBook> uploadJsonTelephone(Integer userId,String accid, String telephone, String uploadJsonStr ) {
		List<AddressBook> address = JSONObject.parseArray(uploadJsonStr, AddressBook.class);
		List<AddressBook> reports = new ArrayList<AddressBook>();
		for(int i = 0; i < address.size(); i++){
			String repPhone = address.get(i).getToTelephone();
			String toTelephone = repPhone.replace(" ", "");
//			toTelephone = toTelephone.replace("-", "");
			User user = null;
				if (0 < get(telephone, toTelephone))
					continue;// 不能让自己成为自己的通讯录好友
				if(toTelephone.equals(telephone))
					continue;
				user = uservice.getUserByMobile(toTelephone);
				AddressBook saveBook = saveBook(telephone, toTelephone, user, userId, accid,address.get(i).getToRemarkName());
				if(saveBook.getRegisterEd()==1)
					reports.add(saveBook);
		}
		log.info("====>  导入完成后：   用户：  "+userId  +"  的 通讯录好友： "+JSONObject.toJSONString(reports));
		return reports;
	}
	
	/** @Description:（普通版通讯录） 
	* @param userId
	* @param telephone
	* @param strs
	* @return
	**/ 
	private List<AddressBook> uploadTelephone(Integer userId,String accid,String telephone, String strs) {
		List<AddressBook> reports = new ArrayList<AddressBook>();
		strs = strs.replace(" ", "");
		strs = strs.replace("-", "");
		String[] array = strs.split(",");
		User user = null;
		for (String str : array) {
			if (0 < get(telephone, str))
				continue;// 不能让自己成为自己的通讯录好友
			user = uservice.getUserByMobile(str);
			if(str.equals(telephone))
				continue;
			AddressBook saveBook = saveBook(telephone, str, user, userId);
			if(saveBook.getRegisterEd()==1)
				reports.add(saveBook);
		}
		return reports;
	}

	private AddressBook saveBook(String telephone, String str, User user, Integer userId,String accid, String toRemark) {
		AddressBook book = null;
		book = new AddressBook();
		book.setTelephone(telephone);
		book.setToTelephone(str);
		book.setRegisterEd(null == user ? 0 : 1);
		book.setUserId(userId);
		book.setToAccid(null == user ? null : user.getAccid());
		book.setToUserId(null == user ? null : user.getId());
		book.setRegisterTime(null == user ? 0 : (null == user.getCreateTime()?0:user.getCreateTime()));
		book.setToUserName(null == user ? null : user.getName());
		book.setToRemarkName(toRemark);
		Config config = cs.getConfig();
		if(null != user){
			JSONObject friends = SDKService.friendGet(accid, 0l, 0l);
			log.info("好友列表："+friends);
			JSONArray jsonArray = friends.getJSONArray("friends");
			List<Friends> listFrieds = JSONArray.parseArray(jsonArray.toJSONString(), Friends.class);
			boolean isFriends=false;
			
			for(Friends f:listFrieds) {
				//查询朋友的手机号
				User fUser = uservice.getUserFromDB(f.getFaccid());
				if(fUser!=null&&fUser.getMobile().equals(str))
					isFriends=true;
			}
			if(!isFriends && 0 == config.getIsAutoAddressBook())
				book.setStatus(0);
			else if(isFriends)
				book.setStatus(2);
			else if(0 == config.getIsAutoAddressBook()){// 不自动添加
				book.setStatus(0);
			}else if (1 == config.getIsAutoAddressBook()) {
				book.setStatus(1);
			}
		}else {// 没有注册im
			book.setStatus(0);
		}
		abr.save(book);
		if(null != user && config.getIsAutoAddressBook()==1){
			Map<String, String> bookMap = Maps.newConcurrentMap();
			bookMap.put("faccid", user.getAccid());
			bookMap.put("toRemark", toRemark);
			log.info("===》       自动添加好友：     "+JSONObject.toJSONString(bookMap));
			autofollowUser(accid, bookMap);
		}
		return book;
	}

	private AddressBook saveBook(String telephone, String str, User user, Integer userId) {
		AddressBook book = null;
		book = new AddressBook();
		book.setTelephone(telephone);
		book.setToTelephone(str);
		book.setRegisterEd(user == null ? 0 : 1);
		book.setUserId(userId);
		book.setToUserId(user == null ? null : user.getId());
		book.setRegisterTime(user == null ? 0 : user.getCreateTime());
		book.setToUserName(user == null ? null : user.getName());
		abr.save(book);
		return book;
	}
	
	/** @Description:（自动成为好友） 
	* @param toUserId
	* @param addressBook
	**/ 
	public void autofollowUser(String accid,Map<String, String> addressBook){
//		SKBeanUtils.getFriendsManager().autofollowUser(userId, addressBook);
		Friends f=new Friends();
		f.setAccid(accid);
		f.setFaccid(addressBook.get("faccid").toString());
		f.setMsg(addressBook.get("toRemark").toString());
		f.setType(1);
		SDKService.friendAdd(f);
	}
	
	
	
//	public void notifyBook(String telephone,Integer userId,String nickName,Long registerTime){
//		System.out.println("注册时修改数据："+"telephone:"+telephone+"   toUserId:"+userId+"    nickName:"+nickName+"   registerTime:"+registerTime);
//		DBCollection lastdbCollection=null;
//		lastdbCollection = getDatastore().getDB().getCollection("AddressBook");
//		BasicDBObject lastquery=new BasicDBObject();
//		lastquery.put("registerEd", 0);
//		lastquery.put("toTelephone", telephone);
//		BasicDBObject values=new BasicDBObject();
//		values.put("registerEd", 1);
//		values.put("toUserName", nickName);
//		values.put("registerTime", registerTime);
//		values.put("toUserId", userId);
//		values.put("status", 0 == getSystemConfig().getIsAutoAddressBook() ? 0 : 1);
//		lastdbCollection.update(lastquery,new BasicDBObject(MongoOperator.SET, values) ,false,true);
//		ThreadUtil.executeInThread(new Callback() {
//			@Override
//			public void execute(Object obj) {
//				notifyBook(telephone);
//			}
//			
//		});
//	}
//	public void notifyBook(String telephone){
//		System.out.println("推送使用的电话号码："+telephone);
//		//a注册   a给b发xmpp通知他我们成为通讯录好友
//		List<AddressBook> list = get(telephone);
//		ThreadUtil.executeInThread(new Callback() {
//			@Override
//			public void execute(Object obj) {
//					for(AddressBook book :list){
//						MessageBean messageBean=new MessageBean();
//						messageBean.setType(KXMPPServiceImpl.registAddressBook);
//						messageBean.setFromUserId(String.valueOf(book.getToUserId()));
//						messageBean.setFromUserName(book.getToUserName());
//						messageBean.setToUserId(String.valueOf(book.getId()));
//						messageBean.setToUserName(SKBeanUtils.getUserManager().getNickName(book.getId()));
//						messageBean.setContent(JSONObject.toJSON(book));
//						messageBean.setMsgType(0);// 单聊消息
//						messageBean.setMessageId(StringUtil.randomUUID());
//						try {
//							KXMPPServiceImpl.getInstance().send(messageBean);
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//						
//					}
//			}
//		});
//	}
	
	private void deleteByStrs(String telephone,String strs){
		strs=strs.replace(" ", "");
		strs=strs.replace("-", "");
		String[] deleteArray=strs.split(",");
		Query<AddressBook> query=abr.createQuery();
		abr.createQuery().filter("telephone", telephone);
		query.filter("toTelephone", new BasicBSONObject(MongoOperator.IN, deleteArray));
		abr.deleteByQuery(query);
	}
	
	public List<AddressBook> findRegisterList(String telephone,int pageIndex,int pageSize){
		Query<AddressBook> query=abr.createQuery();
		query.filter("registerEd", 1);
		query.filter("telephone",telephone);
		List<AddressBook> list=query.offset(pageIndex*pageSize).limit(pageSize).asList();
		list.forEach(book ->{
			book.setToUserName(uservice.getUserName(book.getToUserId()));
		});
		
		return list;
	}
	public List<AddressBook> get(String toTelephone){
		Query<AddressBook> query=abr.createQuery();
		query.filter("toTelephone", toTelephone);
//		query.filter("registerEd", 0);
		List<AddressBook> asList = query.asList();
		System.out.println("通讯录好友："+asList);
		return query.asList();
	}
	public long get(String telephone,String toTelephone){
		Query<AddressBook> query=abr.createQuery();
		query.filter("telephone", telephone);
		query.filter("toTelephone", toTelephone);
		return query.countAll();
	}
	
	public boolean get(String telephone,Integer toUserId){
		Query<AddressBook> query=abr.createQuery();
		query.filter("telephone", telephone);
		query.filter("toUserId", toUserId);
		return query.get()!=null;
	}
	
	public void delete(String telephone,String toTelephone,Integer userId){
		Query<AddressBook> query=abr.createQuery();
		if(!StringUtil.isEmpty(telephone))
			query.filter("telephone", telephone);
		if(!StringUtil.isEmpty(telephone))
			query.filter("toTelephone", toTelephone);
		if(0 != userId)
			query.filter("userId", userId);
		abr.deleteByQuery(query);
	}
	
	public List<AddressBook> getAll(String telephone,int pageIndex,int pageSize){
		Query<AddressBook> query=abr.createQuery();
		query.filter("telephone", telephone);
		query.filter("registerEd", 1);// 注册im的通讯录的人
		return query.asList();
	}
	
	public void checkAddressBook(String toTelephone,Integer toUserId){
		DBObject q=new BasicDBObject("toTelephone", toTelephone);
		List<Object> list=ds.getCollection(AddressBook.class).distinct("telephone",q);
		BasicDBObject query=null;
		BasicDBObject obj=null;
		BasicDBObject value=null;
		
		String telephone=null;
		for (Object str : list) {
			telephone=str.toString();
			query=new BasicDBObject("telephone", str.toString());
			query.append("toTelephone", toTelephone);
			obj=(BasicDBObject) ds.getCollection(AddressBook.class).findOne(query);
			if(1==obj.getInt("registerEd"))
				continue;
			query.append("registerEd", 0);
			value=new BasicDBObject("registerEd", 1);
			long registerTime= DateUtil.currentTimeSeconds();
			value.append("registerTime",registerTime);
			value.append("toUserId", toUserId);
			
		}
		
	}
	//注销手机号码
	public void writeOffUser(String telephone){
		BasicDBObject value=new BasicDBObject("registerEd", 0);
		value.append("registerTime",0);
//		updateAttributeSet("AddressBook", "toTelephone", telephone, value);
		
		BasicDBObject query=new BasicDBObject("toTelephone", telephone);
		BasicDBObject values=new BasicDBObject("$set",value);
		ds.getCollection(AddressBook.class).update(query, values);
	}
	
//	private void push(Integer receiver,Integer fromUserId,String telephone,long registerTime){
//		JSONObject json=new JSONObject();
//		//消息类型  审核专长
//		//json.put("type", KConstants.MsgType.AddressBook);
//		json.put("timeSend", DateUtil.currentTimeSeconds());
//		json.put("messageId", UUID.randomUUID());
//		
//		long from=KConstants.SystemNo.AddressBook;
//		json.put("objectId", fromUserId);
//		
//		json.put("fromUserId", from);
//		json.put("fromUserName",ConstantUtil.getMsgByCode(from+"", "").getValue());
//		JSONObject contentJson=new JSONObject();
//		
//		contentJson.put("registerTime", registerTime);
//		contentJson.put("phone", telephone);
//		json.put("content",contentJson.toJSONString());
//		
//		
//	}
	
	// 获取通讯录好友
	public List<Integer> getAddressBookUserIds(Integer userId){
		Query<AddressBook> query = abr.createQuery().field("userId").equal(userId).retrievedFields(true, "toUserId");
		query.or(query.criteria("status").equal(1),query.criteria("status").equal(2));
		List<Integer> userIds = new ArrayList<Integer>();
		query.asList().forEach(addressBook ->{
			userIds.add(addressBook.getToUserId());
		});
		log.debug("用户 "+userId+" : 的通讯录好友:{"+JSONObject.toJSONString(userIds)+"}");
		return userIds;
	}
	

}
