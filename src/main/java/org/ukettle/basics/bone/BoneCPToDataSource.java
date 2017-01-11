package org.ukettle.basics.bone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.jolbox.bonecp.BoneCPDataSource;

/**
 * <p>
 * 继承BoneCPDataSource 重写相关方法
 * </p>
 * 
 * @author Kimi Liu
 * @Date Mar 12, 2014
 * @Time 10:50:37
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class BoneCPToDataSource extends BoneCPDataSource {

	private static final long serialVersionUID = 4700427770713980088L;

	@Override
	public void setUsername(String username) {
		super.setUsername(username);
	}

	@Override
	public void setPassword(String password) {
		super.setPassword(password);
	}

	@Override
	public void setJdbcUrl(String url) {
		super.setJdbcUrl(url);
	}

	@Override
	public void setDriverClass(String driverClass) {
		super.setDriverClass(driverClass);
	}

	/**
	 * 功能描述：连接数据库
	 * 
	 * @param driverClass
	 *            连接驱动
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @param url
	 *            连接url
	 */
	public static Connection getConnection(String driverClass, String username,
			String password, String url) {
		if (null != driverClass && null != username && null != password
				&& null != url) {
			try {
				BoneCPToDataSource ds = new BoneCPToDataSource();
				ds.setDriverClass(driverClass);
				ds.setUsername(username);
				ds.setPassword(password);
				ds.setJdbcUrl(url);
				ds.setPartitionCount(3);
				ds.setAcquireIncrement(5);
				ds.setStatementsCacheSize(100);
				ds.setMaxConnectionsPerPartition(30);
				ds.setMinConnectionsPerPartition(10);
				return ds.getConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 功能描述：执行SQL
	 * 
	 * @param stmt
	 *            Statement
	 * @param resource
	 *            InputStream
	 */
	public static void executeSQL(Statement stmt, InputStream resource)
			throws IOException, SQLException {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(resource, "UTF-8"));
			boolean done = false;
			while (!done) {
				StringBuilder command = new StringBuilder();
				String line;
				do {
					line = in.readLine();
					if (line == null) {
						done = true;
						break;
					}
					if (isSQLCommandPart(line))
						command.append(" ").append(line.trim());
				} while (!line.trim().endsWith(";"));

				if ((done) || (command.toString().equals(""))) {
					continue;
				}
				command.deleteCharAt(command.length() - 1);
				stmt.execute(command.toString());
			}
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (Exception e) {
					e.getMessage();
				}
		}
	}

	private static boolean isSQLCommandPart(String line) {
		line = line.trim();
		if (line.equals("")) {
			return false;
		}
		return (!line.startsWith("//")) && (!line.startsWith("--"))
				&& (!line.startsWith("#")) && (!line.startsWith("REM "))
				&& (!line.startsWith("/*")) && (!line.startsWith("*"));
	}

	public static void close(ResultSet rs, Statement stmt,
			PreparedStatement pstmt, Connection conn) {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (Exception e) {
			e.getMessage();
		}
	}

}