package com.ukettle.www.toolkit;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ListQueue<E> {
	
	private final AtomicInteger count = new AtomicInteger(0);
	private transient Node<E> head;
	private transient Node<E> last;
	private final ReentrantLock takeLock = new ReentrantLock();
	private final Condition notEmpty = this.takeLock.newCondition();
	private final ReentrantLock putLock = new ReentrantLock();

	private void signalNotEmpty() {
		ReentrantLock takeLock = this.takeLock;
		takeLock.lock();
		try {
			this.notEmpty.signal();
		} finally {
			takeLock.unlock();
		}
	}

	private void insert(E x) {
		this.last = (this.last.next = new Node<E>(x));
	}

	private E extract() {
		Node<E> first = this.head.next;
		this.head = null;
		this.head = first;
		E x = first.item;
		first.item = null;
		this.head.item = null;
		first = null;
		return x;
	}

	private void fullyLock() {
		this.putLock.lock();
		this.takeLock.lock();
	}

	private void fullyUnlock() {
		this.takeLock.unlock();
		this.putLock.unlock();
	}

	public ListQueue() {
		this.last = (this.head = new Node<E>(null));
	}

	public int size() {
		return this.count.get();
	}

	public boolean offer(E o) {
		if (o == null)
			throw new NullPointerException();

		int c = -1;
		ReentrantLock putLock = this.putLock;
		AtomicInteger count = this.count;
		putLock.lock();
		try {
			insert(o);
			c = count.getAndIncrement();
		} finally {
			putLock.unlock();
		}
		if (c == 0)
			signalNotEmpty();
		return c >= 0;
	}

	public E take() throws InterruptedException {
		int c = -1;
		AtomicInteger count = this.count;
		ReentrantLock takeLock = this.takeLock;
		takeLock.lockInterruptibly();
		E x;
		try {
			try {
				while (count.get() == 0)
					this.notEmpty.await();
			} catch (InterruptedException ie) {
				this.notEmpty.signal();
				throw ie;
			}

			x = extract();
			c = count.getAndDecrement();
			if (c > 1)
				this.notEmpty.signal();
		} finally {
			takeLock.unlock();
		}
		return x;
	}

	public void clear() {
		fullyLock();
		try {
			this.head.next = null;
			assert (this.head.item == null);
			this.last = this.head;
		} finally {
			fullyUnlock();
		}
	}

	static class Node<E> {
		volatile E item;
		Node<E> next;

		Node(E x) {
			this.item = x;
		}
	}

}