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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.xfeep.asura.core.event.ServiceEvent;
import org.xfeep.asura.core.reflect.TypeItem;

public class NReference extends Reference {

//	protected List<ComponentInstance> targets;// = new ArrayList<ComponentInstance>();
	
	protected LinkedHashSet<ComponentInstance> targets;
	
	public NReference(ReferenceDefinition definition, Component component) {
		super(definition, component);
	}

	@Override
	public boolean isSatisfied() {
		if (!definition.multiplicityType.isRequired()){
			return true;
		}
		if (targets != null){
			return !targets.isEmpty();
		}
		return false;
	}

	@Override
	public  boolean bind(ComponentInstance instance) {
//		if (binded){
//			return false;
//		}
		if ( targets == null && !definition.multiplicityType.isRequired()){
			return true;
		}
		TypeItem bind= definition.bind;
		List<Object> services = new ArrayList<Object>();
		for (ComponentInstance target : targets){
			try {
				//use instance.service instead of instance.getService() to avoid dead circle invoke
				//because if instance is lazy component instance, instance.getService() will call bind() too.
				Object service = target.getService(this);
				// if target is lazy or on-demand component,  target.getService may invoke service.activate 
				// which maybe throws exception then  target.getService == null
				if (service == null){
					continue;
				}
				services.add(service);
			} catch (Throwable e) {
				LogManager.getInstance().warn("error when bind service reference : " + bind.getName() + "@" + source.definition.getImplement().getName(), e);
			} 
		}
		try{
			if (bind.getType().isArray()){
				Object array = Array.newInstance(definition.serviceClass, services.size());
				for (int i = 0; i < services.size(); i++){
					Array.set(array, i, services.get(i));
				}
				bind.setValue(instance.service, array);
			}else{
				bind.setValue(instance.service, services);
			}
//			binded = (true);
			return true;
		}catch (Throwable e) {
			LogManager.getInstance().error("error when bind service reference : " + bind.getName() + "@" + source.definition.getImplement().getName(), e);
		}
		return false;
	}
	
	public  boolean unbind(ComponentInstance instance, ComponentInstance target) {
//		if (!binded){
//			return false;
//		}
		TypeItem bind= definition.bind;
		try {
			bind.setValue(instance.service, null);
			target.ungetService(this);
//			binded = (false);
//			target = null;
			return true;
		} catch (Throwable e) {
			LogManager.getInstance().error("error when unbind service reference : " + bind.getName() + "@" + source.definition.getImplement().getName(), e);
		} 
		return false;
	}

	@Override
	public  boolean unbind(ComponentInstance instance) {
//		if (!binded){
//			return false;
//		}
		for (ComponentInstance target : targets){
			unbind(instance, target);
		}
//		targets.clear();
		return true;
	}

	@Override
	public  void onServiceChanged(ServiceEvent event) {
		synchronized (source.instanceMainLock) {
			ComponentInstance eventSource = event.getSource();
			if (event.getType() == ServiceEvent.ACTIVE && definition.getMatcher().match(eventSource.getProperties())){
				if (!source.isStatisfied()){
						if (targets == null){
							targets = new LinkedHashSet<ComponentInstance>();
						}
						if (!targets.contains(eventSource)){
							targets.add(eventSource);
						}
						source.tryStatisfy();
				}else { 
					if (targets == null){ // in some required=false cases
						targets = new LinkedHashSet<ComponentInstance>();
					}
					if (!targets.contains(eventSource)){
						targets.add(eventSource);
						for (ComponentInstance ci : source.getInstances(null)){
							bind(ci);
						}
					}
				}
			}else if (targets != null && definition.contractType != ContractType.CARELESS){
//				int i = targets.indexOf(eventSource);
				if (targets.contains(eventSource)){
					targets.remove(eventSource);
					if (definition.multiplicityType.isRequired() && targets.isEmpty()){
						source.tryUnstatisfy();
						targets = null;
					}else{
						for (ComponentInstance ci : source.getInstances(null)){
							bind(ci);
						}
					}
					
				}
			}
		}

	}
	
	@Override
	public void closeContract(boolean cascade) {
		targets = null;
		super.closeContract(cascade);
	}

	@Override
	public void reset() {
		targets = null;
	}


}
