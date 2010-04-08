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
import org.xfeep.asura.core.ComponentInstance;
import org.xfeep.asura.core.OReference;
/**
 * Reference is enough now.
 * this class will be removed at 1.0M3
 * @author zhang yuexiang
 *
 */
@Deprecated
public class ConfigReference extends OReference {

	public ConfigReference() {
	}

	public ConfigReference(ConfigReferenceDefinintion definition, Component source) {
		super(definition, source);
	}
	
	@Override
	public void openContract() {
		if (definition.getName().length() > 0){
			super.openContract();
		}
	}
	
	@Override
	public boolean isSatisfied() {
		return  definition.getName().length() == 0 || super.isSatisfied();
	}
	
	@Override
	public void closeContract() {
		if (definition == null || definition.getName() == null){
			return;
		}
		if (definition.getName().length() > 0){
			super.closeContract();
		}
	}
	
	@Override
	public boolean bind(ComponentInstance instance) {
		boolean rt = super.bind(instance);
		if (rt && target != null){
			instance.setProperties( ( (ConfigService) target.getService()).getConfig()  );
		}
		return rt;
	}
	
	@Override
	public boolean unbind(ComponentInstance instance) {
		boolean rt = super.unbind(instance);
		
		if (rt){
			instance.setProperties(null);
		}
		
		return rt;
	}
}
