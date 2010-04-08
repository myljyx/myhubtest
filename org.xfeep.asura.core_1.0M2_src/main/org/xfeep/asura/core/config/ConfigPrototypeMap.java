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
package org.xfeep.asura.core.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.xfeep.asura.core.el.SimpleComparator;

public class ConfigPrototypeMap extends LazyConfigInfo  implements Map<String, Object> {

	
	
	public ConfigPrototypeMap(String id,
			Callable<Map<String, Object>> configFetcher) {
		super(id, configFetcher);
	}


	public ConfigPrototypeMap() {
	}
	
	
	public void clear() {
		throw new UnsupportedOperationException("this map can not modify");
	}

	public boolean containsKey(Object key) {
		return ConfigServiceImp.CONFIG_ID.equals(key) || ConfigServiceImp.CONFIG_FUTURE.equals(key);
	}

	public boolean containsValue(Object value) {
		return SimpleComparator.compareEquals(id, value) || SimpleComparator.compareEquals(configFetcher, value);
	}

	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		throw new UnsupportedOperationException("this map  not support entrySet()");
	}

	public Object get(Object key) {
		return ConfigServiceImp.CONFIG_ID.equals(key) ? id : ( ConfigServiceImp.CONFIG_FUTURE.equals(key) ?  configFetcher : null) ;
	}

	public boolean isEmpty() {
		return false;
	}

	public Set<String> keySet() {
		throw new UnsupportedOperationException("this map  not support keySet()");
	}

	public Object put(String key, Object value) {
		throw new UnsupportedOperationException("this map  not support put()");
	}

	public void putAll(Map<? extends String, ? extends Object> m) {
		throw new UnsupportedOperationException("this map  not support putAll()");
	}

	public Object remove(Object key) {
		throw new UnsupportedOperationException("this map  not support remove()");
	}

	public int size() {
		return 2;
	}

	public Collection<Object> values() {
		return Arrays.asList(id, configFetcher);
	}

}
