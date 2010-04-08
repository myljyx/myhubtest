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
package org.xfeep.asura.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class ReadWriteProtectedArrayList<E> extends ArrayList<E> implements ReadWriteProtectedList<E>, UnSafeList<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	ReadLock readLock;
	WriteLock writeLock;
	ReentrantReadWriteLock reentrantReadWriteLock;
	
	public ReadWriteProtectedArrayList() {
		this(10, false);
	}

	
	public ReadWriteProtectedArrayList(Collection<? extends E> c, boolean fair) {
		super(c);
		reentrantReadWriteLock = new ReentrantReadWriteLock(fair);
		readLock = reentrantReadWriteLock.readLock();
		writeLock= reentrantReadWriteLock.writeLock();
	}
	
	public ReadWriteProtectedArrayList(Collection<? extends E> c) {
		this(c, false);
	}

	public ReadWriteProtectedArrayList(int initialCapacity, boolean fair) {
		super(initialCapacity);
		reentrantReadWriteLock = new ReentrantReadWriteLock(fair);
		readLock = reentrantReadWriteLock.readLock();
		writeLock= reentrantReadWriteLock.writeLock();
	}

	public ReadWriteProtectedArrayList(int initialCapacity){
		this(initialCapacity, false);
	}

	public ReadLock getReadLock() {
		return readLock;
	}

	public WriteLock getWriteLock() {
		return writeLock;
	}


	@Override
	public boolean add(E e) {
		writeLock.lock();
		try{
			return super.add(e);
		}finally{
			writeLock.unlock();
		}
		
	}


	@Override
	public void add(int index, E element) {
		writeLock.lock();
		try{
			super.add(index, element);
		}finally{
			writeLock.unlock();
		}
	}


	@Override
	public boolean addAll(Collection<? extends E> c) {
		writeLock.lock();
		try{
			return super.addAll(c);
		}finally{
			writeLock.unlock();
		}
	}


	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		writeLock.lock();
		try{
			return super.addAll(index, c);
		}finally{
			writeLock.unlock();
		}	
	}


	@Override
	public void clear() {
		writeLock.lock();
		try{
			super.clear();
		}finally{
			writeLock.unlock();
		}	
		
	}


	@Override
	public Object clone() {
		readLock.lock();
		try{
			return super.clone();
		}finally{
			readLock.unlock();
		}	
	}


	@Override
	public boolean contains(Object o) {
		readLock.lock();
		try{
			return super.contains(o);
		}finally{
			readLock.unlock();
		}	
		
	}


	@Override
	public void ensureCapacity(int minCapacity) {
		if (reentrantReadWriteLock.getWriteHoldCount() == 0){
			throw new IllegalStateException("Unsafe invoke, please call writeLock.lock() first");
		}
		super.ensureCapacity(minCapacity);
	}


	@Override
	public E get(int index) {
		readLock.lock();
		try{
			return super.get(index);
		}finally{
			readLock.unlock();
		}	
	}


	@Override
	public int indexOf(Object o) {
		readLock.lock();
		try{
			return super.indexOf(o);
		}finally{
			readLock.unlock();
		}	
		
	}


	@Override
	public boolean isEmpty() {
		readLock.lock();
		try{
			return super.isEmpty();
		}finally{
			readLock.unlock();
		}	
	}


	@Override
	public int lastIndexOf(Object o) {
		readLock.lock();
		try{
			return super.lastIndexOf(o);
		}finally{
			readLock.unlock();
		}	
	}


	@Override
	public E remove(int index) {
		writeLock.lock();
		try{
			return super.remove(index);
		}finally{
			writeLock.unlock();
		}	
	}


	@Override
	public boolean remove(Object o) {
		writeLock.lock();
		try{
			return super.remove(o);
		}finally{
			writeLock.unlock();
		}	
	}


//	@Override
//	protected void removeRange(int fromIndex, int toIndex) {
//		super.removeRange(fromIndex, toIndex);
//	}


	@Override
	public E set(int index, E element) {
		writeLock.lock();
		try{
			return super.set(index, element);
		}finally{
			writeLock.unlock();
		}	
	}


	@Override
	public int size() {
		readLock.lock();
		try{
			return super.size();
		}finally{
			readLock.unlock();
		}
	}


	@Override
	public Object[] toArray() {
		readLock.lock();
		try{
			return super.toArray();
		}finally{
			readLock.unlock();
		}
	}


	@Override
	public <T> T[] toArray(T[] a) {
		readLock.lock();
		try{
			return super.toArray(a);
		}finally{
			readLock.unlock();
		}
	}


	@Override
	public void trimToSize() {
		writeLock.lock();
		try{
			super.trimToSize();
		}finally{
			writeLock.unlock();
		}	
	}


	@Override
	public boolean equals(Object o) {
		readLock.lock();
		try{
			return super.equals(o);
		}finally{
			readLock.unlock();
		}
	}


	@Override
	public int hashCode() {
		readLock.lock();
		try{
			return super.hashCode();
		}finally{
			readLock.unlock();
		}
	}


	@Override
	public Iterator<E> iterator() {
		if (reentrantReadWriteLock.getReadHoldCount() == 0){
			throw new IllegalStateException("Unsafe invoke iterator(), please call readLock.lock() first");
		}
		return super.iterator();
	}


	@Override
	public ListIterator<E> listIterator() {
		if (reentrantReadWriteLock.getWriteHoldCount() == 0){
			throw new IllegalStateException("Unsafe invoke listIterator(), please call writeLock.lock() first");
		}
		return super.listIterator();
	}


	@Override
	public ListIterator<E> listIterator(int index) {
		if (reentrantReadWriteLock.getWriteHoldCount() == 0){
			throw new IllegalStateException("Unsafe invoke listIterator(), please call writeLock.lock() first");
		}
		return super.listIterator(index);
	}


	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		readLock.lock();
		try{
			return super.subList(fromIndex, toIndex);
		}finally{
			readLock.unlock();
		}
	}


	@Override
	public boolean containsAll(Collection<?> c) {
		readLock.lock();
		try{
			return super.containsAll(c);
		}finally{
			readLock.unlock();
		}
	}


	@Override
	public boolean removeAll(Collection<?> c) {
		writeLock.lock();
		try{
			return super.removeAll(c);
		}finally{
			writeLock.unlock();
		}
	}


	@Override
	public boolean retainAll(Collection<?> c) {
		writeLock.lock();
		try{
			return super.retainAll(c);
		}finally{
			writeLock.unlock();
		}
	}


	@Override
	public String toString() {
		readLock.lock();
		try{
			return super.toString();
		}finally{
			readLock.unlock();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	///////////////////Unsafe Operation  Begin ////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	
	public boolean unsafeAdd(E e) {
		return super.add(e);
	}

	

	public void unsafeAdd(int index, E element) {
		super.add(index, element);
	}

	

	public boolean unsafeaddAll(Collection<? extends E> c) {
		return super.addAll(c);
	}

	
	
	public boolean unsafeAddAll(int index, Collection<? extends E> c) {
		return super.addAll(index, c);
	}


	public void unsafeClear() {
		super.clear();
	}

	

	public Object unsafeClone() {
		return super.clone();
	}

	
	
	public boolean unsafeContains(Object o) {
		return super.contains(o);
	}

	
	
	public void unsafeEnsureCapacity(int minCapacity) {
		super.ensureCapacity(minCapacity);
	}


	public E unsafeGet(int index) {
		return super.get(index);
	}

	

	public int unsafeIndexOf(Object o) {
		return super.indexOf(o);
	}

	
	
	public boolean unsafeIsEmpty() {
		return super.isEmpty();
	}

	
	
	public int unsafeLastIndexOf(Object o) {
		return super.lastIndexOf(o);
	}

	
	
	public E unsafeRemove(int index) {
		return super.remove(index);
	}


	public boolean unsafeRemove(Object o) {
		return super.remove(o);
	}

	
//	protected void unsafeRemoveRange(int fromIndex, int toIndex) {
//		super.removeRange(fromIndex, toIndex);
//	}

	
	
	public E unsafeSet(int index, E element) {
		return super.set(index, element);
	}

	
	public int unsafeSize() {
		return super.size();
	}

	

	public Object[] unsafeToArray() {
		return super.toArray();
	}

	

	public <T> T[] unsafeToArray(T[] a) {
		return super.toArray(a);
	}

	
	
	public void unsafeTrimToSize() {
		super.trimToSize();
	}


	public boolean unsafeEquals(Object o) {
		return super.equals(o);
	}

	
	
	public int unsafeHashCode() {
		return super.hashCode();
	}


	public Iterator<E> unsafeIterator() {
		return super.iterator();
	}

	
	
	public ListIterator<E> unsafeListIterator() {
		return super.listIterator();
	}


	public ListIterator<E> unsafeListIterator(int index) {
		return super.listIterator(index);
	}

	
	
	public List<E> unsafeSubList(int fromIndex, int toIndex) {
		return super.subList(fromIndex, toIndex);
	}

	
	
	public boolean unsafeContainsAll(Collection<?> c) {
		return super.containsAll(c);
	}

	
	
	public boolean unsafeRemoveAll(Collection<?> c) {
		return super.removeAll(c);
	}

	
	
	public boolean unsafeRetainAll(Collection<?> c) {
		return super.retainAll(c);
	}

	

	public String unsafeToString() {
		return super.toString();
	}

	
	


}
