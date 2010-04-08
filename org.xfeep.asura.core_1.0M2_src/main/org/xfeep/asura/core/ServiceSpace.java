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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.xfeep.asura.core.event.ServiceEvent;
import org.xfeep.asura.core.event.ServiceEventCenter;
import org.xfeep.asura.core.event.ServiceEventListener;
import org.xfeep.asura.core.index.FieldIndex;
import org.xfeep.asura.core.index.IndexEvent;
import org.xfeep.asura.core.index.IndexEventListener;
import org.xfeep.asura.core.index.IndexedObjectVisitor;
import org.xfeep.asura.core.match.Matcher;
import org.xfeep.asura.core.match.SmartMatcher;

public class ServiceSpace implements IndexEventListener  {
	
	public final static String APPLICATION_SERVICE_SPACE_ID = "$app$";
	
	protected String id;
	
	protected ScopeType scopeType;
	
	protected ServiceSpace parent;
	
	/**
	 * a map of ServiceSpace ID to ServiceSpace
	 */
	protected Map<String, ServiceSpace> children;

	protected FieldIndex<ComponentDefinition> componentDefinitionIndex;
	
	/**
	 * a map of ComponentDefinition to Component in current Service Space
	 */
	protected ConcurrentHashMap<Class, Component> implementToComponentMap;
	
	protected ServiceEventCenter eventCenter;
	
	protected ComponentManager componentManager;
	
	protected ExecutorService executorService;
	
	public ServiceSpace() {
	}
	
	public ServiceSpace(String id, ScopeType scopeType, ServiceSpace parent, ComponentManager componentManager, ExecutorService executorService) {
		super();
		this.id = id;
		this.scopeType = scopeType;
		this.parent = parent;
		this.componentManager = componentManager;
		this.executorService = executorService;
	}
	
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public ServiceSpace(String id, ScopeType scopeType, ServiceSpace parent) {
		super();
		this.id = id;
		this.scopeType = scopeType;
		this.parent = parent;
		this.componentManager = parent.componentManager;
		this.executorService = parent.executorService;
	}
	
	public synchronized void open(boolean initChildren){
		if (initChildren){
			children = new ConcurrentHashMap<String, ServiceSpace>();
		}
		open();
	}
	
	public synchronized void open(){
		eventCenter = new ServiceEventCenter(executorService);
		this.componentDefinitionIndex = componentManager.getComponentDefinitionIndex(scopeType);
		if (!componentDefinitionIndex.isEmpty()){
			implementToComponentMap = new ConcurrentHashMap<Class, Component>();
			IndexedObjectVisitor<ComponentDefinition> componentVisitor = new IndexedObjectVisitor<ComponentDefinition>(){

				public void visit(ComponentDefinition componentDefinition) {
					Component component = ComponentManager.createComponent(componentDefinition, ServiceSpace.this);
					implementToComponentMap.putIfAbsent(componentDefinition.implement, component);
				}
				
			};
			componentDefinitionIndex.visitAll(componentVisitor);
//			try{
//				eventCenter.setRunInCurrentThreadMode(true);
				for (Component component : implementToComponentMap.values()){
					component.enable();
				}
//			}finally{
//				eventCenter.setRunInCurrentThreadMode(false);
//			}
		}
	}
	
	public Collection<Component> getAllComponent() {
		if (implementToComponentMap == null){
			return Collections.EMPTY_LIST;
		}
		return Collections.unmodifiableCollection(implementToComponentMap.values());
	}

	public synchronized void close(){
		if (eventCenter == null){
			return;
		}
		eventCenter.prepareShutdown();
		if (implementToComponentMap != null && !implementToComponentMap.isEmpty()){
			for (Component component : implementToComponentMap.values()){
				component.disable();
			}
		}
		eventCenter.shutdown(true);
		if (implementToComponentMap != null){
			implementToComponentMap.clear();
		}
		eventCenter = null;
	}
	
	public Component findComponent(ComponentDefinition definition){
		if (implementToComponentMap == null){
			return null;
		}
		return implementToComponentMap.get(definition.implement);
	}
	
	public Component findComponent(Class implementClass){
		if (implementToComponentMap == null){
			return null;
		}
		return implementToComponentMap.get(implementClass);
	}
	
	
	public List<ComponentDefinition> findComponentDefinition(Class serviceClass){
		return componentDefinitionIndex.match(serviceClass);
	}
	
	public ComponentDefinition findComponentDefinition(String serviceName){
		return componentDefinitionIndex.matchSingle(serviceName);
	}
	
	public <T> T findService(String serviceName){
		if (implementToComponentMap != null){
			ComponentDefinition cd = componentDefinitionIndex.matchSingle(serviceName);
			Component c = implementToComponentMap.get(cd.implement);
			ComponentInstance instance = c.getInstance(null);
			if (instance != null){
					return (T)instance.getService();
			}
		}
		if (parent != null){
			return parent.findService(serviceName);
		}
		return null;
	}
	
	/**
	 * if there's a implementation of requested service 's lazy component and the component is satisfied,
	 * the component will prepare to new  a service instance and return it.
	 * @param serviceClass
	 * @return
	 */
	public <T> T findService(Class<T> serviceClass){
		return findService(serviceClass, (Matcher)null);
	}
	
	public <T> T findService(Class<T> serviceClass, String matcher){
		if (matcher == null){
			return findService(serviceClass, (Matcher)null);
		}
		return findService(serviceClass, SmartMatcher.getMatcher(matcher, null));
	}
	
	public <T> T findService(Class<T> serviceClass, Matcher matcher){
		if (implementToComponentMap != null && !implementToComponentMap.isEmpty()){
			for (ComponentDefinition cd : componentDefinitionIndex.match(serviceClass)){
				Component c = implementToComponentMap.get(cd.implement);
				ComponentInstance instance = c.getInstance(matcher);
				if (instance != null){
					return (T)instance.getService();
				}
			}
		}
		
		if (parent != null){
			return parent.findService(serviceClass, matcher);
		}
		return null;
	}
	
	public <T> List<T>  findServices(Class<T> serviceClass){
		List<T> list = new ArrayList<T>();
		if (implementToComponentMap != null){
			for (ComponentDefinition cd : componentDefinitionIndex.match(serviceClass)){
				Component c = implementToComponentMap.get(cd.implement);
				list.addAll((Collection<? extends T>) c.getInstances(null));
			}
		}
		if (list.isEmpty() && parent != null){
			return parent.findServices(serviceClass);
		}
		return list;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ScopeType getScopeType() {
		return scopeType;
	}

	public void setScopeType(ScopeType scopeType) {
		this.scopeType = scopeType;
	}

	public ServiceSpace getParent() {
		return parent;
	}

	public void setParent(ServiceSpace parent) {
		this.parent = parent;
	}
	
	public ServiceSpace getChild(String id){
		return children.get(id);
	}

	public Map<String, ServiceSpace> getChildren() {
		return children;
	}
	
	public void addChild(ServiceSpace serviceSpace){
		ServiceSpace old = children.put(serviceSpace.getId(), serviceSpace);
		if (old != null){
			old.close();
		}
	}
	
	public ServiceSpace removeChild(String id){
		return children.remove(id);
	}

	public void setChildren(Map<String, ServiceSpace> children) {
		this.children = children;
	}


	public ServiceEventCenter getEventCenter() {
		return eventCenter;
	}

	public void setEventCenter(ServiceEventCenter eventCenter) {
		this.eventCenter = eventCenter;
	}



	public void sendEvent(ServiceEvent event){
		if (eventCenter != null){//TODO:should check whether it is open
			eventCenter.sendEvent(event);
//Note: for better performance, we replace those code  by invoking parent.addTraceListener in addTraceListener method
//			if (children != null){ 
//				for (ServiceSpace ss : children.values()){
//					ss.sendEvent(event);
//				}
//			}
		}
	}
	
	public ServiceEventListener addTraceListener(ServiceEventListener listener){
		Class serviceClass = listener.getServiceClass();
		List<ComponentDefinition> list =  componentDefinitionIndex.match(serviceClass);
		if (list.isEmpty()){
			if (parent != null){
				ServiceEventListener parentReturn = parent.addTraceListener(listener);
				if (parentReturn != null){
					return parentReturn;
				}
			}
//			return null;
		}
		if (implementToComponentMap != null){
			for (ComponentDefinition def : list){
				Component component = implementToComponentMap.get(def.implement);
				ComponentInstance componentInstance = component.getInstance(listener.getDetailMatcher());
				if (componentInstance != null) {
					listener.onServiceChanged(new ServiceEvent(componentInstance, serviceClass, ServiceEvent.ACTIVE));
				}
			}
		}
		
		return eventCenter.addListener(listener);
	}
	
	public ComponentManager getComponentManager() {
		return componentManager;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public ServiceEventListener removeTraceListener(ServiceEventListener listener){
		ServiceEventListener rt = eventCenter.removeListener(listener);
		if (parent != null){
			ServiceEventListener prt = parent.eventCenter.removeListener(listener);
			if (rt == null){
				rt = prt;
			}
		}
		return rt;
	}

	public synchronized void onIndexChanged(IndexEvent event) {
		ComponentDefinition componentDefinition = (ComponentDefinition)event.getSource();
		if (componentDefinition.getScope() == scopeType){
			if (event.getType() == IndexEvent.INSERT){
				if (implementToComponentMap == null){
					implementToComponentMap = new ConcurrentHashMap<Class, Component>();
				}
				Component component = ComponentManager.createComponent(componentDefinition, ServiceSpace.this);
				if ( implementToComponentMap.putIfAbsent(componentDefinition.implement, component) == null){
					component.enable();
				}
				
			}else { // event type is remove
				if (implementToComponentMap != null){
					Component component = implementToComponentMap.remove(componentDefinition.implement);
					if (component != null){
						component.disable();
					}
				}
			}
		}else {
			if (children != null){
				for (ServiceSpace ss : children.values()){
					ss.onIndexChanged(event);
				}
			}
		}
	}

}
