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

import org.xfeep.asura.core.match.Matcher;
import org.xfeep.asura.core.match.SmartMatcher;

public abstract class DynamicReference extends Reference {
	
	protected ComponentInstance sourceInstance;
	protected Matcher dynamicMatcher;
	
	public DynamicReference() {
		super();
	}

	public DynamicReference(ReferenceDefinition definition, ComponentInstance sourceInstance) {
		super(definition, sourceInstance.component);
		this.sourceInstance = sourceInstance;
	}
	
	@Override
	public Matcher getDetailMatcher() {
		return dynamicMatcher;
	}
	
	public DynamicReference getNext() {
		int next = openOrder+1;
		if (next == sourceInstance.dynamicReferences.length){
			return null;
		}
		return sourceInstance.dynamicReferences[next];
	}
	
	public boolean bind() {
		return bind(sourceInstance);
	}
	
	public boolean unbind() {
		return unbind(sourceInstance);
	}
	
	@Override
	public void openContract() {
		dynamicMatcher = ((SmartMatcher)definition.matcher).staticizing(sourceInstance.properties);
		super.openContract();
	}
	
	@Override
	public void closeContract() {
		dynamicMatcher = null;
		super.closeContract();
	}
	
}
