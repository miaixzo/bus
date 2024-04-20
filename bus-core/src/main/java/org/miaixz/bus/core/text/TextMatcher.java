package org.miaixz.bus.core.text;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.MapKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字符串模式匹配，使用${XXXXX}作为变量
 *
 * <pre>
 *     pattern: ${name}-${age}-${gender}-${country}-${province}-${city}-${status}
 *     text:    "小明-19-男-中国-上海-静安-已婚"
 *     result:  {name=小明, age=19, gender=男, country=中国, province=上海, city=静安, status=已婚}
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TextMatcher {

    List<String> patterns;

    /**
     * 构造
     *
     * @param pattern 模式，变量用${XXX}占位
     */
    public TextMatcher(String pattern) {
        this.patterns = parse(pattern);
    }

    /**
     * 解析表达式
     *
     * @param pattern 表达式，使用${XXXX}作为变量占位符
     * @return 表达式
     */
    private static List<String> parse(String pattern) {
        List<String> patterns = new ArrayList<>();
        final int length = pattern.length();
        char c = 0;
        char pre;
        boolean inVar = false;
        StringBuilder part = new StringBuilder();
        for (int i = 0; i < length; i++) {
            pre = c;
            c = pattern.charAt(i);
            if (inVar) {
                part.append(c);
                if ('}' == c) {
                    // 变量结束
                    inVar = false;
                    patterns.add(part.toString());
                    part.setLength(0);
                }
            } else if (Symbol.C_BRACE_LEFT == c && Symbol.C_DOLLAR == pre) {
                // 变量开始
                inVar = true;
                final String preText = part.substring(0, part.length() - 1);
                if (StringKit.isNotEmpty(preText)) {
                    patterns.add(preText);
                }
                part.setLength(0);
                part.append(pre).append(c);
            } else {
                // 普通字符
                part.append(c);
            }
        }

        if (part.length() > 0) {
            patterns.add(part.toString());
        }
        return patterns;
    }

    /**
     * 匹配并提取匹配到的内容
     *
     * @param text 被匹配的文本
     * @return 匹配的map，key为变量名，value为匹配到的值
     */
    public Map<String, String> match(String text) {
        final HashMap<String, String> result = MapKit.newHashMap(true);
        int from = 0;
        String key = null;
        int to;
        for (String part : patterns) {
            if (StringKit.isWrap(part, Symbol.DOLLAR + Symbol.BRACE_LEFT, Symbol.BRACE_RIGHT)) {
                // 变量
                key = StringKit.sub(part, 2, part.length() - 1);
            } else {
                to = text.indexOf(part, from);
                if (to < 0) {
                    //普通字符串未匹配到，说明整个模式不能匹配，返回空
                    return MapKit.empty();
                }
                if (null != key && to > from) {
                    // 变量对应部分有内容
                    result.put(key, text.substring(from, to));
                }
                // 下一个起始点是普通字符串的末尾
                from = to + part.length();
                key = null;
            }
        }

        if (null != key && from < text.length()) {
            // 变量对应部分有内容
            result.put(key, text.substring(from));
        }

        return result;
    }

}
