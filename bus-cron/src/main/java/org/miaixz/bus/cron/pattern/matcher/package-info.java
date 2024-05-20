/**
 * 定时任务表达式匹配器，内部使用
 * 单一表达式使用{@link org.miaixz.bus.cron.pattern.matcher.PatternMatcher}表示
 * {@link org.miaixz.bus.cron.pattern.matcher.PatternMatcher}由7个{@link org.miaixz.bus.cron.pattern.matcher.PartMatcher}组成，
 * 分别表示定时任务表达式中的7个位置:
 * <pre>
 *         0      1     2        3         4       5        6
 *      SECOND MINUTE HOUR DAY_OF_MONTH MONTH DAY_OF_WEEK YEAR
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.cron.pattern.matcher;
