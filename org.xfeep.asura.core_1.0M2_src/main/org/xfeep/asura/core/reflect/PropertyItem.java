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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.xfeep.asura.core.reflect.TypeInfoUtil.getItemTypes;

public class PropertyItem implements TypeItem {
	
	
	
	protected Class<?> hostType;
	protected String name;
	protected Method getter;
	protected Method setter;
	Class[] itemTypes = nullMemberTypes;
	
	
	public PropertyItem() {
		super();
	}

	void correntAccess() {
		if (setter != null){
			setter.setAccessible(true);
		}
		if (getter != null){
			getter.setAccessible(true);
		}
	}

	public PropertyItem(Class<?> hostType, String name, Method getter, Method setter) {
		super();
		this.name = name;
		this.getter = getter;
		this.setter = setter;
		this.hostType = hostType;
		correntAccess();
	}
	
	

	public PropertyItem(Class<?> hostType, String field) throws SecurityException, NoSuchMethodException{
		if (hostType == null){
			return;
		}
		this.hostType = hostType;
		this.name = field;
		String getterName = "get" + field.substring(0, 1).toUpperCase();
		String setterName = "set"+ field.substring(0, 1).toUpperCase();
		if (field.length() > 1){
			getterName += field.substring(1);
			setterName += field.substring(1);
		}
		getter = hostType.getMethod(getterName);
		setter = hostType.getMethod(setterName, getter.getReturnType());
		correntAccess();
	}
	

	public Annotation[] getAnnotations(){
		return getter != null ? getter.getAnnotations() : setter.getAnnotations();
	}
	

	public <A extends Annotation> A getAnnotation(Class<A> annotationType){
		return getter != null ? getter.getAnnotation(annotationType) : setter.getAnnotation(annotationType);
	}
	
	public Class getType(){
		return getter != null ? getter.getReturnType() : setter.getParameterTypes()[0];
	}
	

	public Object getValue(Object obj) throws  InvocationTargetException, IllegalAccessException{
		if (isWriteOnly()){
			throw new IllegalAccessError("write-only property can not be read");
		}
		return getter.invoke(obj);
	}
	

	public void setValue(Object target, Object v) throws InvocationTargetException, IllegalAccessException{
		if (isReadOnly()){
			throw new IllegalAccessError("read-only property can not be write");
		}
		setter.invoke(target, new Object[]{v});
	}
	
	public static boolean isPropertyMember(Method m){
		String n = m.getName();
		return ( n.startsWith("get") || ((n.startsWith("is") && ( m.getReturnType() == Boolean.TYPE || m.getReturnType() == Boolean.class ) ) ) ) 
		&& m.getDeclaringClass() != Object.class && ( (m.getModifiers() & Modifier.STATIC) == 0) && (m.getParameterTypes().length == 0);
	}

	public Class[] getMemberTypes(){
		if (itemTypes != nullMemberTypes){
			return itemTypes;
		}
		if (getter != null){
			return itemTypes = getItemTypes(getter.getReturnType(), getter.getGenericReturnType(), this.hostType.getClassLoader());
		}
		return itemTypes = getItemTypes(setter.getParameterTypes()[0], setter.getGenericParameterTypes()[0],  this.hostType.getClassLoader());
	}
 	
	public String getName(){
		return name;
	}



	public Class getDeclaringClass() {
		return getter.getDeclaringClass();
	}

	public boolean isReadOnly() {
		return setter == null;
	}

	public boolean isWriteOnly() {
		return getter == null;
	}
}
