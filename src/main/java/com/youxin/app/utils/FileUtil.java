package com.youxin.app.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youxin.app.ex.ServiceException;

public final class FileUtil {

	public static String readAll(InputStream in) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		StringBuffer sb = new StringBuffer();
		String ln = null;

		while (null != (ln = reader.readLine()))
			sb.append(ln);

		return sb.toString();
	}

	public static String readAll(InputStream in, String charsetName) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, charsetName));
		StringBuffer sb = new StringBuffer();
		String ln = null;

		while (null != (ln = reader.readLine()))
			sb.append(ln);

		return sb.toString();
	}

	public static String readAll(BufferedReader reader) {
		try {
			StringBuffer sb = new StringBuffer();
			String ln = null;

			while (null != (ln = reader.readLine()))
				sb.append(ln);

			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 删除文件(图片、视频、语音)的方法
	 * 
	 * @param paths 文件路径（支持多个）
	 * @return
	 */
	public static String deleteFileToUploadDomain(String domain, String... paths) throws Exception {

		try {

			new Thread(new Runnable() {

				@Override
				public void run() {
					Map<String, Object> params = null;
					String url = "/upload/deleteFileServlet";
					String path = null;
					for (int i = 0; i < paths.length; i++) {
						System.out.println("删除文件  ===> " + paths[i]);
						path = paths[i];
						if (null == path)
							return;
						// -1 表示空地址，不执行删除操作
						else if (path.equals("-1"))
							return;

						params = new HashMap<String, Object>();
						url = domain + url; // 拼接URl
						System.out.println(" domain ===> " + domain + " deleteDomain ===>" + url);
						params.put("paths", paths[i]);
						HttpUtil.URLPost(url, params);
					}

				}
			}).start();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 复制文件(图片、视频、语音)的方法
	 * 
	 * @param paths 文件路径（支持多个）
	 * @return
	 */

	public static String copyFileToUploadDomain(String domain, int validTime, String... paths) {
		String newUrl = null;
//		try {

		Map<String, Object> params = null;
		String url = "/upload/copyFileServlet";
		String path = null;

		for (int i = 0; i < paths.length; i++) {
			System.out.println("复制文件  ===> " + paths[i]);
			path = paths[i];
			// -1 表示空地址，不执行删除操作
			if (path.equals("-1"))
				return null;
			params = new HashMap<String, Object>();
			url = domain + url; // 拼接URl
			System.out.println(" domain ===> " + domain + " deleteDomain ===>" + url);
			params.put("paths", paths[i]);
			params.put("validTime", validTime);
			String resultStr = HttpUtil.URLPost(url, params);
			if (StringUtil.isEmpty(resultStr)) {
				throw new ServiceException("连接文件服务器超时");
			}
			JSONObject resultObj = JSON.parseObject(resultStr);
			JSONObject resultData = resultObj.getJSONObject("data");
			if (null == resultData)
				throw new ServiceException("源文件不存在");
			newUrl = resultData.getString("url");
			System.out.println(" copy new Url =====>" + newUrl);
			return newUrl;
		}
		return newUrl;
	}

	/**
	 * MultipartFile 转file
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static File multipartFileToFile(MultipartFile file) throws Exception {
		File toFile = null;
		InputStream ins = null;
		try {

			if ("".equals(file) || file.getSize() <= 0) {
				file = null;
			} else {
				ins = file.getInputStream();
				toFile = new File(file.getOriginalFilename());
				inputStreamToFile(ins, toFile);
			}
		} finally {
			if (ins != null) {
				ins.close();
			}

		}

		return toFile;
	}

	/**
	 * 获取流文件
	 * 
	 * @param ins
	 * @param file
	 */
	public static void inputStreamToFile(InputStream ins, File file) {
		try {
			OutputStream os = new FileOutputStream(file);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.close();
			ins.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * file转base64
	 * 
	 * @param f
	 * @return
	 * @throws IOException
	 */
	public static String base64(File f) throws IOException {

		FileInputStream inputFile = new FileInputStream(f);
		byte[] base64 = null;
		byte[] buffer = new byte[(int) f.length()];
		inputFile.read(buffer);
		inputFile.close();
		base64 = Base64.getEncoder().encode(buffer);
		f.delete();
		return new String(base64);
	}

	/**
	 * 
	 * @param file    上传的文件
	 * @param relPath 真实存储地址
	 * @param urlPath url隐射地址
	 * @param loanId  文件名
	 * @param request HttpServletRequest
	 * @return
	 */
	public static String uploadPicture(MultipartFile file, String relPath, String urlPath, String loanId,
			HttpServletRequest request) {
		try {
			if (file == null || loanId == null || loanId == "")
				return null;
			File targetFile = null;
			String url = "";// 返回存储路径

			String fileName = file.getOriginalFilename();// 获取文件名加后缀
			if (fileName != null && fileName != "") {
//				String returnUrl = request.getScheme() + "://" + request.getServerName() + ":9898"
//						+ request.getContextPath() + urlPath + "/";// 存储路径
				String returnUrl="http://pic.youxinruanjian.cn"+urlPath+"/";
				String path = ""; // 文件存储位置

//	            	path = "E:\\txt\\loan";
				path = relPath;

				String fileF = fileName.substring(fileName.lastIndexOf("."), fileName.length());// 文件后缀

				fileName = loanId + fileF;// 新的文件名

				// 先判断文件是否存在

				// 获取文件夹路径
				File file1 = new File(path);
				// 如果文件夹不存在则创建
				if (!file1.exists() && !file1.isDirectory()) {
					file1.mkdir();
				}
				// 将图片存入文件夹
				targetFile = new File(file1, fileName);
				try {
					// 将上传的文件写到服务器上指定的文件。
					file.transferTo(targetFile);
					url = returnUrl + fileName;

					return url;
				} catch (Exception e) {
					System.out.println("文件上传错误：" + e.getMessage());
					return null;
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 删除文件
	 * 
	 * @param file
	 * @return
	 */
	public static boolean delFile(File file) {
		try {

			if (!file.exists()) {
				return false;
			}

			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File f : files) {
					delFile(f);
				}
			}
			return file.delete();
		} catch (Exception e) {
			return false;
		}
	}
}
