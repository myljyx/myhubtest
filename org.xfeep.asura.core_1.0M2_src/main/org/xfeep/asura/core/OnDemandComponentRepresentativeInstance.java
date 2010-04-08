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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.xfeep.asura.core.config.ConfigAdminService;
import org.xfeep.asura.core.event.ServiceEvent;
import org.xfeep.asura.core.match.Matcher;
import org.xfeep.asura.core.match.SmartMatcher;

public class OnDemandComponentRepresentativeInstance extends ComponentInstance {

	public final static String FLAG = "OnDemandComponentRepresentativeInstanceFlag";
	
	protected List<OnDemandComponentInstance> instances = new ArrayList<OnDemandComponentInstance>();
	
	
	public OnDemandComponentRepresentativeInstance() {
	}

	public OnDemandComponentRepresentativeInstance(String configId, Component component) {
		super(configId, component);
	}
	
	@Override
	public Object getService() {
		return getService(null);
	}
	
	public  OnDemandComponentInstance getInstance(Matcher matcher){
		synchronized (instanceLock) {
			Map<String, Object> ondemandRequirement = null;
			if (matcher != null){
				ondemandRequirement = matcher.getOndemandConfigRequirement();
			}
			if (ondemandRequirement != null){
				
				//TODO: use some index for fast get
				for (OnDemandComponentInstance instance : instances){
					if (instance.imply(ondemandRequirement)){
						return instance;
					}
				}
				
				String configId = null;
				Map<String, Object> instanceConfig = null ;
				if (component.definition.hasDynamicConfigReference()){
					configId = (String)ondemandRequirement.get(Matcher.ON_DEMAND_CONFIG_ID);
					ConfigAdminService configAdminService = component.getServiceSpace().findService(ConfigAdminService.class);
					if (configId == null){
						configAdminService.put(configId = UUID.randomUUID().toString(), ondemandRequirement);
					}
//					instanceConfig = configAdminService.get(configId);
				}else {
					instanceConfig = new HashMap<String, Object>() ;
					instanceConfig.putAll(ondemandRequirement);
				}
				
				OnDemandComponentInstance newInstance = new OnDemandComponentInstance(component, configId);
				newInstance.representativeInstance = this;
				if (!component.definition.hasDynamicConfigReference()){
					newInstance.getProperties().config(instanceConfig);
				}
				newInstance.enable();
//				instances.add(e); move to OnDemandComponentInstance.activate
				return newInstance;
			}else{
				for (OnDemandComponentInstance instance : instances){
					if (matcher == null || matcher.match(instance.getProperties())){
						return instance;
					}
				}
				return null;
			}

		}
	}
	
	@Override
	public   Object getService(Reference reference) {
		synchronized (instanceLock) {
			if (status != ComponentInstanceStatus.ACTIVE){
				return null;
			}
			Matcher matcher = null;
			if (reference != null){
				matcher = reference.getDetailMatcher();
			}
			if (matcher == null){
				matcher = SmartMatcher.MATCH_ALL_MATCHER;
			}
			OnDemandComponentInstance ci = getInstance(matcher);
			return ci == null ? null : ci.getService(reference);
		}
	}
	//synchronized may cause deadlock when dispose()
//	@Override
//	public synchronized void ungetService(Reference reference) {
//		if (status != ComponentInstanceStatus.ACTIVE){
//			return;
//		}
//	}
	
	@Override
	public boolean enable() {
		return activate();
	}

	@Override
	public void disable() {
		deactivate();
	}

	
	@Override
	public  boolean activate() {
		synchronized (instanceLock) {
			if (status == ComponentInstanceStatus.ACTIVE){
				return false;
			}
			status = ComponentInstanceStatus.ACTIVE;
			for (Class serviceClass : component.definition.interfaces){
				component.serviceSpace.sendEvent(new ServiceEvent(this, serviceClass, ServiceEvent.ACTIVE));
			}
			return true;
		}
	}

	@Override
	public  boolean deactivate() {
		synchronized (instanceLock) {
			if (status != ComponentInstanceStatus.ACTIVE){
				return false;
			}
			try {
//				if (status == ComponentInstanceStatus.ACTIVE){
					status = ComponentInstanceStatus.ACTIVE_TO_INACTIVE;
					for (Class serviceClass : component.definition.interfaces){
						component.serviceSpace.sendEvent(new ServiceEvent(this, serviceClass, ServiceEvent.INACTIVE));
					}
//				}
				for (Object odci : instances.toArray()){
					((OnDemandComponentInstance)odci).dispose();
				}
//				instances.clear();
				this.status = ComponentInstanceStatus.INACTIVE;
				return true;
			} catch (Throwable e) {
				log("can not activate instance of " + component.definition.implement.getName(), e);
			} 
			return false;
		}
	}

	@Override
	public  boolean destroy() {
		synchronized (instanceLock) {
			if (status == ComponentInstanceStatus.DESTROYED){
				return false;
			}
			this.status = ComponentInstanceStatus.DESTROYED;
			return true;
		}
	}

	@Override
	public synchronized boolean init() {
		return true;
	}


}
