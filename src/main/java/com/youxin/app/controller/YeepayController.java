package com.youxin.app.controller;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.yeepay.g3.sdk.yop.encrypt.DigitalEnvelopeDTO;
import com.yeepay.g3.sdk.yop.utils.DigitalEnvelopeUtils;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.repository.ConsumeRecordRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.ConsumeRecordManagerImpl;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.yoppay.YeeCallBackPram;
import com.youxin.app.utils.yoppay.YeePayUtil;

@RestController
@RequestMapping("/yeepay")
public class YeepayController {
	protected static Log log = LogFactory.getLog("pay");

	@Autowired
	private ConsumeRecordRepository crpository;
	@Autowired
	private ConsumeRecordManagerImpl cr;
	@Autowired
	private UserService userService;

	@Autowired
	@Qualifier("get")
	private Datastore dfds;

	@RequestMapping("/callBack")
	public String callBack(HttpServletRequest request, HttpServletResponse response) {
		log.debug("yeepay回调开始");
		String responseMsg = request.getParameter("response");
		
		PrivateKey privateKey = YeePayUtil.getPrivateKey();
		PublicKey publicKey = YeePayUtil.getPubKey();
		try {
			// 开始解密
			DigitalEnvelopeDTO dto = new DigitalEnvelopeDTO();
			dto.setCipherText(responseMsg);
			//解密验签
			dto = DigitalEnvelopeUtils.decrypt(dto, privateKey, publicKey);
			System.out.println("DTo结果："+JSON.toJSONString(dto));
			String plainText = dto.getPlainText();			
			System.out.println("解密结果:" + plainText);
			YeeCallBackPram yee=JSON.parseObject(plainText, YeeCallBackPram.class);
			System.out.println("yee"+yee);
	
			ConsumeRecord entity = cr.getConsumeRecordByNo(yee.getOrderId());
			//系统订单金额
			long sys_amount = new BigDecimal(entity.getMoney()+"").multiply(new BigDecimal(100)).longValue();
			//yee实付金额
			long yee_payAmount = new BigDecimal(yee.getPayAmount()).multiply(new BigDecimal(100)).longValue();
			//yee订单金额
			long yee_orderAmount = new BigDecimal(yee.getOrderAmount()+"").multiply(new BigDecimal(100)).longValue();
			
			if(entity.getStatus() != KConstants.OrderStatus.END
					&&sys_amount==yee_payAmount&&sys_amount==yee_orderAmount
					&&"SUCCESS".equals(yee.getStatus())) {
				
				entity.setStatus(KConstants.OrderStatus.END);
				yee.setUserid(entity.getUserId());
				Key<ConsumeRecord> save = crpository.save(entity);
				log.debug("易宝返回保存消费记录："+save.getId());
				Double rechargeUserMoeny = userService.rechargeUserMoeny(entity.getUserId(), entity.getMoney(), KConstants.MOENY_ADD);
				log.debug("易宝进行金额处理："+rechargeUserMoeny);
				Key<YeeCallBackPram> save2 = dfds.save(yee);
				log.debug("易宝返回保存易宝消费记录："+save2.getId());
			
			}else {
				log.debug("易宝支付失败"+yee.getStatus());
				return "failure";
			}
		} catch (Exception e) {
			log.debug("易宝支付失败"+e.getMessage());
			return "failure";
		}
		return "SUCCESS";
	}
	
	@RequestMapping("/callBackXCX")
	public String callBackXCX(HttpServletRequest request, HttpServletResponse response) {
		log.debug("yeepay XCX回调开始");
		String responseMsg = request.getParameter("response");
		
		PrivateKey privateKey = YeePayUtil.getPrivateKey();
		PublicKey publicKey = YeePayUtil.getPubKey();
//		try {
			// 开始解密
			DigitalEnvelopeDTO dto = new DigitalEnvelopeDTO();
			dto.setCipherText(responseMsg);
			//解密验签
			dto = DigitalEnvelopeUtils.decrypt(dto, privateKey, publicKey);
			System.out.println("DTo结果："+JSON.toJSONString(dto));
			String plainText = dto.getPlainText();			
			System.out.println("解密结果:" + plainText);
			YeeCallBackPram yee=JSON.parseObject(plainText, YeeCallBackPram.class);
			System.out.println("yee"+yee);
	
//			ConsumeRecord entity = cr.getConsumeRecordByNo(yee.getOrderId());
//			//系统订单金额
//			long sys_amount = new BigDecimal(entity.getMoney()+"").multiply(new BigDecimal(100)).longValue();
//			//yee实付金额
//			long yee_payAmount = new BigDecimal(yee.getPayAmount()).multiply(new BigDecimal(100)).longValue();
//			//yee订单金额
//			long yee_orderAmount = new BigDecimal(yee.getOrderAmount()+"").multiply(new BigDecimal(100)).longValue();
//			
//			if(entity.getStatus() != KConstants.OrderStatus.END
//					&&sys_amount==yee_payAmount&&sys_amount==yee_orderAmount
//					&&"SUCCESS".equals(yee.getStatus())) {
//				
//				entity.setStatus(KConstants.OrderStatus.END);
//				yee.setUserid(entity.getUserId());
//				Key<ConsumeRecord> save = crpository.save(entity);
//				log.debug("易宝返回保存消费记录："+save.getId());
//				Double rechargeUserMoeny = userService.rechargeUserMoeny(entity.getUserId(), entity.getMoney(), KConstants.MOENY_ADD);
//				log.debug("易宝进行金额处理："+rechargeUserMoeny);
//				Key<YeeCallBackPram> save2 = dfds.save(yee);
//				log.debug("易宝返回保存易宝消费记录："+save2.getId());
//			
//			}else {
//				log.debug("易宝支付失败"+yee.getStatus());
//				return "failure";
//			}
//		} catch (Exception e) {
//			log.debug("易宝支付失败"+e.getMessage());
//			return "failure";
//		}
		return "SUCCESS";
	}
}
