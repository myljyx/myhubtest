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



public class EagerComponent extends Component {
	
	
	protected ComponentInstance instance;
	
	public EagerComponent() {
	}

	public EagerComponent(ComponentDefinition definition, ServiceSpace serviceSpace) {
		super(definition, serviceSpace);
	}

	@Override
	public  ComponentInstance getInstance(Matcher matcher) {
		synchronized (instanceMainLock) {
			if (instance != null && instance.getStatus() == ComponentInstanceStatus.ACTIVE && (matcher == null || matcher.match(instance.getProperties())) ){
				return instance;
			}
			return null;
		}
	}

	@Override
	public  List<ComponentInstance> getInstances(Matcher matcher) {
		synchronized (instanceMainLock) {
			if (instance != null && instance.getStatus() == ComponentInstanceStatus.ACTIVE && (matcher == null || matcher.match(instance.getProperties())) ){
				return Arrays.asList(instance);
			}
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public  boolean tryStatisfy() {
		synchronized (instanceMainLock) {
			if (instance != null){
				return false;
			}
			for (Reference r : references){
				if (!r.isSatisfied()){
					return false;
				}
			}
			status = ComponentStatus.SATISFIED;
			instance = new ComponentInstance(definition.getStaticConfigId(), this);
			boolean rt = instance.enable();
			if (!rt && instance.status == ComponentInstanceStatus.DESTROYED){
				tryUnstatisfy();
			}
			return rt;
		}
	}

	@Override
	public  void tryUnstatisfy() {
		synchronized (instanceMainLock) {
			if (status != ComponentStatus.SATISFIED){
				return;
			}
			status = ComponentStatus.IDLE;
			if (instance != null){
				instance.disable();
				instance = null;
			}
//			for (Reference r : references){
//				r.reset();
//			}
		}
	}

	@Override
	public List<ComponentInstance> getAllInstancesForDebugInfo() {
		synchronized (instanceMainLock) {
			return   instance != null  ? Arrays.asList(instance) : Collections.EMPTY_LIST;
		}
	}


	
	
	
}
