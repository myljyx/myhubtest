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

import java.lang.reflect.Method;
import java.util.Map;

import org.xfeep.asura.core.LogManager;

public class MethodInnerMatcher implements InnerMatcher {
	
	protected Method matcherMethod;
	
	public MethodInnerMatcher() {
	}
	
	
	
	public MethodInnerMatcher(Method matcherMethod) {
		super();
		this.matcherMethod = matcherMethod;
	}



	public Method getMatcherMethod() {
		return matcherMethod;
	}
	public void setMatcherMethod(Method matcherMethod) {
		this.matcherMethod = matcherMethod;
	}
	public boolean matchFilter(Map<String, Object> srcProperties,
			Map<String, Object> targetProperties) {
		try {
			return (Boolean)matcherMethod.invoke(null, srcProperties, targetProperties);
		} catch (Throwable e) {
			LogManager.getInstance().warn("unstable match method " + matcherMethod.getName(), e);
			return false;
		} 
	}


	public boolean matchOnDemand(Map<String, Object> ondemandConfigRequirement,
			Map<String, Object> srcProperties,
			Map<String, Object> targetProperties) {
		return OnDemandInnerMatcher.single.matchOnDemand(ondemandConfigRequirement, srcProperties, targetProperties);
	}

}
