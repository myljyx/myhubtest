/*******************************************************************************
 * Copyright (c) 2008-2009 zhang yuexiang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.xfeep.asura.core.test.pre;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.xfeep.asura.core.ServiceSpace;
import org.xfeep.asura.core.event.ServiceEventCenter;
import org.xfeep.asura.core.reflect.FieldItem;
import org.xfeep.asura.core.util.InlineExecutorService;

public class TestHelper {
	
	public ExecutorService executorService;
	public ServiceSpace appServiceSpace;
	public ServiceEventCenter serviceEventCenter;
	
	public ExecutorService getExecutorService() {
		return executorService;
	}

	public static class MyCountableBlockingQueue<E> extends LinkedBlockingQueue<E> {
		private static final long serialVersionUID = 1L;

		AtomicInteger insertedCount = new AtomicInteger(0);
		
		public  AtomicInteger getInsertedCount() {
			return insertedCount;
		}

		public  MyCountableBlockingQueue() {
		}

		@Override
		public  boolean offer(E e, long timeout, TimeUnit unit)
				throws InterruptedException {
			insertedCount.incrementAndGet();
			return super.offer(e, timeout, unit);
		}

		@Override
		public  boolean offer(E e) {
			insertedCount.incrementAndGet();
			return super.offer(e);
		}

		@Override
		public  E peek() {
			return super.peek();
		}
		
		@Override
		public  E take() throws InterruptedException {
			E e =  super.take();
//			if (e != null){
//				insertedCount.decrementAndGet();
//			}
			return e;
		}

		@Override
		public  E poll() {
			E e =  super.poll();
//			if (e != null){
//				insertedCount.decrementAndGet();
//			}
			return e;
		}

		@Override
		public  E poll(long timeout, TimeUnit unit) throws InterruptedException {
			E e =  super.poll(timeout, unit);
//			if (e != null){
//				insertedCount.decrementAndGet();
//			}
			return e;
		}

		@Override
		public  synchronized void put(E e) throws InterruptedException {
			super.put(e);
			insertedCount.incrementAndGet();
		}

		@Override
		public  boolean remove(Object o) {
			boolean rt = super.remove(o);
//			if (rt){
//				insertedCount.decrementAndGet();
//			}
			return rt;
		}
		
		
	}
//
//	public void setExecutorService(ExecutorService executorService) {
//		this.executorService = executorService;
//	}

	MyCountableBlockingQueue queue;
	public TestHelper(){
//		queue = new MyCountableBlockingQueue<Runnable>();
		ThreadPoolExecutor threadPoolExecutor = 
			new ThreadPoolExecutor(2,2,Long.MAX_VALUE, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>());
		executorService = threadPoolExecutor;
//		executorService = new InlineExecutorService();
	}
	
	
	public void destory() {
		sleepUntilNoTask();
		if (executorService != null){
			executorService.shutdownNow();
		}
	}
	
	public  void sleepUntilNoTask(){
		if (executorService == null || executorService instanceof InlineExecutorService ){
			return;
		}
		if (appServiceSpace == null){
			return;
		}
		if (serviceEventCenter == null){
			serviceEventCenter = appServiceSpace.getEventCenter();
		}
		
		if (serviceEventCenter == null){
			return;
		}
//		try {
//			FieldItem mainLockField = new FieldItem(executorService.getClass(), "mainLock");
//			ReentrantLock mainLock = (ReentrantLock) mainLockField.getValue(executorService);
//			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
//			while(true){
////				Thread.sleep(0);
//				mainLock.lock();
//				try{
//					synchronized (queue) {
//						ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor)executorService;
//						MyCountableBlockingQueue<Runnable> queue = (MyCountableBlockingQueue<Runnable>)poolExecutor.getQueue();
//						if( queue.getInsertedCount().intValue() > poolExecutor.getCompletedTaskCount() || poolExecutor.getActiveCount() > 0){
//							continue;
//						}else{
//							break;
//						}
//					}
//				}finally{
//					mainLock.unlock();
//				}
//				
//			}
//		} catch (Throwable e1) {
//			e1.printStackTrace();
//		} 
		while(true) {
			if (serviceEventCenter.getWaintingEventCount() == 0){
				return;
			}
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
}
