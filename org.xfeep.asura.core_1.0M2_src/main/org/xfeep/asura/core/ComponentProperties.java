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

import java.util.HashMap;
import java.util.Map;

public class ComponentProperties extends HashMap<String, Object> implements LazyResolvablePropertyCollection {

	private static final long serialVersionUID = 1L;
	protected Map<String, Object> config;
	
	@Override
	public boolean containsKey(Object key) {
		return super.containsKey(key) || (config != null && config.containsKey(key));
	}
	
	public void initComponentProperties(String configId, Component component){
		put(CoreConsts.CONFIG_ID, configId);
		put(CoreConsts.VAR_COMPONENT_NAME, component.getDefinition().getName());
		put(CoreConsts.VAR_COMPONENT_IMP, component.getDefinition().getImplement().getName());
	}
	
	@Override
	public Object get(Object key) {
		Object rt =  super.get(key);
		if (rt == null && config != null){
			rt = config.get(key);
		}
		return rt;
	}


	public Object getLazyResolvableProperty(String name, Class<?> type,
			Class<?>[] memberTypes) {
		Object rt = super.get(name);
		if (rt == null && config != null){
			if (config instanceof LazyResolvablePropertyCollection) {
				LazyResolvablePropertyCollection lc = (LazyResolvablePropertyCollection) config;
				rt = lc.getLazyResolvableProperty(name, type, memberTypes);
			}else {
				rt = config.get(name);
			}
		}
		return rt;
	}


	public void config(Map<String, Object> config) {
		this.config = config;
	}


	public Map<String, Object> config() {
		return config;
	}
	
	

}
