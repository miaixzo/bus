package org.aoju.bus.pager;

import org.apache.ibatis.session.RowBounds;

/**
 * 分页结果信息
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class PageRowBounds extends RowBounds {

    private Long total;
    private Boolean count;

    public PageRowBounds(int offset, int limit) {
        super(offset, limit);
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Boolean getCount() {
        return count;
    }

    public void setCount(Boolean count) {
        this.count = count;
    }

}
