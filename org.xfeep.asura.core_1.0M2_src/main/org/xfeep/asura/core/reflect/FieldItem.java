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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import static org.xfeep.asura.core.reflect.TypeInfoUtil.searchField;

public class FieldItem implements TypeItem {

	protected Field field = null;
	protected Class[] memberTypes = nullMemberTypes;
	protected Class<?> hostType;
	
	public String getName(){
		return field.getName();
	}
	
	public FieldItem(Field field){
		this.field = field;
		this.hostType = field.getDeclaringClass();
	}
	
	public FieldItem(Class<?> host, String field) throws NoSuchFieldException{
		hostType = host;
		this.field  = searchField(host, field);
		if (this.field == null){
			throw new NoSuchFieldException(field + " in type : " + host);
		}
	}
	
	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		return field.getAnnotation(annotationType);
	}

	public Annotation[] getAnnotations() {
		return field.getAnnotations();
	}

	public Class[] getMemberTypes() {
		
		if (memberTypes != nullMemberTypes){
			return memberTypes;
		}
		
		return TypeInfoUtil.getItemTypes(field.getType(), field.getGenericType(), this.hostType.getClassLoader());
	}

	public Class getType() {
		return field.getType();
	}

	public Object getValue(Object obj) throws  InvocationTargetException {
		boolean a = this.field.isAccessible();
		if (!a){
			field.setAccessible(!a);
		}
		try {
			return this.field.get(obj);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}finally{
			if (!a){
				field.setAccessible(a);
			}
		}
		return null;
	}

	public void setValue(Object target, Object v) throws  InvocationTargetException {
		boolean a = this.field.isAccessible();
		if (!a){
			field.setAccessible(!a);
		}
		try {
			 this.field.set(target, v);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}finally{
			if (!a){
				field.setAccessible(a);
			}
		}
		
	}
	
	public Class getDeclaringClass(){
		return field.getDeclaringClass();
	}

	public boolean isReadOnly() {
		return false;
	}

	public boolean isWriteOnly() {
		return false;
	}

}
