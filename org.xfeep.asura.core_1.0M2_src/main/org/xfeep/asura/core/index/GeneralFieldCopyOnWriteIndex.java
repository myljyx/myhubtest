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
package org.xfeep.asura.core.index;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GeneralFieldCopyOnWriteIndex<T> implements FieldIndex<T> {

	String field;
	ObjectAccessor<T> fieldAccessor;
	ConcurrentHashMap<Object, CopyOnWriteArrayList<T>> index = new ConcurrentHashMap<Object, CopyOnWriteArrayList<T>>();
	CopyOnWriteArrayList<IndexEventListener> listeners = new CopyOnWriteArrayList<IndexEventListener>();
	
	public GeneralFieldCopyOnWriteIndex() {
	}
	
	public GeneralFieldCopyOnWriteIndex(String field, ObjectAccessor<T> fieldAccessor) {
		super();
		this.field = field;
		this.fieldAccessor = fieldAccessor;
	}
	
	public T insert(T o) {
		Object val = fieldAccessor.resolveField(field, o);
		return insert(val, o);
	}

	public T insert(Object val, T o) {
		if (val == null){
			return null;
		}
		CopyOnWriteArrayList<T> list = null;
		CopyOnWriteArrayList<T> old = null;
		
		// We should do some tricker on it to maintain data consistency.
		// After insert we check the index again to make sure that the 
		// list in current index is still the one we just get.  
		 
		do{
			list = index.get(val);
			if (list == null){
				list = new CopyOnWriteArrayList<T>();
				index.put(val, list);
			}
			list.add(o);
			old = index.get(val);
		}while(old != list);
		
		if (!listeners.isEmpty()){
			IndexEvent event = new IndexEvent(IndexEvent.INSERT, o);
			for (IndexEventListener listener : listeners){
				listener.onIndexChanged(event);
			}
		}
		return o;
	}

	public boolean isUniqueIndex() {
		return false;
	}

	public List<T> match(Object val) {
		if (val == null){
			return Collections.EMPTY_LIST;
		}
		CopyOnWriteArrayList<T> list = index.get(val); 
		if (list == null){
			return Collections.EMPTY_LIST;
		}
		return list;
	}

	public T matchSingle(Object val) {
		if (val == null){
			return null;
		}
		CopyOnWriteArrayList<T> old = index.get(val); 
		if (old == null ){
			return null;
		}
		
		//can not code as this: 
		//		  		if (!list.isEmpty) { return list.get(0); } 
		//because list maybe is empty at any time
		 
		for (T t : old){ 
			return t;
		}
		return null;
	}

	/**
	 * It is very very difficult to maintain both data consistency and event consistency
	 * in the implementation of this method. The old and commented implementation is buggy
	 * and fail to keep event consistent when insert() and remove() are invoked at the same
	 * time.
	 */
	public List<T> remove(Object val) {
//		if (val == null){
//			return Collections.EMPTY_LIST;
//		}
//		CopyOnWriteArrayList<T> list = index.remove(val);
//		if (list == null){
//			return Collections.EMPTY_LIST;
//		}
//		if (!listeners.isEmpty()){
//			IndexEvent event = new IndexEvent(IndexEvent.REMOVE, list);
//			for (IndexEventListener listener : listeners){
//				listener.onIndexChanged(event);
//			}
//		}
//		return list;
		throw new UnsupportedOperationException("not support method : remove(val) ");
	}

	public void removeSilent(Object val) {
		index.remove(val);
	}

	public T removeSingle(T o) {
		Object val = fieldAccessor.resolveField(field, o);
		return removeSingle(val, o);
	}

	public T removeSingle(Object val, T o) {
		if (val == null){
			return null;
		}
		CopyOnWriteArrayList<T> list = index.get(val); 
		if (list == null ){
			return null;
		}
		T rt = list.remove(o) ? o : null;
		if (rt != null){
			if (list.isEmpty()){
				index.remove(val);
			}
		}
		if (rt != null && !listeners.isEmpty()){
			IndexEvent event = new IndexEvent(IndexEvent.REMOVE, rt);
			for (IndexEventListener listener : listeners){
				listener.onIndexChanged(event);
			}
		}
		return rt;
	}

	public void visitAll(IndexedObjectVisitor<T> visitor) {
		for (CopyOnWriteArrayList<T> list : index.values()){
			for (T t : list){
				visitor.visit(t);
			}
		}
	}
	
	public IndexEventListener addEventListener(IndexEventListener listener) {
		listeners.add(listener);
		return listener;
	}

	public IndexEventListener removeEventListener(IndexEventListener listener) {
		return listeners.remove(listener) ? listener : null;
	}

	public boolean isEmpty() {
		if (index.isEmpty()){
			return true;
		}
		for (CopyOnWriteArrayList<T> list : index.values()){
			if (!list.isEmpty()){
				return false;
			}
		}
		return true;
	}
}
