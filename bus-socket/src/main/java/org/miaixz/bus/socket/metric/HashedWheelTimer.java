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
package org.miaixz.bus.socket.metric;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A Hashed Wheel Timer for scheduling tasks.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HashedWheelTimer implements SocketTimer, Runnable {

    public static final HashedWheelTimer DEFAULT_TIMER = new HashedWheelTimer(r -> {
        Thread thread = new Thread(r, "defaultHashedWheelTimer");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * The duration between ticks.
     */
    private final long tickDuration;
    /**
     * The buckets of the hashed wheel.
     */
    private final HashedWheelBucket[] wheel;
    private final int mask;
    /**
     * Queue for newly scheduled tasks.
     */
    private final Queue<HashedWheelSocketTask> newTimeouts = new ConcurrentLinkedQueue<>();
    /**
     * Queue for cancelled tasks.
     */
    private final Queue<HashedWheelSocketTask> cancelledTimeouts = new ConcurrentLinkedQueue<>();
    /**
     * Number of pending timeouts.
     */
    private final AtomicLong pendingTimeouts = new AtomicLong(0);
    /**
     * The start time of the timer.
     */
    private volatile long startTime;
    private boolean running = true;
    private long tick;

    public HashedWheelTimer(ThreadFactory threadFactory) {
        this(threadFactory, 100, 512);
    }

    /**
     * Constructor for HashedWheelTimer.
     *
     * @param threadFactory a {@link ThreadFactory} that creates a background {@link Thread} which drives this timer.
     * @param tickDuration  the duration between ticks in milliseconds.
     * @param ticksPerWheel the size of the wheel, rounded up to the next power of 2.
     */
    public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, int ticksPerWheel) {
        // Create a wheel of size 2^n
        wheel = createWheel(ticksPerWheel);
        mask = wheel.length - 1;
        this.tickDuration = tickDuration;
        Thread workerThread = threadFactory.newThread(this);
        workerThread.start();
    }

    private static HashedWheelBucket[] createWheel(int ticksPerWheel) {
        ticksPerWheel = normalizeTicksPerWheel(ticksPerWheel);
        HashedWheelBucket[] wheel = new HashedWheelBucket[ticksPerWheel];
        for (int i = 0; i < wheel.length; i++) {
            wheel[i] = new HashedWheelBucket();
        }
        return wheel;
    }

    private static int normalizeTicksPerWheel(int ticksPerWheel) {
        int n = ticksPerWheel - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= 1073741824) ? 1073741824 : n + 1;
    }

    @Override
    public void shutdown() {
        running = false;
    }

    public SocketTask scheduleWithFixedDelay(Runnable runnable, long delay, TimeUnit unit) {
        long deadline = System.currentTimeMillis() + unit.toMillis(delay);
        if (deadline <= 0) {
            throw new IllegalArgumentException();
        }
        HashedWheelSocketTask timeout = new HashedWheelSocketTask(this, runnable, deadline);
        timeout.runnable = () -> {
            try {
                runnable.run();
            } finally {
                if (!timeout.isCancelled()) {
                    timeout.deadline = System.currentTimeMillis() + unit.toMillis(delay);
                    timeout.bucket = null;
                    timeout.next = null;
                    timeout.prev = null;
                    timeout.state = HashedWheelSocketTask.ST_INIT;
                    pendingTimeouts.incrementAndGet();
                    newTimeouts.add(timeout);
                }
            }
        };
        pendingTimeouts.incrementAndGet();
        newTimeouts.add(timeout);
        return timeout;
    }

    @Override
    public SocketTask schedule(Runnable runnable, long delay, TimeUnit unit) {
        long deadline = System.currentTimeMillis() + unit.toMillis(delay);
        if (deadline <= 0) {
            throw new IllegalArgumentException();
        }
        HashedWheelSocketTask timeout = new HashedWheelSocketTask(this, runnable, deadline);
        pendingTimeouts.incrementAndGet();
        newTimeouts.add(timeout);
        return timeout;
    }

    public long pendingTimeouts() {
        return pendingTimeouts.get();
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        while (running) {
            final long deadline = waitForNextTick();
            // Process cancelled tasks
            processCancelledTasks();
            // Transfer new timeouts to their respective buckets
            transferTimeoutsToBuckets();
            wheel[(int) (tick & mask)].execute(deadline, tickDuration);
            tick++;
        }
    }

    private void transferTimeoutsToBuckets() {
        // Limit the number of registrations per tick to prevent delaying other tasks
        for (int i = 0; i < 100000; i++) {
            HashedWheelSocketTask timeout = newTimeouts.poll();
            if (timeout == null) {
                break;
            }
            if (timeout.state() == HashedWheelSocketTask.ST_CANCELLED) {
                continue;
            }

            long calculated = (timeout.deadline - startTime) / tickDuration;
            final long ticks = Math.max(calculated, tick);
            int stopIndex = (int) (ticks & mask);

            HashedWheelBucket bucket = wheel[stopIndex];
            bucket.addTimeout(timeout);
        }
    }

    /**
     * Process tasks that have been cancelled.
     */
    private void processCancelledTasks() {
        HashedWheelSocketTask timeout;
        while ((timeout = cancelledTimeouts.poll()) != null) {
            timeout.remove();
        }
    }

    private long waitForNextTick() {
        long deadline = startTime + tickDuration * (tick + 1);
        while (true) {
            // Align with the next tick
            long currentTime = System.currentTimeMillis();
            if (deadline <= currentTime) {
                return currentTime;
            }
            try {
                Thread.sleep(deadline - currentTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final class HashedWheelSocketTask implements SocketTask {

        private static final int ST_INIT = 0;
        private static final int ST_CANCELLED = 1;
        private static final int ST_EXPIRED = 2;
        private static final AtomicIntegerFieldUpdater<HashedWheelSocketTask> STATE_UPDATER = AtomicIntegerFieldUpdater
                .newUpdater(HashedWheelSocketTask.class, "state");

        private final HashedWheelTimer timer;
        private Runnable runnable;
        private long deadline;
        private volatile int state = ST_INIT;
        private HashedWheelSocketTask next;
        private HashedWheelSocketTask prev;
        private HashedWheelBucket bucket;

        HashedWheelSocketTask(HashedWheelTimer timer, Runnable runnable, long deadline) {
            this.timer = timer;
            this.runnable = runnable;
            this.deadline = deadline;
        }

        @Override
        public void cancel() {
            state = ST_CANCELLED;
            timer.cancelledTimeouts.add(this);
        }

        void remove() {
            HashedWheelBucket bucket = this.bucket;
            if (bucket != null) {
                bucket.remove(this);
            } else {
                timer.pendingTimeouts.decrementAndGet();
            }
        }

        public boolean compareAndSetState(int expected, int state) {
            return STATE_UPDATER.compareAndSet(this, expected, state);
        }

        public int state() {
            return state;
        }

        @Override
        public boolean isCancelled() {
            return state() == ST_CANCELLED;
        }

        @Override
        public boolean isDone() {
            return state() == ST_EXPIRED;
        }

        public void execute() {
            if (!compareAndSetState(ST_INIT, ST_EXPIRED)) {
                return;
            }
            try {
                runnable.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        @Override
        public String toString() {
            final long currentTime = System.nanoTime();
            long remaining = deadline - currentTime + timer.startTime;

            StringBuilder buf = new StringBuilder(192).append(getClass().getSimpleName()).append('(')
                    .append("deadline: ");
            if (remaining > 0) {
                buf.append(remaining).append(" ns later");
            } else if (remaining < 0) {
                buf.append(-remaining).append(" ns ago");
            } else {
                buf.append("now");
            }

            if (isCancelled()) {
                buf.append(", cancelled");
            }

            return buf.append(", task: ").append(runnable).append(')').toString();
        }
    }

    /**
     * A bucket in the hashed wheel timer, which holds a linked list of tasks.
     */
    private static final class HashedWheelBucket {

        private HashedWheelSocketTask head;
        private HashedWheelSocketTask tail;

        public void addTimeout(HashedWheelSocketTask timeout) {
            assert timeout.bucket == null;
            timeout.bucket = this;
            if (head == null) {
                head = tail = timeout;
            } else {
                tail.next = timeout;
                timeout.prev = tail;
                tail = timeout;
            }
        }

        public void execute(long deadline, long tickDuration) {
            HashedWheelSocketTask timeout = head;
            while (timeout != null) {
                HashedWheelSocketTask next = timeout.next;
                if (timeout.deadline <= deadline || timeout.deadline < System.currentTimeMillis() + tickDuration) {
                    next = remove(timeout);
                    timeout.execute();
                } else if (timeout.isCancelled()) {
                    next = remove(timeout);
                }
                timeout = next;
            }
        }

        public HashedWheelSocketTask remove(HashedWheelSocketTask timeout) {
            HashedWheelSocketTask next = timeout.next;
            if (timeout.prev != null) {
                timeout.prev.next = next;
            }
            if (timeout.next != null) {
                timeout.next.prev = timeout.prev;
            }

            if (timeout == head) {
                if (timeout == tail) {
                    tail = null;
                    head = null;
                } else {
                    head = next;
                }
            } else if (timeout == tail) {
                tail = timeout.prev;
            }
            timeout.prev = null;
            timeout.next = null;
            timeout.bucket = null;
            timeout.timer.pendingTimeouts.decrementAndGet();
            return next;
        }
    }

}
