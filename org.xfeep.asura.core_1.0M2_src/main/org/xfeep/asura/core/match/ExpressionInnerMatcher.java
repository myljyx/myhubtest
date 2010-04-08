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

import java.io.StringReader;
import java.util.Map;

import org.xfeep.asura.core.LogManager;
import org.xfeep.asura.core.el.SimpleExpressionInterpreter;

/**
 * 
 * @author zhang yuexiang
 * @since 1.0M2
 */
public class ExpressionInnerMatcher implements InnerMatcher {

	protected String expression;
	
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public ExpressionInnerMatcher(){
	}
	
	public ExpressionInnerMatcher(String expression) {
		super();
		this.expression = expression;
	}

	public boolean matchFilter(final Map<String, Object> srcProperties,
			final Map<String, Object> targetProperties) {
		try {
			Map<String, Object> target = new NouseMap<String, Object>() {
				public boolean containsKey(Object key) {
					return (targetProperties != null && targetProperties.containsKey(key) ) || (srcProperties != null && "$".equals(key));
				}
				public Object get(Object key) {
					if ("$".equals(key)){
						return srcProperties;
					}
					if (targetProperties != null){
						return  targetProperties.get(key);
					}
					return null;
				}

				public boolean isEmpty() {
					return srcProperties == null && targetProperties == null;
				}
			};
            // Now evaluate the expression, getting the result
        	SimpleExpressionInterpreter interpreter = new SimpleExpressionInterpreter(new StringReader(expression));
        	interpreter.setTarget(target);
            return  interpreter.eval();
		} catch (Throwable e) {
			LogManager.getInstance().warn("unstable match expression " + expression, e);
			return false;
		}
	}

	public boolean matchOnDemand(Map<String, Object> ondemandConfigRequirement,
			Map<String, Object> srcProperties,
			Map<String, Object> targetProperties) {
		return OnDemandInnerMatcher.single.matchOnDemand(ondemandConfigRequirement, srcProperties, targetProperties);
	}

}
