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

import java.util.List;

/**
 * A FieldIndex is an index manager used  to create, update and query on an index.
 * There two kinds of IFieldIndex, one manage unique values, another manage general values. 
 * @author zhang yuexiang
 *
 * @param <T>
 */
public interface FieldIndex<T> {

	/**
	 * Find the first object which the field value matches 
	 * @param val
	 * @return the first matched object, or null if there 's no object matches.
	 */
	public  T  matchSingle(Object val);
	
	/**
	 * Find all objects which the field value matches 
	 * @param val
	 * @return all matched objects, or empty list if there's no object matches.
	 */
	public List<T> match(Object val);

	/**
	 * Insert a new object, if the field value is null, this object will be ignored.
	 * @param o
	 * @return the same to o if insert successfully; null otherwise.
	 */
	public  T insert(T o);
	
	
	/**
	 * Insert a new object with the field value val
	 * @param key
	 * @param o
	 * @return
	 */
	public T insert(Object val, T o);
	
	/**
	 * Remove all objects witch the field value matches.
	 * @param val
	 * @return all matched and removed objects.
	 */
	public  List<T> remove(Object val);
	
	/**
	 * The only different from remove(Object val) is there 's no result returned.
	 * For some implements it is more fast than remove(Object val) because the conversion 
	 * of inner data structure into List or creation of a temporary list is omitted.
	 * @param val
	 */
	public void removeSilent(Object val);
	
	
	/**
	 * remove single object
	 * @param o
	 * @return  
	 */
	public  T removeSingle(T o);
	
	
	/**
	 * remove single object with the field value <code>val</code>
	 * @param val
	 * @param o
	 * @return
	 */
	public T removeSingle(Object val, T o);
	
	/**
	 * Test whether this index just manages unique values.
	 * @return true if this index just manages unique values; false otherwise.
	 */
	public boolean isUniqueIndex();
	
	public boolean isEmpty();
	
	public void visitAll(IndexedObjectVisitor<T> visitor);
	
	public IndexEventListener addEventListener(IndexEventListener listener);
	
	public IndexEventListener removeEventListener(IndexEventListener listener);
	
	
}