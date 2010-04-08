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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.xfeep.asura.core.util.ReadWriteProtectedArrayList;
import org.xfeep.asura.core.util.ReadWriteProtectedList;

public class BackedResultGeneralFieldIndex<T> implements BackedResultFieldIndex<T> {
	
	String field;
	ObjectAccessor<T> fieldAccessor;
	ConcurrentHashMap<Object, ReadWriteProtectedList<T>> index = new ConcurrentHashMap<Object, ReadWriteProtectedList<T>>();
	ReadWriteProtectedList<IndexEventListener> listeners = new ReadWriteProtectedArrayList<IndexEventListener>();
	
	public BackedResultGeneralFieldIndex() {
	}
	
	public BackedResultGeneralFieldIndex(String field, ObjectAccessor<T> fieldAccessor) {
		super();
		this.field = field;
		this.fieldAccessor = fieldAccessor;
	}

	
	
	public ReadWriteProtectedList<T> matchBacked(Object val) {
		return val == null ? null : index.get(val);
	}

	public ReadWriteProtectedList<T> removeBacked(Object val) {
		if (val == null){
			return null;
		}
		ReadWriteProtectedList<T> list = index.get(val);
		if (list == null){
			return null;
		}
		WriteLock writeLock = list.getWriteLock();
		writeLock.lock();
		try{
			index.remove(val);
			return list;
		}finally{
			writeLock.unlock();
		}
	}

	public IndexEventListener addEventListener(IndexEventListener listener) {
		return listeners.add(listener) ? listener : null;
	}

	public T insert(T o) {
		Object val = fieldAccessor.resolveField(field, o);
		return insert(val, o);
	}

	public boolean isEmpty() {
		return index.isEmpty();
	}

	public boolean isUniqueIndex() {
		return false;
	}

	public List<T> match(Object val) {
		List<T> list = matchBacked(val);
		if (list == null){
			return Collections.EMPTY_LIST;
		}
		return (List<T>) Arrays.asList(list.toArray());
	}

	public T matchSingle(Object val) {
		ReadWriteProtectedList<T> list = matchBacked(val);
		if (list == null){
			return null;
		}
		list.getReadLock().lock();
		try{
			if (!list.unsafeIsEmpty()){
				return list.unsafeGet(0);
			}
		}finally{
			list.getReadLock().unlock();
		}
		return null;
	}

	public List<T> remove(Object val) {
		List<T> list = removeBacked(val);
		if (list == null){
			return Collections.EMPTY_LIST;
		}
		//create a read only list as return value for  event safe dispatching
		list = (List<T>) Arrays.asList(list.toArray());
		if (!listeners.isEmpty()){
			IndexEvent event = new IndexEvent(IndexEvent.REMOVE, list);
			for (IndexEventListener listener : listeners){
				listener.onIndexChanged(event);
			}
		}
		return list;
	}

	public IndexEventListener removeEventListener(IndexEventListener listener) {
		return listeners.remove(listener) ? listener : null;
	}

	public void removeSilent(Object val) {
		index.remove(val);
	}

	public T removeSingle(T o) {
		Object val = fieldAccessor.resolveField(field, o);
		return removeSingle(val, o);
	}

	public void visitAll(IndexedObjectVisitor<T> visitor) {
		for (ReadWriteProtectedList<T> list : index.values()){
			ReadLock readLock = list.getReadLock();
			readLock.lock();
			try{
				for (T t : list){
					visitor.visit(t);
				}
			}finally{
				readLock.unlock();
			}
		}
	}



	public T insert(Object val, T o) {
		
		if (val == null){
			return null;
		}
		ReadWriteProtectedList<T> list = index.get(val);
		ReadWriteProtectedList<T> old = null;
		
		if (list == null){
			list = new ReadWriteProtectedArrayList<T>(1, true);
			old = index.putIfAbsent(val, list);
			if (old != null){
				list = old;
			}
		}
		
		list.add(o);
		
//		WriteLock writeLock = list.getWriteLock();
//		writeLock.lock();
//		try{
//			list.unsafeAdd(o);
//		}finally{
//			writeLock.unlock();
//		}
		
		if (!listeners.isEmpty()){
			IndexEvent event = new IndexEvent(IndexEvent.INSERT, o);
			for (IndexEventListener listener : listeners){
				listener.onIndexChanged(event);
			}
		}
		return o;
	}



	public T removeSingle(Object val, T o) {
		if (val == null){
			return null;
		}
		ReadWriteProtectedList<T> list = null;
		boolean removed = false;

		list = index.get(val);
		if (list == null){
			return null;
		}
		
		WriteLock writeLock = list.getWriteLock();
		writeLock.lock();
		try{
			removed = list.unsafeRemove(o);
			if (removed && list.unsafeIsEmpty()){
				index.remove(val);
			}
		}finally{
			writeLock.unlock();
		}

		if (removed){
			if (!listeners.isEmpty()){
				IndexEvent event = new IndexEvent(IndexEvent.INSERT, o);
				for (IndexEventListener listener : listeners){
					listener.onIndexChanged(event);
				}
			}
			return o;
		}
		return null;
	}

}
