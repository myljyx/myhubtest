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
package org.xfeep.asura.core.config;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;


import org.xfeep.asura.core.ComponentContext;
import org.xfeep.asura.core.ComponentInstance;
import org.xfeep.asura.core.CoreConsts;
import org.xfeep.asura.core.FactoryComponent;
import org.xfeep.asura.core.annotation.Activate;
import org.xfeep.asura.core.annotation.Ref;
import org.xfeep.asura.core.annotation.Service;
import org.xfeep.asura.core.util.FinishedCallable;

@Service
public class ConfigAdminService {


	@Ref(matcher=CoreConsts.FACTORY_SAMPLE_INSTANCE_MATCHER)
	protected ConfigServiceImp sample; 

	FactoryComponent configServiceFactoryComponent;
	
	//TODO: should provide real pesistence cache.
	protected ConcurrentHashMap<String,  ComponentInstance> caches =  new ConcurrentHashMap<String, ComponentInstance>();
//	protected ConcurrentHashMap<String, Map<String, Object> > computedConfigCaches =  new ConcurrentHashMap<String, Map<String,Object>>();
//	protected ServiceSpace serviceSpace;
	
//	public Map<String, Object> get(final String configId) {
//		if (configId == null){
//			return null;
//		}
//	}
	
	public ComponentInstance put(String configId, Callable<Map<String, Object>> lazyConfigCallable){
		if (configId == null || lazyConfigCallable == null){
			throw new IllegalArgumentException("neither configId nor configFuture can be null");
		}
		ConfigPrototypeMap configPrototypeMap = new ConfigPrototypeMap(configId, lazyConfigCallable);
		ComponentInstance nci = configServiceFactoryComponent.newInstance(configPrototypeMap, configId);
		ComponentInstance oci = caches.put(configId, nci);
		if (oci != null){
			oci.dispose();
		}
		return nci;
	}
	
	public ComponentInstance put(String configId,  Map<String, Object> config){
		if (configId == null || config == null){
			throw new IllegalArgumentException("neither configId nor config can be null");
		}
//		Future<Map<String, Object>> f = new InlineFuture<Map<String, Object>>(config);
		return put(configId, new FinishedCallable<Map<String,Object>>(config));

	}
	
	/**
	 * create a new configuration service.
	 * @param configId
	 * @param config
	 * @return null if there exists a configuration whose id is the same with configId, otherwise the new configuration ComponentIntance 
	 */
	public ComponentInstance create(String configId,  Map<String, Object> config){
		if (configId == null || config == null){
			throw new IllegalArgumentException("neither configId nor config can be null");
		}
		if (caches.containsKey(configId)){
			return null;
		}
		return put(configId, config);
	}
	
	/**
	 * create a new configuration service.
	 * @param configId
	 * @param keyValuePairs the number of keyValuePairs must be even
	 * @return
	 */
	public ComponentInstance create(String configId,   Object... keyValuePairs){
		if (keyValuePairs.length == 0 || (  keyValuePairs.length % 2 != 0 ) ) {
			throw new IllegalArgumentException("the number of configPairs must be even");
		}
		Map<String, Object> config = new HashMap<String, Object>();
		for (int i = 0; i < keyValuePairs.length / 2; i++){
			config.put(keyValuePairs[i*2].toString(), keyValuePairs[i*2+1]);
		}
		return create(configId, config);
	}
	
	/**
	 * create a new configuration service.
	 * @param configId
	 * @param lazyConfigCallable
	 * @return null if there exists a configuration whose id is the same with configId, otherwise the new configuration ComponentIntance 
	 */
	public ComponentInstance create(String configId,  Callable<Map<String, Object>> lazyConfigCallable){
		if (configId == null || lazyConfigCallable == null){
			throw new IllegalArgumentException("neither configId nor lazyConfigCallable can be null");
		}
		if (caches.contains(configId)){
			return null;
		}
		return put(configId, lazyConfigCallable);
	}
	
	/**
	 * get a Configuration ComponentInstance by given configId
	 * @param configId
	 * @return
	 */
	public ComponentInstance get(String configId){
		return configId == null ? null : caches.get(configId);
	}
	
	
	public Map<String, Object>  getConfig(String configId){
		if (configId == null){
			return null;
		}
		ComponentInstance ci = caches.get(configId);
		return ci == null ? null : ci.getProperties();
	}
	
	/**
	 * update a Configuration whose id is the given one. this method will cause the old one deacivate and a new one with the same
	 * id activate. If there's no Configuration associated with the given id, this method will do nothing but return null.
	 * @param configId
	 * @param config
	 * @return the new ComponentInstance of the new Configuration Service
	 */
	public ComponentInstance update(String configId, Map<String, Object> config){
		if (configId == null || !caches.containsKey(configId)){
			return null;
		}
		return put(configId, config);
	}
	
	/**
	 * update a Configuration whose id is the given one. this method will cause the old one deacivate and a new one with the same
	 * id activate. If there's no Configuration associated with the given id, this method will do nothing but return null.
	 * @param configId
	 * @param keyValuePairs the number of keyValuePairs must be even, the odd key object must be String and followed by the even value object
	 * @return
	 */
	public ComponentInstance update(String configId,  Object... keyValuePairs){
		if (keyValuePairs.length == 0 || (  keyValuePairs.length % 2 != 0 ) ) {
			throw new IllegalArgumentException("the number of configPairs must be even");
		}
		Map<String, Object> config = new HashMap<String, Object>();
		for (int i = 0; i < keyValuePairs.length / 2; i++){
			config.put(keyValuePairs[i*2].toString(), keyValuePairs[i*2+1]);
		}
		return update(configId, config);
	}
	
	/**
	 * update a Configuration whose id is the given one. this method will cause the old one deacivate and a new one with the same
	 * id activate. If there's no Configuration associated with the given id, this method will do nothing but return null.
	 * @param configId
	 * @param lazyConfigCallable
	 * @return the new ComponentInstance of the new Configuration Service, or null if there's no Configuration whose id is the given one
	 */
	public ComponentInstance update(String configId, Callable<Map<String, Object>> lazyConfigCallable){
		if (configId == null || !caches.containsKey(configId)){
			return null;
		}
		return put(configId, lazyConfigCallable);
	}
	
	/**
	 * remove a Configuration whose id is the given one. this method will cause the  matched Configuration service deacivate.
	 * @param configId
	 * @return true if there's a Configuration service associated with <code>configId</code>, otherwise false
	 */
	public boolean remove(String configId){
		if (configId == null || !caches.containsKey(configId)){
			return false;
		}
		ComponentInstance ci = caches.remove(configId);
		if (ci != null){
			ci.dispose();
			return true;
		}
		return false;
	}
	
	public void load(ConfigLoader loader){
		Iterator<LazyConfigInfo> i = loader.iterator();
		if (i != null){
			while (i.hasNext()){
				LazyConfigInfo lc = i.next();
				put(lc.id, lc.configFetcher);
			}
		}
	}
	
	@Activate
	protected void activate(ComponentContext ctx)  {
		configServiceFactoryComponent = (FactoryComponent) ctx.getComponent().getServiceSpace().findComponent(ConfigServiceImp.class);
	}
}
