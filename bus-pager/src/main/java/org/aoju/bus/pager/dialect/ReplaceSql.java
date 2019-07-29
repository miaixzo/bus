package org.aoju.bus.pager.dialect;

/**
 * 替换和还原 SQL
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface ReplaceSql {

    /**
     * 临时替换后用于 jsqlparser 解析
     *
     * @param sql
     * @return
     */
    String replace(String sql);

    /**
     * 还原经过解析后的 sql
     *
     * @param sql
     * @return
     */
    String restore(String sql);

}
