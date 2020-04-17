package com.youxin.app.controller;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.domain.Account;
import com.mongodb.DBObject;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.HelpCenter;
import com.youxin.app.entity.NearbyUser;
import com.youxin.app.entity.Opinion;
import com.youxin.app.entity.SdkLoginInfo;
import com.youxin.app.entity.User;
import com.youxin.app.entity.User.DeviceInfo;
import com.youxin.app.entity.User.LoginLog;
import com.youxin.app.entity.User.UserLoginLog;
import com.youxin.app.entity.User.UserSettings;
import com.youxin.app.entity.UserVo;
import com.youxin.app.entity.exam.BaseExample;
import com.youxin.app.entity.exam.UserExample;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.ConsumeRecordManagerImpl;
import com.youxin.app.utils.AuthServiceUtils;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.HttpUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.KSessionUtil;
import com.youxin.app.utils.Md5Util;
import com.youxin.app.utils.MongoUtil;
import com.youxin.app.utils.PageResult;
import com.youxin.app.utils.PageVO;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.ResultCode;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.alipay.util.AliPayUtil;
import com.youxin.app.utils.sms.SMSServiceImpl;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.Friends;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.HttpURLConnection;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = "人人商城")
@RestController
@RequestMapping("/renren/")
public class RenShopController extends AbstractController{
	

	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository repository;
	@Autowired
	private SMSServiceImpl sendSms;
	@Autowired
	private ConsumeRecordManagerImpl consumeRecordServer;
	@Autowired
	@Qualifier("get")
	private Datastore dfds;
	@ApiOperation(value = "订单支付",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "oid", value = "订单id", required = true, paramType = "query"),
	@ApiImplicitParam(name = "money", value = "付款金额", required = true, paramType = "query"),
	@ApiImplicitParam(name = "secret", value = "密钥(md5(md5(apiKey+time+money+payPassword)+userId+token))", required = true, paramType = "query"),
	@ApiImplicitParam(name = "time", value = "时间", required = true, paramType = "query") 
	})
	@PostMapping(value = "/payShopOrder")
	public Object payShopOrder(@RequestParam(defaultValue = "0") int oid,String money,Long time,String secret) throws Exception {
		log.debug("订单开始"+oid);
		Integer userId=ReqUtil.getUserId();
		String token = getAccess_token();
		User user = userService.getUserFromDB(userId);
		if(oid>0) {
			
			Assert.isTrue(AuthServiceUtils.authQRCodeReceipt(userId.toString(), token, money, time, user.getPayPassword(), secret), "支付密码错误");
			String orderNo = AliPayUtil.getOutTradeNo();
			ConsumeRecord entity = new ConsumeRecord();
			entity.setUserId(ReqUtil.getUserId());
			entity.setTime(DateUtil.currentTimeSeconds());
			entity.setType(KConstants.ConsumeType.BUY_SHOP);
			entity.setDesc("购买商品");
			entity.setStatus(KConstants.OrderStatus.CREATE);
			entity.setTradeNo(orderNo+"order_"+oid);
			entity.setPayType(3);
			entity.setMoney(new Double(money));
			Assert.isTrue(user.getBalance()>=entity.getMoney(), "余额不足");
			userService.rechargeUserMoeny(userId, entity.getMoney(), KConstants.MOENY_REDUCE);
			entity.setStatus(KConstants.OrderStatus.END);
			consumeRecordServer.saveConsumeRecord(entity);
			  // 登陆 Url
	        String loginUrl = "https://wkt.youxinruanjian.cn/web/index.php?c=user&a=login&";
	        // 需登陆后访问的 Url
	        String dataUrl = "https://wkt.youxinruanjian.cn/web/index.php?c=site&a=entry&m=ewei_shopv2&do=web&r=order.op.pay&id="+oid;
	        
	        String cookie = postLogin(loginUrl);
	        
	        Assert.isTrue(!StringUtil.isEmpty(cookie), "付款成功，下单失败，请联系客服");
	        
	        String resContent = HttpUtil.cookiePostJson(dataUrl, "",cookie);
	        Assert.isTrue(!StringUtil.isEmpty(resContent), "付款成功，下单失败，请联系客服");
	        try {
	    	   JSON.parseObject(resContent);
			} catch (Exception e) {
				return Result.error("付款成功，下单失败，请联系客服");
			}
	        return Result.success("付款成功"); 
		}	
	
		return Result.error("请重试");
	}
	public static void main(String[] args) {
		String s ="11121";
		System.out.println();
	}
	private String postLogin(String loginUrl) {
		HttpClient httpClient = new HttpClient();
 
		// 模拟登陆，按实际服务器端要求选用 Post 或 Get 请求方式
		PostMethod postMethod = new PostMethod(loginUrl);
		
		// 设置登陆时要求的信息，用户名和密码
		NameValuePair[] data = {new NameValuePair("token", "8c305d25"),new NameValuePair("login_type", "system"),new NameValuePair("username", "admin"),new NameValuePair("referer", ""),new NameValuePair("submit", "登录"), new NameValuePair("username", "admin"), new NameValuePair("password", "admin") };
		postMethod.setRequestBody(data);
		try {
		    // 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
		    httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		    int statusCode=httpClient.executeMethod(postMethod);
		                     
		    // 获得登陆后的 Cookie
		    Cookie[] cookies = httpClient.getState().getCookies();
		    StringBuffer tmpcookies = new StringBuffer();
		    for (Cookie c : cookies) {
		        tmpcookies.append(c.toString() + ";");
		      
		    }
		    System.out.println("statusCode = "+statusCode+"cookies = "+tmpcookies.toString());
		    if(statusCode==302){//重定向到新的URL
		        System.out.println("模拟登录成功");
		        return tmpcookies.toString();
		    }
		    else {
		    	return tmpcookies.toString();
		    }
		}
		catch (Exception e) {
		    e.printStackTrace();
		    return "";
		}
	}
	
	

	
	
}
