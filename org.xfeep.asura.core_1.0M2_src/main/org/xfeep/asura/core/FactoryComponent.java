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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import org.xfeep.asura.core.event.ServiceEvent;
import org.xfeep.asura.core.match.Matcher;
import org.xfeep.asura.core.match.SmartMatcher;
import org.xfeep.asura.core.util.ReadWriteProtectedArrayList;

public class FactoryComponent extends Component {

	protected ReadWriteProtectedArrayList<ComponentInstance> instances = new ReadWriteProtectedArrayList<ComponentInstance>();
	protected ComponentInstance factorySampleInstance;
	protected static Map<String, Object> sampleProperties = null;
//	static {
//		sampleProperties = new HashMap<String, Object>(1);
//		sampleProperties.put(SmartMatcher.FACTORY_SAMPLE_INSTANCE_MATCHER.toString(), true);
//	}
	public FactoryComponent() {
		super();
	}

	public FactoryComponent(ComponentDefinition definition,
			ServiceSpace serviceSpace) {
		super(definition, serviceSpace);
	}

	@Override
	public ComponentInstance getInstance(Matcher matcher) {
		if (matcher == SmartMatcher.FACTORY_SAMPLE_INSTANCE_MATCHER){
			return factorySampleInstance;
		}
		ReadLock readLock = instances.getReadLock();
		try{
			readLock.lock();
			for (ComponentInstance ci : instances){
				if (ci.getStatus() == ComponentInstanceStatus.ACTIVE && (matcher == null || matcher.match(ci.getProperties())) ){
					return ci;
				}
			}
		}finally{
			readLock.unlock();
		}
		return null;
	}
	
	@Override
	public List<ComponentInstance> getInstances(Matcher matcher) {
		if (factorySampleInstance == null){
			return Collections.EMPTY_LIST;
		}
		if (SmartMatcher.FACTORY_SAMPLE_INSTANCE_MATCHER.equals(matcher)){
			return Arrays.asList(factorySampleInstance);
		}
		ArrayList<ComponentInstance> rt = new ArrayList<ComponentInstance>();
		ReadLock readLock = instances.getReadLock();
		try{
			readLock.lock();
			for (ComponentInstance ci : instances){
				if (ci.getStatus() == ComponentInstanceStatus.ACTIVE && (matcher == null || matcher.match(ci.getProperties())) ){
					rt.add(ci);
				}
			}
		}finally{
			readLock.unlock();
		}
		return rt;
	}

	@Override
	public  boolean tryStatisfy() {
		synchronized (instanceMainLock) {
			if (factorySampleInstance != null){
				return false;
			}
			for (Reference r : references){
				if (!r.isSatisfied()){
					return false;
				}
			}
			//create an representation service
//			WriteLock writeLock = instances.getWriteLock();
//			try{
//				writeLock.lock();
				factorySampleInstance = new FactoryComponentInstance(this, null);
				factorySampleInstance.addProperty(SmartMatcher.FACTORY_SAMPLE_INSTANCE_MATCHER.toString(), true);
				factorySampleInstance.init();
				factorySampleInstance.status  = ComponentInstanceStatus.ACTIVE;
				for (Class serviceClass : definition.interfaces){
					serviceSpace.sendEvent(new ServiceEvent(factorySampleInstance, serviceClass, ServiceEvent.ACTIVE));
				}
//				instances.unsafeAdd(instance);
//			}finally{
//				writeLock.unlock();
//			}
			
			status = ComponentStatus.SATISFIED;
			return true;
		}
	}

	public ComponentInstance newInstance(Map<String, Object> config, String configId) {
		synchronized (instanceMainLock) {
			if (factorySampleInstance == null ||  !factorySampleInstance.isActive()){
				return null;
			}
			
			FactoryComponentInstance instance = new FactoryComponentInstance(this, configId);
			instance.getProperties().config(config);
//			if ( !instance.init() ) {
//				instance.destroy();
//				return null;
//			}
//			if (!instance.activate()){
//				instance.deactivate();
//				instance.destroy();
//				instance = null;
//				return null;
//			}
			instance.enable();
			return instance;
		}
	}
	
	@Override
	public  void tryUnstatisfy() {
		synchronized (instanceMainLock) {
			if (factorySampleInstance == null || factorySampleInstance.status != ComponentInstanceStatus.ACTIVE){
				return;
			}
			//destroy all instances
//			WriteLock writeLock = instances.getWriteLock();
//			try{
//				writeLock.lock();
				for (Object ci : instances.toArray()){
					((ComponentInstance)ci).tryLostChance();
//					((ComponentInstance)ci).deactivate();
//					((ComponentInstance)ci).destroy();
				}
				factorySampleInstance.status  = ComponentInstanceStatus.INACTIVE;
				for (Class serviceClass : definition.interfaces){
					serviceSpace.sendEvent(new ServiceEvent(factorySampleInstance, serviceClass, ServiceEvent.INACTIVE));
				}
				factorySampleInstance = null;
				for (Reference r : references){
					r.reset();
				}
				this.status = ComponentStatus.IDLE;
//				instances.unsafeClear();
//			}finally{
//				writeLock.unlock();
//			}
		}
	}

	@Override
	public List<ComponentInstance> getAllInstancesForDebugInfo() {
		synchronized (instanceMainLock) {
			Object[] ins = instances.toArray();
			return   ins.length > 0  ? Arrays.asList(ins) : Collections.EMPTY_LIST;
		}
	}

}
