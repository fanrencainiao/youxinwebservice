package com.youxin.app.utils.yoppay.demo;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import com.yeepay.g3.sdk.yop.encrypt.DigitalEnvelopeDTO;
import com.yeepay.g3.sdk.yop.utils.DigitalEnvelopeUtils;
import sun.misc.BASE64Decoder;

public class callback_str {
	public static void main(String[] args) {
		String response="idVNHEHbbE70jl_pXRkhscN81ovB91hX6JKIKo3oJbNhSPXDB62nA8o7AB01XTcN-4kD3XlaW_UuRiDDHaxB96lWkPTIt_ofYT-WueUwrVIIGa2h6yO1IOx7bcZnCmypaX4aCmAybRNCt2rxnmTAYCeT46pjFiKwvJr_JeurB_Ub22F4I627b2xEqIL5mw6jf1xIDQzG_0vzciw9A46IoidQ4GUFJIehM_Ilpci5_qpJm1G9Qe0nOAGiM91RqNlJodRmPKTBKUHh7SScpSZpCvi0FWbLHy2ILwNyZooY0SXK7TLVIdobGnUfN3ORaCMfiZJ4jCoKUCZ8a5nFzbtpHQ$5SoB5vMLWGzvifpu13qniJve3OLI9DrM8CSxUMjukY2gCdcEnveshkXSKQNC81iLw3cvn0Ryxe8RfRlPy6gWRQr_T-xuVU2N3eE7IdoWBDvmUHuz_Gy71U_vfj0QNVVpz3mrHkrTW9I3VdVtqv05ZMbJkK9s7VqfOUk8tvJa82ONjG3rjOdoFISbANP9mCMJ9ag2zG1aBMGSMKwydne7DvG7-DUY8xoaW9YrEfgejq7rGD7RhHYkbu2SIkJVv3WhHMMpZ6c53eiI0zgSuHB5pv2P5AH9fn6OztRI-1hjcJFn2s1Ttq67PQ2U9PA2wB83br0HvSuuzHfHsw5bwa21DYM63lZxcz0IP6cz0tmdKafu_QtxqlP31HkZBxCDsZVcd4mHDxN944NbmNeTueGvnhaAGJ9NWNmwi06tWLSl-fu0khhQNDZ33tfT7gLao7gB6AgGprP17wM1C2SRd9Vv9TJiPbWtjTGzgZS9dM3R0xcK02dx7u_X26JjhpacfvsTZ4bTYO7cyLiZs-0gJNiJ9tVJK_QpeAHlo4dc_1I0IHnTRLlWEuQA82UXV2Pu7YKWokQ2qTO1pKpXk0gRWgcnB2D_P3AG0Nw47mta35wxmIcsQAtAncbVCOE2txWnB6gZa1u1S5zKfQTu-k8wOu9vtn5nyf1IxX0ZygzSk3wX88NhgKkOk2KxRFEf8DkwNpI2ExGeCkvNy4xUkDA1YORxDXw3K7FKs9439u6Tvo4ighZFag1BHXJe4v7-xZTd1K3n4FvyM0dqzrn6qYQhH48xDw$AES$SHA256";
		
		try {
			//开始解密
		Map<String,String> jsonMap  = new HashMap<>();
			DigitalEnvelopeDTO dto = new DigitalEnvelopeDTO();
			dto.setCipherText(response);
			PrivateKey privateKey =getPrivateKey();
			System.out.println("privateKey: "+privateKey);
			PublicKey publicKey = getPubKey();
			System.out.println("publicKey: "+publicKey);
			
			dto = DigitalEnvelopeUtils.decrypt(dto, privateKey, publicKey);
			System.out.println("解密结果:"+dto.getPlainText());
			System.out.println(jsonMap);
		} catch (Exception e) {
			throw new RuntimeException("回调解密失败！");
		}
		}
	
	/**
	  * 实例化公钥
	  * 
	  * @return
	  */
	 private static PublicKey getPubKey() {
	  PublicKey publicKey = null;
	  try {

	   // 自己的公钥(测试)
		 String publickey="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6p0XWjscY+gsyqKRhw9MeLsEmhFdBRhT2emOck/F1Omw38ZWhJxh9kDfs5HzFJMrVozgU+SJFDONxs8UB0wMILKRmqfLcfClG9MyCNuJkkfm0HFQv1hRGdOvZPXj3Bckuwa7FrEXBRYUhK7vJ40afumspthmse6bs6mZxNn/mALZ2X07uznOrrc2rk41Y2HftduxZw6T4EmtWuN2x4CZ8gwSyPAW5ZzZJLQ6tZDojBK4GZTAGhnn3bg5bBsBlw2+FLkCQBuDsJVsFPiGh/b6K/+zGTvWyUcu+LUj2MejYQELDO3i2vQXVDk7lVi2/TcUYefvIcssnzsfCfjaorxsuwIDAQAB";
		java.security.spec.X509EncodedKeySpec bobPubKeySpec = new java.security.spec.X509EncodedKeySpec(
	     new BASE64Decoder().decodeBuffer(publickey));
	   // RSA对称加密算法
	   java.security.KeyFactory keyFactory;
	   keyFactory = java.security.KeyFactory.getInstance("RSA");
	   // 取公钥匙对象
	   publicKey = keyFactory.generatePublic(bobPubKeySpec);
	  } catch (NoSuchAlgorithmException e) {
	   e.printStackTrace();
	  } catch (InvalidKeySpecException e) {
	   e.printStackTrace();
	  } catch (IOException e) {
	   e.printStackTrace();
	  }
	  return publicKey;
	 }

	 private static PrivateKey getPrivateKey() {
		  PrivateKey privateKey = null;
		  String priKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCZAluNt7uZOLyhlhMuJVvY9E6y2mPeHJ/TByRQJB0F80qpOqQjQte0F5WLJolyRx7h5yZzOCQ+yCrdHrz1m8PeKK9WUuw/Fg0Wq9tJyWo3s7lvEUifLdMn4eKmEI403WHPgqOqR5LfigbM6OKDwMtta/GazUAXbP7LtXP/O4uL9anfzxs19qT0PzYszwTA3ZyaodqIQfAbASA7v0adrWGeXW1U5DkI9afBO7Hj+gK8tL3jRwcB0Rsnsf7we6h3ZzczyxJ/CaUbgy5ULQhxMPtbeBoZYKWKXkSPl4qCjI12mdkvz6gfITXeyzRgOBoomRF6ThKiWO9WTPndtAfJTrm1AgMBAAECggEAT7rgxjolcTQVZwXyvKsO70Bu+de/DnBQAADKtU/8J6udDg3WleEw0VEwxa+xE3Fn8EMo0AVDLcvUOiDYSgt+xvbgVtUi8cSl0ViADjT8OkZWrD+PXhLc4v1bwzkBQR7S5vSmIAbny7/6xy/bSNhfqIcFAKtPaGWNZ80Hwsneb3EmR84Tt+1VKWTYYxuDhhDo9xppfyMfSLm7ssT6XyJi7ZZodVpWJf/0IYe/AFQs5BDD6pCcQ7tTIO7eB1FZpUJngJMCtAZR9ZN6PxEz0tPyH5m8vLrhce/s7EVAdJnEsibq8qC3qqrMhcgiwktyDexehRU/VE4WX3nR7g2psBI/SQKBgQDb92j72R8AXfhqH7vFGWo+htFwlnU4Vs/Kvu9zoBn44L0exke3U2pFvXXzPcOzj0UM5BwdhMfVoE4+VgI0FsFCoWzfM/XkOTTzXsBIKXOAZyTuOgc2rM77V/v8d7qbFvlqTD1lGkhirmPjaGyYTboQjGTGj42d/XdLKYuXcGCuAwKBgQCyEwFj6L8ES3pfn9baeL7F3yJvLiupzzCQ7gQ3DQM5XTLBSGjgKRefl/It1Zw0lCODg5Q6uy8XeCwP9VHaCc9vNFwqsdgkhrCGDY1afqUj2a8WxjWhFbZGiDT8B5hR96ucBTPSGUN1ftcgrF2A2u2LD53Z1SI5qW8Ttdrtu6/n5wKBgGUq73AVtjp2/c/hdHz8aW8ElsNPj+4vjzZShtMJKajbxF+pFkbs6I3Wy67qE148YKfDKmMxNK++IP5ulGjcplo84FPwFAG5he8A/zKxTdwalFO1AKhW5oOXPeAqOPsnn+MR2ZLseapZRrvKxVdvOEpwJ9FjKOEnOHyrSPaHyw99AoGAKHo7eathKCfEBFJ/8x85Nh83Y5BaKnpHLtp7qMvTbMmrjNXZyQJuNT/Ds/l3TVhTDkTtf8Amvy34zhPUbDyIo12Cic3tNLhNDduswuJAxXpo20No01ntGfFCIfvT+uXSY1+nXN2TQPT5D5s+kdix4EdHeUmCQxspfZB72EgufwcCgYBgff+Evkxr8FYA2k1YPcMdfJHJN+Yk4Rvuw752fx5iGAphqmZfLckg0PhjNoPzrkXI4QwoYrmXwdpd2MV5NzB6peXSTlD/HY6miV/mlOtjqZFURIWoh2+YONome0buj5Lxmz8CHfQHCjgTVskzBYJ27GxTxuViH+EUhfdT1M0H6Q==";
		  
		  PKCS8EncodedKeySpec priPKCS8;
		  try {
		   priPKCS8 = new PKCS8EncodedKeySpec(new BASE64Decoder().decodeBuffer(priKey));
		   KeyFactory keyf = KeyFactory.getInstance("RSA");
		   privateKey = keyf.generatePrivate(priPKCS8);
		  } catch (IOException e) {
		   e.printStackTrace();
		  } catch (NoSuchAlgorithmException e) {
		   e.printStackTrace();
		  } catch (InvalidKeySpecException e) {
		   e.printStackTrace();
		  }
		  return privateKey;
		 }

	
	}