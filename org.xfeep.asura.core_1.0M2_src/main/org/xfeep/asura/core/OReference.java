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


import org.xfeep.asura.core.config.ConfigService;
import org.xfeep.asura.core.config.ConfigServiceImp;
import org.xfeep.asura.core.event.ServiceEvent;
import org.xfeep.asura.core.reflect.TypeInfoPool;
import org.xfeep.asura.core.reflect.TypeItem;

public class OReference extends Reference {
	
	protected ComponentInstance target;
//	AtomicBoolean binded = new AtomicBoolean(false);
//	boolean connected = false;
	
	public OReference() {
	}

	public OReference(ReferenceDefinition definition, Component source) {
		super(definition, source);
	}

	@Override
	public boolean isSatisfied() {
		if (!definition.multiplicityType.isRequired()){
			return true;
		}
		if (target != null){
			return true;
		}
		return false;
	}

	@Override
	public  boolean bind(ComponentInstance instance) {
//		if (binded){
//			return false;
//		}
		if (target == null && !definition.multiplicityType.isRequired()){
			return true;
		}
		TypeItem bind= definition.bind;
		Object service = null;
		try {
			service = target.getService(this);
			// if target is lazy or on-demand component,  target.getService may invoke service.activate 
			// which maybe throws exception then  target.getService == null
			if (service == null){
				target = null;
				return false;
			}
			//use instance.service instead of instance.getService() to avoid dead circle invoke
			//because if instance is lazy component instance, instance.getService() will call bind() too.
			if (bind != null){ // configuration bind is null
				bind.setValue(instance.service, service);
			}
			InjectPropertyItem[] injectProperties = definition.injectProperties;
			if (injectProperties != null){
				LazyResolvablePropertyCollection lrc = null;
				if (service instanceof LazyResolvablePropertyCollection) {
					lrc = (LazyResolvablePropertyCollection) service;
				}
				for (InjectPropertyItem injectBind : injectProperties){
					Object v = null;
					if (lrc != null) {
						v = lrc.getLazyResolvableProperty(injectBind.valueExpression, injectBind.getType(), injectBind.getMemberTypes());
					}
					if (v == null){
						v = instance.getProperties().getLazyResolvableProperty(injectBind.valueExpression, injectBind.getType(), injectBind.getMemberTypes());
					}
					if (v == null){
						v = TypeInfoPool.getDefault().resolvePropertityValue(service, injectBind.valueExpression);
					}
//					instance.addProperty(injectBind.getName(), v);
					injectBind.setValue(instance.service, v);
				}
			}
//			binded = (true);
			return true;
		} catch (Throwable e) {
			LogManager.getInstance().error("error when bind service reference : " + bind.getName() + "@" + source.definition.getImplement().getName(), e);
			if (service != null){
				try{
					target.ungetService(this);
				}catch (Throwable e2) {
					LogManager.getInstance().error("error when unget service reference : " + bind.getName() + "@" + source.definition.getImplement().getName(), e2);
				}
				target = null;
			}
		} 
		return false;
	}

	//TODO: should check whether there are some unsafe problems exist for concurrent invoke, may cause repeated invoking
	@Override
	public  boolean unbind(ComponentInstance instance) {
//		if (!binded){
//			return false;
//		}
		if (target == null){
			return false;
		}
		TypeItem bind= definition.bind;
		try {
			if (bind != null){ // configuration bind is null, so we must check it
				bind.setValue(instance.service, null);
			}
			InjectPropertyItem[] injectProperties = definition.injectProperties;
			if (injectProperties != null){
				for (InjectPropertyItem injectBind : injectProperties){
					if (!injectBind.getType().isPrimitive()){
						injectBind.setValue(instance.service, null);
						instance.removeProperty(injectBind.getName());
					}
				}
			}
			target.ungetService(this);
//			binded = (false);
//			target = null;
			return true;
		} catch (Throwable e) {
			LogManager.getInstance().error("error when unbind service reference : " + (bind == null ? definition.getName() : bind.getName() ) + "@" + source.definition.getImplement().getName(), e);
		} 
		return false;
	}

	@Override
	public  void onServiceChanged(ServiceEvent event) {
		synchronized (source.instanceMainLock) {
			ComponentInstance eventSource = event.getSource();
			if (event.getType() == ServiceEvent.ACTIVE){
				if (target == null){
					if (!definition.getMatcher().match(eventSource.getProperties())){
						return;
					}
					if (definition.getName().equals(CoreConsts.CONFIG_SERVICE_REF_NAME)){
						source.properties.config( ((ConfigService )eventSource.getService()).getConfig());
					}
					InjectPropertyItem[] injectProperties = definition.injectProperties;
					if (injectProperties != null){
						Object targetService = eventSource.getCurrentService();
						LazyResolvablePropertyCollection lrc = null;
						if (targetService instanceof LazyResolvablePropertyCollection) {
							lrc = (LazyResolvablePropertyCollection) targetService;
						}
						for (InjectPropertyItem injectBind : injectProperties){
							Object v = eventSource.getProperties().getLazyResolvableProperty(injectBind.valueExpression, injectBind.getType(), injectBind.getMemberTypes());
							if (v == null){
								if (lrc != null){
									v = lrc.getLazyResolvableProperty(injectBind.valueExpression, injectBind.getType(), injectBind.getMemberTypes());
								}
								if (v == null && targetService != null){
									try {
										v = TypeInfoPool.getDefault().resolvePropertityValue(targetService, injectBind.valueExpression);
									} catch (Throwable e) {
										LogManager.getInstance().warn("in component " + source.definition.getName() + ", compute injected property { " + injectBind.valueExpression + " } failed" , e);
									} 
								}
							}
							if (v != null){
								source.getProperties().put(injectBind.getName(), v);
							}
						}
					}
					if (!source.isStatisfied()){
						target = eventSource;
						if (openOrder == 0){
							for (int i = 1; i < source.references.length; i++){
								source.references[i].openContract();
							}
						}
						source.tryStatisfy();
					}else { //in some required=false cases
						target = eventSource;
						for (ComponentInstance ci : source.getInstances(null)){ 
							bind(ci);
						}
					}
				}
			}else if (target != null){
				if (eventSource == target){
					if (definition.contractType != ContractType.CARELESS){
						//check if there's replacement
						ComponentInstance replacement = null;
						ServiceSpace serviceSpace = source.getServiceSpace();
						for (ComponentDefinition cd : serviceSpace.findComponentDefinition(definition.serviceClass)){
							Component c = serviceSpace.findComponent(cd.implement);
							if (c != null){
								//TODO: should keep lock of c to safely access c
								replacement = c.getInstance(definition.matcher);
								if (replacement != null){
									for (ComponentInstance ci : source.getInstances(null)){
										unbind(ci);
									}
									target = replacement;
									for (ComponentInstance ci : source.getInstances(null)){
										//TODO: should check  return value of bind(ci)
										bind(ci);
									}
									break;
								}
							}
						}
						if (definition.multiplicityType.isRequired() && replacement == null){ // we fund no replacement, so tryUnsatify it.
							source.tryUnstatisfy();
							target = null;
						}
					}
				}
			}
		}

	}

	@Override
	public void closeContract(boolean cascade) {
		target = null;
		super.closeContract(cascade);
	}
	
	@Override
	public void reset() {
		target = null;
	}
	
	
	
}
