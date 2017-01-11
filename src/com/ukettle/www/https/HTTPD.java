package com.ukettle.www.https;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import com.ukettle.www.toolkit.Constant;
import com.ukettle.www.toolkit.Encode;

/**
 * 功能描述：HTTP请求GET/POST
 * 
 * @author Kimi Liu
 * @Date Sep 03, 2014
 * @Time 19:03:12
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class HTTPD {

	public static String defaulEncoding = Constant.DEFAULT_ENCODING;

	public static final String POST = "POST";
	public static final String GET = "GET";

	public static final int connectTimeout = 5000;
	public static final int readTimeout = 10000;

	/**
	 * 设置全局编码
	 * 
	 * @param encoding
	 *            编码
	 */
	private static void setEncoding(String encoding) {
		if (null != encoding && !"".equals(encoding)) {
			defaulEncoding = encoding;
		}
	}

	/**
	 * 发送POST消息
	 * 
	 * @param url
	 * @return
	 */
	public static String post(String url) {
		try {
			return connect(url, POST, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 发送POST消息
	 * 
	 * @param url
	 * @param msg
	 * @return
	 */
	public static String post(String url, String message) {
		try {
			return connect(url, POST, message, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 发送POST消息
	 * 
	 * @param url
	 * @param msg
	 * @return
	 */
	public static String post(String url, Map<String, Object> params) {
		try {
			return connect(url, POST, null, params);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 发送POST消息
	 * 
	 * @param url
	 * @param msg
	 * @param params
	 * @return
	 */
	public static String post(String url, String msg, Map<String, Object> params) {
		try {
			return connect(url, POST, msg, params);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 发送POST请求
	 * 
	 * @param url
	 *            目的地址
	 * @param parameters
	 *            请求参数，Map类型。
	 * @param encoding
	 *            编码
	 * @return 远程响应结果
	 */
	public static String post(String url, Map<String, String> params,
			String encoding) {
		setEncoding(encoding);
		// 返回的结果
		String result = "";
		// 读取响应输入流
		BufferedReader in = null;
		// 输出响应输入流
		PrintWriter out = null;
		try {
			// 创建URL对象
			URL connURL = new URL(url);
			// 打开URL连接
			HttpURLConnection httpConn = (HttpURLConnection) connURL
					.openConnection();
			// 设置通用属性
			httpConn.setRequestProperty("Accept", "*/*");
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			httpConn.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
			// 设置POST方式
			httpConn.setRequestMethod("POST");
			httpConn.setDoInput(true);
			httpConn.setDoOutput(true);
			httpConn.setConnectTimeout(connectTimeout);
			httpConn.setReadTimeout(readTimeout);
			// 获取HttpURLConnection对象对应的输出流
			out = new PrintWriter(httpConn.getOutputStream());
			// 发送请求参数
			out.write(set(params, "POST", true));
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应，设置编码方式
			in = new BufferedReader(new InputStreamReader(
					httpConn.getInputStream(), defaulEncoding));
			String line;
			// 读取返回的内容
			while (null != (line = in.readLine())) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != out) {
					out.close();
				}
				if (null != in) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 获取消息
	 * 
	 * @param url
	 * @return
	 */
	public static String get(String url) {
		try {
			return connect(url, GET, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取消息
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String get(String url, Map<String, Object> params) {
		try {
			return connect(url, GET, null, params);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 发送GET请求
	 * 
	 * @param url
	 *            目的地址
	 * @param params
	 *            参数集合
	 * @param encoding
	 *            编码
	 * @return 远程响应结果
	 */
	public static String get(String url, Map<String, String> params,
			String encoding) {
		setEncoding(encoding);
		// 返回的结果
		String result = "";
		// 读取响应输入流
		BufferedReader in = null;
		// 编码之后的参数
		try {
			// 编码请求参数
			url = url + set(params, "GET", true);
			// 创建URL对象
			URL connURL = new URL(url);
			// 打开URL连接
			HttpURLConnection httpConn = (HttpURLConnection) connURL
					.openConnection();
			// 设置通用属性
			httpConn.setRequestProperty("Accept", "*/*");
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			httpConn.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
			httpConn.setRequestMethod("GET");
			httpConn.setConnectTimeout(connectTimeout);
			httpConn.setReadTimeout(readTimeout);
			// 建立实际的连接
			httpConn.connect();
			// 定义BufferedReader输入流来读取URL的响应,并设置编码方式
			in = new BufferedReader(new InputStreamReader(
					httpConn.getInputStream(), defaulEncoding));
			String line;
			// 读取返回的内容
			while (null != (line = in.readLine())) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != in) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 发送POST请求
	 * 
	 * @param parameters
	 *            请求参数，Map类型。
	 * @param encoding
	 *            编码
	 * @param method
	 *            请求方式
	 * @return 返回编码参数
	 */
	public static String set(Map<String, String> params, String method,
			boolean encoding) {
		Set<String> keysSet = params.keySet();
		Object[] keys = keysSet.toArray();
		Arrays.sort(keys);
		StringBuffer result = new StringBuffer();
		if ("GET".equalsIgnoreCase(method) && null != params) {
			result.append("?");
		}
		boolean first = true;
		for (Object key : keys) {
			if (first) {
				first = false;
			} else {
				result.append("&");
			}
			result.append(key).append("=");
			Object value = params.get(key);
			String strVal = "";
			if (null != value) {
				strVal = value.toString();
			}
			if (encoding) {
				result.append(Encode.urlEncode(strVal, defaulEncoding));
			} else {
				result.append(strVal);
			}
		}
		return result.toString();
	}

	/**
	 * connect to the URL and send a message
	 * 
	 * @param strUrl
	 * @param method
	 * @param sendMsg
	 * @param params
	 * @return
	 * @throws IOException
	 */
	private static String connect(String strUrl, String method, String message,
			Map<String, Object> params) throws IOException {
		if (null != params)
			strUrl = compile(strUrl, params);
		URL url = new URL(strUrl);
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("content-type", "text/html");
			con.setRequestMethod(method);
			con.setConnectTimeout(5000);
			if (method.equals(POST)) {
				con.setDoOutput(true);
				// send message
				sendBytes(con, message);
			} else
				con.setDoOutput(false);
			// get reply message
			return getReceive(con);
		} finally {
			if (con != null)
				con.disconnect();
		}
	}

	/**
	 * Send bytes
	 * 
	 * @param con
	 * @param msg
	 * @throws IOException
	 */
	private static void sendBytes(HttpURLConnection con, String message)
			throws IOException {
		OutputStream os = null;
		try {
			os = con.getOutputStream();
			if (null == message) {
				os.write(0);
			} else {
				os.write(message.getBytes(defaulEncoding));
			}
			os.flush();
		} finally {
			if (null != os)
				os.close();
		}
	}

	/**
	 * 获取回复消息
	 * 
	 * @param con
	 * @return
	 * @throws IOException
	 */
	private static String getReceive(HttpURLConnection con) throws IOException {
		InputStream is = null;
		try {
			is = con.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					defaulEncoding));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null)
				sb.append(line);
			return sb.toString();
		} finally {
			if (null != is)
				is.close();
		}
	}

	/**
	 * 把参数编译到URL
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	private static String compile(String url, Map<String, Object> params) {
		if (null == params || params.size() == 0)
			return url;
		StringBuffer sb = new StringBuffer();
		sb.append("?");
		for (Map.Entry<String, Object> entry : params.entrySet())
			sb.append(entry.getKey()).append("=").append(entry.getValue())
					.append("&");
		sb.delete(sb.length() - 1, sb.length());
		return url + sb.toString();
	}

	public InputStream getAsStream(String url) throws IOException {
		try {
			URL connURL = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) connURL
					.openConnection();
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
			conn.setRequestMethod("GET");
			conn.connect();
			return conn.getInputStream();
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 请求（GET）一个资源，以文件对象返回
	 * 
	 * @param url
	 *            资源地址
	 * @param filenameSuffix
	 *            文件名后缀
	 * @param actionName
	 *            请求名称，用于日志
	 * @return 成功返回这个资源的文件对象
	 * @throws IOException
	 *             如果发生错误
	 * @throws WeixinException
	 *             如果发生错误
	 */
	protected final File getAsFile(String url, String path, String filename) {
		FileOutputStream fos = null;
		InputStream in = null;
		try {
			File file = new File(path, filename);
			fos = new FileOutputStream(file);
			in = getAsStream(url);
			byte[] buf = new byte[1024];
			int len = 0;
			while ((len = in.read(buf)) != -1) {
				if (len == 0) {
					Thread.sleep(10); // 如果读取到0字节，则留休息一会免得CPU走火入魔
					continue;
				} else {
					fos.write(buf, 0, len);
				}
			}
			fos.flush();
			in.close();
			return file;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}