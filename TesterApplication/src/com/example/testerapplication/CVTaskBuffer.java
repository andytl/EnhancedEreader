package com.example.testerapplication;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class CVTaskBuffer {
	private Queue<MatPoint> tasks;
	private Lock lock;
	//private Condition notFull;
	private Condition notEmpty;
	
	public CVTaskBuffer() {
		lock = new ReentrantLock();
		//notFull = lock.newCondition();
		notEmpty = lock.newCondition();
		tasks = new LinkedList<>();
	}
	
	public boolean isEmpty() {
		return tasks.isEmpty();
	}
	
	public MatPoint getTask() throws InterruptedException {
		lock.lock();
		try {
			while (tasks.isEmpty()) {
				notEmpty.await();
			}
			return tasks.remove();
		} finally {
			lock.unlock();
		}
	}
	public void addTask(MatPoint e) {
		lock.lock();
		try {
			tasks.add(e);
			notEmpty.signal();
		} finally {
			lock.unlock();
		}
	}
	
}