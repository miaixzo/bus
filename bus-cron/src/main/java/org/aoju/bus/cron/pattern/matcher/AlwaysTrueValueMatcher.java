package org.aoju.bus.cron.pattern.matcher;


import org.aoju.bus.core.utils.StringUtils;

/**
 * 值匹配，始终返回<code>true</code>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class AlwaysTrueValueMatcher implements ValueMatcher {

    @Override
    public boolean match(Integer t) {
        return true;
    }

    @Override
    public String toString() {
        return StringUtils.format("[Matcher]: always true.");
    }

}
