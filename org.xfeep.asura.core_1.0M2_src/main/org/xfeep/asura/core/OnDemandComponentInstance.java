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

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.xfeep.asura.core.config.ConfigDynamicReference;
import org.xfeep.asura.core.config.ConfigReferenceDefinintion;
import org.xfeep.asura.core.event.ServiceEvent;

public class OnDemandComponentInstance extends ComponentInstance {

	protected OnDemandComponentRepresentativeInstance representativeInstance;
//	Map<String, String> ondemandRequirement; // merge it into config now
	
	
	public OnDemandComponentInstance() {
	}

	public OnDemandComponentInstance(Component component, String configId) {
		super(configId, component);
	}

	public boolean imply(Map<String, Object> requirement){
		if (requirement == null || requirement.isEmpty()){
			return true;
		}
		if (properties == null){
			return false;
		}
		for (Map.Entry<String, Object> e : requirement.entrySet()){
			Object v = this.properties.get(e.getKey());
			if (v == null || !v.equals(e.getValue())){
				return false;
			}
		}
		return true;
	}
	
	
	@Override
	public synchronized boolean activate() {
		if (status == ComponentInstanceStatus.ACTIVE){
			return false;
		}
		try {
			if (component.bindReferences(this) && bindReferences()){
				
				ComponentDefinition definition = component.definition;
				if (definition.activate != null){
					definition.activate.setAccessible(true);
					if (definition.activate.getParameterTypes().length > 0){
						definition.activate.invoke(service, context);
					}else{
						definition.activate.invoke(service);
					}
				}
				this.status = ComponentInstanceStatus.ACTIVE;
				representativeInstance.instances.add(this);
				for (Class serviceClass : component.definition.interfaces){
					component.serviceSpace.sendEvent(new ServiceEvent(this, serviceClass, ServiceEvent.ACTIVE));
				}
				return true;
			}
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				InvocationTargetException ie = (InvocationTargetException) e;
				e = ie.getTargetException();
			}
			if (e instanceof OnDemandDynamicNotMatchException){
				
			}else{
				log("can not activate instance of " + component.definition.implement.getName(), e);
			}
		} 
		return false;
	}
	
	@Override
	public synchronized boolean deactivate() {
		

		boolean rt = false;
		do {
			if (status != ComponentInstanceStatus.ACTIVE){
				rt = false;
				break;
			}
			try {
//				if (status == ComponentInstanceStatus.ACTIVE){
					status = ComponentInstanceStatus.ACTIVE_TO_INACTIVE;
					for (Class serviceClass : component.definition.interfaces){
						component.serviceSpace.sendEvent(new ServiceEvent(this, serviceClass, ServiceEvent.INACTIVE));
					}
//				}
				ComponentDefinition definition = component.definition;
				if (definition.deactivate != null){
					definition.deactivate.setAccessible(true);
					definition.deactivate.invoke(service);
				}
				unbindReferences();
				component.unbindReferences(this);
				this.status = ComponentInstanceStatus.INACTIVE;
				rt = true;
				break;
			} catch (Throwable e) {
				log("can not deactivate instance of " + component.definition.implement.getName(), e);
			} 
			rt = false;
		}while(false);
		if (rt){
			representativeInstance.instances.remove(this);
		}
		return rt;
	}
}
