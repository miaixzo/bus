package org.aoju.bus.core.compare;

import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.CompareUtils;

import java.util.Comparator;

/**
 * 按照数组的顺序正序排列，数组的元素位置决定了对象的排序先后<br>
 * 如果参与排序的元素并不在数组中，则排序在前
 *
 * @param <T> 被排序元素类型
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
public class IndexedCompare<T> implements Comparator<T> {

    private T[] array;

    /**
     * 构造
     *
     * @param objs 参与排序的数组，数组的元素位置决定了对象的排序先后
     */
    public IndexedCompare(T... objs) {
        this.array = objs;
    }

    @Override
    public int compare(T o1, T o2) {
        final int index1 = ArrayUtils.indexOf(array, o1);
        final int index2 = ArrayUtils.indexOf(array, o2);
        if (index1 == index2) {
            //位置相同使用自然排序
            return CompareUtils.compare(o1, o2, true);
        }
        return index1 < index2 ? -1 : 1;
    }

}
