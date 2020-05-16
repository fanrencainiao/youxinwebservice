package com.youxin.app.utils.yoppay.demo;

import java.io.IOException;
import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import com.yeepay.g3.sdk.yop.client.YopRsaClient;

//import DS.runtestgethmackey;
//import DS.runtesthmac;

public class Runtestorder {
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		order1("1");
	}
	public static void order1(String money) {
//		System.out.println("appkey:"+yeePayConfig.getAppkey());
		
		//secretKey:商户私钥（字符串形式）
		String OPRkey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDKl+Fcf7l1kxuNpTk/8UvgR3SSTn3nlQL/xDGR0u9rKqySNPLDzzHcsLFqEPabhC7IQRmsMwBdUdEgDMVyJLYreObFI09fp8ZgSShNgC0VZTbRUvKCDu7yGN4+fRQXBVc5tvDoqiIZSK+heBXsXm1ZWRcaUWSDLnAltoZ1kfHonn1NgjDr0hSZEn3PZwELlndV1IIO6err+P13n3VLI318ohHyR3nfUbfELoZRxjdNVscLIkfhbh+FGxNOnAzEM+x9Lx/hjP7Bzw5EOMCQh19D6JblOmFM3BeTraivW0JuHnb9mGm8o6IYp/MKCUZ+TSPzxsf2C57LqV3E1BQuSE/bAgMBAAECggEATXD0LNMH9gkXCbSEJ0yZ1/AIf9qwJwNfY5w/5IGQklL8J2FFARNk+Hq0PpqgYb8L0PGNmjTeqU1alpk7KlAdMI3lDKtcLnDoFCsy1DkwQpLveuFtoMiLjxBxd8qBEPGyL3YeLZnHMgoGVL8eBj3sJ7C185H0TW7FYK0eRbfVlDi+rWDOBiAlqD3kMalRXdvvr7u1G43kgs2HsacnGEzkxab2cffGePXhi1puUqDLb89wrywsXAf1N0BUaDpl0soVzJ6FrrTCzZRZgckButNwgPNpnhqPjI6nVfqvkyFcgnQ1hL/jz+CSBuU6jE6i3mgkNb27Lz84j9Cg6aaD4n2fJQKBgQDy9G4vvTTtTSP+8pucSL7NuI6Q++p97CkpcB/mgi5IexabTWjUc3S9v4agKCbBk6j8++s0cRctMjCYkSXLK9vleV0y5GRwCJU0A/eaWH+cKCl+pYBEgcuGG/JGzTgFR7lVBScaLrMhgPsj18H4gL7V6jHSbmaMXaVZ4iRwfTLzRQKBgQDVeKemJGX2kuQ55jyX/fNqsH8C07ObCer+zx3l2IbKYtuAm7N4wxqWLTmvl8GkPvnhMFn+ZqoGLAw16OZKiOYbDA2fK1VU7UJibWWEtHmWb/d/XPI+XHvAMpXRI5CsEs2XUKnrAepfNm8boPAJw2EZoXjJRZXboVk/xVKbEm3YnwKBgBVqiH/6zLySTTb/CcqH+xHFUAPlwPDirWoysQ0vsoa/GuoLs87ucn77cBGUGH591qqeRkh0I5ffUbtRKCS1tAeRYC7JRTU+/G/+XYgkfvMz8xxv2FOxoVAsDHpDQsnsXaDp+F1temrcpMavETEnoCrZHxUumCmNY7L9pTqpPqGFAoGBALD3lzP6lLS2zZDaN31hP7xAmOqFCi4LKM8YcJaszAFXttSFP2FrjMzWrS3ORu+cbZareM+X2HfJaE4Ax3keyHO/qEM9rz36esASJMjAvNNj+u+Q5BmincyVovF/5F1iMSGx80spiVxpSe7Dmnrdh8+xtupUT4mF1vEw6ew4eOvNAoGBALEGHqqY1AsC/69mvKg6DuixUZSV4/f8yRMFT3hf2jnhad5kUlhoV/8c9YAmC1ZnrVzNkrAuWOCQhf8Z33U3L7S1Z/vAtblegPsqTlI9DXCzkODv2C9xnx9RjXcwXtewOoadZHx4xmzruoK26DqxEjGGihKIC7rWBfE9Ad8mYmSG";
//		String OPRkey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCqSVrBTiEEDKZukFScaTRIX80IMgMg6gpCC5KM5MdMIw6fWmIponUo66seRS7MglNYYjNNz/9lDE8mjvAoPoPYdjI5eToGPQ7M63x4EQB2HICxB4l2Kqat9r4sUIj9/ANpM+yJR2zfZY7EsR0heMfJkQfYLgEfKI3as/4GgaDorVprKk0y/iK30e7NmDbVE4i68FiT/B5XezBq+orOatZlLIaHQqXUHS3Cj7M+E39WNvDNWp9/9ZZUaipIyun7MwKhlhRnW5mo/3qecutkO5g/b7wFWwQoqwq5ZN3m3z9gsvcxAY+yeOQCxTYgQgNUcrURp/s/gsXH3Mz8c2deNQ59AgMBAAECggEADp4Is1U7PPdqW01Pu17F2nyWw2BU/RX6AXIFh3X6rEzKFuBm/Wnw+AKa3W3/jBXtL5tmX1npHh8bYD+e2pAwjdE/kx1r31iNv7DAg3ykzH66hqJoqK2/7Gj6KXWpp3ENnec+I7PvjZGl2rkqu1J8ho9I3BlphpIwSG4bnSqyyaY2pwsNNlqhEtxtG+NxmMW5NKpaqahuhun3qGsx2GX5WSMFzIWXRdcSDskqOnN1ROJLsuaTl8bBmeIVN1zgqwsKF9zLfXNTPy1k0ChHjONVyO0U3VHxut8/BM3QpnvW53TpgTLUi2navN1rbQwjaxJyIudFdQ8USIFBHyE7hTYzhQKBgQDSa1nh8eF0ktO+hk2IQ51UGzDqM9RyjDrjSoX8m1oz5EBFSgGhmN0HsHM/VF8DgQw9aT0R8ByD3BflJ0KwBMsJ/ONYVvCWWw2bk9vrTvUZ1iF8gBGkl26cpOAhbEk4bgkCx7SQcFpvX4mikeV0bT99kajXH6GbfI02vtLGsFWFkwKBgQDPLHf9XrKtj5rxJdHjPSPm4e7g8XsTG0Xyw+NhVY0mY6k2cd8Fj9SAgycCbIh2S7a9prko+mvuqILZt5sQeubZpKxFneN3FBfSzjrZcO3ZLvFtDbixdKT2K5jzUJeh7UmTTA5Bo9ja9mEyRiIrGXmTBsQakYdGnh87NjloAR2lrwKBgHNPFOB2tsA1PggofRBxTSQsCnAtmvxy0EqCKk61q4bITFgsKBywMl/mWCGaUL8Q1u5IX4kW9elkkUuoaikfV0zP4p4kdo9OsnRRYLDggfx4lb0uSXzS53C8AX8PYkikNBfr7I1CpKxnxHrsTLuyqppbWhUZZmxYouIfTE5Jj3Q1AoGAM2w7QEWgHhp2AAM+LKRBZA6SZ30o6l4rp41dxAwjI/M6zgvHqq6/tUJYjW55FLvIWRyn+vblkXB8QiQjthx7bmxEYmdFTYpMO4P68XvpXa4cONBeFpX4WC4MIeDQMl4elBQducc8jWT4TS1BT+db2NWmGV4j8LBQ2jakWx9jx3sCgYEAuM2tnYZ076ob5lVRDhSTEND2yYT+wuFbx8Wh/KTvwjFtWCSy9Q/s7B6+PnCI6sL7CQr3Vh64RoQDkpk7REI/gWALFz7rIvx0mq4eqpADaqHhdOyfv99ppFjWo5gfuV+JyDWsasXLqZoP0S+HBVZgEPYzUmSyM5yUW/x/bxjet4c=";
		//step1  生成yop请求对象
		//arg0:appkey（举例授权扣款是SQKK+商户编号，亿企通是OPR:+商户编号，具体是什么请参考自己开通产品的手册。
		//arg1:商户私钥字符串
		YopRequest request = new YopRequest("OPR:10033580387",OPRkey);
		
		//step2 配置参数
	    //arg0:参数名
	    //arg1:参数值
		request.addParam("parentMerchantNo", "10033580387");
		request.addParam("merchantNo", "10033580387");
		request.addParam("orderId", UUID.randomUUID().toString().replaceAll("-", ""));
		request.addParam("orderAmount", money);
		request.addParam("notifyUrl", "http://pay.jiaxinapp.cn:8092/yeepay/callBack");
		request.addParam("goodsParamExt", "{\"goodsName\":\"abc商品名称\" ,\"goodsDesc\" : \"商品描 述\" }");
		//request.addParam("fundProcessType", "");
		//request.addParam("divideDetail", "");
		
	
	    //step3 发起请求
		//arg0:接口的uri（参见手册）
		//arg1:配置好参数的请求对象
		System.out.println(request.getParams().toString());
	    YopResponse response;
		try {
			response = YopRsaClient.post("/rest/v1.0/std/trade/order", request);
			System.out.println("获取order"+response);
			System.out.println("获取order"+response.toString());
			System.out.println("获取order"+response.getStringResult());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void order(String money) {
		//secretKey:商户私钥（字符串形式）
				String OPRkey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDKl+Fcf7l1kxuNpTk/8UvgR3SSTn3nlQL/xDGR0u9rKqySNPLDzzHcsLFqEPabhC7IQRmsMwBdUdEgDMVyJLYreObFI09fp8ZgSShNgC0VZTbRUvKCDu7yGN4+fRQXBVc5tvDoqiIZSK+heBXsXm1ZWRcaUWSDLnAltoZ1kfHonn1NgjDr0hSZEn3PZwELlndV1IIO6err+P13n3VLI318ohHyR3nfUbfELoZRxjdNVscLIkfhbh+FGxNOnAzEM+x9Lx/hjP7Bzw5EOMCQh19D6JblOmFM3BeTraivW0JuHnb9mGm8o6IYp/MKCUZ+TSPzxsf2C57LqV3E1BQuSE/bAgMBAAECggEATXD0LNMH9gkXCbSEJ0yZ1/AIf9qwJwNfY5w/5IGQklL8J2FFARNk+Hq0PpqgYb8L0PGNmjTeqU1alpk7KlAdMI3lDKtcLnDoFCsy1DkwQpLveuFtoMiLjxBxd8qBEPGyL3YeLZnHMgoGVL8eBj3sJ7C185H0TW7FYK0eRbfVlDi+rWDOBiAlqD3kMalRXdvvr7u1G43kgs2HsacnGEzkxab2cffGePXhi1puUqDLb89wrywsXAf1N0BUaDpl0soVzJ6FrrTCzZRZgckButNwgPNpnhqPjI6nVfqvkyFcgnQ1hL/jz+CSBuU6jE6i3mgkNb27Lz84j9Cg6aaD4n2fJQKBgQDy9G4vvTTtTSP+8pucSL7NuI6Q++p97CkpcB/mgi5IexabTWjUc3S9v4agKCbBk6j8++s0cRctMjCYkSXLK9vleV0y5GRwCJU0A/eaWH+cKCl+pYBEgcuGG/JGzTgFR7lVBScaLrMhgPsj18H4gL7V6jHSbmaMXaVZ4iRwfTLzRQKBgQDVeKemJGX2kuQ55jyX/fNqsH8C07ObCer+zx3l2IbKYtuAm7N4wxqWLTmvl8GkPvnhMFn+ZqoGLAw16OZKiOYbDA2fK1VU7UJibWWEtHmWb/d/XPI+XHvAMpXRI5CsEs2XUKnrAepfNm8boPAJw2EZoXjJRZXboVk/xVKbEm3YnwKBgBVqiH/6zLySTTb/CcqH+xHFUAPlwPDirWoysQ0vsoa/GuoLs87ucn77cBGUGH591qqeRkh0I5ffUbtRKCS1tAeRYC7JRTU+/G/+XYgkfvMz8xxv2FOxoVAsDHpDQsnsXaDp+F1temrcpMavETEnoCrZHxUumCmNY7L9pTqpPqGFAoGBALD3lzP6lLS2zZDaN31hP7xAmOqFCi4LKM8YcJaszAFXttSFP2FrjMzWrS3ORu+cbZareM+X2HfJaE4Ax3keyHO/qEM9rz36esASJMjAvNNj+u+Q5BmincyVovF/5F1iMSGx80spiVxpSe7Dmnrdh8+xtupUT4mF1vEw6ew4eOvNAoGBALEGHqqY1AsC/69mvKg6DuixUZSV4/f8yRMFT3hf2jnhad5kUlhoV/8c9YAmC1ZnrVzNkrAuWOCQhf8Z33U3L7S1Z/vAtblegPsqTlI9DXCzkODv2C9xnx9RjXcwXtewOoadZHx4xmzruoK26DqxEjGGihKIC7rWBfE9Ad8mYmSG";
				//step1  生成yop请求对象
				//arg0:appkey（举例授权扣款是SQKK+商户编号，亿企通是OPR:+商户编号，具体是什么请参考自己开通产品的手册。
				//arg1:商户私钥字符串
				YopRequest request = new YopRequest("OPR:10033580387",OPRkey);
				//step2 配置参数
			    //arg0:参数名
			    //arg1:参数值
				request.addParam("parentMerchantNo", "10033580387");
				request.addParam("merchantNo", "10033580387");
				request.addParam("orderId", UUID.randomUUID().toString().replaceAll("-", ""));
				request.addParam("orderAmount", money);
				request.addParam("notifyUrl", "http://pay.jiaxinapp.cn:8092/yeepay/callBack");
				request.addParam("goodsParamExt", "{\"goodsName\":\"abc商品名称\" ,\"goodsDesc\" : \"商品描 述\" }");
				//request.addParam("fundProcessType", "");
				//request.addParam("divideDetail", "");
				
			
			    //step3 发起请求
				//arg0:接口的uri（参见手册）
				//arg1:配置好参数的请求对象
				System.out.println("request:"+request.getParams().toString());
			    YopResponse response;
				try {
					response = YopRsaClient.post("/rest/v1.0/std/trade/order", request);
						System.out.println(response);
						
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
	
}

