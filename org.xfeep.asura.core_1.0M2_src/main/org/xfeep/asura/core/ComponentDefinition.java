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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xfeep.asura.core.annotation.Activate;
import org.xfeep.asura.core.annotation.Config;
import org.xfeep.asura.core.annotation.Property;
import org.xfeep.asura.core.annotation.Deactivate;
import org.xfeep.asura.core.annotation.Destroy;
import org.xfeep.asura.core.annotation.Init;
import org.xfeep.asura.core.annotation.Ref;
import org.xfeep.asura.core.annotation.Service;
import org.xfeep.asura.core.annotation.ConstProperty;
import org.xfeep.asura.core.annotation.ConstPropertySet;
import org.xfeep.asura.core.config.ConfigService;
import org.xfeep.asura.core.match.Matcher;
import org.xfeep.asura.core.match.SmartMatcher;
import org.xfeep.asura.core.reflect.FieldItem;
import org.xfeep.asura.core.reflect.PropertyItem;
import org.xfeep.asura.core.reflect.TypeItem;


public  class ComponentDefinition {

	
	protected String name;
	protected ComponentType type = ComponentType.EAGER;
	protected ScopeType scope = ScopeType.APPLICATION;
	protected Class[] interfaces;
	protected Class implement;
	protected Method init; //init
	protected Method activate;
	protected Method deactivate;
	protected Method destroy; // destroy
	protected String[][] constPropertySet;
	protected Object[] indexKeys;
	
	protected ReferenceDefinition[] references;
//	protected ConfigReferenceDefinintion configReferenceDefinintion;
	protected int dynamicReferencePosition = -1;
	


	public String[][] getConstPropertySet() {
		return constPropertySet;
	}

	public void setConstPropertySet(String[][] constPropertySet) {
		this.constPropertySet = constPropertySet;
	}

	public int getDynamicReferencePosition() {
		return dynamicReferencePosition;
	}

	public void setDynamicReferencePosition(int dynamicReferencePosition) {
		this.dynamicReferencePosition = dynamicReferencePosition;
	}

	public void buildOrderedChainAndInjectedItem(List<ReferenceDefinition> list, Map<String, List<InjectPropertyItem>> injectedPropertiesItemMap) {
		Collections.sort(list);
		for (int i = 0; i < list.size(); i++){
			ReferenceDefinition rd = list.get(i);
			if (dynamicReferencePosition < 0 && rd.matcher != null && rd.matcher.isDynamic() ){
				dynamicReferencePosition = i;
			}
			List<InjectPropertyItem> injects = injectedPropertiesItemMap.get(rd.getName());
			if (injects != null){
				rd.setInjectProperties(injects.toArray(new InjectPropertyItem[injects.size()]));
			}
		}
		if (dynamicReferencePosition == -1){
			dynamicReferencePosition = list.size();
		}
		references = list.toArray(new ReferenceDefinition[list.size()]);
	}

	public Object[] getIndexKeys() {
		return indexKeys;
	}

	public void setIndexKeys(Object[] indexKeys) {
		this.indexKeys = indexKeys;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static void putIntoListValueMap(Object key, Object value, Map<Object, List<Object> > map){
		List<Object> pil = map.get(key);
		if (pil == null){
			map.put(key, pil = new ArrayList());
		}
		pil.add(value);
	}
	
//	public ConfigReferenceDefinintion getConfigReferenceDefinintion() {
//		return configReferenceDefinintion;
//	}
//
//	public void setConfigReferenceDefinintion(
//			ConfigReferenceDefinintion configReferenceDefinintion) {
//		this.configReferenceDefinintion = configReferenceDefinintion;
//	}

	//TODO: should provide hook register to create custom component definition such as  
	//      JEE annotations, for example,
	//@PersistenceContext(type=PersistenceContextType.EXTENDED)
	//EntityManager orderEM;
	public static ComponentDefinition create(Class implement){
		ComponentDefinition cd = new ComponentDefinition();
		cd.implement = implement;
		Service serviceInfo = (Service) implement.getAnnotation(Service.class);
		if (serviceInfo == null){
			return null;
		}
		
		ConstPropertySet staticPropertySet = (ConstPropertySet)implement.getAnnotation(ConstPropertySet.class);
		if (staticPropertySet != null){
			ConstProperty[] svs = staticPropertySet.value();
			cd.constPropertySet = new String[svs.length][2];
			String[][] sps = cd.constPropertySet;
			for (int i = 0; i < svs.length; i++){
				ConstProperty sp = svs[i];
				sps[i] = new String[]{sp.name(), sp.value()};
			}
		}
		
		if (serviceInfo.name().length() > 0){
			cd.name = serviceInfo.name();
		}else{
			cd.name = implement.getName();
		}
		if (serviceInfo.interfaces().length > 0){
			cd.interfaces = serviceInfo.interfaces();
		}else if (serviceInfo.value() != Void.class){
			cd.interfaces = new Class[] {serviceInfo.value()};
		}else {
			ArrayList<Class> itfs = new ArrayList<Class>();
			if (implement.getInterfaces().length > 0){
				for (Class c : implement.getInterfaces()){
					if (c != Serializable.class){
						itfs.add(c);
					}
				}
			}
//			else{
			//we alway make  index of implement avaliable
				itfs.add(implement);
//			}
			Class[] itfstrs = new Class[itfs.size()];
			cd.interfaces = itfs.toArray(itfstrs);
		}
		cd.scope = serviceInfo.scope();
		cd.type = serviceInfo.type();
		
		/**
		 * look config, init, destroy, activate, deactivate method and reference properties or method  from self to parent class recursively
		 * until parent class is null or parent class is a system class
		 */
		Class clz = implement;
		Map<String, List<InjectPropertyItem>> injectedPropertiesItemMap = new HashMap<String, List<InjectPropertyItem>>(0);
//		List<TypeItem> configFields = new ArrayList<TypeItem>();
		List<ReferenceDefinition> references = new ArrayList<ReferenceDefinition>();
		do{
			for (Method m : clz.getDeclaredMethods()){
				if (cd.init == null && m.isAnnotationPresent(Init.class)){
					cd.init = m;
				}else if (cd.destroy == null && m.isAnnotationPresent(Destroy.class)){
					cd.destroy = m;
				}else if (cd.activate == null && m.isAnnotationPresent(Activate.class)){
					cd.activate = m;
				}else if (cd.deactivate == null && m.isAnnotationPresent(Deactivate.class)){
					cd.deactivate = m;
				}else if (m.isAnnotationPresent(Ref.class) && m.getParameterTypes().length == 1){
					ReferenceDefinition rd = new ReferenceDefinition(cd, m.getName(), new PropertyItem(clz, m.getName(), null, m), null);
					references.add(rd);
				}else if (m.isAnnotationPresent(Property.class) && m.getParameterTypes().length == 1 ){
					Property property = m.getAnnotation(Property.class);
					String propertyValue = property.value();
					if (property.value().length() == 0){
						String configPropertyName = m.getName();
						if (configPropertyName.startsWith("set")){
							configPropertyName = configPropertyName.substring(3);
						}
						putIntoListValueMap(CoreConsts.CONFIG_SERVICE_REF_NAME, new InjectPropertyItem(configPropertyName, new PropertyItem(clz, m.getName(), null, m)), (Map)injectedPropertiesItemMap);
					}else{
						int pos = propertyValue.indexOf('.');
						if (pos < 0){
							throw new IllegalArgumentException("property  value  {" +propertyValue + "} is illegal in  " + m.getName()+"@" + clz.getName());
						}
						String serviceRefName = propertyValue.substring(0, pos);
						putIntoListValueMap(serviceRefName, new InjectPropertyItem(propertyValue.substring(pos+1), new PropertyItem(clz, m.getName(), null, m)), (Map)injectedPropertiesItemMap);
					}
				}
			}
			for (Field f : clz.getDeclaredFields()){
				if (f.isAnnotationPresent(Ref.class)){
					ReferenceDefinition rd = new ReferenceDefinition(cd, f.getName(), new FieldItem(f), null);
					references.add(rd);
				}else if (f.isAnnotationPresent(Property.class)){
					Property property = f.getAnnotation(Property.class);
					String propertyValue = property.value();
					if (property.value().length() == 0){
						putIntoListValueMap(CoreConsts.CONFIG_SERVICE_REF_NAME, new InjectPropertyItem(f.getName(), new FieldItem(f)), (Map)injectedPropertiesItemMap);
					}else{
						int pos = propertyValue.indexOf('.');
						if (pos < 0){
							throw new IllegalArgumentException("property  value  {" +propertyValue + "} is illegal in  " + f.getName()+"@" + clz.getName());
						}
						String serviceRefName = propertyValue.substring(0, pos);
						putIntoListValueMap(serviceRefName, new InjectPropertyItem(propertyValue.substring(pos+1), new FieldItem(f)), (Map)injectedPropertiesItemMap);
					}
				}
			}
			clz = clz.getSuperclass();
		}while (clz != null && !clz.getName().startsWith("java."));
		Config config = (Config) implement.getAnnotation(Config.class);
		if (config != null){
			if (config.value().length() != 0){
				ReferenceDefinition configReferenceDefinintion = new ReferenceDefinition(cd, CoreConsts.CONFIG_SERVICE_REF_NAME, ConfigService.class, SmartMatcher.getMatcher(CoreConsts.CONFIG_ID+"="+config.value() + Matcher.ON_DEMAND_SEPRATOR), null);
				configReferenceDefinintion.setOrder(CoreConsts.CONFIG_REF_DEFALUT_ORDER);
				references.add(configReferenceDefinintion);
			}
			if (cd.type == ComponentType.FACTORY || cd.type == ComponentType.ON_DEMAND){
				ReferenceDefinition configReferenceDefinintion = new ReferenceDefinition(cd, CoreConsts.CONFIG_SERVICE_REF_NAME, ConfigService.class, SmartMatcher.getMatcher(CoreConsts.CONFIG_ID+"="+CoreConsts.VAR_SOURCE_CONFIG_ID +  Matcher.ON_DEMAND_SEPRATOR), null);
				configReferenceDefinintion.setOrder(CoreConsts.CONFIG_REF_DEFALUT_ORDER);
				references.add(configReferenceDefinintion);
			}
		}
		
//		cd.setReferences(references.toArray(new ReferenceDefinition[references.size()]));
		cd.buildOrderedChainAndInjectedItem(references, injectedPropertiesItemMap);
		cd.indexKeys = new Object[cd.interfaces.length+1];
		int i = 0;
		for (Class c : cd.interfaces){
			cd.indexKeys[i++] = c;
		}
		cd.indexKeys[i] = cd.name;
		return cd;
	}
	
	public Method getDeactivate() {
		return deactivate;
	}

	public void setDeactivate(Method deactivate) {
		this.deactivate = deactivate;
	}

	public Method getDestroy() {
		return destroy;
	}

	public void setDestroy(Method destroy) {
		this.destroy = destroy;
	}
	
	public boolean hasDynamicConfigReference() {
		return dynamicReferencePosition != references.length;
	}

	public String getStaticConfigId() {
		for (int i = 0; i < dynamicReferencePosition; i++){
			ReferenceDefinition rd = references[i];
			if (CoreConsts.CONFIG_SERVICE_REF_NAME.equals(rd.getName())){
				Map<String, Object> map = rd.getMatcher().getOndemandConfigRequirement();
				if (map != null){
					return (String)map.get(CoreConsts.CONFIG_ID);
				}
				return null;
			}
		}
		return null;
	}


	public ComponentType getType() {
		return type;
	}

	public void setType(ComponentType type) {
		this.type = type;
	}

	public ScopeType getScope() {
		return scope;
	}

	public void setScope(ScopeType scope) {
		this.scope = scope;
	}

	public Class[] getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(Class[] interfaces) {
		this.interfaces = interfaces;
	}

	public Class getImplement() {
		return implement;
	}

	public void setImplement(Class implement) {
		this.implement = implement;
	}

	public Method getInit() {
		return init;
	}

	public void setInit(Method init) {
		this.init = init;
	}	
	
	public ReferenceDefinition[] getReferences() {
		return references;
	}

	public void setReferences(ReferenceDefinition[] references) {
		this.references = references;
	}
	
	public Method getActivate() {
		return activate;
	}

	public void setActivate(Method activate) {
		this.activate = activate;
	}

}
