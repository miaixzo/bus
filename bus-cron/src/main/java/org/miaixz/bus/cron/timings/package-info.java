/**
 * 时间轮实现，重写了kafka的TimingWheel
 * 时间轮一般会实现成一个环形结构，类似一个时钟，分为很多槽，一个槽代表一个时间间隔，每个槽使用双向链表存储定时任务
 * 指针周期性地跳动，跳动到一个槽位，就执行该槽位的定时任务
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.cron.timings;
