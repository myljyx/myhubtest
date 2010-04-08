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
package org.xfeep.asura.core;

//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.locks.ReentrantReadWriteLock;
//import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
//import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.xfeep.asura.core.event.ServiceEvent;

public class LazyComponentInstance extends ComponentInstance {

//	ReentrantReadWriteLock lazyServiceReadWriteLock = new ReentrantReadWriteLock();
//	AtomicInteger serviceReferenceCount = new AtomicInteger(0);
	protected int serviceReferenceCount = 0;
	
	public LazyComponentInstance() {
		super();
	}

	public LazyComponentInstance(String configId, Component component) {
		super(configId, component);
	}
	
	@Override
	public Object getService() {
		return getService(null);
	}
	
	@Override
	public synchronized  Object getService(Reference reference) {
		//old not finished implementation which has some concurrent bug without synchronized around the whole method
//		if (serviceReferenceCount.get() == 0){
//			WriteLock writeLock = lazyServiceReadWriteLock.writeLock();
//			try{
//				writeLock.lock();
//				if (service == null){
//					
//				}
//				
//			}finally{
//				writeLock.unlock();
//			}
//		}
//		ReadLock readLock = lazyServiceReadWriteLock.readLock();
//		readLock.lock();
//		try{
//				if (service != null){
//					serviceReferenceCount.incrementAndGet();
//				}
//				return service;
//		}finally{
//				readLock.unlock();
//		}
		if (status != ComponentInstanceStatus.ACTIVE){
			return null;
		}
		if (serviceReferenceCount == 0){
			ComponentDefinition definition = component.definition;
			do{
				
				if ( !super.init() ) {
					service = null;
					break;
				}
				try {
					if ( component.bindReferences(this) && bindReferences()) {
						if (definition.activate != null){
							definition.activate.setAccessible(true);
							if (definition.activate.getParameterTypes().length > 0){
								definition.activate.invoke(service, context);
							}else{
								definition.activate.invoke(service);
							}
						}
					}else {
						service = null;
						break;
					}
				} catch (Throwable e) {
					service = null;
					log("can not activate instance of " + component.definition.implement.getName(), e);
					break;
				}
				
			}while(false);
		}
		if (service != null){
			serviceReferenceCount++;
		}else{
			component.tryUnstatisfy();
		}
		return service;
		
	}
	
	@Override
	public synchronized void ungetService(Reference reference) {
		if (status != ComponentInstanceStatus.ACTIVE){
			return;
		}
		if (service != null){
			if ( --serviceReferenceCount == 0 ){
				ComponentDefinition definition = component.definition;
				try {
					if (definition.deactivate != null){
						definition.deactivate.setAccessible(true);
						definition.deactivate.invoke(service);
					}
					component.unbindReferences(this);
					unbindReferences();
				} catch (Throwable e) {
					log("can not deactivate instance of " + component.definition.implement.getName(), e);
				} 
				try {
					if (definition.destroy != null){
						definition.destroy.setAccessible(true);
						definition.destroy.invoke(service);
					}
				} catch (Throwable e) {
					log("can not destory instance of " + component.definition.implement.getName(), e);
				} 
				service = null;
			}
			
		}
	}

	@Override
	public synchronized boolean activate() {
		if (status == ComponentInstanceStatus.ACTIVE){
			return false;
		}
		status = ComponentInstanceStatus.ACTIVE;
		for (Class serviceClass : component.definition.interfaces){
			component.serviceSpace.sendEvent(new ServiceEvent(this, serviceClass, ServiceEvent.ACTIVE));
		}
		return true;
	}

	@Override
	public synchronized boolean deactivate() {
		if (status != ComponentInstanceStatus.ACTIVE){
			return false;
		}
		try {
//			if (status == ComponentInstanceStatus.ACTIVE){
				status = ComponentInstanceStatus.ACTIVE_TO_INACTIVE;
				for (Class serviceClass : component.definition.interfaces){
					component.serviceSpace.sendEvent(new ServiceEvent(this, serviceClass, ServiceEvent.INACTIVE));
				}
//			}
			ComponentDefinition definition = component.definition;
			if (definition.deactivate != null && service != null){ // check service is not null because under lazy policy service may has not been created.
				definition.deactivate.setAccessible(true);
				definition.deactivate.invoke(service);
			}
			if (service != null){
				component.unbindReferences(this);
				unbindReferences();
			}
			this.status = ComponentInstanceStatus.INACTIVE;
			serviceReferenceCount = 0;
			return true;
		} catch (Throwable e) {
			log("can not activate instance of " + component.definition.implement.getName(), e);
		} 
		return false;
	}

	@Override
	public synchronized boolean destroy() {

		if (status == ComponentInstanceStatus.DESTROYED){
			return false;
		}
		try {
			ComponentDefinition definition = component.definition;
			if (definition.destroy != null && service != null){
				definition.destroy.setAccessible(true);
				definition.destroy.invoke(service);
			}
			this.status = ComponentInstanceStatus.DESTROYED;
			return true;
		} catch (Throwable e) {
			log("can not destory instance of " + component.definition.implement.getName(), e);
		} 
		return false;
	
	}

	@Override
	public synchronized boolean init() {
		return true;
	}

	
	
	
}
