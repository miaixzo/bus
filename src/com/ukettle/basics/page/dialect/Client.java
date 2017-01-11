package com.ukettle.basics.page.dialect;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.ukettle.basics.page.dialect.db.DB2Dialect;
import com.ukettle.basics.page.dialect.db.H2Dialect;
import com.ukettle.basics.page.dialect.db.HSQLDialect;
import com.ukettle.basics.page.dialect.db.MySQLDialect;
import com.ukettle.basics.page.dialect.db.OracleDialect;
import com.ukettle.basics.page.dialect.db.PostgreSQLDialect;
import com.ukettle.basics.page.dialect.db.SQLServer2005Dialect;
import com.ukettle.basics.page.dialect.db.SQLServerDialect;
import com.ukettle.basics.page.dialect.db.SybaseDialect;


/**
 * @author Kimi Liu
 * @Date Apr 19, 2014
 * @Time 10:21:33
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class Client implements Serializable {
	private static final long serialVersionUID = 8107330250767760951L;
	private static final Map<DB, Dialect> DB_DIALECT = new HashMap<DB, Dialect>();

	/**
	 * 根据数据库名称获取数据库分页查询的方言实现。
	 * 
	 * @param db
	 *            数据库名称
	 * @return 数据库分页方言实现
	 */
	public static Dialect getDialect(DB db) {
		if (DB_DIALECT.containsKey(db)) {
			return DB_DIALECT.get(db);
		}
		Dialect dialect = createDialect(db);
		DB_DIALECT.put(db, dialect);
		return dialect;
	}

	/**
	 * 插入自定义方言的实例
	 * 
	 * @param exDialect
	 *            方言实现
	 */
	public static void putEx(Dialect exDialect) {
		DB_DIALECT.put(DB.EX, exDialect);
	}

	/**
	 * 创建数据库方言
	 * 
	 * @param db
	 *            数据库
	 * @return 数据库
	 */
	private static Dialect createDialect(DB db) {
		switch (db) {
		case MYSQL:
			return new MySQLDialect();
		case ORACLE:
			return new OracleDialect();
		case DB2:
			return new DB2Dialect();
		case POSTGRE:
			return new PostgreSQLDialect();
		case SQLSERVER:
			return new SQLServerDialect();
		case SQLSERVER2005:
			return new SQLServer2005Dialect();
		case SYBASE:
			return new SybaseDialect();
		case H2:
			return new H2Dialect();
		case HSQL:
			return new HSQLDialect();
		default:
			throw new UnsupportedOperationException("Empty db dialect");
		}
	}

}