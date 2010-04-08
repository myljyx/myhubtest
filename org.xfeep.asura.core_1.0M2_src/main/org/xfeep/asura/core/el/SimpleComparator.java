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
package org.xfeep.asura.core.el;

public class SimpleComparator {

	public static boolean compareEquals(Object a, Object b) {
		if (a == null || b == null){
			return b == a;
		}
		if (a == b){
			return true;
		}
		if ( isSimple(a.getClass()) && isSimple(b.getClass()) ){
			return a.toString().equals(b.toString());
		}
		return a.equals(b);
	}
	
	public static boolean isSimple(Class clz){
		return clz == Integer.class || clz == String.class;
	}
	
	
}
