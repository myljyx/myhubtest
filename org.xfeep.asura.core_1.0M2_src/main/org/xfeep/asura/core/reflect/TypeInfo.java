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
import java.lang.reflect.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TypeInfo {
	
	
	protected Class  type;
	
	protected Map<String, TypeItem> itemMap = new HashMap<String, TypeItem>();
	
	
	public Annotation[] getAnnotations(){
		return type.getAnnotations();
	}
	public <T extends Annotation> T getAnnotation(Class<T> annotationType){
		return (T) type.getAnnotation(annotationType);
	}
	
	public TypeInfo(Class  type){
		this.type = type;
	}
	
	public Collection<TypeItem> getItems(){
		return itemMap.values();
	}
	
	public TypeItem getWritableItem(String item){
		TypeItem ti = getItem(item);
		if (ti == null){
			throw new IllegalArgumentException("can not find property or method for " + item + " in component " + type.getName());
		}
		if (ti.isReadOnly()){
			throw new IllegalArgumentException(item + " is not a setter or a field " + " in component " + type.getName());
		}
		return ti;
	}
	
	public void buildPropertyIndex(){
			for (Method m : type.getMethods()){
				if (PropertyItem.isPropertyMember(m)){
					String n =  m.getName();
					if (n.startsWith("is")){
						n = "get" + n.substring(2);
					}
					try {
						Method setter = null;
						try{
							setter = type.getMethod("s" +n.substring(1), m.getReturnType());
						}catch(NoSuchMethodException e){ //is read only property
						}
						String field = n.substring("set".length());
						if (field.length() > 1){
							field = field.substring(0, 1).toLowerCase() + field.substring(1);
						}else{
							field = field.substring(0, 1).toLowerCase();
						}
						itemMap.put(field, new PropertyItem(this.type, field, m, setter));
					} catch (Throwable e) {
						
					} 
				}
			}
			for (Field f : type.getFields()){
				if (!Modifier.isStatic(f.getModifiers()) )
					itemMap.put(f.getName(), new FieldItem(f));
			}

	}
	
	public void addDeclaredFiledIndex(Class<?> clz){
		for (Field f : clz.getDeclaredFields()){
			itemMap.put(f.getName(), new FieldItem(f));
		}
	}
	
	public  void addAllFieldIndex(Class<?> clz){
		addDeclaredFiledIndex(clz);
		// Direct superinterfaces, recursively
        Class[] interfaces = clz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            addDeclaredFiledIndex(interfaces[i]);
        }
        // Direct superclass, recursively
        if (!clz.isInterface()) {
            Class c = clz.getSuperclass();
            if (c != null) {
            	addAllFieldIndex(c);
            }
        }
		
	}
	
	public  void addAllFieldIndex(){
		addAllFieldIndex(this.type);
	}
	
	public  TypeItem getItem(String name){
		return itemMap.get(name);
	}
	
	public Class getType() {
		return type;
	}

	public void setType(Class type) {
		this.type = type;
	}


}
