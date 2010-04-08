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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypeInfoUtil {
	
	static Map<Class, Class> systemGenericImplementMap = new HashMap<Class, Class>();
	
	static {
		systemGenericImplementMap.put(List.class, ArrayList.class);
		systemGenericImplementMap.put(Map.class, HashMap.class);
		systemGenericImplementMap.put(Set.class, HashSet.class);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getGeneralGenericImplement(Class<T> type){
		Class<T> rt =  systemGenericImplementMap.get(type);
		return rt == null ? type : rt;
	}
	
	public static  Class<?> loadClass(String name) throws ClassNotFoundException{
		Class p = null;
		try {
			p = Class.forName(name);
		} catch (ClassNotFoundException e) {
		}
		if (p == null){
			p = Thread.currentThread().getContextClassLoader().loadClass(name);
		}
		return p;
	}
	
	public static  Class<?> loadClass(String name, ClassLoader loader) throws ClassNotFoundException{
		Class<?> p = null;
		if (loader != null){
			try {
				p = loader.loadClass(name);
			} catch (ClassNotFoundException e) {
			}
		}
		if (p == null){
			p = loadClass(name);
		}
		return (Class<?>)p;
	}
	
	public static boolean isCollection(Class<?> c){
		return Collection.class.isAssignableFrom(c);
	}
	
	public static boolean isMap(Class<?> c){
		return Map.class.isAssignableFrom(c);
	}
	
	@SuppressWarnings("unchecked")
	public static boolean  isMultiValue(Class type){
		if (
				isCollection(type)
				|| Map.class.isAssignableFrom( type )
				|| type.isArray()
				){
			return true;
		}
		return false;
	}
	
	public static RuntimeException handleThrowable(Throwable t) {
	    if (t instanceof RuntimeException)
	        return (RuntimeException) t;
	    else if (t instanceof Error)
	        throw (Error) t;
	    throw new IllegalStateException("Not unchecked", t);
	}
	
	public static <T> T handleThrowable(Throwable t, Class<? extends T> checkedExceptionType) {
	    if (t instanceof RuntimeException)
	        throw (RuntimeException) t;
	    else if (t instanceof Error)
	        throw (Error) t;
	    else if (checkedExceptionType.isAssignableFrom(t.getClass())) {
	    	return (T)t;
	    }
	    throw new IllegalStateException("Not unchecked", t);
	}
	
	public static Class[] getItemTypes(Class type, Type gtype, ClassLoader loader) {
		if (loader == null){
			loader = Thread.currentThread().getContextClassLoader();
		}
		if (type.isArray()){
			return new Class[]{type.getComponentType()};
		}
		if (gtype != null && gtype instanceof ParameterizedType){
			Type[] ts =  ((ParameterizedType)gtype).getActualTypeArguments();
			try {
				Class[] cs = new Class[ts.length];
				for (int i = 0; i < ts.length; i++){
					if (ts[i] instanceof ParameterizedType){
						ts[i] = ((ParameterizedType)ts[i]).getRawType();
					}
					String tmp = ts[i].toString();
					if (tmp.equals("?")){
						cs[i] = Object.class;
					}else{
						cs[i] = loadClass(tmp.substring(tmp.indexOf(" ")+1), loader);
					}
					
				}
				return  cs;
			} catch (ClassNotFoundException e) {
				return  null;
			}
		}
		return null;
	}
	
	public static Field searchField(Class<?> cls, String fname) {
		Field[] fds = cls.getDeclaredFields();
		for (Field fd : fds){
			if (fd.getName().equals(fname)){
				return fd;
			}
		}
		 // Direct superinterfaces, recursively
        Class[] interfaces = cls.getInterfaces();
        Field rt = null;
        for (int i = 0; i < interfaces.length; i++) {
            Class c = interfaces[i];
            
            if ((rt = searchField(c, fname)) != null) {
                return rt;
            }
        }
        // Direct superclass, recursively
        if (!cls.isInterface()) {
            Class c = cls.getSuperclass();
            if (c != null) {
                if ((rt = searchField(c, fname)) != null) {
                    return rt;
                }
            }
        }
        return null;
		
	}
}
