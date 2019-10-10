package com.youxin.app.controller;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.repository.ConsumeRecordRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.ConsumeRecordManagerImpl;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.wxpay.utils.WXNotify;
import com.youxin.app.utils.wxpay.utils.WXPayUtil;
import com.youxin.app.utils.wxpay.utils.WxPayResult;




@RestController
@RequestMapping("/wxpay")
public class WxpayController extends AbstractController{
	
//	@Autowired
//	private TransfersRecordManagerImpl transfersManager;
	@Autowired
	private ConsumeRecordRepository crpository;
	@Autowired
	private ConsumeRecordManagerImpl cr;
	@Autowired
	private UserService userService;
	@Autowired
	@Qualifier("get")
	private Datastore dfds;
	@RequestMapping(value="/callBack",method=RequestMethod.POST)
	public void wxPayCallBack(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		//把如下代码贴到的你的处理回调的servlet 或者.do 中即可明白回调操作
		log.info("微信支付回调数据开始");
		BufferedOutputStream out = null;
		String inputLine;
		String notityXml = "";
		String resXml = "";
		try {
			while ((inputLine = request.getReader().readLine()) != null) {
				notityXml += inputLine;
			}
			request.getReader().close();
			
			Map<String,String> m = WXNotify.parseXmlToList2(notityXml);
			log.info("接收到的报文：" + m);
				String tradeNo=m.get("out_trade_no");
				ConsumeRecord entity=cr.getConsumeRecordByNo(tradeNo);
				if(null==entity)
					log.info("交易订单号不存在！-----"+tradeNo);
				else if(0!=entity.getStatus())
					log.info(tradeNo+"===status==="+entity.getStatus()+"=======交易已处理或已取消!");
				else if("SUCCESS".equals(m.get("result_code"))){
					boolean flag=Double.valueOf(m.get("cash_fee"))==entity.getMoney()*100;
					if(flag){
						 //log.info("支付金额比较"+m.get("cash_fee")+"=="+entity.getMoney()*100+"=======>"+flag);
						WxPayResult wpr = WXPayUtil.mapToWxPayResult(m);
						//支付成功
						resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
						+ "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
						entity.setStatus(KConstants.OrderStatus.END);
						crpository.save(entity);
						dfds.save(wpr);
						userService.rechargeUserMoeny(entity.getUserId(), entity.getMoney(), KConstants.MOENY_ADD);
						log.info(tradeNo+"========>>微信支付成功!");
					}else{
						log.info("微信数据返回错误!");
						log.info("localhost:Money---------"+entity.getMoney()*100);
						log.info("Wxpay:Cash_fee---------"+m.get("cash_fee"));
					}
				}else{
					log.info("微信支付失败======"+m.get("return_msg"));
					resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
					+ "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
				}
				out = new BufferedOutputStream(response.getOutputStream());
				out.write(resXml.getBytes());
				out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (out != null)
				out.close();
		}
		
	}
	
}
