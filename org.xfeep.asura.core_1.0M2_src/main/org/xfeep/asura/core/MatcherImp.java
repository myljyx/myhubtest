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

//import java.lang.reflect.Method;
//import java.security.AccessController;
//import java.security.PrivilegedActionException;
//import java.security.PrivilegedExceptionAction;
//import java.security.ProtectionDomain;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import org.xfeep.asura.core.annotation.Ref;
//
//import javassist.CannotCompileException;
//import javassist.ClassPool;
//import javassist.CtClass;
//import javassist.CtNewConstructor;
//import javassist.CtNewMethod;
//import javassist.LoaderClassPath;
//import javassist.NotFoundException;

@Deprecated
public class MatcherImp {
//
//implements Matcher {
//
//	public final static String ON_DEMAND_SEPRATOR = "/*END_ONDEMAND_REQUIREMENT*/";
//	
//	private static java.lang.reflect.Method defineClass1, defineClass2;
//
//	 static {
//		try {
//			AccessController.doPrivileged(new PrivilegedExceptionAction() {
//				public Object run() throws Exception {
//					Class cl = Class.forName("java.lang.ClassLoader");
//					defineClass1 = cl.getDeclaredMethod("defineClass", new Class[] {String.class, byte[].class, int.class, int.class});
//
//					defineClass2 = cl.getDeclaredMethod("defineClass", new Class[] {String.class, byte[].class, int.class, int.class, ProtectionDomain.class});
//					return null;
//				}
//			});
//		} catch (PrivilegedActionException pae) {
//			throw new RuntimeException("cannot initialize ", pae.getException());
//		}
//	}
//    public static Class toClass(CtClass ct, ClassLoader loader, ProtectionDomain domain) throws CannotCompileException {
//		try {
//			ct.stopPruning(true);
//			byte[] b = ct.toBytecode();
//			ct.defrost();
//			Method method;
//			Object[] args;
//			if (domain == null) {
//				method = defineClass1;
//				args = new Object[] {ct.getName(), b, new Integer(0), new Integer(b.length)};
//			} else {
//				method = defineClass2;
//				args = new Object[] {ct.getName(), b, new Integer(0), new Integer(b.length), domain};
//			}
//			
////			FileOutputStream o = new FileOutputStream("e:/ff.class");
////			o.write(b);
////			o.close();
//			return toClass2(method, loader, args);
//		} catch (RuntimeException e) {
//			throw e;
//		} catch (java.lang.reflect.InvocationTargetException e) {
//			throw new CannotCompileException(e.getTargetException());
//		} catch (Exception e) {
//			throw new CannotCompileException(e);
//		}
//	}
//
//	private static synchronized Class toClass2(Method method, ClassLoader loader, Object[] args) throws Exception {
//		method.setAccessible(true);
//		Class clazz = (Class) method.invoke(loader, args);
//		method.setAccessible(false);
//		return clazz;
//	}
//	
////	static ClassPool classPool;
//	static AtomicInteger innerMatcherImpCount = new AtomicInteger(0);
////	static {
////		classPool = new ClassPool(true);
////		classPool.appendClassPath(new LoaderClassPath());
////		classPool.importPackage("org.xfeep.asura.core");
////	}
//	InnerMatcher innerMatcher;
//	Map<String, String> ondemandConfigRequirement;
//	
//	public final static Matcher FACTORY_SAMPLE_INSTANCE_MATCHER = new Matcher() {
//		public boolean match(ComponentInstance instance) {
//			if (instance instanceof FactoryComponentInstance) {
//				FactoryComponentInstance fci = (FactoryComponentInstance) instance;
//				return ((FactoryComponent)fci.component).factorySampleInstance == instance;
//			}
//			return false;
//		}
//
//		public Map<String, String> getOndemandConfigRequirement() {
//			return null;
//		}
//
//	};
//	
//	public static Matcher getMatcher(String matcher) {
//		if (Ref.FACTORY_SAMPLE_INSTANCE_MATCHER.equals(matcher)){
//			return FACTORY_SAMPLE_INSTANCE_MATCHER;
//		}
//		return new MatcherImp(matcher);
//	}
//	
//	private MatcherImp(String matcher){
//		int dspos = matcher.indexOf(ON_DEMAND_SEPRATOR);
//		if (dspos >-1){
//			String ondemandDef = matcher.substring(0, dspos);
//			matcher = matcher.substring(dspos + ON_DEMAND_SEPRATOR.length());
//			ondemandConfigRequirement = new HashMap<String, String>();
//			for (String item : ondemandDef.split("\\;")){
//				int ep = item.indexOf('=');
//				ondemandConfigRequirement.put(item.substring(0, ep), item.substring(ep+1));
//			}
//		}
//		if (matcher == null || matcher.length() == 0){
//			innerMatcher = new InnerMatcher(){
//				public boolean match(String name, String imp, Object c) {
//					return true;
//				}
//			};
//			return;
//		}
//		StringBuilder sb = new StringBuilder("public boolean match(String name, String imp, Object c){");
//		sb.append(matcher);
//		sb.append("}");
//		ClassPool classPool;
//		classPool = new ClassPool(true);
//		classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
//		classPool.importPackage("org.xfeep.asura.core");
//		String clz = InnerMatcher.class.getName()+"Imp"+innerMatcherImpCount.incrementAndGet();
//		try {
//			CtClass ct = classPool.makeClass(clz);
//			ct.addInterface(classPool.get(InnerMatcher.class.getName()));
//			ct.addConstructor(CtNewConstructor.defaultConstructor(ct));
//			ct.addMethod(CtNewMethod.make(sb.toString(), ct));
//			Class clazz = toClass(ct, this.getClass().getClassLoader(), this.getClass().getProtectionDomain());
//			innerMatcher = (InnerMatcher)clazz.newInstance();
//		} catch (RuntimeException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (CannotCompileException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//
//	public boolean match(ComponentInstance instance) {
////		if (instance instanceof OnDemandComponentInstance) {
////			OnDemandComponentInstance onDemandComponentInstance = (OnDemandComponentInstance) instance;
////			if (onDemandComponentInstance == ( (OnDemandComponent)onDemandComponentInstance.component).matchAllSampleInstance){
////				return true;
////			}
////		}
//		String name = instance.component.definition.name;
//		String imp = instance.component.definition.implement.getName();
//		Object c = instance.getConfig();
//		return innerMatcher.match(name, imp, c);
//	}
//
//	public Map<String, String> getOndemandConfigRequirement() {
//		return ondemandConfigRequirement;
//	}
//

}
