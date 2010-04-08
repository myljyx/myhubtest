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

import org.xfeep.asura.core.ComponentContext;
import org.xfeep.asura.core.ComponentType;
import org.xfeep.asura.core.OnDemandDynamicNotMatchException;
import org.xfeep.asura.core.annotation.Activate;
import org.xfeep.asura.core.annotation.Property;
import org.xfeep.asura.core.annotation.Ref;
import org.xfeep.asura.core.annotation.Service;
import org.xfeep.asura.core.match.Matcher;

/**
 * use ConfigServiceImp now
 * this class will be deleted in 1.0 M3
 * @author zhang yuexiang
 *
 */
@Deprecated
@Service(type=ComponentType.ON_DEMAND)
//@Config(value="")
public class Configuration {
	
	@Property
	String id;
	@Ref
	ConfigAdminService configAdminService;

	ConfigLazyMap configLazyMap;
	
	public ConfigLazyMap getConfigLazyMap() {
		return configLazyMap;
	}

//	@Activate
//	protected void start(ComponentContext ctx) {
//		id = (String)ctx.getConfig().get(Matcher.ON_DEMAND_CONFIG_ID);
//		configLazyMap = configAdminService.get(id);
//		if (configLazyMap == null){
//			throw new OnDemandDynamicNotMatchException("no config with id = " + id);
//		}
//	}
//	
//	public Object getConfigProperty(String name, Class<?> type){
//		return configLazyMap.getConfigProperty(name, type);
//	}
//	
//	public boolean contains(String name){
//		return configLazyMap.containsKey(name);
//	}
	
	
}
