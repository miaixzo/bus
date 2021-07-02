/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.shade.beans;

import lombok.Data;
import org.aoju.bus.core.lang.Symbol;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动生成需要的基本信息
 *
 * @author Kimi Liu
 * @version 6.2.3
 * @since JDK 1.8+
 */
@Data
public class TableEntity implements Serializable {

    private String project;

    private String author;

    private String version;

    private String dbUrl;

    private String dbName;

    private String dbPassword;

    private String database;

    private String table;

    private String entityName;

    private String objectName;

    private String entityComment;

    private String createTime;

    private String agile;

    private String entityUrl;

    private String mapperUrl;

    private String mapperXmlUrl;

    private String serviceUrl;

    private String serviceImplUrl;

    private String controllerUrl;

    private String idType;

    private String idJdbcType;

    private List<PropertyInfo> cis;

    private String isSwagger;

    private boolean isHump;

    private String isDubbo;

    public TableEntity(String project, String author, String version, String dbUrl, String dbName, String dbPassword,
                       String database, String table, String agile, String entityUrl, String mapperUrl, String mapperXmlUrl,
                       String serviceUrl, String serviceImplUrl, String controllerUrl, String isSwagger, String isDubbo, boolean isHump) {
        super();
        this.project = project;
        this.author = author;
        this.version = version;
        this.dbUrl = dbUrl.replace("database", database);
        this.dbName = dbName;
        this.dbPassword = dbPassword;
        this.database = database;
        this.table = table;
        this.agile = agile;
        this.entityUrl = entityUrl;
        this.mapperUrl = mapperUrl;
        this.mapperXmlUrl = mapperXmlUrl;
        this.serviceUrl = serviceUrl;
        this.serviceImplUrl = serviceImplUrl;
        this.controllerUrl = controllerUrl;
        this.isSwagger = isSwagger;
        this.isDubbo = isDubbo;
        this.isHump = isHump;
    }

    public static TableEntity get(TableEntity tableEntity) {
        List<PropertyInfo> columns = new ArrayList<>();
        // 创建连接
        Connection con = null;
        PreparedStatement pstemt = null;
        ResultSet rs = null;
        //sql
        String sql = "select column_name,data_type,column_comment from information_schema.columns where table_schema='" + tableEntity.getDatabase() + "' and table_name='" + tableEntity.getTable() + Symbol.SINGLE_QUOTE;
        try {
            con = DriverManager.getConnection(tableEntity.getDbUrl(), tableEntity.getDbName(), tableEntity.getDbPassword());
            pstemt = con.prepareStatement(sql);
            rs = pstemt.executeQuery();
            while (rs.next()) {
                String column = rs.getString(1);
                String jdbcType = rs.getString(2);
                String comment = rs.getString(3);
                PropertyInfo ci = new PropertyInfo();
                ci.setColumn(column);
                if (jdbcType.equalsIgnoreCase("int")) {
                    ci.setJdbcType("Integer");
                } else if (jdbcType.equalsIgnoreCase("datetime")) {
                    ci.setJdbcType("timestamp");
                } else {
                    ci.setJdbcType(jdbcType);
                }
                ci.setComment(comment);
                ci.setProperty(NamingRules.changeToJavaFiled(column, tableEntity.isHump));
                ci.setJavaType(NamingRules.jdbcTypeToJavaType(jdbcType));
                //设置注解类型
                if (column.equalsIgnoreCase("id")) {
                    tableEntity.setIdType(ci.getJavaType());
                    tableEntity.setIdJdbcType(ci.getJdbcType());
                }
                columns.add(ci);
            }
            tableEntity.setCis(columns);
            // 完成后关闭
            rs.close();
            pstemt.close();
            con.close();
            if (null == columns || columns.size() == 0) {
                throw new RuntimeException("未能读取到表或表中的字段 请检查链接url,数据库账户,数据库密码,查询的数据名、是否正确 ");
            }
            return tableEntity;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("自动生成实体类错误：" + e.getMessage());
        } finally {
            try {
                if (null != rs) rs.close();
            } catch (SQLException se2) {
            }
            // 关闭资源
            try {
                if (null != pstemt) pstemt.close();
            } catch (SQLException se2) {
            }// 什么都不做
            try {
                if (null != con) con.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

}
