package org.aoju.bus.core.compare;

import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * 按照GBK拼音顺序对给定的汉字字符串排序
 *
 * @author Kimi Liu
 * @version 5.2.2
 * @since JDK 1.8+
 */
public class PinyinCompare implements Comparator<String>, Serializable {

    private static final long serialVersionUID = 1L;

    final Collator collator;

    /**
     * 构造
     */
    public PinyinCompare() {
        collator = Collator.getInstance(Locale.CHINESE);
    }

    @Override
    public int compare(String o1, String o2) {
        return collator.compare(o1, o2);
    }

}
