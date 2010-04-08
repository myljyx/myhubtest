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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public interface UnSafeList<E> {

	public  boolean unsafeAdd(E e);

	public  void unsafeAdd(int index, E element);

	public  boolean unsafeaddAll(Collection<? extends E> c);

	public  boolean unsafeAddAll(int index, Collection<? extends E> c);

	public  void unsafeClear();

	public  Object unsafeClone();

	public  boolean unsafeContains(Object o);

	public  void unsafeEnsureCapacity(int minCapacity);

	public  E unsafeGet(int index);

	public  int unsafeIndexOf(Object o);

	public  boolean unsafeIsEmpty();

	public  int unsafeLastIndexOf(Object o);

	public  E unsafeRemove(int index);

	public  boolean unsafeRemove(Object o);

	public  E unsafeSet(int index, E element);

	public  int unsafeSize();

	public  Object[] unsafeToArray();

	public  <T> T[] unsafeToArray(T[] a);

	public  void unsafeTrimToSize();

	public  boolean unsafeEquals(Object o);

	public  int unsafeHashCode();

	public  Iterator<E> unsafeIterator();

	public  ListIterator<E> unsafeListIterator();

	public  ListIterator<E> unsafeListIterator(int index);

	public  List<E> unsafeSubList(int fromIndex, int toIndex);

	public  boolean unsafeContainsAll(Collection<?> c);

	public  boolean unsafeRemoveAll(Collection<?> c);

	public  boolean unsafeRetainAll(Collection<?> c);

	public  String unsafeToString();

}