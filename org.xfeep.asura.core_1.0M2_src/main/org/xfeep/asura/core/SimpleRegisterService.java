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

import java.util.concurrent.ConcurrentHashMap;

import org.xfeep.asura.core.annotation.Service;

@Service
public class SimpleRegisterService {

	protected ConcurrentHashMap<Object, Object> registry = new ConcurrentHashMap<Object, Object>();
	
	public <T> T  lookup(Object key){
		return (T)registry.get(key);
	}
	
	public Object bind(Object key, Object value){
		return registry.put(key, value);
	}
	
	public boolean exists(Object key){
		return registry.contains(key);
	}
	
}
