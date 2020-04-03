package com.youxin.app.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.youxin.app.ex.ServiceException;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.ResultCode;
import com.youxin.app.utils.ValidateCode;
import com.youxin.app.utils.applicationBean.SmsConfig;
import com.youxin.app.utils.jedis.RedisCRUD;
import com.youxin.app.utils.sms.SMSServiceImpl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = "第三方等服务接口")
@RestController
@RequestMapping("/other")
public class OtherServerController {
	@Autowired
	private UserService us;
	
	@Autowired
	@Qualifier("scf")
	private SmsConfig smsConfig;
	
	@Autowired
	private SMSServiceImpl smsServer;
	@Autowired
	private RedisCRUD redisServer;
	
	@ApiOperation(value = "获取/更新图形验证码",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "telephone", value = "区号+手机号", required = true, paramType = "query")
	})
	@GetMapping("/getImgCode")
	public void getImgCode(HttpServletRequest request, HttpServletResponse response,@RequestParam String telephone) throws Exception {
		
		 // 设置响应的类型格式为图片格式  
        response.setContentType("image/jpeg");  
        //禁止图像缓存。  
        response.setHeader("Pragma", "no-cache");  
        response.setHeader("Cache-Control", "no-cache");  
        response.setDateHeader("Expires", 0); 
        HttpSession session = request.getSession();  
          
      
        ValidateCode vCode = new ValidateCode(140,50,4,0);  
        String key = String.format(KConstants.Key.IMGCODE, telephone.trim());
        redisServer.set(key, vCode.getCode());
        redisServer.expire(key, 600);
		
        session.setAttribute("code", vCode.getCode()); 
       // session.setMaxInactiveInterval(10*60);
        System.out.println("getImgCode telephone ===>"+telephone+" code "+vCode.getCode());
        vCode.write(response.getOutputStream());  
	}
	@ApiOperation(value = "发送短信",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "telephone", value = "手机号", required = true, paramType = "query")
	,@ApiImplicitParam(name = "areaCode", value = "区号", defaultValue="86", paramType = "query"),
	@ApiImplicitParam(name = "imgCode", value = "图形验证码", paramType = "query"),
	@ApiImplicitParam(name = "language", value = "国家编码",defaultValue="zh",  paramType = "query"),
	@ApiImplicitParam(name = "isRegister", value = "是否是注册发送短信", defaultValue="1", paramType = "query"),
	})
	@PostMapping("/randcode/sendSms")
	public Object sendSms(@RequestParam String telephone, @RequestParam(defaultValue="86") String areaCode,
			@RequestParam(defaultValue="") String imgCode, @RequestParam(defaultValue="zh") String language,
			@RequestParam(defaultValue="1") int isRegister){
		Map<String, Object> params = new HashMap<String, Object>();
		
		if(1 == isRegister){
			if (us.mobileCount(telephone)>=1){
				return Result.failure(ResultCode.USER_HAS_EXISTED);
			}
		}
		
		telephone = areaCode + telephone;
		System.out.println("smsConfig.getIsstart()"+smsConfig.getIsstart());
		
		System.out.println("smsConfig.getIsstart()==1"+(smsConfig.getIsstart()==1));
		if(smsConfig.getIsstart()==1) {
			if(StringUtils.isEmpty(imgCode)){
				return Result.failure(ResultCode.PARAM_IMG_ERROR);
			}else{
				if(!smsServer.checkImgCode(telephone, imgCode)){
					String key = String.format(KConstants.Key.IMGCODE, telephone);
					String cached = redisServer.get(key);
					System.out.println("ImgCodeError  getImgCode "+cached+"  imgCode "+imgCode);
					return Result.failure(KConstants.ResultCode.ImgCodeError,"图形验证码错误",params);
				}
			}
		}
		
		
		String code=null;
		
		try {
			
			code=smsServer.sendSmsToInternational(telephone, areaCode,language,code);
			//线程延时返回结果
			Thread.sleep(2000);
			params.put("code", code);
			System.out.println("code >>>  "+code);
			//return JSONMessage.success(null,params);
		} catch (ServiceException e) {
			e.printStackTrace();
			if(null==e.getResultCode())
				return Result.errorMsg(e.getErrMessage());
			return Result.failure(e.getResultCode(), e.getErrMessage(), null);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}
		return Result.success(params);
	}
	
	
}
