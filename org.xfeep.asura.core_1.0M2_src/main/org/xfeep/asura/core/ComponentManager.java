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



import java.util.List;
import java.util.concurrent.ExecutorService;

import org.xfeep.asura.core.index.FieldIndex;
import org.xfeep.asura.core.index.MultiValuedFieldCopyOnWriteIndex;
import org.xfeep.asura.core.index.ObjectAccessor;

public class ComponentManager {
	
//	ConcurrentHashMap<String, Class> interfacesMap = new ConcurrentHashMap<String, Class>();
	protected FieldIndex<ComponentDefinition>[] indexes;// = new FieldIndex[ScopeType.values().length];
	protected ServiceSpace applicationServiceSpace;// = new ServiceSpace(ServiceSpace.APPLICATION_SERVICE_SPACE_ID, ScopeType.APPLICATION, null);
	
	
	public ServiceSpace getApplicationServiceSpace() {
		return applicationServiceSpace;
	}


	public ComponentManager(ExecutorService executorService) {
		indexes = new FieldIndex[ScopeType.values().length];
		applicationServiceSpace = new ServiceSpace(ServiceSpace.APPLICATION_SERVICE_SPACE_ID, ScopeType.APPLICATION, null, this, executorService);
		ObjectAccessor<ComponentDefinition> accessor = new ObjectAccessor<ComponentDefinition>(){
			public ComponentDefinition[] createArray(int size) {
				return new ComponentDefinition[size];
			}
			
			public Object resolveField(String field, ComponentDefinition obj) {
				return obj.indexKeys;
			}
		};
		for (ScopeType s : ScopeType.values()){
			MultiValuedFieldCopyOnWriteIndex<ComponentDefinition> index = new MultiValuedFieldCopyOnWriteIndex<ComponentDefinition>("interfaces", accessor);
//			MultiValuedFieldBackedResultFieldIndex<ComponentDefinition> index = new MultiValuedFieldBackedResultFieldIndex<ComponentDefinition>("interfaces", accessor);
			indexes[s.ordinal()]  = index;
		}
	}
	
	
	public void openApplicationServiceSpace() {
		for (ScopeType s : ScopeType.values()){
			FieldIndex<ComponentDefinition> index = indexes[s.ordinal()];
			index.addEventListener(applicationServiceSpace);
		}
		applicationServiceSpace.open(true);
	}
	
	public void addAll(List<ComponentDefinition> componentDefList){
		for (ComponentDefinition cd : componentDefList){
			add(cd);
		}
	}
	
	public void add(ComponentDefinition componentDef){
//		LogManager.getInstance().info(componentDef.getImplement());
		int i = componentDef.getScope().ordinal();
		indexes[i].insert(componentDef);
//		interfacesMap.putIfAbsent(component.implement.getName(), component.implement);
		//check whether satisfied
		
	}
	
	public void add(Class implementClass){
		add(ComponentDefinition.create(implementClass));
	}
	
	public static Component createComponent(ComponentDefinition definition, ServiceSpace space){
		if (definition == null){
			return null;
		}
		switch(definition.type){
		case EAGER:
			return new EagerComponent(definition, space);
		case LAZY:
			return new LazyComponent(definition, space);
		case FACTORY:
			return new FactoryComponent(definition, space);
		case ON_DEMAND:
			return new OnDemandComponent(definition, space);
		default:
			return new EagerComponent(definition, space);
		}
	}
	
	public void remove(ComponentDefinition componentDef){
		int i = componentDef.getScope().ordinal();
		FieldIndex<ComponentDefinition> index = indexes[i];
		index.removeSingle(componentDef);
//		if(index.matchSingle(component))
	}
	
	public FieldIndex<ComponentDefinition> getComponentDefinitionIndex(ScopeType scopeType){
		return indexes[scopeType.ordinal()];
	}
	
}
