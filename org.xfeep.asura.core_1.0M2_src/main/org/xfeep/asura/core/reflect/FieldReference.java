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

import java.lang.reflect.Field;
import static org.xfeep.asura.core.reflect.TypeInfoUtil.searchField;

public class FieldReference<O, F> {
	
	protected O obj;
	protected Field field;
	
	public FieldReference(O obj, String field) throws NoSuchFieldException{
		this.obj = obj;
		Class cls = obj.getClass();
		this.field = searchField(cls, field);
		if (this.field == null){
			throw new NoSuchFieldException("Thare's no such field : " + field + " in class " + cls.getName());
		}
	}

	public FieldReference(O obj, Class cls, String field) throws NoSuchFieldException{
		this.obj = obj;
		this.field = searchField(cls, field);
		if (this.field == null){
			throw new NoSuchFieldException("There's no such field : " + field + " in class " + cls.getName());
		}
	}
	
	public FieldReference(O obj, Field f) {
		super();
		this.obj = obj;
		this.field = f;
	}
	
	public void set(F newValue){
//		System.out.println(newValue);
		boolean a = this.field.isAccessible();
		if (!a){
			field.setAccessible(!a);
		}
		try {
			this.field.set(obj, newValue);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if (!a){
			field.setAccessible(a);
		}
	}
	
	public F get(){
		boolean a = this.field.isAccessible();
		if (!a){
			field.setAccessible(!a);
		}
		try {
			return (F)this.field.get(obj);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}finally{
			if (!a){
				field.setAccessible(a);
			}
		}
		return null;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public O getObj() {
		return obj;
	}

	public void setObj(O obj) {
		this.obj = obj;
	}
	
}
