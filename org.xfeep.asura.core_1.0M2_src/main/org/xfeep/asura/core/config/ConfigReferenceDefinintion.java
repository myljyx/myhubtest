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

import org.xfeep.asura.core.ComponentDefinition;
import org.xfeep.asura.core.MultiplicityType;
import org.xfeep.asura.core.ReferenceDefinition;
import org.xfeep.asura.core.annotation.Config;
import org.xfeep.asura.core.match.Matcher;
import org.xfeep.asura.core.match.SmartMatcher;

/**
 * ReferenceDefinition is enough now.
 * this class will be removed at 1.0M3
 * @author zhang yuexiang
 *
 */
@Deprecated
public class ConfigReferenceDefinintion extends ReferenceDefinition {

	
	public ConfigReferenceDefinintion() {
	}
	
	public ConfigReferenceDefinintion(ComponentDefinition componentDefinition, ConfigVirtualFieldItem bind){
		this.componentDefinition = componentDefinition;
		this.name = bind.getName();
		this.bind = bind;
		serviceClass = bind.getType();
		Config config = bind.getAnnotation(Config.class);
		multiplicityType = config.required() ? MultiplicityType.ONE_ONE : MultiplicityType.ZERO_ONE; 
		contractType = config.contractType();
//		if (config.matcher().length() > 0){
			matcher = SmartMatcher.getMatcher(Matcher.ON_DEMAND_CONFIG_ID + "="+name+Matcher.ON_DEMAND_SEPRATOR, componentDefinition.getImplement());
//		}
		if (name.length()  == 0){
			multiplicityType = MultiplicityType.ZERO_ONE;
		}
	}
	
	
}
