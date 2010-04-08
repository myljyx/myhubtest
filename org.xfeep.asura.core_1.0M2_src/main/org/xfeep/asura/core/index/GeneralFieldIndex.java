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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class GeneralFieldIndex<T> implements FieldIndex<T> {

	String field;
	ObjectAccessor<T> fieldAccessor;
	ConcurrentHashMap<Object, ArrayList<T>> index = new ConcurrentHashMap<Object, ArrayList<T>>();
	ConcurrentHashMap<Object, ReentrantReadWriteLock> indexLocks = new ConcurrentHashMap<Object, ReentrantReadWriteLock>();
	
	CopyOnWriteArrayList<IndexEventListener> listeners = new CopyOnWriteArrayList<IndexEventListener>();
	
	public GeneralFieldIndex() {
	}
	
	
	
	public GeneralFieldIndex(String field, ObjectAccessor<T> fieldAccessor) {
		super();
		this.field = field;
		this.fieldAccessor = fieldAccessor;
	}



	public T insert(T o) {
		Object key = fieldAccessor.resolveField(field, o);
		return insert(key, o);
	}



	public T insert(Object key, T o) {
		if (key == null){
			return null;
		}
		ReentrantReadWriteLock lock = indexLocks.get(key);
		ReentrantReadWriteLock olock = null;
		if (lock == null){
			lock = new ReentrantReadWriteLock(true);
			olock = indexLocks.putIfAbsent(key, lock);
			if (olock != null){
				lock = olock;
			}
		}
		
		ArrayList<T> list = null;
		ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		writeLock.lock();
		try{
			list = index.get(key);
			if (list == null){
				index.put(key, list = new ArrayList<T>());
				//maybe it has been removed in method : public T[] remove(Object val) 
				indexLocks.putIfAbsent(key, lock);
			}
			list.add(o);
		}finally{
			writeLock.unlock();
		}
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
		ReentrantReadWriteLock lock = indexLocks.get(val);
		if (lock == null){
			return Collections.EMPTY_LIST;
		}
		ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
		readLock.lock();
		try{
			ArrayList<T> list = index.get(val);
			if (list == null){
				return Collections.EMPTY_LIST;
			}
			return (List<T>) Arrays.asList(list.toArray());
		}finally{
			readLock.unlock();
		}
		
	}

	public T matchSingle(Object val) {
		if (val == null){
			return null;
		}
		ReentrantReadWriteLock lock = indexLocks.get(val);
		if (lock == null){
			return null;
		}
		ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
		readLock.lock();
		try{
			ArrayList<T> list = index.get(val);
			if (list == null){
				return null;
			}
			return list.get(0);
		}finally{
			readLock.unlock();
		}
		
	}

	public List<T> remove(Object val) {
		if (val == null){
			return Collections.EMPTY_LIST;
		}
		ReentrantReadWriteLock lock = indexLocks.get(val);
		if (lock == null){
			return Collections.EMPTY_LIST;
		}
		ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		writeLock.lock();
		List<T> rt = Collections.EMPTY_LIST;
		try{
			ArrayList<T> list = index.remove(val);
			if (list != null){
				//create a read only list as return value for  event safe dispatching 
				rt = (List<T>) Arrays.asList(list.toArray());
			}
			indexLocks.remove(val);
			if (rt != null && !listeners.isEmpty()){
				IndexEvent event = new IndexEvent(IndexEvent.REMOVE, rt);
				for (IndexEventListener listener : listeners){
					listener.onIndexChanged(event);
				}
			}
			return rt;
		}finally{
			writeLock.unlock();
		}
		
	}



	public void removeSilent(Object val) {
		indexLocks.remove(val);
		index.remove(val);
	}

	public T removeSingle(T o) {
		Object val = fieldAccessor.resolveField(field, o);
		return removeSingle(val, o);
	}
	
	public T removeSingle(Object val, T o){
		if (val == null){
			return null;
		}
		ReentrantReadWriteLock lock = indexLocks.get(val);
		if (lock == null){
			return null;
		}
		ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
		writeLock.lock();
		
		try{
			ArrayList<T> list = index.get(val);
			T rt = null;
			if ( list.remove(o) ) {
				rt = o;
				if (list.isEmpty()){
					index.remove(val);
					indexLocks.remove(val);
				}
			}
			if (rt != null && !listeners.isEmpty()){
				IndexEvent event = new IndexEvent(IndexEvent.REMOVE, rt);
				for (IndexEventListener listener : listeners){
					listener.onIndexChanged(event);
				}
			}
			return rt;
		}finally{
			writeLock.unlock();
		}
	}
	
	public void visitAll(IndexedObjectVisitor<T> visitor) {
		for (Object val : index.keySet()){
			ReentrantReadWriteLock lock = indexLocks.get(val);
			if (lock == null){
				continue;
			}
			ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
			readLock.lock();
			try{
				ArrayList<T> list = index.get(val);
				if (list == null){
					continue;
				}
				for (T t : list){
					visitor.visit(t);
				}
			}finally{
				readLock.unlock();
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
		return index.isEmpty();
	}
}
