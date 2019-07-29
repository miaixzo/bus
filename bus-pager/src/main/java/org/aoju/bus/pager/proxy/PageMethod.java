package org.aoju.bus.pager.proxy;

import org.aoju.bus.pager.ISelect;
import org.aoju.bus.pager.Page;
import org.aoju.bus.pager.plugin.PageFromObject;

import java.util.Properties;

/**
 * 基础分页方法
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class PageMethod {

    protected static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<Page>();
    protected static boolean DEFAULT_COUNT = true;

    /**
     * 获取 Page 参数
     *
     * @return
     */
    public static <T> Page<T> getLocalPage() {
        return LOCAL_PAGE.get();
    }

    /**
     * 设置 Page 参数
     *
     * @param page
     */
    protected static void setLocalPage(Page page) {
        LOCAL_PAGE.set(page);
    }

    /**
     * 移除本地变量
     */
    public static void clearPage() {
        LOCAL_PAGE.remove();
    }

    /**
     * 获取任意查询方法的count总数
     *
     * @param select
     * @return
     */
    public static long count(ISelect select) {
        Page<?> page = startPage(1, -1, true);
        select.doSelect();
        return page.getTotal();
    }

    /**
     * 开始分页
     *
     * @param params
     */
    public static <E> Page<E> startPage(Object params) {
        Page<E> page = PageFromObject.getPageFromObject(params, true);
        //当已经执行过orderBy的时候
        Page<E> oldPage = getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        setLocalPage(page);
        return page;
    }

    /**
     * 开始分页
     *
     * @param pageNum  页码
     * @param pageSize 每页显示数量
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize) {
        return startPage(pageNum, pageSize, DEFAULT_COUNT);
    }

    /**
     * 开始分页
     *
     * @param pageNum  页码
     * @param pageSize 每页显示数量
     * @param count    是否进行count查询
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize, boolean count) {
        return startPage(pageNum, pageSize, count, null, null);
    }

    /**
     * 开始分页
     *
     * @param pageNum  页码
     * @param pageSize 每页显示数量
     * @param orderBy  排序
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize, String orderBy) {
        Page<E> page = startPage(pageNum, pageSize);
        page.setOrderBy(orderBy);
        return page;
    }

    /**
     * 开始分页
     *
     * @param pageNum      页码
     * @param pageSize     每页显示数量
     * @param count        是否进行count查询
     * @param reasonable   分页合理化,null时用默认配置
     * @param pageSizeZero true且pageSize=0时返回全部结果，false时分页,null时用默认配置
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize, boolean count, Boolean reasonable, Boolean pageSizeZero) {
        Page<E> page = new Page<E>(pageNum, pageSize, count);
        page.setReasonable(reasonable);
        page.setPageSizeZero(pageSizeZero);
        //当已经执行过orderBy的时候
        Page<E> oldPage = getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        setLocalPage(page);
        return page;
    }

    /**
     * 开始分页
     *
     * @param offset 起始位置，偏移位置
     * @param limit  每页显示数量
     */
    public static <E> Page<E> offsetPage(int offset, int limit) {
        return offsetPage(offset, limit, DEFAULT_COUNT);
    }

    /**
     * 开始分页
     *
     * @param offset 起始位置，偏移位置
     * @param limit  每页显示数量
     * @param count  是否进行count查询
     */
    public static <E> Page<E> offsetPage(int offset, int limit, boolean count) {
        Page<E> page = new Page<E>(new int[]{offset, limit}, count);
        //当已经执行过orderBy的时候
        Page<E> oldPage = getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        setLocalPage(page);
        return page;
    }

    /**
     * 排序
     *
     * @param orderBy
     */
    public static void orderBy(String orderBy) {
        Page<?> page = getLocalPage();
        if (page != null) {
            page.setOrderBy(orderBy);
        } else {
            page = new Page();
            page.setOrderBy(orderBy);
            page.setOrderByOnly(true);
            setLocalPage(page);
        }
    }

    /**
     * 设置参数
     *
     * @param properties 插件属性
     */
    protected static void setStaticProperties(Properties properties) {
        //defaultCount，这是一个全局生效的参数，多数据源时也是统一的行为
        if (properties != null) {
            DEFAULT_COUNT = Boolean.valueOf(properties.getProperty("defaultCount", "true"));
        }
    }

}
