package org.aoju.bus.pager.dialect.replace;

import org.aoju.bus.pager.dialect.ReplaceSql;

/**
 * 正则处理 with(nolock)，转换为一个 table_PAGEWITHNOLOCK
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class RegexWithNolockReplaceSql implements ReplaceSql {

    //with(nolock)
    protected String WITHNOLOCK = ", PAGEWITHNOLOCK";

    @Override
    public String replace(String sql) {
        return sql.replaceAll("((?i)\\s*(\\w+)\\s*with\\s*\\(nolock\\))", " $2_PAGEWITHNOLOCK");
    }

    @Override
    public String restore(String sql) {
        return sql.replaceAll("\\s*(\\w*?)_PAGEWITHNOLOCK", " $1 WITH(NOLOCK)");
    }
}
