package org.aoju.bus.pager;

/**
 * 分页查询接口
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface ISelect {

    /**
     * 在接口中调用自己的查询方法，不要在该方法内写过多代码，只要一行查询方法最好
     */
    void doSelect();

}
