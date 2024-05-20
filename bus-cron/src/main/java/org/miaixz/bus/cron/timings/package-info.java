/**
 * 定时任务中作业的抽象封装和实现，包括Runnable实现和反射实现
 * {@link org.miaixz.bus.cron.crontab.Crontab}表示一个具体的任务，当满足时间匹配要求时，会执行{@link org.miaixz.bus.cron.crontab.Crontab#execute()}方法。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.cron.timings;
