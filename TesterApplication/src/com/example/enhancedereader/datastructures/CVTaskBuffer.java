package com.example.enhancedereader.datastructures;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

// condition variable based task buffer
public class CVTaskBuffer<E> {
	private Queue<E> tasks;
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
	
	public int getSize() {
		return tasks.size();
	}
	
	// tries to get a task from the task list, and waits until there is a task available
	public E getTask() throws InterruptedException {
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
	
	// adds a task to the task list
	public void addTask(E e) {
		lock.lock();
		try {
			tasks.add(e);
			notEmpty.signal();
		} finally {
			lock.unlock();
		}
	}
	
}
