/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.data.id;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.ip.IPv4;

/**
 * ID生成器工具类，此工具类中主要封装：
 *
 * <pre>
 * 1. 唯一性ID生成器：UUID、ObjectId（MongoDB）、Snowflake
 * </pre>
 *
 * <p>
 * ID相关文章见：<a href="http://calvin1978.blogcn.com/articles/uuid.html">http://calvin1978.blogcn.com/articles/uuid.html</a>
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ID {

    /**
     * 获取随机UUID
     *
     * @return 随机UUID
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 简化的UUID，去掉了横线
     *
     * @return 简化的UUID，去掉了横线
     */
    public static String simpleUUID() {
        return UUID.randomUUID().toString(true);
    }

    /**
     * 获取随机UUID，使用性能更好的ThreadLocalRandom生成UUID
     *
     * @return 随机UUID
     */
    public static String fastUUID() {
        return UUID.fastUUID().toString();
    }

    /**
     * 简化的UUID，去掉了横线，使用性能更好的ThreadLocalRandom生成UUID
     *
     * @return 简化的UUID，去掉了横线
     */
    public static String fastSimpleUUID() {
        return UUID.fastUUID().toString(true);
    }

    /**
     * 创建MongoDB ID生成策略实现 ObjectId由以下几部分组成：
     *
     * <pre>
     * 1. Time 时间戳。
     * 2. Machine 所在主机的唯一标识符，一般是机器主机名的散列值。
     * 3. PID 进程ID。确保同一机器中不冲突
     * 4. INC 自增计数器。确保同一秒内产生objectId的唯一性。
     * </pre>
     * <p>
     * 参考：<a href=
     * "http://blog.csdn.net/qxc1281/article/details/54021882">http://blog.csdn.net/qxc1281/article/details/54021882</a>
     *
     * @return ObjectId
     */
    public static String objectId() {
        return ObjectId.next();
    }

    /**
     * 获取单例的Twitter的Snowflake 算法生成器对象 分布式系统中，有一些需要使用全局唯一ID的场景，有些时候我们希望能使用一种简单一些的ID，并且希望ID能够按照时间有序生成。
     *
     * <p>
     * snowflake的结构如下(每部分用-分开):
     * 
     * <pre>
     * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
     * </pre>
     * <p>
     * 第一位为未使用，接下来的41位为毫秒级时间(41位的长度可以使用69年) 然后是5位datacenterId和5位workerId(10位的长度最多支持部署1024个节点）
     * 最后12位是毫秒内的计数（12位的计数顺序号支持每个节点每毫秒产生4096个ID序号）
     * </p>
     * <p>
     * 参考：<a href="http://www.cnblogs.com/relucent/p/4955340.html">http://www.cnblogs.com/relucent/p/4955340.html</a>
     * </p>
     *
     * @param workerId     终端ID
     * @param datacenterId 数据中心ID
     * @return {@link Snowflake}
     */
    public static Snowflake getSnowflake(final long workerId, final long datacenterId) {
        return Instances.get(Snowflake.class, workerId, datacenterId);
    }

    /**
     * 获取单例的Twitter的Snowflake 算法生成器对象 分布式系统中，有一些需要使用全局唯一ID的场景，有些时候我们希望能使用一种简单一些的ID，并且希望ID能够按照时间有序生成。
     * snowflake的结构如下(每部分用-分开):
     * 
     * <pre>
     * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
     * </pre>
     *
     * <p>
     * 第一位为未使用，接下来的41位为毫秒级时间(41位的长度可以使用69年) 然后是5位datacenterId和5位workerId(10位的长度最多支持部署1024个节点）
     * 最后12位是毫秒内的计数（12位的计数顺序号支持每个节点每毫秒产生4096个ID序号）
     * </p>
     *
     * <p>
     * 参考：<a href="http://www.cnblogs.com/relucent/p/4955340.html">http://www.cnblogs.com/relucent/p/4955340.html</a>
     * </p>
     *
     * @param workerId 终端ID
     * @return {@link Snowflake}
     */
    public static Snowflake getSnowflake(final long workerId) {
        return Instances.get(Snowflake.class, workerId);
    }

    /**
     * 获取单例的Twitter的Snowflake 算法生成器对象 分布式系统中，有一些需要使用全局唯一ID的场景，有些时候我们希望能使用一种简单一些的ID，并且希望ID能够按照时间有序生成。
     * snowflake的结构如下(每部分用-分开):
     * 
     * <pre>
     * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
     * </pre>
     *
     * <p>
     * 第一位为未使用，接下来的41位为毫秒级时间(41位的长度可以使用69年) 然后是5位datacenterId和5位workerId(10位的长度最多支持部署1024个节点）
     * 最后12位是毫秒内的计数（12位的计数顺序号支持每个节点每毫秒产生4096个ID序号）
     * </p>
     *
     * <p>
     * 参考：<a href="http://www.cnblogs.com/relucent/p/4955340.html">http://www.cnblogs.com/relucent/p/4955340.html</a>
     * </p>
     *
     * @return {@link Snowflake}
     */
    public static Snowflake getSnowflake() {
        return Instances.get(Snowflake.class);
    }

    /**
     * 获取数据中心ID 数据中心ID依赖于本地网卡MAC地址。
     * <p>
     * 此算法来自于mybatis-plus#Sequence
     * </p>
     *
     * @param maxDatacenterId 最大的中心ID
     * @return 数据中心ID
     */
    public static long getDataCenterId(long maxDatacenterId) {
        Assert.isTrue(maxDatacenterId > 0, "maxDatacenterId must be > 0");
        if (maxDatacenterId == Long.MAX_VALUE) {
            maxDatacenterId -= 1;
        }
        long id = 1L;
        byte[] mac = null;
        try {
            mac = IPv4.getLocalHardwareAddress();
        } catch (final InternalException ignore) {
            // ignore
        }
        if (null != mac) {
            id = ((0x000000FF & (long) mac[mac.length - 2]) | (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
            id = id % (maxDatacenterId + 1);
        }

        return id;
    }

    /**
     * 获取机器ID，使用进程ID配合数据中心ID生成 机器依赖于本进程ID或进程名的Hash值。
     *
     * <p>
     * 此算法来自于mybatis-plus#Sequence
     * </p>
     *
     * @param datacenterId 数据中心ID
     * @param maxWorkerId  最大的机器节点ID
     * @return the long
     */
    public static long getWorkerId(final long datacenterId, final long maxWorkerId) {
        final StringBuilder mpid = new StringBuilder();
        mpid.append(datacenterId);
        try {
            mpid.append(Pid.INSTANCE.get());
        } catch (final InternalException igonre) {
            // ignore
        }
        // MAC + PID 的 hashcode 获取16个低位
        return (mpid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }

    /**
     * 获取随机NanoId
     *
     * @return the string
     */
    public static String nanoId() {
        return NanoId.randomNanoId();
    }

    /**
     * 获取随机NanoId
     *
     * @param size ID中的字符数量
     * @return the string
     */
    public static String nanoId(final int size) {
        return NanoId.randomNanoId(size);
    }

    /**
     * 简单获取Snowflake 的 nextId 终端ID 数据中心ID 默认为PID和MAC地址生成
     *
     * @return the long
     */
    public static long getSnowflakeNextId() {
        return getSnowflake().next();
    }

    /**
     * 简单获取Snowflake 的 nextId 终端ID 数据中心ID 默认为PID和MAC地址生成
     *
     * @return the string
     */
    public static String getSnowflakeNextIds() {
        return getSnowflake().nextString();
    }

}
