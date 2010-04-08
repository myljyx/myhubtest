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

import org.xfeep.asura.core.event.ServiceEvent;

public class FactoryComponentInstance extends ComponentInstance {

	public FactoryComponentInstance() {
	}

	public FactoryComponentInstance(Component component, String configId) {
		super(configId, component);
	}
	
	@Override
	public  boolean activate() {
		synchronized (instanceLock) {
			if (status == ComponentInstanceStatus.ACTIVE){
				return false;
			}
			try {
				if ( component.bindReferences(this) && bindReferences()) {
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
					FactoryComponent factoryComponent = (FactoryComponent)component;
					factoryComponent.instances.add(this);
					for (Class serviceClass : component.definition.interfaces){
						component.serviceSpace.sendEvent(new ServiceEvent(this, serviceClass, ServiceEvent.ACTIVE));
					}
					return true;
				}
			} catch (Throwable e) {
				log("can not activate instance of " + component.definition.implement.getName(), e);
			} 
			return false;
		}
	}
	
	@Override
	public  boolean deactivate() {
		synchronized (instanceLock) {
			boolean rt = false;
			do {
				if (status != ComponentInstanceStatus.ACTIVE){
					rt = false;
					break;
				}
				try {
//					if (status == ComponentInstanceStatus.ACTIVE){
						status = ComponentInstanceStatus.ACTIVE_TO_INACTIVE;
						for (Class serviceClass : component.definition.interfaces){
							component.serviceSpace.sendEvent(new ServiceEvent(this, serviceClass, ServiceEvent.INACTIVE));
						}
//					}
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
				((FactoryComponent)component).instances.remove(this);
			}
			return rt;
		}

	}

}
