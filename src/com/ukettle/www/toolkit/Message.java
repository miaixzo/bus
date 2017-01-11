package com.ukettle.www.toolkit;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

/**
 * <p>
 * 本程序用java来实现Email的发送
 * </p>
 * 
 * @author Kimi Liu
 * @Date Mar 21, 2014
 * @Time 21:00:09
 * @version 1.0
 * @since JDK 1.6
 */
public class Message {

	// 邮件对象
	private static MimeMessage message;
	// 发送邮件的Session会话
	private static Session session;
	// 邮件发送时的一些配置信息的一个属性对象
	private static Properties props;
	// 发件人的用户名
	private static String username;
	// 发件人密码
	private static String password;
	// 附件添加的组件
	private static Multipart mp;
	// 存放附件文件
	private static List<FileDataSource> attachment = new LinkedList<FileDataSource>();

	public static void setting(String host, int port) {
		username = "";
		password = "";
		if (null == props) {
			props = new Properties();
		}
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.host", host);
		// 创建邮件
		createMimeMessage();
	}

	public static boolean createMimeMessage() {
		try {
			// 用props对象来创建并初始化session对象
			session = Session.getDefaultInstance(props, null);
			// 用session对象来创建并初始化邮件对象
			message = new MimeMessage(session);
			// 生成附件组件的实例
			mp = new MimeMultipart();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 设置SMTP的身份认证
	 */
	public static void setNeedAuth(boolean need) {
		if (props == null)
			props = System.getProperties();
		if (need)
			props.put("mail.smtp.auth", "true");
		else
			props.put("mail.smtp.auth", "false");
	}

	/**
	 * 进行用户身份验证时，设置用户名和密码
	 */
	public static void setNamePass(String name, String pass) {
		username = name;
		password = pass;
	}

	/**
	 * 设置邮件主题
	 * 
	 * @param mailSubject
	 * @return
	 */
	public static boolean setSubject(String mailSubject) {
		try {
			message.setSubject(mailSubject);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 设置邮件内容,并设置其为文本格式或HTML文件格式，编码方式为UTF-8
	 * 
	 * @param mailBody
	 * @return
	 */
	public static boolean setBody(String mailBody) {
		try {
			BodyPart bp = new MimeBodyPart();
			bp.setContent(
					"<meta http-equiv=Content-Type content=text/html; charset=UTF-8>"
							+ mailBody, "text/html;charset=UTF-8");
			mp.addBodyPart(bp);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 增加发送附件
	 * 
	 * @param filename
	 *            邮件附件的地址，只能是本机地址而不能是网络地址，否则抛出异常
	 * @return
	 */
	public static boolean addFileAffix(String filename) {
		try {
			BodyPart bp = new MimeBodyPart();
			FileDataSource fileds = new FileDataSource(filename);
			bp.setDataHandler(new DataHandler(fileds));
			bp.setFileName(MimeUtility.encodeText(fileds.getName(), "utf-8",
					null)); // 解决附件名称乱码
			mp.addBodyPart(bp);// 添加附件
			attachment.add(fileds);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static boolean delFileAffix() {
		try {
			FileDataSource fileds = null;
			for (Iterator<?> it = attachment.iterator(); it.hasNext();) {
				fileds = (FileDataSource) it.next();
				if (fileds != null && fileds.getFile() != null) {
					fileds.getFile().delete();
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 设置发件人地址
	 * 
	 * @param from
	 */
	public static boolean setFrom(String from, String sender) {
		try {
			message.setFrom(new InternetAddress(format(from, sender)));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 格式化 Name <email@address.com> 的地址
	 * 
	 * @param name
	 *            名字
	 * @param email
	 *            Email地址
	 * @return 格式化的地址
	 */
	public static String format(String from, String sender) {
		try {
			if (null == sender || "".equals(sender)) {
				return from;
			}
			return String.format("%1$s <%2$s>",
					MimeUtility.encodeText(sender, "UTF-8", "B"), from);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return from;
	}

	public static boolean setTo(String to) {
		if (to == null)
			return false;
		try {
			message.setRecipients(javax.mail.Message.RecipientType.TO,
					InternetAddress.parse(to));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static boolean setCopyTo(String copyto) {
		if (copyto == null)
			return false;
		try {
			message.setRecipients(javax.mail.Message.RecipientType.CC,
					InternetAddress.parse(copyto));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static boolean iMessage() {
		try {
			message.setContent(mp);
			message.saveChanges();
			Session mailSession = Session.getInstance(props, null);
			Transport transport = mailSession.getTransport("smtp");
			transport.connect((String) props.get("mail.smtp.host"),
					(Integer) props.get("mail.smtp.port"), username, password);
			transport.sendMessage(message,
					message.getRecipients(javax.mail.Message.RecipientType.TO));
			transport.close();
		} catch (MessagingException e) {
			return false;
		}
		return true;
	}

	public static int iMessage(String username, String password, String smtp,
			int port, String sender, String email, String subject,
			String content) throws Exception {
		if (null == email || "".equals(email)) {
			return -1;
		}
		setting(smtp, port);
		setNamePass(username, password);
		setNeedAuth(true);
		setFrom(username, sender);
		setSubject(subject);
		setBody(content);
		setTo(email);
		iMessage();
		return 0;
	}
}