package com.youxin.app.aspect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.mongodb.morphia.Datastore;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.youxin.app.entity.SysApiLog;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.StringUtil;


/**
 * 切面输出接口访问情况，并根据配置是否保存日志
 * @author cf
 *
 */
@Aspect
@Order(1)
@Component
public class SysApiLogAspect extends AbstractQueueRunnable<SysApiLog> {
	private Log log=LogFactory.getLog("api.log");
	
	@Value("${youxin.isSaveRequestLogs}")
	int isSaveRequestLogs;
	
	@Autowired
	@Qualifier("get")
	private Datastore dfds;

	/**
	 * 
	 */
	public SysApiLogAspect() {
		setBatchSize(20);
		new Thread(this).start();
	}

	@Override
	public void runTask() {
		SysApiLog document = null;
		List<SysApiLog> list = new ArrayList<>();
		try {
			while (!msgQueue.isEmpty()) {
				document = msgQueue.poll();
				if (null == document)
					continue;
				list.add(document);
				if (loopCount.incrementAndGet() > batchSize)
					break;
			}
		} catch (Exception e) {
			log.debug(e.toString(), e);
		} finally {
 			if(!list.isEmpty())
 				dfds.save(list);
		}

	}

	@Pointcut("execution(* com.youxin.app.controller.*.* (..)) && !execution(* com.youxin.app.controller.ReceiveMsgController.*(..)) \"")
	public void apiLogAspect() {

	}

	// @Before("apiLogAspect()")
	public void dobefore(JoinPoint joinPoint) {

		RequestAttributes ra = RequestContextHolder.getRequestAttributes();

		ServletRequestAttributes sra = (ServletRequestAttributes) ra;

		HttpServletRequest request = sra.getRequest();

		// 使用log4j的MDC及NDC特性，识别请求方的IP及调用资料，输出到日志中

		MDC.put("uri", request.getRequestURI());

		// 记录下请求内容

		log.debug("HTTP_METHOD : " + request.getMethod());

		log.debug("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "."

				+ joinPoint.getSignature().getName());

		log.debug("ARGS : " + Arrays.toString(joinPoint.getArgs()));

		MDC.get("uri");

		MDC.remove("uri");

	}

	@AfterReturning(returning = "ret", pointcut = "apiLogAspect()")
	public void doAfterReturning(Object ret) throws Throwable {

		// 处理完请求，返回内容

		// log.info("RESPONSE : " + ret);
	}

	@Around("apiLogAspect()")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		Object response = null;// 定义返回信息
		String stackTrace = null;
		Exception exception = null;
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();

		Signature curSignature = joinPoint.getSignature();

		String className = curSignature.getDeclaringTypeName();// 类名

		String methodName = curSignature.getName(); // 方法名

		// String queryString = request.getQueryString();

		// 获取方法参数
		// String reqParamArr = Arrays.toString(joinPoint.getArgs());

		// 记录请求
		// log.info(String.format("【%s】类的【%s】方法，请求参数：%s", className, methodName,
		// reqParamArr));
		StringBuffer fullUri = new StringBuffer();
		fullUri.append(request.getRequestURI());
		Map<String, String[]> paramMap = request.getParameterMap();
		if (!paramMap.isEmpty())
			fullUri.append("?");
		for (String key : paramMap.keySet()) {
			fullUri.append(key).append("=").append(paramMap.get(key)[0]).append("&");
		}
		SysApiLog apiLog = new SysApiLog();
		apiLog.setTime(DateUtil.currentTimeSeconds());
		apiLog.setApiId(className + "_" + methodName);
		apiLog.setClientIp(request.getRemoteAddr());

		apiLog.setFullUri(fullUri.toString());
		apiLog.setUserAgent(request.getHeader("User-Agent"));

		log.debug(String.format("请求参数： %s", apiLog.getFullUri()));
		log.debug(String.format("客户端ip [%s]  User-Agent %s ", apiLog.getClientIp(), apiLog.getUserAgent()));
		log.debug(String.format("【%s】类的【%s】方法", className, methodName));
		// 用于统计调用耗时
		long startTime = System.currentTimeMillis();

		try {
			response = joinPoint.proceed(); // 执行服务方法
		} catch (Exception e) {
			// TODO: handle exception
			exception = e;

		}

		long totalTime = System.currentTimeMillis() - startTime;
		apiLog.setTotalTime(totalTime);
		// 记录应答
		// log.info(String.format("【%s】类的【%s】方法，应答参数：%s", className, methodName,
		// response));
		// log.info("RESPONSE : " + response);

		// 获取执行完的时间
		log.debug(String.format("接口【%s】总耗时(毫秒)：%s", methodName, totalTime));

		log.debug("********************************************   ");
		/**
		 * 代码异常了
		 */
		if (null != exception) {
			stackTrace = ExceptionUtils.getStackTrace(exception);
			apiLog.setStackTrace(stackTrace);
			if (1 == Integer.valueOf(isSaveRequestLogs))
				saveSysApiLogToDB(apiLog);
			return handleErrors(exception);
		}
		if (1 == Integer.valueOf(isSaveRequestLogs))
			saveSysApiLogToDB(apiLog);

		return response;

	}

	private void saveSysApiLogToDB(SysApiLog apiLog) {
		apiLog.setUserId(ReqUtil.getUserId());
		msgQueue.offer(apiLog);

	}

	private Object handleErrors(Exception e) {
		int resultCode = 1020101;
		String resultMsg = "接口内部异常";
		String detailMsg = "";
		if (e instanceof MissingServletRequestParameterException || e instanceof BindException) {
			resultCode = 1010101;
			resultMsg = "请求参数验证失败，缺少必填参数或参数错误";
		} else if (e instanceof ServiceException) {
			ServiceException ex = ((ServiceException) e);

			resultCode = null == ex.getResultCode() ? 0 : ex.getResultCode();
			resultMsg = ex.getMessage();
		} else if (e instanceof ClientAbortException) {
			resultMsg = "====> ClientAbortException";
			resultCode = -1;
		}else if (e instanceof IllegalArgumentException) {
			IllegalArgumentException ex = ((IllegalArgumentException) e);
			boolean jsonValid = StringUtil.isJSONValid(ex.getMessage());
			if(jsonValid) {
				Result result=JSON.parseObject(ex.getMessage(),Result.class);
				resultCode = result.getCode();
				resultMsg = result.getMsg();
				detailMsg = result.getData().toString();
			}else {
				resultCode = 0;
				resultMsg = ex.getMessage();
			}
				
			
		} else {
			e.printStackTrace();
			detailMsg = e.getMessage();
		}
		log.debug(resultMsg);

		Map<String, Object> map = Maps.newHashMap();
		map.put("resultCode", resultCode);
		map.put("resultMsg", resultMsg);
		map.put("detailMsg", detailMsg);

//		return JSONMessage.failureAndData(null, map);
		return Result.failure(resultCode,resultMsg, map);
	}

}
