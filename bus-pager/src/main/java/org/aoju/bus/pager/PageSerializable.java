package org.aoju.bus.pager;

import java.io.Serializable;
import java.util.List;

/**
 * 分页信息
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class PageSerializable<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    //总记录数
    protected long total;
    //结果集
    protected List<T> list;

    public PageSerializable() {
    }

    public PageSerializable(List<T> list) {
        this.list = list;
        if (list instanceof Page) {
            this.total = ((Page) list).getTotal();
        } else {
            this.total = list.size();
        }
    }

    public static <T> PageSerializable<T> of(List<T> list) {
        return new PageSerializable<T>(list);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "PageSerializable{" +
                "total=" + total +
                ", list=" + list +
                '}';
    }

}
