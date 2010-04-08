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
package org.xfeep.asura.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

import org.xfeep.asura.core.reflect.TypeItem;

public class DependOnVirtualFieldItem implements TypeItem {

	Annotation annotation;
	Class<?> declaringClass;
	
	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		return (A) (annotation.getClass().isAssignableFrom(annotationType) ? annotation : null);
	}

	public Annotation[] getAnnotations() {
		return new Annotation[] {annotation};
	}

	public Class getDeclaringClass() {
		return declaringClass;
	}

	public Class[] getMemberTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Class getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getValue(Object target) throws InvocationTargetException,
			IllegalAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isWriteOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setValue(Object target, Object v)
			throws InvocationTargetException, IllegalAccessException {
		// TODO Auto-generated method stub

	}

}
