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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.xfeep.asura.core.match.Matcher;


public class OnDemandComponent extends Component {

//	ReadWriteProtectedArrayList<ComponentInstance> instances = new ReadWriteProtectedArrayList<ComponentInstance>();
	protected OnDemandComponentRepresentativeInstance instance;
	
	public OnDemandComponent() {
		super();
	}

	public OnDemandComponent(ComponentDefinition definition,
			ServiceSpace serviceSpace) {
		super(definition, serviceSpace);
	}

	@Override
	public ComponentInstance getInstance(Matcher matcher) {
		if (instance == null || status != ComponentStatus.SATISFIED){
			return null;
		}
//		if (matcher == null || matcher.match(instance)){
//			return instance;
//		} else {
//			return instance.getInstance(matcher);
//		}
		
		
		if (matcher == null){
			return instance;
		}
		
		return instance.getInstance(matcher);
		
	}
	
	@Override
	public List<ComponentInstance> getInstances(Matcher matcher) {

		if (instance == null || status != ComponentStatus.SATISFIED){
			return Collections.EMPTY_LIST;
		}
		synchronized (instance) {
			if (matcher == null){
				return (List)Arrays.asList(instance.instances.toArray());
			}
			List<ComponentInstance> rt = new ArrayList<ComponentInstance>();
			for (ComponentInstance ci : instance.instances){
				if (matcher.match(ci.getProperties())){
					rt.add(ci);
				}
			}
			return rt;
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
			instance = new OnDemandComponentRepresentativeInstance(definition.getStaticConfigId(), this);
			instance.addProperty(OnDemandComponentRepresentativeInstance.FLAG, true);
//			if ( !instance.init() ) {
//				instance.destroy();
//				return false;
//			}
//			if (!instance.activate()){
//				instance.deactivate();
//				instance.destroy();
//				instance = null;
//				return false;
//			}
			instance.enable();
			return true;
		}
	}
	
	@Override
	public  void tryUnstatisfy() {
		synchronized (instanceMainLock) {
			if (status != ComponentStatus.SATISFIED){
				return;
			}
			status = ComponentStatus.IDLE;
//			if (instance != null){
//				instance.deactivate();
//				instance.destroy();
//				instance = null;
//			}
			if (instance != null){
				instance.disable();
			}
			instance = null;
		}
	}

	@Override
	public List<ComponentInstance> getAllInstancesForDebugInfo() {
		synchronized (instanceMainLock) {
			if (instance == null){
				return Collections.EMPTY_LIST;
			}
			Object[] ins = instance.instances.toArray();
			return   ins.length > 0  ? Arrays.asList(ins) : Collections.EMPTY_LIST;
		}
	}
}
