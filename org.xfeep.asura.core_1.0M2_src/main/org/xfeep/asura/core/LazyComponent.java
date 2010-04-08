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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.xfeep.asura.core.match.Matcher;

public class LazyComponent extends Component {

	protected LazyComponentInstance instance;
	
	public LazyComponent() {
	}
	
	public LazyComponent(ComponentDefinition definition, ServiceSpace serviceSpace) {
		super(definition, serviceSpace);
	}
	
	@Override
	public ComponentInstance getInstance(Matcher matcher) {
		if (instance == null || status != ComponentStatus.SATISFIED){
			return null;
		}
		if (matcher == null || matcher.match(instance.getProperties())){
			return instance;
		}
		return null;
	}

	@Override
	public List<ComponentInstance> getInstances(Matcher matcher) {
		if (instance == null || status != ComponentStatus.SATISFIED){
			return Collections.EMPTY_LIST;
		}
		if (matcher == null || matcher.match(instance.getProperties())){
			return Arrays.asList((ComponentInstance)instance);
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	public synchronized boolean tryStatisfy() {
		if (instance != null){
			return false;
		}
		for (Reference r : references){
			if (!r.isSatisfied()){
				return false;
			}
		}
		status = ComponentStatus.SATISFIED;
		instance = new LazyComponentInstance(definition.getStaticConfigId(), this);
		instance.enable();
//		if ( !instance.init() ) {
//			instance.destroy();
//			return false;
//		}
//		if (!instance.activate()){
//			instance.deactivate();
//			instance.destroy();
//			instance = null;
//			return false;
//		}
		return true;
	}

	@Override
	public synchronized void tryUnstatisfy() {
		if (status != ComponentStatus.SATISFIED){
			return;
		}
		status = ComponentStatus.IDLE;
		instance.disable();
		instance = null;
//		for (Reference r : references){
//			r.reset();
//		}
	}

	@Override
	public List<ComponentInstance> getAllInstancesForDebugInfo() {
		synchronized (instanceMainLock) {
			return   instance != null  ? Arrays.asList(instance) : Collections.EMPTY_LIST;
		}
	}

}
