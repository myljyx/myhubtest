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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.xfeep.asura.core.ComponentContext;
import org.xfeep.asura.core.ComponentType;
import org.xfeep.asura.core.annotation.Activate;
import org.xfeep.asura.core.annotation.Service;
import org.xfeep.asura.core.match.Matcher;
import org.xfeep.asura.core.reflect.TypeInfoUtil;


@Service(type=ComponentType.FACTORY)
//@Config("")
public class ConfigServiceImp extends LazyConfigInfo implements ConfigService {

	public static final String CONFIG_FUTURE = "configFuture";
	
	public static final String CONFIG_ID = Matcher.ON_DEMAND_CONFIG_ID;

	protected FutureTask<Map<String, Object>> configFuture;
	
	@Activate
	protected void start(ComponentContext ctx) {
		id = (String)ctx.getProperties().get(CONFIG_ID);
		configFuture = new FutureTask<Map<String,Object>>((Callable<Map<String,Object>>)ctx.getProperties().get(CONFIG_FUTURE));
//		id = (String)ctx.getConfig().get(Matcher.ON_DEMAND_CONFIG_ID);
//		configFuture = configAdminService.get(id);
//		if (configLazyMap == null){
//			throw new OnDemandDynamicNotMatchException("no config with id = " + id);
//		}
	}
	
	
	public boolean contains(String name) {
		return getConfig().containsKey(name);
	}

	public Map<String, Object> getConfig() {
		try {
			if (!configFuture.isDone()){
				configFuture.run();
			}
			return configFuture.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw TypeInfoUtil.handleThrowable(e);
		}
	}

	public Object getLazyResolvableProperty(String name, Class<?> type, Class<?>[] memberTypes){
		Map<String, Object> config = getConfig();
		if (config == null){
			return null;
		}
		if (config instanceof LazyResolvedConfigMap) {
			LazyResolvedConfigMap lm = (LazyResolvedConfigMap) config;
			return lm.getLazyResolvableProperty(name, type, memberTypes);
		}
		return config.get(name);
	}

	public String getId() {
		return id;
	}

}
