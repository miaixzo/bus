/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
 ********************************************************************************/
package org.aoju.bus.health.unix.solaris;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.solaris.LibKstat;
import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;
import com.sun.jna.platform.unix.solaris.LibKstat.KstatNamed;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.health.Builder;
import org.aoju.bus.logger.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 提供对Solaris上的kstat信息的访问
 *
 * @author Kimi Liu
 * @version 5.9.0
 * @since JDK 1.8+
 */
@ThreadSafe
public final class KstatCtl {

    private static final LibKstat KS = LibKstat.INSTANCE;

    // 打开kstat链。自动关闭在退出。任何时候只有一个线程可以访问这个链，
    // 所以我们将这个对象封装在KstatChain类中，这个类会一直锁定这个类直到关闭.
    private static final LibKstat.KstatCtl KC = KS.kstat_open();
    private static final ReentrantLock CHAIN = new ReentrantLock();

    private KstatCtl() {
    }

    /**
     * 创建Kstat链的副本并将其锁定，以供该对象使用
     *
     * @return 一个锁上的链的副本。当您使用{@link KstatChain#close()}完成时，它应该被解锁/释放
     */
    public static KstatChain openChain() {
        return new KstatChain();
    }

    /**
     * 方便的方法{@link LibKstat#kstat_data_lookup}与字符串返回值
     * 在kstat的data部分中搜索具有指定名称的记录。此操作仅对已命名数据记录的kstat类型有效
     * 目前，只有KSTAT_TYPE_NAMED和KSTAT_TYPE_TIMER kstats有命名数据记录.
     *
     * @param ksp  要搜索的kstat
     * @param name 名称-值对的键，或计时器的名称(如适用)
     * @return 字符串值
     */
    public static String dataLookupString(Kstat ksp, String name) {
        if (ksp.ks_type != LibKstat.KSTAT_TYPE_NAMED && ksp.ks_type != LibKstat.KSTAT_TYPE_TIMER) {
            throw new IllegalArgumentException("Not a kstat_named or kstat_timer kstat.");
        }
        Pointer p = KS.kstat_data_lookup(ksp, name);
        if (p == null) {
            Logger.error("Failed lo lookup kstat value for key {}", name);
            return Normal.EMPTY;
        }
        KstatNamed data = new KstatNamed(p);
        switch (data.data_type) {
            case LibKstat.KSTAT_DATA_CHAR:
                return new String(data.value.charc, StandardCharsets.UTF_8).trim();
            case LibKstat.KSTAT_DATA_INT32:
                return Integer.toString(data.value.i32);
            case LibKstat.KSTAT_DATA_UINT32:
                return Builder.toUnsignedString(data.value.ui32);
            case LibKstat.KSTAT_DATA_INT64:
                return Long.toString(data.value.i64);
            case LibKstat.KSTAT_DATA_UINT64:
                return Builder.toUnsignedString(data.value.ui64);
            case LibKstat.KSTAT_DATA_STRING:
                return data.value.str.addr.getString(0);
            default:
                Logger.error("Unimplemented kstat data type {}", data.data_type);
                return Normal.EMPTY;
        }
    }

    /**
     * 具有数字返回值的{@link LibKstat#kstat_data_lookup}的便利方法
     * 在kstat的data部分中搜索具有指定名称的记录。此操作仅对已命名数据记录的kstat类型有效
     * 目前，只有KSTAT_TYPE_NAMED和KSTAT_TYPE_TIMER kstats有命名数据记录。
     *
     * @param ksp  要搜索的kstat
     * @param name 名称-值对的键，或计时器的名称(如适用)
     * @return 长整型的值。如果数据类型是字符或字符串类型，则返回0并记录错误。
     */
    public static long dataLookupLong(Kstat ksp, String name) {
        if (ksp.ks_type != LibKstat.KSTAT_TYPE_NAMED && ksp.ks_type != LibKstat.KSTAT_TYPE_TIMER) {
            throw new IllegalArgumentException("Not a kstat_named or kstat_timer kstat.");
        }
        Pointer p = KS.kstat_data_lookup(ksp, name);
        if (p == null) {
            if (Logger.get().isError()) {
                Logger.error("Failed lo lookup kstat value on {}:{}:{} for key {}",
                        new String(ksp.ks_module, StandardCharsets.US_ASCII).trim(), ksp.ks_instance,
                        new String(ksp.ks_name, StandardCharsets.US_ASCII).trim(), name);
            }
            return 0L;
        }
        KstatNamed data = new KstatNamed(p);
        switch (data.data_type) {
            case LibKstat.KSTAT_DATA_INT32:
                return data.value.i32;
            case LibKstat.KSTAT_DATA_UINT32:
                return Builder.getUnsignedInt(data.value.ui32);
            case LibKstat.KSTAT_DATA_INT64:
                return data.value.i64;
            case LibKstat.KSTAT_DATA_UINT64:
                return data.value.ui64;
            default:
                Logger.error("Unimplemented or non-numeric kstat data type {}", data.data_type);
                return 0L;
        }
    }

    /**
     * Kstat链的一个副本，它封装了一个{@code kstat_ctl_t}对象
     * 任何时候只有一个线程可以积极地使用这个对象
     * 实例化这个对象是使用{@link KstatCtl#openChain}方法完成的。它锁定和更新链
     * 相当于调用{@link LibKstat#kstat_open}。控件对象应该使用{@link #close}关闭
     * 这相当于调用{@link LibKstat#kstat_close}。
     */
    public static final class KstatChain implements AutoCloseable {

        private KstatChain() {
            CHAIN.lock();
            this.update();
        }

        /**
         * {@link LibKstat#kstat_read}的便利方法，它从内核获取由{@code ksp}指向的kstat的数据
         * {@code ksp.ks_data}被自动分配(或重新分配)到足够大以容纳所有数据。
         * {@code ksp.ks_data}设置为数据字段的数量{@code ksp.ks_data_size}设置为
         * 数据的总大小和ksp。ks_snaptime设置为拍摄数据快照的高分辨率时间
         *
         * @param ksp 要从中检索数据的kstat
         * @return {@code true}如果成功;{@code false}
         */
        public boolean read(Kstat ksp) {
            int retry = 0;
            while (0 > KS.kstat_read(KC, ksp, null)) {
                if (LibKstat.EAGAIN != Native.getLastError() || 5 <= ++retry) {
                    if (Logger.get().isError()) {
                        Logger.error("Failed to read kstat {}:{}:{}",
                                new String(ksp.ks_module, StandardCharsets.US_ASCII).trim(), ksp.ks_instance,
                                new String(ksp.ks_name, StandardCharsets.US_ASCII).trim());
                    }
                    return false;
                }
                Builder.sleep(8 << retry);
            }
            return true;
        }

        /**
         * 方便的方法{@link LibKstat#kstat_lookup}。遍历kstat链，使用相同的
         * {@code module}、{@code instance}和{@code name}字段搜索kstat;这个三元组唯
         * 一地标识一个kstat，如果{@code module}是{@code null}， {@code instance}是-1
         * 或者{@code name}是{@code null}，那么这些字段将在搜索中被忽略。
         *
         * @param module   模块，或忽略null
         * @param instance 实例，或者忽略-1
         * @param name     名称，或忽略null
         * @return 如果找到请求的Kstat结构的第一个匹配项，或者{@code null}
         */
        public Kstat lookup(String module, int instance, String name) {
            return KS.kstat_lookup(KC, module, instance, name);
        }

        /**
         * 方便的方法{@link LibKstat#kstat_lookup}。遍历kstat链，使用相同的
         * {@code module}、{@code instance}和{@code name}字段搜索所有kstats;
         * 这个三元组唯一地标识一个kstat。如果{@code module}是{@code null}，
         * {@code instance}是-1，或者{@code name}是{@code null}，那么这些字段将在搜索中被忽略
         *
         * @param module   模块，或忽略null
         * @param instance 实例，或者忽略-1
         * @param name     名称，或忽略null
         * @return 如果找到所请求的Kstat结构的所有匹配项，则为空列表
         */
        public List<Kstat> lookupAll(String module, int instance, String name) {
            List<Kstat> kstats = new ArrayList<>();
            for (Kstat ksp = KS.kstat_lookup(KC, module, instance, name); ksp != null; ksp = ksp.next()) {
                if ((module == null || module.equals(new String(ksp.ks_module, StandardCharsets.US_ASCII).trim()))
                        && (instance < 0 || instance == ksp.ks_instance)
                        && (name == null || name.equals(new String(ksp.ks_name, StandardCharsets.US_ASCII).trim()))) {
                    kstats.add(ksp);
                }
            }
            return kstats;
        }

        /**
         * 方便的方法{@link LibKstat#kstat_chain_update}。使这个kstat头链与内核的头链同步。
         *
         * @return 如果kstat链已经更改，则为0，如果没有更改，则为-1
         */
        public int update() {
            return KS.kstat_chain_update(KC);
        }

        /**
         * 解开链条上的锁
         */
        @Override
        public void close() {
            CHAIN.unlock();
        }

    }

}
