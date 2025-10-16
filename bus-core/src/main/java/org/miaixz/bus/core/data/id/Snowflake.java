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

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import org.miaixz.bus.core.center.date.NonClock;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.xyz.RandomKit;

/**
 * Twitter's Snowflake algorithm. In distributed systems, there are scenarios that require globally unique IDs.
 * Sometimes we want to use a simpler ID, and we want the ID to be generated in chronological order. The structure of a
 * snowflake ID is as follows (each part is separated by a hyphen):
 *
 * <pre>
 * Sign bit (1bit) - Relative timestamp (41bit) - Data center ID (5bit) - Machine ID (5bit) - Sequence number (12bit)
 * (0) - (0000000000 0000000000 0000000000 0000000000 0) - (00000) - (00000) - (000000000000)
 * </pre>
 *
 * The first bit is unused (the sign bit indicates a positive number). The next 41 bits are the timestamp in
 * milliseconds (41 bits can be used for 69 years). Then there are 5 bits for the datacenterId and 5 bits for the
 * workerId (10 bits can support up to 1024 nodes). The last 12 bits are a counter within the millisecond (a 12-bit
 * counter supports 4096 IDs per node per millisecond). The generation time, datacenterId, and workerId can be
 * reverse-engineered from the generated ID. Reference: http://www.cnblogs.com/relucent/p/4955340.html For the issue of
 * whether the length is 18 or 19, see: https://blog.csdn.net/unifirst/article/details/80408050
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Snowflake implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852275920328L;

    /**
     * Default start time: Thu, 04 Nov 2010 01:42:54 GMT
     */
    public static final long DEFAULT_TWEPOCH = 1288834974657L;
    private static final long WORKER_ID_BITS = 5L;
    /**
     * Maximum supported machine nodes: 0~31, for a total of 32.
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long DATA_CENTER_ID_BITS = 5L;
    /**
     * Maximum supported data center nodes: 0~31, for a total of 32.
     */
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    /**
     * Sequence number is 12 bits (meaning the sequence can range from 0 to 4095).
     */
    private static final long SEQUENCE_BITS = 12L;
    /**
     * Machine node left shift 12 bits.
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    /**
     * Data center node left shift 17 bits.
     */
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    /**
     * Timestamp in milliseconds left shift 22 bits.
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;
    /**
     * Sequence mask, used to limit the maximum sequence value to not exceed 4095.
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);// 4095

    /**
     * The start timestamp.
     */
    private final long twepoch;
    /**
     * The worker ID.
     */
    private final long workerId;
    /**
     * The data center ID.
     */
    private final long dataCenterId;
    /**
     * Whether to use the system clock.
     */
    private final boolean useSystemClock;
    /**
     * When in low-frequency mode, the sequence number is always 0, causing the generated ID to always be even. This
     * property is used to limit a random upper bound. When generating a sequence number in a different millisecond, a
     * random number is given to avoid the even number problem. Note that this number must be less than
     * {@link #SEQUENCE_MASK}. {@code 0} means no random number is used. This upper bound does not include the value
     * itself.
     */
    private final long randomSequenceLimit;

    /**
     * The sequence number. In high-frequency mode, when N IDs are generated in the same millisecond, this sequence
     * number is incremented to avoid ID duplication.
     */
    private long sequence = 0L;
    /**
     * The last timestamp.
     */
    private long lastTimestamp = -1L;

    /**
     * Constructor, uses automatically generated worker ID and data center ID.
     */
    public Snowflake() {
        this(ID.getWorkerId(ID.getDataCenterId(MAX_DATA_CENTER_ID), MAX_WORKER_ID));
    }

    /**
     * Constructor.
     *
     * @param workerId The worker ID.
     */
    public Snowflake(final long workerId) {
        this(workerId, ID.getDataCenterId(MAX_DATA_CENTER_ID));
    }

    /**
     * Constructor.
     *
     * @param workerId     The worker ID.
     * @param dataCenterId The data center ID.
     */
    public Snowflake(final long workerId, final long dataCenterId) {
        this(workerId, dataCenterId, false);
    }

    /**
     * Constructor.
     *
     * @param workerId         The worker ID.
     * @param dataCenterId     The data center ID.
     * @param isUseSystemClock Whether to use {@link NonClock} to get the current timestamp.
     */
    public Snowflake(final long workerId, final long dataCenterId, final boolean isUseSystemClock) {
        this(null, workerId, dataCenterId, isUseSystemClock);
    }

    /**
     * Constructor.
     *
     * @param epochDate        The start date of the epoch (null means the default start date). Modifying this later
     *                         will cause ID duplication. Use with caution if you also need to modify workerId and
     *                         dataCenterId.
     * @param workerId         The worker machine node ID.
     * @param dataCenterId     The data center ID.
     * @param isUseSystemClock Whether to use {@link NonClock} to get the current timestamp.
     */
    public Snowflake(final Date epochDate, final long workerId, final long dataCenterId,
            final boolean isUseSystemClock) {
        this(epochDate, workerId, dataCenterId, isUseSystemClock, 0);
    }

    /**
     * Constructor.
     *
     * @param epochDate           The start date of the epoch (null means the default start date). Modifying this later
     *                            will cause ID duplication. Use with caution if you also need to modify workerId and
     *                            dataCenterId.
     * @param workerId            The worker machine node ID.
     * @param dataCenterId        The data center ID.
     * @param isUseSystemClock    Whether to use {@link NonClock} to get the current timestamp.
     * @param randomSequenceLimit A random upper limit. When generating a sequence number in a different millisecond, a
     *                            random number is given to avoid the even number problem. 0 means no random number. The
     *                            upper limit does not include the value itself.
     */
    public Snowflake(final Date epochDate, final long workerId, final long dataCenterId, final boolean isUseSystemClock,
            final long randomSequenceLimit) {
        this.twepoch = (null != epochDate) ? epochDate.getTime() : DEFAULT_TWEPOCH;
        this.workerId = Assert.checkBetween(workerId, 0, MAX_WORKER_ID);
        this.dataCenterId = Assert.checkBetween(dataCenterId, 0, MAX_DATA_CENTER_ID);
        this.useSystemClock = isUseSystemClock;
        this.randomSequenceLimit = Assert.checkBetween(randomSequenceLimit, 0, SEQUENCE_MASK);
    }

    /**
     * Gets the machine ID from a Snowflake ID.
     *
     * @param id The Snowflake ID.
     * @return The machine ID.
     */
    public long getWorkerId(final long id) {
        return id >> WORKER_ID_SHIFT & ~(-1L << WORKER_ID_BITS);
    }

    /**
     * Gets the data center ID from a Snowflake ID.
     *
     * @param id The Snowflake ID.
     * @return The data center ID.
     */
    public long getDataCenterId(final long id) {
        return id >> DATA_CENTER_ID_SHIFT & ~(-1L << DATA_CENTER_ID_BITS);
    }

    /**
     * Gets the generation time from a Snowflake ID.
     *
     * @param id The Snowflake ID.
     * @return The generation time.
     */
    public long getGenerateDateTime(final long id) {
        return (id >> TIMESTAMP_LEFT_SHIFT & ~(-1L << 41L)) + twepoch;
    }

    /**
     * Generates the next ID.
     *
     * @return The next ID.
     */
    public synchronized Long next() {
        long timestamp = genTime();
        if (timestamp < this.lastTimestamp) {
            // If the clock moves backwards, use the last timestamp.
            timestamp = lastTimestamp;
        }

        if (timestamp == this.lastTimestamp) {
            // If the timestamp is the same, increment the sequence.
            final long sequence = (this.sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                // If the sequence overflows, instead of waiting for the time to catch up,
                // we "borrow" from the future by incrementing the timestamp.
                // This avoids system pauses and allows the ID to catch up when the generation rate slows down.
                timestamp += 1;
            }
            this.sequence = sequence;
        } else {
            // Use a random number to avoid the problem of the sequence number always being 0 in low-frequency
            // generation.
            sequence = randomSequenceLimit > 1 ? RandomKit.randomLong(randomSequenceLimit) : 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << TIMESTAMP_LEFT_SHIFT) | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT) | sequence;
    }

    /**
     * Generates the next ID as a string.
     *
     * @return The next ID as a string.
     */
    public String nextString() {
        return Long.toString(next());
    }

    /**
     * Calculates the start and end IDs for a given timestamp range.
     *
     * @param timestampStart The start timestamp.
     * @param timestampEnd   The end timestamp.
     * @return A pair containing the start ID and end ID.
     */
    public Pair<Long, Long> getIdScopeByTimestamp(final long timestampStart, final long timestampEnd) {
        return getIdScopeByTimestamp(timestampStart, timestampEnd, true);
    }

    /**
     * Calculates the start and end IDs for a given timestamp range.
     *
     * @param timestampStart        The start timestamp.
     * @param timestampEnd          The end timestamp.
     * @param ignoreCenterAndWorker Whether to ignore the data center and worker node placeholders. If ignored, a
     *                              globally reliable start and end point can be obtained in a distributed environment.
     * @return A pair containing the start ID and end ID.
     */
    public Pair<Long, Long> getIdScopeByTimestamp(
            final long timestampStart,
            final long timestampEnd,
            final boolean ignoreCenterAndWorker) {
        final long startTimeMinId = (timestampStart - twepoch) << TIMESTAMP_LEFT_SHIFT;
        final long endTimeMinId = (timestampEnd - twepoch) << TIMESTAMP_LEFT_SHIFT;
        if (ignoreCenterAndWorker) {
            final long endId = endTimeMinId | ~(-1 << TIMESTAMP_LEFT_SHIFT);
            return Pair.of(startTimeMinId, endId);
        } else {
            final long startId = startTimeMinId | (dataCenterId << DATA_CENTER_ID_SHIFT)
                    | (workerId << WORKER_ID_SHIFT);
            final long endId = endTimeMinId | (dataCenterId << DATA_CENTER_ID_SHIFT) | (workerId << WORKER_ID_SHIFT)
                    | SEQUENCE_MASK;
            return Pair.of(startId, endId);
        }
    }

    /**
     * Generates a timestamp.
     *
     * @return The timestamp.
     */
    private long genTime() {
        return this.useSystemClock ? NonClock.now() : System.currentTimeMillis();
    }

}
