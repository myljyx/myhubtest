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


import org.xfeep.asura.core.Component;
import org.xfeep.asura.core.ComponentDefinition;
import org.xfeep.asura.core.ComponentInstance;
import org.xfeep.asura.core.ContractType;
import org.xfeep.asura.core.ServiceSpace;
import org.xfeep.asura.core.event.ServiceEvent;
import org.xfeep.asura.core.match.Matcher;
import org.xfeep.asura.core.match.SmartMatcher;
/**
 * DynamicOReference is enough now.
 * this class will be removed at 1.0M2
 * @author zhang yuexiang
 *
 */
@Deprecated
public class ConfigDynamicReference extends ConfigReference {

	protected ComponentInstance sourceInstance;
	
	public ConfigDynamicReference() {
	}

	public static ConfigReferenceDefinintion buildDynamicDefinition(String configId, ConfigReferenceDefinintion definition) {
		ConfigReferenceDefinintion rt = new ConfigReferenceDefinintion();
		rt.setComponentDefinition( definition.getComponentDefinition() );
		rt.setName(configId);
		rt.setBind( definition.getBind() );
		rt.setServiceClass(definition.getServiceClass());
		rt.setMultiplicityType(definition.getMultiplicityType()); 
		rt.setContractType( definition.getContractType() );
		rt.setMatcher(SmartMatcher.getMatcher(Matcher.ON_DEMAND_CONFIG_ID + "=" + rt.getName()+Matcher.ON_DEMAND_SEPRATOR, rt.getComponentDefinition().getImplement()));
		return rt;
	}
	
	public ConfigDynamicReference(ConfigReferenceDefinintion dynamicDefinition,
			ComponentInstance sourceInstance) {
		super(dynamicDefinition, sourceInstance.getComponent());
		this.sourceInstance = sourceInstance;
	}
	
	@Override
	public void onServiceChanged(ServiceEvent event) {

		synchronized (sourceInstance) {
			ComponentInstance eventSource = event.getSource();
			if (event.getType() == ServiceEvent.ACTIVE){
				if (target == null){
						target = eventSource;
//						bind(sourceInstance);
				}
			}else if (target != null){
				if (eventSource == target){
					if (definition.getContractType() != ContractType.CARELESS){
						//check if there's replacement
						ComponentInstance replacement = null;
						ServiceSpace serviceSpace = source.getServiceSpace();
						for (ComponentDefinition cd : serviceSpace.findComponentDefinition(definition.getServiceClass())){
							Component c = serviceSpace.findComponent(cd.getImplement());
							if (c != null){
								replacement = c.getInstance(definition.getMatcher());
								if (replacement != null){
									unbind(sourceInstance);
									target = replacement;
									bind(sourceInstance);
									break;
								}
							}
						}
						if (definition.getMultiplicityType().isRequired() && replacement == null){ // we fund no replacement, so tryUnsatify it.
							sourceInstance.dispose();
						}
					}
				}
			}
		}

	
	}

}
