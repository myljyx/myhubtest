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
package org.xfeep.asura.core.event;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;


import org.xfeep.asura.core.LogManager;
import org.xfeep.asura.core.index.BackedResultGeneralFieldIndex;
import org.xfeep.asura.core.index.ObjectAccessor;



public class ServiceEventCenter {
	
	BackedResultGeneralFieldIndex<ServiceEventListener> index;
	ExecutorService executorService;
	volatile boolean runInCurrentThreadMode = false;
	AtomicInteger waintingEventCount = new AtomicInteger(0);
	volatile CountDownLatch shutdownCountDownLatch  = null;
	
	public void prepareShutdown() {
		while(waintingEventCount.get() != 0) {
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				Thread.interrupted();
				return;
			}
		}
		shutdownCountDownLatch = new CountDownLatch(1);
	}
	
	public void shutdown(boolean haveEventAfterPrepare) {
		if (haveEventAfterPrepare && waintingEventCount.get() != 0){
			try {
				shutdownCountDownLatch.await();
			} catch (InterruptedException e) {
				Thread.interrupted();
				return;
			}
		}else{
			while(waintingEventCount.get() != 0) {
				try {
					Thread.sleep(0);
				} catch (InterruptedException e) {
					Thread.interrupted();
					return;
				}
			}
		}
		
	}
	
	public int getWaintingEventCount() {
		return waintingEventCount.get();
	}


	public boolean isRunInCurrentThreadMode() {
		return runInCurrentThreadMode;
	}




	public void setRunInCurrentThreadMode(boolean runInCurrentThreadMode) {
		this.runInCurrentThreadMode = runInCurrentThreadMode;
	}




	public ServiceEventCenter(ExecutorService executorService) {
		this.executorService = executorService;
		index = new BackedResultGeneralFieldIndex<ServiceEventListener>("serviceClass", new ObjectAccessor<ServiceEventListener>(){
			public ServiceEventListener[] createArray(int size) {
				return new ServiceEventListener[size];
			}
			public Object resolveField(String field, ServiceEventListener obj) {
				return obj.getServiceClass();
			}
		});
	}
	
	
	
	
	public void sendEvent(final ServiceEvent event){
		waintingEventCount.incrementAndGet();
		Runnable task = new Runnable() {
			public void run() {
//				ReadWriteProtectedList<ServiceEventListener> list = index.matchBacked(event.getServiceClass());
//				if (list == null){
//					return;
//				}
//				ReadLock readLock = list.getReadLock();
//				readLock.lock();
//				try{
//					for (ServiceEventListener listener :  list ){
//						listener.onServiceChanged(event);
//					}
//				}finally{
//					readLock.unlock();
//				}
				try{
					List<ServiceEventListener> list = index.match(event.getServiceClass());
					if (list == null){
						return;
					}
					for (ServiceEventListener listener :  list ){
						//we comment these follow code to let listener have chance to use more flexible matching
//						Matcher matcher = listener.getDetailMatcher();
//						if (matcher == null || matcher.match(event.getSource())){
							listener.onServiceChanged(event);
//						}
					}
				}catch (Throwable e) {
					LogManager.getInstance().error("error when handle event: {service=" + event.getServiceClass()+", source=" + event.getSource() + "}", e);
				}finally{
					if ( waintingEventCount.decrementAndGet() == 0 && shutdownCountDownLatch != null){
						shutdownCountDownLatch.countDown();
					}
				}
				
				
			}
		};
		if (runInCurrentThreadMode || executorService == null 
//				|| event.getSource().getProperties() != null
				){
			task.run();
		}else {
			executorService.execute(task);
		}
	}
	
	public ServiceEventListener addListener(ServiceEventListener listener){
		return index.insert(listener);
	}
	
	public ServiceEventListener removeListener(ServiceEventListener listener){
		return index.removeSingle(listener);
	}
	
	
}
