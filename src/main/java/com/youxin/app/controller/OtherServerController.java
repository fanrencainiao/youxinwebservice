package com.youxin.app.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.youxin.app.ex.ServiceException;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.ResultCode;
import com.youxin.app.utils.applicationBean.SmsConfig;
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
		
		if(smsConfig.getIsstart()==1) {
			if(StringUtils.isEmpty(imgCode)){
				return Result.failure(ResultCode.PARAM_IMG_ERROR);
			}else{
//				if(!SKBeanUtils.getSMSService().checkImgCode(telephone, imgCode)){
//					String key = String.format(KConstants.Key.IMGCODE, telephone);
//					String cached = SKBeanUtils.getRedisCRUD().get(key);
//					System.out.println("ImgCodeError  getImgCode "+cached+"  imgCode "+imgCode);
//					return JSONMessage.failureByErrCode(KConstants.ResultCode.ImgCodeError,language,params);
//				}
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
