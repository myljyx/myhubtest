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

public class InjectPropertyItem implements TypeItem {
	
	protected String valueExpression;
	protected TypeItem delagate;
	
	public InjectPropertyItem() {
	}
	
	
	public InjectPropertyItem(String valueExpression, TypeItem delagate) {
		super();
		this.valueExpression = valueExpression;
		this.delagate = delagate;
	}



	public String getValueExpression() {
		return valueExpression;
	}
	public void setValueExpression(String valueExpression) {
		this.valueExpression = valueExpression;
	}
	
	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		return delagate.getAnnotation(annotationType);
	}
	public Annotation[] getAnnotations() {
		return delagate.getAnnotations();
	}
	public Class getDeclaringClass() {
		return delagate.getDeclaringClass();
	}
	public Class[] getMemberTypes() {
		return delagate.getMemberTypes();
	}
	public String getName() {
		return delagate.getName();
	}
	public Class getType() {
		return delagate.getType();
	}
	public Object getValue(Object target) throws InvocationTargetException,
			IllegalAccessException {
		return delagate.getValue(target);
	}
	public boolean isReadOnly() {
		return delagate.isReadOnly();
	}
	public boolean isWriteOnly() {
		return delagate.isWriteOnly();
	}
	public void setValue(Object target, Object v)
			throws InvocationTargetException, IllegalAccessException {
		delagate.setValue(target, v);
	}
	
	
	
	
}
