package com.youxin.app.advice;

import java.io.EOFException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.utils.ResponseUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.StringUtil;



@ControllerAdvice
public class ExceptionHandlerAdvice {
	protected Log log=LogFactory.getLog(ExceptionHandlerAdvice.class);
	
	@ExceptionHandler(value = { Exception.class, RuntimeException.class })
	public void handleErrors(HttpServletRequest request,
			HttpServletResponse response, Exception e) throws Exception {
		

		int resultCode = 60001;
		String resultMsg = "系统异常：";
		Object resultData=".";
		log.info(request.getRequestURI() + "错误：");
		
		if (e instanceof MissingServletRequestParameterException
				|| e instanceof BindException) {
			resultCode = 10001;
			resultMsg = "请求参数验证失败，缺少必填参数或参数错误";
		} else if (e instanceof IllegalArgumentException) {
			IllegalArgumentException ex = ((IllegalArgumentException) e);
			boolean jsonValid = StringUtil.isJSONValid(ex.getMessage());
			if(jsonValid) {
				Result result=JSON.parseObject(ex.getMessage(),Result.class);
				resultCode = result.getCode();
				resultMsg = result.getMsg();
				resultData = result.getData();
			}else {
				resultCode = 0;
				resultMsg = ex.getMessage();
			}
				
			
		} else if (e instanceof ServiceException) {
			ServiceException ex = ((ServiceException) e);

			resultCode = null == ex.getResultCode() ? 0 : ex.getResultCode();
			resultMsg = ex.getMessage();
		} else if (e instanceof ClientAbortException) {
			resultMsg="====> ClientAbortException";
			resultCode=-1;
		}else if(e instanceof EOFException){
			log.info("====》 拦截    EOFException ");
			resultMsg = resultMsg+e.getMessage();
		}else {
			e.printStackTrace();
//			resultMsg = resultMsg+e.getMessage();
			log.info("ex:"+e.getMessage());
		}
		log.info(resultMsg);

//		Map<String, Object> map = Maps.newHashMap();
//		map.put("resultCode", resultCode);
//		map.put("resultMsg", resultMsg);
//		map.put("detailMsg", detailMsg);	
		Result map=new Result();
		map.setCode(resultCode);
		map.setMsg(resultMsg);
		map.setData(resultData);
		String text = JSONObject.toJSONString(map);

		ResponseUtil.output(response, text);
	}
//	public static void main(String[] args) {
//		String s="";
//		Object resultDat = "";
//		System.out.println("resultData"+resultDat);
//		boolean jsonValid = StringUtil.isJSONValid(s);
//		System.out.println(jsonValid);
//	}
}
