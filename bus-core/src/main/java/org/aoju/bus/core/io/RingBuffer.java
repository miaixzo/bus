/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.io;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Kimi Liu
 * @version 5.6.3
 * @since JDK 1.8+
 */
public final class RingBuffer<T> {

    private static final byte READABLE = 1, READING = 1 << 1, WRITEABLE = 1 << 2, WRITING = 1 << 3;
    /**
     * 排队的项目
     */
    private final Node<T>[] items;
    /**
     * 主锁保护所有入口
     */
    private final ReentrantLock lock;
    /**
     * 等待条件
     */
    private final Condition notEmpty;
    /**
     * 等待权条件
     */
    private final Condition notFull;
    /**
     * 项目索引为下一次采取，投票删除
     */
    private int takeIndex;
    /**
     * 项目索引的下一个出售，出价，或增加
     */
    private int putIndex;
    private volatile boolean needFullSingle = false;

    private volatile boolean needEmptySingle = false;

    private EventFactory<T> eventFactory;

    public RingBuffer(int capacity, EventFactory<T> factory) {
        if (capacity <= 0)
            throw new IllegalArgumentException();
        this.items = new Node[capacity];
        lock = new ReentrantLock(false);
        notEmpty = lock.newCondition();
        notFull = lock.newCondition();
        this.eventFactory = factory;
    }

    public int nextWriteIndex() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            notFullSignal();
            final Node<T>[] items = this.items;
            Node<T> node = items[putIndex];
            if (node == null) {
                node = new Node<>(eventFactory.newInstance());
                node.status = WRITEABLE;
                items[putIndex] = node;
            }
            while (node.status != WRITEABLE) {
                notFull.await();
                notFullSignal();
                node = items[putIndex];
            }

            node.status = WRITING;
            int index = putIndex;
            if (++putIndex == items.length)
                putIndex = 0;
            return index;
        } finally {
            lock.unlock();
        }
    }

    public int tryNextWriteIndex() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            notFullSignal();
            final Node<T>[] items = this.items;
            Node<T> node = items[putIndex];
            if (node == null) {
                node = new Node<>(eventFactory.newInstance());
                node.status = WRITEABLE;
                items[putIndex] = node;
            }
            if (node.status != WRITEABLE) {
                return -1;
            }

            node.status = WRITING;
            int index = putIndex;
            if (++putIndex == items.length)
                putIndex = 0;
            return index;
        } finally {
            lock.unlock();
        }
    }

    public void publishWriteIndex(int sequence) {
        Node<T> node = items[sequence];
        if (node.status != WRITING) {
            throw new RuntimeException("invalid status");
        }
        node.status = READABLE;
        final ReentrantLock lock = this.lock;
        needEmptySingle = true;
        if (lock.tryLock()) {
            try {
                notFullSignal();
            } finally {
                lock.unlock();
            }
        }
    }

    public T get(int sequence) {
        return items[sequence].entity;
    }

    public int tryNextReadIndex() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            notFullSignal();
            final Node[] items = this.items;
            Node x = items[takeIndex];
            if (x == null || x.status != READABLE) {
                return -1;
            }
            x.status = READING;
            int index = takeIndex;
            if (++takeIndex == items.length)
                takeIndex = 0;
            return index;
        } finally {
            lock.unlock();
        }
    }

    public int nextReadIndex() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            notFullSignal();
            final Node[] items = this.items;
            Node x = items[takeIndex];
            while (x == null || x.status != READABLE) {
                notEmpty.await();
                notFullSignal();
                x = items[takeIndex];
            }
            x.status = READING;
            int index = takeIndex;
            if (++takeIndex == items.length)
                takeIndex = 0;
            return index;
        } finally {
            lock.unlock();
        }
    }

    private void notFullSignal() {
        if (needFullSingle) {
            notFull.signal();
            needFullSingle = false;
        }
        if (needEmptySingle) {
            notEmpty.signal();
            needEmptySingle = false;
        }
    }

    public void publishReadIndex(int sequence) {
        Node<T> node = items[sequence];
        if (node.status != READING) {
            throw new RuntimeException("invalid status");
        }
        eventFactory.restEntity(node.entity);
        node.status = WRITEABLE;
        final ReentrantLock lock = this.lock;
        needFullSingle = true;
        if (lock.tryLock()) {
            try {
                notFullSignal();
            } finally {
                lock.unlock();
            }
        }
    }

    class Node<T1> {

        byte status;
        T1 entity;

        Node(T1 entity) {
            this.entity = entity;
        }
    }

}
