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

public class MultiValuedFieldBackedResultFieldIndex<T> extends BackedResultGeneralFieldIndex<T> {

	public MultiValuedFieldBackedResultFieldIndex() {
		super();
	}

	public MultiValuedFieldBackedResultFieldIndex(String field,
			ObjectAccessor<T> fieldAccessor) {
		super(field, fieldAccessor);
	}
	
	public T insert(T o) {
		Object[] keys = (Object[])fieldAccessor.resolveField(field, o);
		T rt = null;
		for (Object key : keys){
			if ( super.insert(key, o) != null && rt == null){
				rt = o;
			}
		}
		return rt;
	}
	
	public T removeSingle(T o) {
		Object[] keys = (Object[])fieldAccessor.resolveField(field, o);
		T rt = null;
		for (Object key : keys){
			if ( super.removeSingle(key, o) != null && rt == null){
				rt = o;
			}
		}
		return rt;
	}
	

}
