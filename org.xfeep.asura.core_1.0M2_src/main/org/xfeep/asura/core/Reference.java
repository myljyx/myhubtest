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
import org.xfeep.asura.core.event.ServiceEventListener;
import org.xfeep.asura.core.match.Matcher;

public abstract class Reference implements ServiceEventListener {
	
	protected ReferenceDefinition definition;
	protected Component source;
	protected int openOrder;
	
	
	public int getOpenOrder() {
		return openOrder;
	}

	public void setOpenOrder(int openOrder) {
		this.openOrder = openOrder;
	}

	public Reference() {
	}
	
	public Reference(ReferenceDefinition definition, Component source) {
		this.definition = definition;
		this.source = source;
	}
	
	public abstract boolean isSatisfied();
	
	public void openContract() {
		ServiceSpace serviceSpace = source.serviceSpace;
		serviceSpace.addTraceListener(this);
	}
	
	public void closeContract() {
		if (openOrder == 0){
			closeContract(true);
		}
	}
	
	public void closeContract(boolean cascade) {
		source.serviceSpace.removeTraceListener(this);
		if (cascade){
			for (int i = openOrder + 1; i < source.references.length; i++){
				source.serviceSpace.removeTraceListener(source.references[i]);
			}
		}
	}
	
	public ReferenceDefinition getDefinition() {
		return definition;
	}
	
	public void setDefinition(ReferenceDefinition definition) {
		this.definition = definition;
	}


	public Class getServiceClass() {
		return definition.serviceClass;
	}
	
	public abstract boolean bind(ComponentInstance instance);
	
	public abstract boolean unbind(ComponentInstance instance);
	
	public abstract void reset();

	public abstract void onServiceChanged(ServiceEvent event);
	
	public Matcher getDetailMatcher() {
		return definition.matcher;
	}
	
	public Reference getNext() {
		int next = openOrder+1;
		if (next == source.references.length){
			return null;
		}
		return source.references[next];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
		sb.append("[").append(definition != null ? definition.name : "").append("->").append(definition != null ? definition.serviceClass.getSimpleName() : "")
		.append("]");
		return sb.toString();
	}
}
