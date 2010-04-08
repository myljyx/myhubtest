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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.xfeep.asura.core.annotation.Activate;
import org.xfeep.asura.core.annotation.Ref;
import org.xfeep.asura.core.annotation.Service;
import org.xfeep.asura.core.config.ConfigAdminService;
import org.xfeep.asura.core.config.ConfigReferenceDefinintion;
import org.xfeep.asura.core.match.Matcher;

@Service
public class ComponentFactoryServiceImp implements ComponentFactoryService {

	protected ComponentContext context;
	
	@Ref
	protected ConfigAdminService configAdminService;
	
	public ComponentInstance newService(Object sample, Map<String, Object> config)
			throws IllegalArgumentException {
		Class sclz = sample.getClass();
		ServiceSpace serviceSpace = context.component.serviceSpace;
		Component component = serviceSpace.findComponent(sclz);
		if (component == null){
			throw new IllegalArgumentException(sample + " is not a valid component service");
		}
		if (component instanceof FactoryComponent) {
			FactoryComponent factoryComponent = (FactoryComponent) component;
			String configId = component.getDefinition().getStaticConfigId();
			if (component.definition.hasDynamicConfigReference() && configAdminService != null){
				configId = (String)config.get(Matcher.ON_DEMAND_CONFIG_ID);
				if (configId == null || configId.length() == 0){
					configAdminService.put(configId = UUID.randomUUID().toString(), config);
				}
//				config = configAdminService.get(configId);
			}
			if (configId == null || configId.length() == 0){
				configId = UUID.randomUUID().toString();
			}
			return factoryComponent.newInstance(config, configId);
		}else{
			throw new IllegalArgumentException(sample + " is not a valid component service");
		}
	}
	
	@Activate
	public void start(ComponentContext context) {
		this.context = context;
	}

	public ComponentInstance newService(Object sample, Object... configPairs)
			throws IllegalArgumentException {
		if (configPairs.length == 0 || (  configPairs.length % 2 != 0 ) ) {
			throw new IllegalArgumentException("the number of configPairs must be even");
		}
		Map<String, Object> config = new HashMap<String, Object>();
		for (int i = 0; i < configPairs.length / 2; i++){
			config.put(configPairs[i*2].toString(), configPairs[i*2+1]);
		}
		return newService(sample, config);
	}

}
