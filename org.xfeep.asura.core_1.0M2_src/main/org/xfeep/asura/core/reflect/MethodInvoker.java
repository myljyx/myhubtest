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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvoker {
	
	protected Object obj;
	protected Method method;
	
	public MethodInvoker(Object obj, Method method) {
		super();
		this.obj = obj;
		this.method = method;
	}
	
	public MethodInvoker(Object obj, String method, Class...parameterTypes) throws SecurityException, NoSuchMethodException {
		super();
		this.obj = obj;
		this.method = obj.getClass().getDeclaredMethod(method, parameterTypes);
	}
	
	public Object invoke(Object...args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		method.setAccessible(true);
		return method.invoke(obj, args);
	}
	
	
}
