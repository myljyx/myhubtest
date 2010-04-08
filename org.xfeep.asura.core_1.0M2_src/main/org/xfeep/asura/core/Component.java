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

import org.xfeep.asura.core.match.Matcher;



public abstract class Component {

	protected ComponentStatus status = ComponentStatus.ASLEEP;
	protected ServiceSpace serviceSpace;
	protected ComponentDefinition definition;
	/**
	 * only contains static references
	 */
	protected Reference[] references;
	protected byte[] instanceMainLock = new byte[0];
	protected ComponentProperties properties;// = new ComponentProperties();
	
	public ComponentProperties getProperties() {
		return properties;
	}

	public void setProperties(ComponentProperties properties) {
		this.properties = properties;
	}

	public Component() {
	}
	
	public Component(ComponentDefinition definition, ServiceSpace serviceSpace) {
		this.definition = definition;
		this.serviceSpace = serviceSpace;
		this.properties = new ComponentProperties();
		references = new Reference[definition.dynamicReferencePosition];
		for (int i = 0; i < references.length; i++){
			ReferenceDefinition rd  = definition.references[i];
			Reference r = null;
			if (rd.getMultiplicityType().isUnary()){
					r = new OReference(rd, this);
			}else{
				r = new NReference(rd, this);
			}
			r.setOpenOrder(i);
			references[i] = r;
		}
		properties.initComponentProperties(definition.getStaticConfigId(), this);
		String[][] sps = definition.constPropertySet;
		if (sps != null){
			for (String[] sp : sps){
				properties.put(sp[0], sp[1]);
			}
		}
	}
	
	public  void enable(){
		synchronized (instanceMainLock) {
//			for (Reference r : references){
//				r.openContract();
//			}
			if (references != null && references.length > 0){
				references[0].openContract();
			}
			if (status == ComponentStatus.ASLEEP){
				status = ComponentStatus.IDLE;
			}
			if (status != ComponentStatus.SATISFIED){
				tryStatisfy();
			}
		}
	}
	
	public  ComponentStatus getStatus() {
		synchronized (instanceMainLock) {
			return status;
		}
	}
	
	public  boolean isStatisfied() {
		synchronized (instanceMainLock) {
			return status == ComponentStatus.SATISFIED;
		}
	}

	public  void disable(){
		synchronized (instanceMainLock) {
			tryUnstatisfy();
			if (references != null && references.length > 0){
				references[0].closeContract();
			}
			this.status = ComponentStatus.ASLEEP;
		}
	}
	
	public abstract boolean tryStatisfy();
	
	public abstract void tryUnstatisfy();
	
	public ServiceSpace getServiceSpace() {
		return serviceSpace;
	}

	public void setServiceSpace(ServiceSpace serviceSpace) {
		this.serviceSpace = serviceSpace;
	}

	public ComponentDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(ComponentDefinition definition) {
		this.definition = definition;
	}

	public Reference[] getReferences() {
		return references;
	}

	public void setReferences(Reference[] references) {
		this.references = references;
	}

	public void setStatus(ComponentStatus status) {
		this.status = status;
	}

	public boolean bindReferences(ComponentInstance instance){
		int i = 0;
		for (i = 0; i < references.length; i++){
			Reference r = references[i];
			if (!r.bind(instance) && r.getDefinition().getMultiplicityType().isRequired()){
				break;
			}
		}
		if (i < references.length){
			while (i > -1){
				references[i].unbind(instance);
				i--;
			}
			return false;
		}
		return true;
	}
	
	public boolean unbindReferences(ComponentInstance instance){
		boolean rt = true;
		for (int i = 0; i < references.length; i++){
			if (!references[i].unbind(instance)){
				rt = false;
			}
		}
		return rt;
	}
	
	public abstract ComponentInstance getInstance(Matcher matcher);
	
	public abstract List<ComponentInstance> getInstances(Matcher matcher);
	
	/**
	 * this method should be used to debug use
	 * @return all instances not only including  ACTIVE ones
	 */
	public abstract List<ComponentInstance> getAllInstancesForDebugInfo();
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
		sb.append("[").append(definition != null ? definition.implement.getSimpleName() : "")
		.append("]");
		return sb.toString();
	}

}
