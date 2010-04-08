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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.w3c.dom.Node;
import org.xfeep.asura.core.match.Matcher;

/**
 * move to org.xfeep.asura.bootstrap.config
 * here this class will be deleted in 1.0 M3
 * @author zhang yuexiang
 *
 */
@Deprecated
public class ConfigLazyMap implements Map<String, Object> {

	protected Map<String, Object> delegate;
	protected String configId;

	public String getConfigId() {
		return configId;
	}



	public void setConfigId(String configId) {
		this.configId = configId;
	}



	public Map<String, Object> getDelegate() {
		return delegate;
	}



	public void setDelegate(Map<String, Object> delegate) {
		this.delegate = delegate;
	}

	JAXBContext jaxbContext;
	
	public ConfigLazyMap() {
	}
	
	
	
	public ConfigLazyMap(Map<String, Object> delegate,
			JAXBContext jaxbContext) {
		super();
		this.delegate = delegate;
		this.jaxbContext = jaxbContext;
	}
	
	public boolean isLazy() {
		return jaxbContext != null;
	}

	public Object getConfigProperty(String key, Class<?> type){
		if (isLazy()){
			Object v = delegate.get(key);
			if (v == null){
				return null;
			}
			if (v instanceof Node) {
				Node n = (Node) v;
				synchronized (n) {
					//check again
					v = delegate.get(key);
					if (v instanceof Node){
						try {
							v = jaxbContext.createUnmarshaller().unmarshal(n, type).getValue();
						} catch (JAXBException e) {
							throw new IllegalArgumentException("can not parse config property : " + key +" whose type is" + type, e);
						}
						delegate.put(key, v);
						return v;
					}else{
						return v;
					}
					
				}
			}else{
				return v;
			}
		}else{
			return delegate.get(key);
		}
	}

	public void clear() {
		delegate.clear();
	}
	
	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}
	
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}
	
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return delegate.entrySet();
	}
	
	public boolean equals(Object o) {
		return delegate.equals(o);
	}
	
	public Object get(Object key) {
		if (Matcher.ON_DEMAND_CONFIG_ID.equals(key)){
			return configId;
		}
		return delegate.get(key);
	}
	
	public int hashCode() {
		return delegate.hashCode();
	}
	
	public boolean isEmpty() {
		return delegate.isEmpty();
	}
	
	public Set<String> keySet() {
		return delegate.keySet();
	}
	
	public Object put(String key, Object value) {
		return delegate.put(key, value);
	}
	
	public void putAll(Map<? extends String, ? extends Object> m) {
		delegate.putAll(m);
	}
	
	public Object remove(Object key) {
		return delegate.remove(key);
	}
	
	public int size() {
		return delegate.size();
	}
	
	public Collection<Object> values() {
		return delegate.values();
	}
	
	@Override
	public String toString() {
		return delegate.toString();
	}
	
}
