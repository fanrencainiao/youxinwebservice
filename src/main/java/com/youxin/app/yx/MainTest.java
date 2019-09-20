package com.youxin.app.yx;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.User;
import com.youxin.app.utils.StringUtil;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Firrela
 * @time 2016/3/7.
 */
public class MainTest {

    private static Logger logger = LoggerFactory.getLogger(MainTest.class);

    private static String APPKEY = "faeb0ec0ce3871b699119420790c8789";  //AppKey
    private static String SECRET = "d53d9d0cd80b";  //AppSecret

    public static final void main(String[] args) throws IOException {
//    	String accid=StringUtil.randomUUID();
//    	User u=new User();
//    	u.setAccid(accid);
//    	u.setName(accid);
//    	u.setGender(1);
//    	
//        JSONObject res = SDKService.createUser(u);
////    	
////    	JSONArray accids=new JSONArray();
////    	accids.add("04343009cac34bd1b47faa722a3dd71f");
////    	String res = getUinfos(accids.toString());
//    	
//    	//accid没有区分大小写
////        String res = updateUinfo("HelloWorld","cfs","13628271337");
//        User us=JSONObject.toJavaObject(res, User.class);
//        u.setName("sds");
//        BeanUtils.copyProperties(us, u,"name");
//        System.out.println(us.toString());
//        System.out.println(u.toString());
        //TODO: 对结果的业务处理，如解析返回结果，并保存成功注册的用户
    }
    /**
     *  用户注册
     * @param accid
     * @param name
     * @param token
     * @return
     * @throws IOException
     */
    public static String createUser(User user) throws IOException {
        String url = "https://api.netease.im/nimserver/user/create.action";
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("accid", user.getAccid()));
        params.add(new BasicNameValuePair("name", user.getName()));
        params.add(new BasicNameValuePair("props", user.getProps()));
        params.add(new BasicNameValuePair("icon", user.getIcon()));
        params.add(new BasicNameValuePair("token", user.getToken()));
        params.add(new BasicNameValuePair("sign", user.getSign()));
        params.add(new BasicNameValuePair("email", user.getEmail()));
        params.add(new BasicNameValuePair("birth", user.getBirth()));
        params.add(new BasicNameValuePair("mobile", user.getMobile()));
        params.add(new BasicNameValuePair("gender", user.getGender()+""));
        params.add(new BasicNameValuePair("ex", user.getEx()));

        //UTF-8编码,解决中文问题
        HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");

        String res = NIMPost.postNIMServer(url, entity, APPKEY, SECRET);
        logger.info("createUser httpRes: {}", res);
        return res;
    }
    /**
     * 
     * @param accids
     * @return
     * @throws IOException
     */
    public static String updateUinfo(String accid,String name,String mobile) throws IOException {
    	String url="https://api.netease.im/nimserver/user/updateUinfo.action";
    	 List<NameValuePair> params = new ArrayList<NameValuePair>();
    	 params.add(new BasicNameValuePair("accid", accid));
    	 params.add(new BasicNameValuePair("name", name));
    	 params.add(new BasicNameValuePair("mobile", mobile));
    	 //UTF-8编码,解决中文问题
         HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");

         String res = NIMPost.postNIMServer(url, entity, APPKEY, SECRET);
         logger.info("createUser httpRes: {}", res);
         return res;
    }
    /**
     * 获取用户信息
     * @param accids
     * @return
     * @throws IOException
     */
    public static String getUinfos(String accids) throws IOException {
    	String url="https://api.netease.im/nimserver/user/getUinfos.action";
    	 List<NameValuePair> params = new ArrayList<NameValuePair>();
    	 params.add(new BasicNameValuePair("accids", accids));
    	 //UTF-8编码,解决中文问题
         HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");

         String res = NIMPost.postNIMServer(url, entity, APPKEY, SECRET);
         logger.info("createUser httpRes: {}", res);
         return res;
    }
}
