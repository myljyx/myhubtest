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

import java.util.Map;

import org.xfeep.asura.core.match.Matcher;


public class ComponentContext {
	
	protected Component component;
	protected Map<String, Object> properties;
//	Map<String, String> ondemandConfigRequirement; //merge it into config now
	
//	public Map<String, String> getOndemandConfigRequirement() {
//		return ondemandConfigRequirement;
//	}

	public ComponentContext() {
	}
	
	public ComponentContext(Component component) {
		this.component = component;
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public Component getComponent() {
		return component;
	}

	public void setComponent(Component component) {
		this.component = component;
	}
	
	public <T> T getService(Class<T> serviceClass) {
		return component.serviceSpace.findService(serviceClass);
	}
	
	public <T> T getService(Class<T> serviceClass, Matcher matcher) {
		return component.serviceSpace.findService(serviceClass, matcher);
	}
	
}
