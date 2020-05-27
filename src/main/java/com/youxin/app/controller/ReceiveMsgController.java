package com.youxin.app.controller;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.MessageReceive;
import com.youxin.app.entity.User;
import com.youxin.app.entity.UserKeyWord;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.ThreadUtil;
import com.youxin.app.utils.KeyFilter.SensitivewordFilter;
import com.youxin.app.utils.supper.Callback;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.utils.CheckSumBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mongodb.morphia.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;
@Controller
@RequestMapping(value = {"/message"})
public class ReceiveMsgController {
    public static final Log logger = LogFactory
            .getLog("getmsg");
    //表示CONVERSATION消息，即会话类型的消息（目前包括P2P会话内消息与自定义系统通知，群聊会话内消息与自定义系统通知，以及云信内置系统通知）。
    public static final String CONVERSATION="1";
    
    @Value("${youxin.yunxinappaecret}")
    private String appSecret;
    @Autowired
	@Qualifier("get")
	private Datastore dfds;
    @Autowired
   	private UserService userService;
    @RequestMapping(value = {"/receive"}, method = {RequestMethod.POST})
    @ResponseBody
    public JSONObject mockClient(HttpServletRequest request)
            throws Exception {
        JSONObject result = new JSONObject();
        try {
        	 // 获取请求体
            byte[] body = readBody(request);
            if (body == null) {
                logger.error("request wrong, empty body!");
                result.put("code", 414);
                return result;
            }
            // 获取部分request header，并打印
            String ContentType = request.getContentType();
            String AppKey = request.getHeader("AppKey");
            String CurTime = request.getHeader("CurTime");
            String MD5 = request.getHeader("MD5");
            String CheckSum = request.getHeader("CheckSum");
//            logger.debug("request headers: ContentType = "+ContentType+", AppKey = "+AppKey+", CurTime = "+CurTime+", " +
//                    "MD5 = "+MD5+", CheckSum = "+CheckSum+"");
            // 将请求体转成String格式，并打印
            String requestBody = new String(body, "utf-8");
//            logger.info("request body = "+requestBody+"");
        	ThreadUtil.executeInThread(new Callback() {
        		
				@Override
				public void execute(Object obj) {
		            // 获取计算过的md5及checkSum
		            String verifyMD5 = CheckSumBuilder.getMD5(requestBody);
		            String verifyChecksum = CheckSumBuilder.getCheckSum(appSecret, verifyMD5, CurTime);
//		            logger.info("verifyMD5 = "+verifyMD5+",MD5 = "+MD5+", verifyChecksum = "+verifyChecksum+", Checksum = "+CheckSum+"" );
		            // TODO: 比较md5、checkSum是否一致，以及后续业务处理
		            if (verifyMD5.equals(MD5)&&CheckSum.equals(verifyChecksum)) {
		            	logger.debug("消息验证通过");
		            	MessageReceive mr=JSON.parseObject(requestBody, MessageReceive.class);
		            	if (mr.getMsgidServer()==null||mr.getMsgidServer()==""||mr.getMsgidServer().equals("0")) {
							
						}else {
							if (mr.getEventType().equals("1")&&("PERSON".equals(mr.getConvType())||"TEAM".equals(mr.getConvType()))) {
								dfds.save(mr);
								//检测敏感词
								SensitivewordFilter filter = new SensitivewordFilter();
								Set<String> sensitiveWord = filter.getSensitiveWord(mr.getBody(), 1);
								if(sensitiveWord.size()>0) {
									UserKeyWord uk=new UserKeyWord();
									uk.setAccid(mr.getFromAccount());
									uk.setKeyWord(sensitiveWord.toString());
									uk.setTime(DateUtil.currentTimeSeconds());
									uk.setMsgid(mr.getMsgidServer());
									dfds.save(uk);
								}
							} else if(mr.getEventType().equals("2")) {
								JSONObject loginObject = JSON.parseObject(requestBody);
								userService.updateUserOnlineByAccid(loginObject.getString("accid"), 1);
							}else if(mr.getEventType().equals("3")) {
								JSONObject logoutObject = JSON.parseObject(requestBody);
								userService.updateUserOnlineByAccid(logoutObject.getString("accid"), 0);
							}
								
						}
//		            	logger.debug(mr.toString());
					}else 
						logger.debug("消息验证失败");
					
				}
			});
           

            result.put("code", 200);
            return result;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            result.put("code", 414);
            return result;
        }
    }
    private byte[] readBody(HttpServletRequest request) throws IOException {
        if (request.getContentLength() > 0) {
            byte[] body = new byte[request.getContentLength()];
            IOUtils.readFully(request.getInputStream(), body);
            return body;
        } else
            return null;
    }
//    public static void main(String[] args) {
//    	long currentTimeMilliSeconds = DateUtil.currentTimeMilliSeconds();
//    	System.out.println(currentTimeMilliSeconds);
//		for (int i = 0; i < 10; i++) {
//			int j=i;
//			String dd="s";
//			ThreadUtil.executeInThread(new Callback() {
//				
//				@Override
//				public  void execute(Object obj) {
//					// TODO Auto-generated method stub
//					String user=obj.toString();
//					System.out.println(obj);
//				}
//			}, dd);
////			System.out.println(u.getId());
//		}
//		System.out.println("耗时===========："+(DateUtil.currentTimeMilliSeconds()-currentTimeMilliSeconds));
//	}
}