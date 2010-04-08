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



public class ComponentInstanceProperties extends ComponentProperties  {

//	protected Map<String, Object> config;
//	protected Map<String, Object> nonConfigProperties; 
	
	private static final long serialVersionUID = 1L;
	protected ComponentProperties componentProperties;
	
	public ComponentInstanceProperties() {
	}
	
	@Override
	public void initComponentProperties(String configId, Component component) {
		put(CoreConsts.CONFIG_ID, configId);
		this.componentProperties = component.getProperties();
	}
	
	public Object getLazyResolvableProperty(String name, Class<?> type,
			Class<?>[] memberTypes) {
		Object rt = super.getLazyResolvableProperty(name, type, memberTypes);
		if (rt == null){
			rt = componentProperties.getLazyResolvableProperty(name, type, memberTypes);
		}
		return rt;
	}
	
	@Override
	public Object get(Object key) {
		Object rt = super.get(key);
		return rt == null ? componentProperties.get(key) : rt;
	}
	
	@Override
	public boolean containsKey(Object key) {
		return super.containsKey(key) ? true : componentProperties.containsKey(key);
	}

}
