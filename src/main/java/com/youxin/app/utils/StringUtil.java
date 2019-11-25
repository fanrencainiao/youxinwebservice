package com.youxin.app.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.multipart.MultipartFile;


public final class StringUtil {

	public static String trim(String s) {
		StringBuilder sb = new StringBuilder();
		for (char ch : s.toCharArray())
			if (' ' != ch)
				sb.append(ch);
		s = sb.toString();

		return s.replaceAll("&nbsp;", "").replaceAll(" ", "").replaceAll("　", "").replaceAll("\t", "").replaceAll("\n", "");
	}

	private static final char[] charArray = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
			'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	public static String getExt(String filename) {
		return filename.substring(filename.lastIndexOf('.'));
	}
	/**
	 * 是否是数字
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str){ 
		   Pattern pattern = Pattern.compile("[0-9]*"); 
		   Matcher isNum = pattern.matcher(str);
		   if( !isNum.matches() ){
		       return false; 
		   } 
		   return true; 
	}
	
	
	public static boolean isEmpty(String s) {
		return isNullOrEmpty(s);
	}

	public static boolean isNullOrEmpty(String s) {
		return null == s || 0 == s.trim().length();
	}

	public static String randomCode() {
		return "" + (new Random().nextInt(899999) + 100000);
	}

	public static String randomAccount(String accountTitle) {
		//用字符数组的方式随机
		String randomAccount = "";
		String model = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		char[] m = model.toCharArray();
		
		for (int j=0;j<6 ;j++ )
		{
			char ch = m[(int)(Math.random()*52)];
			randomAccount = randomAccount + ch;
		}
		if(isEmpty(accountTitle)) 
			return randomAccount;
		return accountTitle + randomAccount;
	}
	public static String randomPassword() {
		return randomString(6);
	}
	
 public static String getOutTradeNo() {
	 int r1 = (int) (Math.random() * (10));// 产生2个0-9的随机数
		int r2 = (int) (Math.random() * (10));
		long now = System.currentTimeMillis();// 一个13位的时间戳
		String id = String.valueOf(r1) + String.valueOf(r2)
				+ String.valueOf(now);// 订单ID
		return id;
   }

	public static String randomString(int length) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < length; i++) {
			int index = new Random().nextInt(36);
			sb.append(charArray[index]);
		}

		return sb.toString();
	}

	public static String randomUUID() {
		UUID uuid = UUID.randomUUID();
		String uuidStr = uuid.toString().replace("-", "");

		return uuidStr;
	}

	public static String getFormatName(String fileName) {
		int index = fileName.lastIndexOf('.');

		return -1 == index ? "jpg" : fileName.substring(index + 1);
	}

	public static String getFormatName(MultipartFile file) {
		return getFormatName(file.getName());
	}

	public static String randomFileName(MultipartFile file) {
		return randomUUID() + "." + getFormatName(file.getName());
	}

	/**默认的文本分隔符 */
	public static final String Default_Split= "#";
	
	
	

	public static String[] getStringList(String str) {
		str = trim(str);
		if (str.endsWith(",")) {
			str = str.substring(0, str.length() - 1);
		}
		String sep = ",";
		if (str.indexOf(':') >= 0) {
			sep = ":";
		}
		return str.split(sep);
	}

	public static String[] getStringList(String str, String sep) {
		str = trim(str);
		return str.split(sep);
	}

	public static int[] getIntArray(String str, String sep) {
		String[] prop = getStringList(str, sep);
		List<Integer> tmp = new ArrayList<Integer>();
		for (int i = 0; i < prop.length; i++) {
			try {
				int r = Integer.parseInt(prop[i]);
				tmp.add(r);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		int[] ints = new int[tmp.size()];
		for (int i = 0; i < tmp.size(); i++) {
			ints[i] = tmp.get(i);
		}
		return ints;
	}

	public static List<Integer> getIntList(String str, String sep) {
		List<Integer> tmp = new ArrayList<Integer>();
		if (str == null || str.trim().equals("")) {
			return tmp;
		}
		String[] prop = getStringList(str, sep);
		for (int i = 0; i < prop.length; i++) {
			try {
				int r = Integer.parseInt(prop[i]);
				tmp.add(r);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tmp;
	}

	public static String join(String[] strs, String sep) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(strs[0]);
		for (int i = 1; i < strs.length; i++) {
			buffer.append(sep).append(strs[i]);
		}
		return buffer.toString();
	}

	public static String join(List<Integer> ints, String sep) {
		StringBuffer sb = new StringBuffer();
		sb.append(ints.get(0));
		for (int i = 1; i < ints.size(); i++) {
			sb.append(sep).append(ints.get(i));
		}
		return sb.toString();
	}
	public static String getStringByList(List<String> ints, String sep) {
		StringBuffer sb = new StringBuffer();
		sb.append(ints.get(0));
		for (int i = 1; i < ints.size(); i++) {
			sb.append(sep).append(ints.get(i));
		}
		return sb.toString();
	}

	public static double[] getDoubleList(String str) {
		String[] prop = getStringList(str);
		double[] ds = new double[prop.length];
		for (int i = 0; i < ds.length; i++) {
			ds[i] = Double.parseDouble(prop[i]);
		}
		return ds;
	}

	public static List<String> getListBySplit(String str, String split) {
		List<String> list = new ArrayList<String>();
		//|| str.trim().equalsIgnoreCase("")
		if (str == null )
			return list;
		String[] strs = str.split(split);
		for (String temp : strs) {
			//&& !temp.trim().equalsIgnoreCase("")
			if (temp != null ) {
				list.add(temp);
			}
		}
		return list;
	}

	public static int[] getIntList(String str) {
		String[] prop = getStringList(str);
		List<Integer> tmp = new ArrayList<Integer>();
		for (int i = 0; i < prop.length; i++) {
			try {
				String sInt = prop[i].trim();
				if (sInt.length() < 20) {
					int r = Integer.parseInt(prop[i].trim());
					tmp.add(r);
				}
			} catch (Exception e) {
			}
		}
		int[] ints = new int[tmp.size()];
		for (int i = 0; i < tmp.size(); i++) {
			ints[i] = tmp.get(i);
		}
		return ints;

	}

	public static String toWrapString(Object obj, String content) {
		if (obj == null) {
			return "null";
		} else {
			return obj.getClass().getName() + "@" + obj.hashCode() + "[\r\n"
					+ content + "\r\n]";
		}
	}

	// 将1,2,3和{1,2,3}格式的字符串转化为JDK的bitset
	// 考虑了两边是否有{}，数字两边是否有空格，是否合法数字
	public static BitSet bitSetFromString(String str) {
		if (str == null) {
			return new BitSet();
		}
		if (str.startsWith("{")) {
			str = str.substring(1);
		}
		if (str.endsWith("}")) {
			str = str.substring(0, str.length() - 1);
		}
		int[] ints = getIntList(str);
		BitSet bs = new BitSet();
		for (int i : ints) {
			bs.set(i);
		}
		return bs;
	}

	public static boolean hasExcludeChar(String str) {
		if (str != null) {
			char[] chs = str.toCharArray();
			for (int i = 0; i < chs.length; i++) {

				if (Character.getType(chs[i]) == Character.PRIVATE_USE) {

					return true;
				}

			}
		}
		return false;
	}

	public static String replaceSql(String str) {
		if (str != null) {
			return str.replaceAll("'", "’").replaceAll("<", "&lt;").replaceAll(
					">", "&gt;").replaceAll("\"", "&quot;");
		}
		return "";
	}

	/**
	 * 判断两个字符串是否相等
	 *
	 * @param s1
	 * @param s2
	 * @return true,字符串相等;false,字符串不相等
	 */
	public static boolean isEquals(String s1, String s2) {
		if (s1 != null) {
			return s1.equals(s2);
		}
		if (s2 != null) {
			return false;
		}
		// 两个字符串都是null
		return true;
	}

	

	/**
	 * 判断字符串是否时数字
	 *
	 * @param text
	 * @return
	 */
	public static boolean isDigit(String text) {
		String reg = "[-]*[\\d]+[\\.\\d+]*";
		Pattern pat = Pattern.compile(reg);
		Matcher mat = pat.matcher(text);
		return mat.matches();
	}

	/**
	 * 判断一句话是否是汉语
	 *
	 * @param text
	 * @return
	 */
	public static boolean isChiness(String text) {
		String reg = "[\\w]*[\\u4e00-\\u9fa5]+[\\w]*";
		Pattern pat = Pattern.compile(reg);
		Matcher mat = pat.matcher(text);
		boolean result = mat.matches();
		return result;
	}

	/**
	 * 判断单个字符是否是汉语
	 *
	 * @param cha
	 * @return
	 */
	public static boolean isChineseChar(char cha) {
		String reg = "[\\u4e00-\\u9fa5]";
		Pattern pat = Pattern.compile(reg);
		String text = Character.toString(cha);
		Matcher mat = pat.matcher(text);
		boolean result = mat.matches();
		return result;
	}

	/**
	 * 判断字符是否是字母(包括大小写)或者数字
	 *
	 * @param cha
	 * @return
	 */
	public static boolean isLetterAndDigit(String cha) {
		String reg = "[\\w]+";
		Pattern pat = Pattern.compile(reg);
		Matcher mat = pat.matcher(cha);
		boolean result = mat.matches();
		return result;
	}

	/**
	 * 返回字符串中汉字的数量
	 *
	 * @param test
	 * @return
	 */
	public static int getChineseCount(String test) {
		int count = 0;
		boolean tempResult = false;
		for (int i = 0; i < test.length(); i++) {
			char cha = test.charAt(i);
			tempResult = isChineseChar(cha);
			if (tempResult) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 返回字符串中字母和数字的个数，其中字母包括大小写
	 *
	 * @param text
	 * @return
	 */
	public static int getLetterAndDigitCount(String text) {
		int count = 0;
		boolean tempResult = false;
		for (int i = 0; i < text.length(); i++) {
			tempResult = isLetterAndDigit(text);
			if (tempResult) {
				count++;
			}
		}
		return count;
	}

	
	/**
	 * 将字符串首字母大写
	 *
	 * @param s
	 * @return
	 */
	public static String upperCaseFirstCharOnly(String s) {
		if (s == null || s.length() < 1) {
			return s;
		}
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}
	  /**
     * 数组转化为String
     * @param s
     * @param sep
     * @return
     */
    public static String arrayToString(String[] s, char sep) {
        if (s == null || s.length == 0){
        	return "";
        }
        StringBuffer buf = new StringBuffer();
        if (s != null) {
            for (int i = 0; i < s.length; i++) {
                if (i > 0)
                    buf.append(sep);
                buf.append(s[i]);
            }
        }
        return buf.toString();
    }
    
	/**
	 * 获取拼起来的key
	 * @param splitString
	 * @param strings
	 * @return
	 */
	public static String getString(String splitString, String... strings){
		StringBuffer stringBuffer = new StringBuffer();
		for(int i = 0; i < strings.length; i++){
			stringBuffer.append(strings[i]);
			if(i == strings.length - 1){
				break;
			}
			stringBuffer.append(splitString);
		}
		return stringBuffer.toString();
	}

    
	/**
	 * 获取拼起来的key
	 * @param splitString
	 * @param strings
	 * @return
	 */
	public static String getString(String splitString, int start, Serializable... strings){
		StringBuffer stringBuffer = new StringBuffer();
		for(int i = start; i < strings.length; i++){
			stringBuffer.append(strings[i]);
			if(i == strings.length - 1){
				break;
			}
			stringBuffer.append(splitString);
		}
		return stringBuffer.toString();
	}

	//首字母转小写
	public static String toLowerCaseFirstOne(String s){
		if(Character.isLowerCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
	}
	/**
	 * 手机号加密
	 * @param p
	 * @return
	 */
	public static String phoneEncryption(String p) {
		try {
			char[] a=p.replace(" ", "").toCharArray();
			String[] p1 = new String[11];
			for (int i = 0; i < a.length; i++) {
				p1[i]=String.valueOf(Integer.valueOf(String.valueOf(a[i]))*3+7);
			}
			return Arrays.toString(p1);
		} catch (Exception e) {
			return "";
		}
	}
}
