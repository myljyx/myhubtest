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

import org.xfeep.asura.core.event.ServiceEvent;

public class DynamicNReference extends DynamicReference {

	public DynamicNReference() {
		// TODO Auto-generated constructor stub
	}

	public DynamicNReference(ReferenceDefinition definition,
			ComponentInstance sourceInstance) {
		super(definition, sourceInstance);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean bind(ComponentInstance instance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSatisfied() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onServiceChanged(ServiceEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean unbind(ComponentInstance instance) {
		// TODO Auto-generated method stub
		return false;
	}

}
