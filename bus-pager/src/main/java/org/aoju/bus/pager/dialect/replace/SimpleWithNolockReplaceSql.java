package org.aoju.bus.pager.dialect.replace;

import org.aoju.bus.pager.dialect.ReplaceSql;

/**
 * 简单处理 with(nolock)
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class SimpleWithNolockReplaceSql implements ReplaceSql {

    //with(nolock)
    protected String WITHNOLOCK = ", PAGEWITHNOLOCK";

    @Override
    public String replace(String sql) {
        return sql.replaceAll("((?i)with\\s*\\(nolock\\))", WITHNOLOCK);
    }

    @Override
    public String restore(String sql) {
        return sql.replaceAll(WITHNOLOCK, " with(nolock)");
    }

}
