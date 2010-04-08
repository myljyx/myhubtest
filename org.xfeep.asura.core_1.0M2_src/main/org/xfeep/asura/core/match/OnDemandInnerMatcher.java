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
package org.xfeep.asura.core.match;

import java.util.Map;

import org.xfeep.asura.core.OnDemandComponentRepresentativeInstance;

public class OnDemandInnerMatcher implements InnerMatcher {

	
	public boolean matchFilter(Map<String, Object> srcProperties, Map<String, Object> targetProperties) {
		return true;
	}
	public boolean matchOnDemand(Map<String, Object> ondemandConfigRequirement, Map<String, Object> srcProperties, Map<String, Object> targetProperties) {
		if (targetProperties.containsKey(OnDemandComponentRepresentativeInstance.FLAG)){
			return true;
		}
		return SmartMatcher.imply(targetProperties, ondemandConfigRequirement);
	}

	public final static OnDemandInnerMatcher single = new OnDemandInnerMatcher();
}
