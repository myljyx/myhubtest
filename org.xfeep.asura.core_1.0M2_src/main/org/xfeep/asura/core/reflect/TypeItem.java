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
package org.xfeep.asura.core.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

public interface TypeItem {

	public static Class[] nullMemberTypes = {};
	
	public   Class[] getMemberTypes();
	
	public   Annotation[] getAnnotations();

	public   <A extends Annotation> A getAnnotation(Class<A> annotationType);

	public   Class getType();

	public   Object getValue(Object target) throws InvocationTargetException, IllegalAccessException;

	public   void setValue(Object target, Object v) throws  InvocationTargetException, IllegalAccessException;



	public   String getName();
	
	public boolean isReadOnly();
	
	public boolean isWriteOnly();
	
	public   Class getDeclaringClass();
}