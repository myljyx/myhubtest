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
package org.xfeep.asura.core.match;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class NouseMap<K, V> implements Map<K, V> {

	public void clear() {
		throw new UnsupportedOperationException("clear");
	}

	public boolean containsKey(Object key) {
		throw new UnsupportedOperationException("containsKey");
	}

	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException("containsValue");
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException("entrySet");
	}

	public V get(Object key) {
		throw new UnsupportedOperationException("get");
	}

	public boolean isEmpty() {
		throw new UnsupportedOperationException("isEmpty");
	}

	public Set<K> keySet() {
		throw new UnsupportedOperationException("keySet");
	}

	public V put(K key, V value) {
		throw new UnsupportedOperationException("put");
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException("putAll");
	}

	public V remove(Object key) {
		throw new UnsupportedOperationException("remove");
	}

	public int size() {
		throw new UnsupportedOperationException("size");
	}

	public Collection<V> values() {
		throw new UnsupportedOperationException("values");
	}

}
