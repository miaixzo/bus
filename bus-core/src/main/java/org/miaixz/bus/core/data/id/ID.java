/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.data.id;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.ip.IPv4;

/**
 * ID generator utility class. This class encapsulates several ID generation strategies:
 *
 * <pre>
 * 1. Unique ID generators: UUID, ObjectId (MongoDB), Snowflake
 * </pre>
 *
 * <p>
 * For more information on IDs, see:
 * <a href="http://calvin1978.blogcn.com/articles/uuid.html">http://calvin1978.blogcn.com/articles/uuid.html</a>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ID {

    /**
     * Constructs a new ID. Utility class constructor for static access.
     */
    private ID() {
    }

    /**
     * Gets a random UUID.
     *
     * @return A random UUID.
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Gets a simplified UUID, with hyphens removed.
     *
     * @return A simplified UUID with hyphens removed.
     */
    public static String simpleUUID() {
        return UUID.randomUUID().toString(true);
    }

    /**
     * Gets a random UUID, using the more performant ThreadLocalRandom to generate the UUID.
     *
     * @return A random UUID.
     */
    public static String fastUUID() {
        return UUID.fastUUID().toString();
    }

    /**
     * Gets a simplified UUID, with hyphens removed, using the more performant ThreadLocalRandom.
     *
     * @return A simplified UUID with hyphens removed.
     */
    public static String fastSimpleUUID() {
        return UUID.fastUUID().toString(true);
    }

    /**
     * Creates a MongoDB ID generation strategy implementation. An ObjectId consists of the following parts:
     *
     * <pre>
     * 1. Time: A timestamp.
     * 2. Machine: A unique identifier for the host, usually a hash of the machine's hostname.
     * 3. PID: The process ID, ensuring no conflicts within the same machine.
     * 4. INC: An auto-incrementing counter, ensuring uniqueness of ObjectIds generated within the same second.
     * </pre>
     * <p>
     * Reference: <a href=
     * "http://blog.csdn.net/qxc1281/article/details/54021882">http://blog.csdn.net/qxc1281/article/details/54021882</a>
     *
     * @return An ObjectId.
     */
    public static String objectId() {
        return ObjectId.next();
    }

    /**
     * Gets a singleton instance of Twitter's Snowflake algorithm generator. In distributed systems, there are scenarios
     * that require globally unique IDs. Sometimes, we want to use a simpler ID, and we want the ID to be generated in
     * chronological order.
     *
     * <p>
     * The structure of a snowflake ID is as follows (each part is separated by a hyphen):
     * 
     * <pre>
     * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
     * </pre>
     * <p>
     * The first bit is unused. The next 41 bits are for the timestamp in milliseconds (41 bits can be used for 69
     * years). Then there are 5 bits for datacenterId and 5 bits for workerId (10 bits can support up to 1024 nodes).
     * The last 12 bits are a counter within the millisecond (a 12-bit counter supports 4096 IDs per node per
     * millisecond).
     *
     * <p>
     * Reference:
     * <a href="http://www.cnblogs.com/relucent/p/4955340.html">http://www.cnblogs.com/relucent/p/4955340.html</a>
     *
     * @param workerId     The worker ID.
     * @param datacenterId The data center ID.
     * @return A {@link Snowflake} instance.
     */
    public static Snowflake getSnowflake(final long workerId, final long datacenterId) {
        return Instances.get(Snowflake.class, workerId, datacenterId);
    }

    /**
     * Gets a singleton instance of Twitter's Snowflake algorithm generator. In distributed systems, there are scenarios
     * that require globally unique IDs. Sometimes, we want to use a simpler ID, and we want the ID to be generated in
     * chronological order.
     *
     * <p>
     * The structure of a snowflake ID is as follows (each part is separated by a hyphen):
     * 
     * <pre>
     * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
     * </pre>
     * <p>
     * The first bit is unused. The next 41 bits are for the timestamp in milliseconds (41 bits can be used for 69
     * years). Then there are 5 bits for datacenterId and 5 bits for workerId (10 bits can support up to 1024 nodes).
     * The last 12 bits are a counter within the millisecond (a 12-bit counter supports 4096 IDs per node per
     * millisecond).
     * <p>
     * Reference:
     * <a href="http://www.cnblogs.com/relucent/p/4955340.html">http://www.cnblogs.com/relucent/p/4955340.html</a>
     *
     * @param workerId The worker ID.
     * @return A {@link Snowflake} instance.
     */
    public static Snowflake getSnowflake(final long workerId) {
        return Instances.get(Snowflake.class, workerId);
    }

    /**
     * Gets a singleton instance of Twitter's Snowflake algorithm generator. In distributed systems, there are scenarios
     * that require globally unique IDs. Sometimes, we want to use a simpler ID, and we want the ID to be generated in
     * chronological order.
     *
     * <p>
     * The structure of a snowflake ID is as follows (each part is separated by a hyphen):
     * 
     * <pre>
     * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
     * </pre>
     * <p>
     * The first bit is unused. The next 41 bits are for the timestamp in milliseconds (41 bits can be used for 69
     * years). Then there are 5 bits for datacenterId and 5 bits for workerId (10 bits can support up to 1024 nodes).
     * The last 12 bits are a counter within the millisecond (a 12-bit counter supports 4096 IDs per node per
     * millisecond).
     * <p>
     * Reference:
     * <a href="http://www.cnblogs.com/relucent/p/4955340.html">http://www.cnblogs.com/relucent/p/4955340.html</a>
     *
     * @return A {@link Snowflake} instance.
     */
    public static Snowflake getSnowflake() {
        return Instances.get(Snowflake.class);
    }

    /**
     * Gets the data center ID. The data center ID depends on the local network card's MAC address.
     * <p>
     * This algorithm comes from mybatis-plus#Sequence.
     *
     * @param maxDatacenterId The maximum data center ID.
     * @return The data center ID.
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
     * Gets the worker ID, generated using the process ID in conjunction with the data center ID. The worker ID depends
     * on the hash value of the current process ID or process name.
     *
     * <p>
     * This algorithm comes from mybatis-plus#Sequence.
     *
     * @param datacenterId The data center ID.
     * @param maxWorkerId  The maximum worker node ID.
     * @return The worker ID.
     */
    public static long getWorkerId(final long datacenterId, final long maxWorkerId) {
        final StringBuilder mpid = new StringBuilder();
        mpid.append(datacenterId);
        try {
            mpid.append(Pid.INSTANCE.get());
        } catch (final InternalException igonre) {
            // ignore
        }
        // Get the lower 16 bits of the hashcode of (MAC + PID)
        return (mpid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }

    /**
     * Gets a random NanoId.
     *
     * @return The random NanoId string.
     */
    public static String nanoId() {
        return NanoId.randomNanoId();
    }

    /**
     * Gets a random NanoId.
     *
     * @param size The number of characters in the ID.
     * @return The random NanoId string.
     */
    public static String nanoId(final int size) {
        return NanoId.randomNanoId(size);
    }

    /**
     * Simply gets the next ID from Snowflake. The worker ID and data center ID are generated by default from the PID
     * and MAC address.
     *
     * @return The next ID.
     */
    public static long getSnowflakeNextId() {
        return getSnowflake().next();
    }

    /**
     * Simply gets the next ID as a string from Snowflake. The worker ID and data center ID are generated by default
     * from the PID and MAC address.
     *
     * @return The next ID as a string.
     */
    public static String getSnowflakeNextIds() {
        return getSnowflake().nextString();
    }

}
