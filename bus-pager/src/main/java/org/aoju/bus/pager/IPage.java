package org.aoju.bus.pager;

/**
 * 分页接口
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface IPage {

    Integer getPageNum();

    Integer getPageSize();

    String getOrderBy();

}
