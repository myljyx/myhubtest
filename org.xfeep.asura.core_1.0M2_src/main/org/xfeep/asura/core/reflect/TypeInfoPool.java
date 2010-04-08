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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;


import static org.xfeep.asura.core.reflect.TypeInfoUtil.*;

public class TypeInfoPool {
	
	static TypeInfoPool defaultPool;
	static {
		defaultPool = new TypeInfoPool();
	}
	
	
	protected ConcurrentHashMap<Class, Future<TypeInfo> > caches =  new ConcurrentHashMap<Class, Future<TypeInfo> >();
	
	public static TypeInfoPool getDefault(){
		return defaultPool;
	}
	
	public void clear() {
		caches.clear();
	}
	

	
	public   TypeInfo  getTypeInfo(final Class c){
		//use a concurrent cache implementation suggested by Java Concurrency in Practice
		//5.6. Building an Efficient, Scalable Result Cache
		while(true) {
			Future<TypeInfo> f = caches.get(c);
	        if (f == null) {
	            Callable<TypeInfo> builder = new Callable<TypeInfo>() {
	                public TypeInfo call()  {
	                	TypeInfo rt = new TypeInfo(c);
	                	rt.buildPropertyIndex();
	                	return rt;
	                }
	            };
	            FutureTask<TypeInfo> ft = new FutureTask<TypeInfo>(builder);
	            f = caches.putIfAbsent(c, ft);
	            if (f == null) { 
	            	f = ft; ft.run(); 
	            }
	        }
	        try {
	            return f.get();
	        } catch (CancellationException e) {
	            caches.remove(c, f);
	        } catch (ExecutionException e) {
	            throw handleThrowable(e.getCause());
	        } catch (InterruptedException e) {
			}
		}
	}
	
	
	
	public Object evalItemValue(Object v, String field) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		if (v == null){
			return null;
		}
		TypeInfo ci =  getTypeInfo(v.getClass());
		TypeItem m = ci.getItem(field);
		if (m == null){
			if ( isMap(v.getClass()) ){
				return ( (Map) v).get(field);
			}
			return null;
		}else{
			return m.getValue(v);
		}
		
	}
	
	public Object assignItemValue(Object v, String field, Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		if (v == null){
			throw new IllegalArgumentException("null value");
		}
		Object rt = null;
		TypeInfo ci =  getTypeInfo(v.getClass());
		TypeItem m = ci.getItem(field);
		if (m == null){
			if ( isMap(v.getClass()) ){
				rt = ( (Map) v).put(field, value);
			}else 
				throw new IllegalArgumentException("null value at " + field);
		}else{
			rt = m.getValue(v);
			m.setValue(v, value);
		}
		return rt;
	}
	
	public Class resolveType(Class root, String dotExp){
		if (dotExp == null || dotExp.length() == 0){
			return root;
		}
		String pn = dotExp;
		int dot = dotExp.indexOf(".");
		while (dot > 0){
			pn = dotExp.substring(0, dot);
			TypeItem m = getTypeInfo(root).getItem(pn);
			if (m == null){
				return null;
			}
			if (isCollection(m.getType())) {
				root = m.getMemberTypes()[m.getMemberTypes().length-1];
			}else {
				root = m.getType();
			}
			dotExp = dotExp.substring(dot+1);
			dot = dotExp.indexOf(".");
		}
		pn = dotExp;
		TypeItem pm =  getTypeInfo(root).getItem(pn);
		return pm == null ? null : pm.getType();
	}
	
	public   TypeItem resolveItem(Class root, String dotExp){
		
		if (dotExp == null || dotExp.length() == 0){
			return null;
		}
		String pn = dotExp;
		int dot = dotExp.indexOf(".");
		while (dot > 0){
			pn = dotExp.substring(0, dot);
			TypeItem m = getTypeInfo(root).getItem(pn);
			if (m == null){
				throw new IllegalArgumentException("no such property : " + pn + " at " + root.getName());
			}
			root = m.getType();
			dotExp = dotExp.substring(dot+1);
			dot = dotExp.indexOf(".");
		}
		pn = dotExp;
		return getTypeInfo(root).getItem(pn);
	}
	
	
	public Class evalType(Object v, String dotExp) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (v == null){
			return null;
		}
		TypeInfo ci =  getTypeInfo(v.getClass());
		String pn = dotExp;
		int dot = dotExp.indexOf(".");
		while (dot > 0){
			pn = dotExp.substring(0, dot);
			v = evalItemValue(v, pn);
			if (v == null){
				return null;
			}
			dotExp = dotExp.substring(dot+1);
			dot = dotExp.indexOf(".");
			ci = getTypeInfo(v.getClass());
		}
		if (v == null){
			return resolveType(ci.getType(), dotExp);
		}
		pn = dotExp;
		v = evalItemValue(v, pn);
		return v == null ? resolveType(ci.getType(), dotExp) : v.getClass();
	}
	
	public  Object resolvePropertityValue(Object v, String dotExp) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		if (v == null){
			return null;
		}
		String pn = dotExp;
		int dot = dotExp.indexOf(".");
		while (dot > 0){
			pn = dotExp.substring(0, dot);
			v = evalItemValue(v, pn);
			if (v == null){
				return null;
			}
			dotExp = dotExp.substring(dot+1);
			dot = dotExp.indexOf(".");
		}
		if (v == null){
			return null;
		}
		getTypeInfo(v.getClass());
		pn = dotExp;
		return evalItemValue(v, pn);
	}
	
	public  Object assignExpressionValue(Object v, String dotExp, Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		String pn = dotExp;
		int dot = dotExp.indexOf(".");
		while (dot > 0){
			getTypeInfo(v.getClass());
			pn = dotExp.substring(0, dot);
			v = evalItemValue(v, pn);
			if (v == null){
				throw new IllegalArgumentException("meet null value in exp : " + dotExp + " at " + pn);
			}
			dotExp = dotExp.substring(dot+1);
			dot = dotExp.indexOf(".");
		}
		if (v == null){
			throw new IllegalArgumentException("meet null value in exp : " + dotExp + " at " + pn);
		}
		getTypeInfo(v.getClass());
		pn = dotExp;
		return assignItemValue(v, pn, value);
	}

	
	public  Object updatePropertity(Object v, String dotExp, Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		TypeInfo ci =  null;
		String pn = dotExp;
		int dot = dotExp.indexOf(".");
		while (dot > 0){
			ci =  getTypeInfo(v.getClass());
			pn = dotExp.substring(0, dot);
			TypeItem m = ci.getItem(pn);
			if (m == null){
				throw new IllegalArgumentException("no such item in exp : " + dotExp + " at " + pn);
			}
			v = m.getValue(v);
			if (v == null){
				throw new IllegalArgumentException("meet null value in exp : " + dotExp + " at " + pn);
			}
			dotExp = dotExp.substring(dot+1);
			dot = dotExp.indexOf(".");
		}
		if (v == null){
			throw new IllegalArgumentException("meet null value in exp : " + dotExp + " at " + pn);
		}
		ci = getTypeInfo(v.getClass());
		pn = dotExp;
		return assignItemValue(v, pn, value);
	}

	
}
