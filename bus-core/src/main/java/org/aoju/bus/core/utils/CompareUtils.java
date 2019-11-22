package org.aoju.bus.core.utils;


import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.consts.Symbol;

/**
 * 各种比较器实现和封装
 *
 * @author Kimi Liu
 * @version 5.2.3
 * @since JDK 1.8+
 */
public class CompareUtils {

    /**
     * {@code null}安全的对象比较,{@code null}对象小于任何对象
     *
     * @param <T> 被比较对象类型
     * @param c1  对象1,可以为{@code null}
     * @param c2  对象2,可以为{@code null}
     * @return 比较结果, 如果c1 &lt; c2,返回数小于0,c1==c2返回0,c1 &gt; c2 大于0
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T extends Comparable<? super T>> int compare(T c1, T c2) {
        return compare(c1, c2, false);
    }

    /**
     * {@code null}安全的对象比较
     *
     * @param <T>           被比较对象类型（必须实现Comparable接口）
     * @param c1            对象1,可以为{@code null}
     * @param c2            对象2,可以为{@code null}
     * @param isNullGreater 当被比较对象为null时是否排在前面,true表示null大于任何对象,false反之
     * @return 比较结果, 如果c1 &lt; c2,返回数小于0,c1==c2返回0,c1 &gt; c2 大于0
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T extends Comparable<? super T>> int compare(T c1, T c2, boolean isNullGreater) {
        if (c1 == c2) {
            return 0;
        } else if (c1 == null) {
            return isNullGreater ? 1 : -1;
        } else if (c2 == null) {
            return isNullGreater ? -1 : 1;
        }
        return c1.compareTo(c2);
    }

    /**
     * 比较2个版本号
     *
     * @param v1       版本1
     * @param v2       版本2
     * @param complete 是否完整的比较两个版本
     * @return (v1 小于 v2) ? -1 : ((v1 等于 v2) ? 0 : 1)
     */
    public static int compare(String v1, String v2, boolean complete) {
        // v1 null视为最小版本,排在前
        if (v1 == v2) {
            return 0;
        } else if (v1 == null) {
            return -1;
        } else if (v2 == null) {
            return 1;
        }
        // 去除空格
        v1 = v1.trim();
        v2 = v2.trim();
        if (v1.equals(v2)) {
            return 0;
        }
        String[] v1s = v1.split(Symbol.BACKSLASH + Symbol.DOT);
        String[] v2s = v2.split(Symbol.BACKSLASH + Symbol.DOT);
        int v1sLen = v1s.length;
        int v2sLen = v2s.length;
        int len = complete
                ? Math.max(v1sLen, v2sLen)
                : Math.min(v1sLen, v2sLen);

        for (int i = 0; i < len; i++) {
            String c1 = len > v1sLen || null == v1s[i] ? Normal.EMPTY : v1s[i];
            String c2 = len > v2sLen || null == v2s[i] ? Normal.EMPTY : v2s[i];

            int result = c1.compareTo(c2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    /**
     * 自然比较两个对象的大小,比较规则如下：
     *
     * <pre>
     * 1、如果实现Comparable调用compareTo比较
     * 2、o1.equals(o2)返回0
     * 3、比较hashCode值
     * 4、比较toString值
     * </pre>
     *
     * @param <T>           被比较对象类型（必须实现Comparable接口）
     * @param o1            对象1
     * @param o2            对象2
     * @param isNullGreater null值是否做为最大值
     * @return 比较结果, 如果o1 &lt; o2,返回数小于0,o1==o2返回0,o1 &gt; o2 大于0
     */
    public static <T> int compare(T o1, T o2, boolean isNullGreater) {
        if (o1 == o2) {
            return 0;
        } else if (null == o1) {// null 排在后面
            return isNullGreater ? 1 : -1;
        } else if (null == o2) {
            return isNullGreater ? -1 : 1;
        }

        if (o1 instanceof Comparable && o2 instanceof Comparable) {
            //如果bean可比较,直接比较bean
            return ((Comparable) o1).compareTo(o2);
        }

        if (o1.equals(o2)) {
            return 0;
        }

        int result = Integer.compare(o1.hashCode(), o2.hashCode());
        if (0 == result) {
            result = compare(o1.toString(), o2.toString());
        }

        return result;
    }

}
