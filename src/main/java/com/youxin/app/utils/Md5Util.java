package com.youxin.app.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Md5Util {

	public static String md5Hex(String data) {
		try {
			StringBuffer sb = new StringBuffer();
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(data.getBytes());
			byte b[] = digest.digest();

			int i;
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					sb.append("0");
				sb.append(Integer.toHexString(i));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String md5HexToAccid(String data) {
		try {
			StringBuffer sb = new StringBuffer();
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(data.getBytes());
			byte b[] = digest.digest();

			int i;
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					sb.append("0");
				sb.append(Integer.toHexString(i));
			}
			
			for (int j = 0; j < data.length(); j++) {
				sb.insert(2*j+1, data.charAt(j));
			}
			
			return sb.substring(0,sb.length()-data.length());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	 /**
     * 加密解密算法 执行一次加密，两次解密
     */
    public static String convertMD5(String inStr){
 
        char[] a = inStr.toCharArray();
        for (int i = 0; i < a.length; i++){
            a[i] = (char) (a[i] ^ 't');
        }
        String s = new String(a);
        return s;
 
    }
 
    public static void main(String[] args) {
//    	System.out.println(convertMD5("1100"));
//    	System.out.println(convertMD5(convertMD5("1100")));]
    	System.out.println(md5Hex("123456"));
    	System.out.println(convertMD5(md5Hex("123456")));
    	System.out.println(md5HexToAccid("100").length());
//    	System.out.println(md5HexToAccid("100000025").substring(0,md5HexToAccid("100000025").length()-"100000025".length()));
	}

}
