/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.cron;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Fields;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.DateKit;
import org.aoju.bus.cron.factory.Task;
import org.aoju.bus.cron.pattern.CronPattern;
import org.aoju.bus.setting.magic.PopSetting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 定时任务工具类
 * 此工具持有一个全局{@link Scheduler},所有定时任务在同一个调度器中执行
 * {@link #setMatchSecond(boolean)} 方法用于定义是否使用秒匹配模式,如果为true,则定时任务表达式中的第一位为秒,否则为分,默认是分
 *
 * @author Kimi Liu
 * @version 6.2.5
 * @since JDK 1.8+
 */
public final class Builder {

    /**
     * Crontab配置文件
     */
    public static final String CRONTAB_CONFIG_PATH = "config/cron.setting";

    private static final Scheduler scheduler = new Scheduler();
    private static PopSetting crontabSetting;

    private Builder() {
    }

    /**
     * 自定义定时任务配置文件
     *
     * @param cronSetting 定时任务配置文件
     */
    public static void setCronSetting(PopSetting cronSetting) {
        crontabSetting = cronSetting;
    }

    /**
     * 自定义定时任务配置文件路径
     *
     * @param cronSettingPath 定时任务配置文件路径(相对绝对都可)
     */
    public static void setCronSetting(String cronSettingPath) {
        try {
            crontabSetting = new PopSetting(cronSettingPath, Charset.UTF_8, false);
        } catch (InstrumentException e) {
            // ignore setting file parse error and no config error
        }
    }

    /**
     * 设置是否支持秒匹配
     * 此方法用于定义是否使用秒匹配模式,如果为true,则定时任务表达式中的第一位为秒,否则为分,默认是分
     *
     * @param isMatchSecond <code>true</code>支持,<code>false</code>不支持
     */
    public static void setMatchSecond(boolean isMatchSecond) {
        scheduler.setMatchSecond(isMatchSecond);
    }

    /**
     * 加入定时任务
     *
     * @param schedulingPattern 定时任务执行时间的crontab表达式
     * @param task              任务
     * @return 定时任务ID
     */
    public static String schedule(String schedulingPattern, Task task) {
        return scheduler.schedule(schedulingPattern, task);
    }

    /**
     * 加入定时任务
     *
     * @param id                定时任务ID
     * @param schedulingPattern 定时任务执行时间的crontab表达式
     * @param task              任务
     * @return 定时任务ID
     */
    public static String schedule(String id, String schedulingPattern, Task task) {
        scheduler.schedule(id, schedulingPattern, task);
        return id;
    }

    /**
     * 加入定时任务
     *
     * @param schedulingPattern 定时任务执行时间的crontab表达式
     * @param task              任务
     * @return 定时任务ID
     */
    public static String schedule(String schedulingPattern, Runnable task) {
        return scheduler.schedule(schedulingPattern, task);
    }

    /**
     * 批量加入配置文件中的定时任务
     *
     * @param cronSetting 定时任务设置文件
     */
    public static void schedule(PopSetting cronSetting) {
        scheduler.schedule(cronSetting);
    }

    /**
     * 移除任务
     *
     * @param schedulerId 任务ID
     */
    public static void remove(String schedulerId) {
        scheduler.deschedule(schedulerId);
    }

    /**
     * 移除Task
     *
     * @param id      Task的ID
     * @param pattern {@link CronPattern}
     */
    public static void updatePattern(String id, CronPattern pattern) {
        scheduler.updatePattern(id, pattern);
    }

    /**
     * @return 获得Scheduler对象
     */
    public static Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * 开始,非守护线程模式
     *
     * @see #start(boolean)
     */
    public static void start() {
        start(false);
    }

    /**
     * 开始
     *
     * @param isDeamon 是否以守护线程方式启动,如果为true,则在调用{@link #stop()}方法后执行的定时任务立即结束,否则等待执行完毕才结束
     */
    synchronized public static void start(boolean isDeamon) {
        if (null == crontabSetting) {
            setCronSetting(CRONTAB_CONFIG_PATH);
        }
        if (scheduler.isStarted()) {
            throw new InstrumentException("Scheduler has been started, please stop it first!");
        }

        schedule(crontabSetting);
        scheduler.start(isDeamon);
    }

    /**
     * 重新启动定时任务
     * 此方法会清除动态加载的任务,重新启动后,守护线程与否与之前保持一致
     */
    synchronized public static void restart() {
        if (null != crontabSetting) {
            //重新读取配置文件
            crontabSetting.load();
        }
        if (scheduler.isStarted()) {
            //关闭并清除已有任务
            stop();
        }

        //重新加载任务
        schedule(crontabSetting);
        //重新启动
        scheduler.start();
    }

    /**
     * 停止
     */
    synchronized public static void stop() {
        scheduler.stop(true);
    }

    /**
     * 列举指定日期之后(到开始日期对应年年底)内所有匹配表达式的日期
     *
     * @param patternStr    表达式字符串
     * @param start         起始时间
     * @param count         列举数量
     * @param isMatchSecond 是否匹配秒
     * @return 日期列表
     */
    public static List<Date> matchedDates(String patternStr, Date start, int count, boolean isMatchSecond) {
        return matchedDates(patternStr, start, DateKit.endOfYear(start), count, isMatchSecond);
    }

    /**
     * 列举指定日期范围内所有匹配表达式的日期
     *
     * @param patternStr    表达式字符串
     * @param start         起始时间
     * @param end           结束时间
     * @param count         列举数量
     * @param isMatchSecond 是否匹配秒
     * @return 日期列表
     */
    public static List<Date> matchedDates(String patternStr, Date start, Date end, int count, boolean isMatchSecond) {
        return matchedDates(patternStr, start.getTime(), end.getTime(), count, isMatchSecond);
    }

    /**
     * 列举指定日期范围内所有匹配表达式的日期
     *
     * @param patternStr    表达式字符串
     * @param start         起始时间
     * @param end           结束时间
     * @param count         列举数量
     * @param isMatchSecond 是否匹配秒
     * @return 日期列表
     */
    public static List<Date> matchedDates(String patternStr, long start, long end, int count, boolean isMatchSecond) {
        return matchedDates(new CronPattern(patternStr), start, end, count, isMatchSecond);
    }

    /**
     * 列举指定日期范围内所有匹配表达式的日期
     *
     * @param pattern       表达式
     * @param start         起始时间
     * @param end           结束时间
     * @param count         列举数量
     * @param isMatchSecond 是否匹配秒
     * @return 日期列表
     */
    public static List<Date> matchedDates(CronPattern pattern, long start, long end, int count, boolean isMatchSecond) {
        Assert.isTrue(start < end, "Start date is later than end !");
        final List<Date> result = new ArrayList<>(count);
        long step = isMatchSecond ? Fields.Units.SECOND.getUnit() : Fields.Units.MINUTE.getUnit();
        for (long i = start; i < end; i += step) {
            if (pattern.match(i, isMatchSecond)) {
                result.add(DateKit.date(i));
                if (result.size() >= count) {
                    break;
                }
            }
        }
        return result;
    }

}
