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


import org.xfeep.asura.core.annotation.Ref;
import org.xfeep.asura.core.match.Matcher;
import org.xfeep.asura.core.match.SmartMatcher;
import org.xfeep.asura.core.reflect.TypeItem;
import org.xfeep.asura.core.reflect.TypeInfoUtil;


public class ReferenceDefinition implements Comparable<ReferenceDefinition>{
	
	protected ComponentDefinition componentDefinition;
	protected String name;
	protected Class serviceClass;
	protected MultiplicityType multiplicityType = MultiplicityType.ONE_ONE;
	protected ContractType contractType = ContractType.CAREFUL;
	protected Matcher matcher = SmartMatcher.MATCH_ALL_MATCHER;
	protected TypeItem bind;
	protected InjectPropertyItem[] injectProperties;
	protected int order = 0;
	
	public ReferenceDefinition() {
	}
	
	
	
	public ReferenceDefinition(ComponentDefinition componentDefinition,
			String name, Class serviceClass, Matcher matcher,
			InjectPropertyItem[] injectProperties) {
		super();
		this.componentDefinition = componentDefinition;
		this.name = name;
		this.serviceClass = serviceClass;
		this.matcher = matcher;
		this.injectProperties = injectProperties;
	}



	public ReferenceDefinition(ComponentDefinition componentDefinition, String name, TypeItem bind, InjectPropertyItem[] injectProperties) {
		super();
		this.componentDefinition = componentDefinition;
		this.name = name;
		this.bind = bind;
		Ref ri = bind.getAnnotation(Ref.class);
		if (ri != null && ri.name().length() != 0){
			this.name = ri.name();
		}
		Class type = bind.getType();
		if (type.isArray() || TypeInfoUtil.isCollection(type)){
			serviceClass = bind.getMemberTypes()[0];
			multiplicityType = ( ri.required() ? MultiplicityType.ONE_N : MultiplicityType.ZERO_N );
		}else {
			serviceClass = (type);
			multiplicityType = ( ri.required() ? MultiplicityType.ONE_ONE : MultiplicityType.ZERO_ONE );
		}
		if (ri.matcher().length() > 0){
			matcher = SmartMatcher.getMatcher(ri.matcher(), componentDefinition.implement);
		}
		contractType = ri.contractType();
		this.injectProperties = injectProperties;
	}



	public String getName() {
		return name;
	}




	public void setName(String name) {
		this.name = name;
	}

	
	public ComponentDefinition getComponentDefinition() {
		return componentDefinition;
	}




	public void setComponentDefinition(ComponentDefinition componentDefinition) {
		this.componentDefinition = componentDefinition;
	}




	public Class getServiceClass() {
		return serviceClass;
	}




	public void setServiceClass(Class serviceClass) {
		this.serviceClass = serviceClass;
	}




	public ContractType getContractType() {
		return contractType;
	}




	public void setContractType(ContractType contractType) {
		this.contractType = contractType;
	}




	public Matcher getMatcher() {
		return matcher;
	}



	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}



	public TypeItem getBind() {
		return bind;
	}




	public void setBind(TypeItem bind) {
		this.bind = bind;
	}




	public MultiplicityType getMultiplicityType() {
		return multiplicityType;
	}


	public void setMultiplicityType(MultiplicityType multiplicityType) {
		this.multiplicityType = multiplicityType;
	}
	
	public boolean isDynamicReference() {
		return matcher != null && matcher.isDynamic();
	}

	
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int compareTo(ReferenceDefinition o) {
		if (this.isDynamicReference() == o.isDynamicReference()){
			if (this.order > o.order){
				return 1;
			}else if (this.order < o.order){
				return -1;
			}
			return 0;
		}
		return this.isDynamicReference() ? 1 : -1;
	}
	
	public InjectPropertyItem[] getInjectProperties() {
		return injectProperties;
	}

	public void setInjectProperties(InjectPropertyItem[] injectProperties) {
		this.injectProperties = injectProperties;
	}
	
}
