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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


import org.xfeep.asura.core.CoreConsts;
import org.xfeep.asura.core.annotation.Ref;
import org.xfeep.asura.core.reflect.TypeInfoPool;

public class SmartMatcher implements Matcher {
	
	protected String matcherExpression;
	protected HashMap<String, Object> ondemandConfigRequirement;
	protected InnerMatcher innerMatcher;
	protected Class<?> hostClass;
	protected boolean isDynamic = false;
	
	public SmartMatcher() {
	}
	
	public SmartMatcher(String matcherExpression){
		parseMatcherExpression(matcherExpression);
	}
	

	
	public SmartMatcher(String matcherExpression, Class<?> hostClass){
		this.hostClass = hostClass;
		parseMatcherExpression(matcherExpression);
	}
	
	public final static Matcher MATCH_ALL_MATCHER = new SmartMatcher(){

		public Map<String, Object> getOndemandConfigRequirement() {
			return Collections.EMPTY_MAP;
		}

		public boolean isDynamic() {
			return false;
		}

		public boolean match(Map<String, Object> targetProperties) {
			return true;
		}
		@Override
		public String toString() {
			return CoreConsts.MATCH_ALL_MATCHER;
		}
		
		@Override
		public String getMatcherExpression() {
			return CoreConsts.MATCH_ALL_MATCHER;
		}
	};
	
	public final static InnerMatcher MATCHALL_INNER_MATCHER = new InnerMatcher() {
		public boolean matchFilter(Map<String, Object> s, Map<String, Object> t) {
			return true;
		}
		public boolean matchOnDemand(
				Map<String, Object> ondemandConfigRequirement,
				Map<String, Object> srcProperties,
				Map<String, Object> targetProperties) {
			return true;
		}
		@Override
		public String toString() {
			return "MATCHALL_INNER_MATCHER";
		}
	};
	
	public final static InnerMatcher NOTHING_MATCHER = new InnerMatcher(){
		public boolean matchFilter(Map<String, Object> s, Map<String, Object> t) {
			return false;
		}
		public boolean matchOnDemand(
				Map<String, Object> ondemandConfigRequirement,
				Map<String, Object> srcProperties,
				Map<String, Object> targetProperties) {
			return false;
		}
		@Override
		public String toString() {
			return CoreConsts.NOTHING_MATCHER;
		}
	};
	
//	public final static InnerMatcher NOT_INITED_MATCHER = new InnerMatcher(){
//		public boolean matchFilter(Map<String, Object> s, Map<String, Object> t) {
//			return false;
//		}
//		public boolean matchOnDemand(Map<String, Object> s, Map<String, Object> t) {
//			return false;
//		}
//	};
	

	public final static Matcher FACTORY_SAMPLE_INSTANCE_MATCHER = new SmartMatcher() {
		public boolean match(Map<String, Object> t) {
			if (t != null && t.containsKey(this.toString())){
				return true;
			}
			return false;
		}

		public Map<String, Object> getOndemandConfigRequirement() {
			return null;
		}

		public boolean isDynamic() {
			return false;
		}
		
		public String toString() {
			return CoreConsts.FACTORY_SAMPLE_INSTANCE_MATCHER;
		}
		
		@Override
		public String getMatcherExpression() {
			return CoreConsts.FACTORY_SAMPLE_INSTANCE_MATCHER;
		}
		

	};
	
	public static Matcher getMatcher(String matcher){
		return getMatcher(matcher, null);
	}
	
	public static Matcher getMatcher(String matcher, Class<?> hostClass) {
		if (CoreConsts.FACTORY_SAMPLE_INSTANCE_MATCHER.equals(matcher)){
			return FACTORY_SAMPLE_INSTANCE_MATCHER;
		}
		if (CoreConsts.MATCH_ALL_MATCHER.equals(matcher)){
			return MATCH_ALL_MATCHER;
		}
		return new SmartMatcher(matcher, hostClass);
	}
	
	public Map<String, Object> getOndemandConfigRequirement() {
		return ondemandConfigRequirement;
	}
	

	public boolean match(Map<String, Object> targetProperties) {
		return innerMatcher.matchOnDemand(ondemandConfigRequirement, null, targetProperties) && innerMatcher.matchFilter(null, targetProperties);
	}
	
	public Matcher staticizing(final Map<String, Object> srcProperties){
		SmartMatcher rt = new SmartMatcher(){
			@Override
			public boolean match(Map<String, Object> targetProperties) {
				return innerMatcher.matchOnDemand(ondemandConfigRequirement, srcProperties, targetProperties) && innerMatcher.matchFilter(srcProperties, targetProperties);
			}
		};
		rt.hostClass = this.hostClass;
		rt.innerMatcher = this.innerMatcher;
		rt.isDynamic = true;
		rt.matcherExpression = this.matcherExpression;
		if (ondemandConfigRequirement != null){
			rt.ondemandConfigRequirement = (HashMap<String, Object>) this.ondemandConfigRequirement.clone();
			Map<String, Object> oneKeyMap = new NouseMap<String, Object>(){
				@Override
				public Object get(Object key) {
					return CoreConsts.VAR_SOURCE_PROPERTIES.equals(key) ?  srcProperties : null;
				}
			};
			for (Entry<String, Object> entry : rt.ondemandConfigRequirement.entrySet()){
				String valuestr = (String)entry.getValue();
				if (valuestr.startsWith(CoreConsts.VAR_SOURCE_PROPERTIES)){
					try {
						entry.setValue(TypeInfoPool.getDefault().resolvePropertityValue(oneKeyMap, valuestr));
					} catch (Throwable e) {
						throw new IllegalArgumentException("can not resolve expression " + valuestr, e);
					} 
				}
			}
		}
		return rt;
	}
	
	public static boolean imply(Map<String,  Object> config, Map<String, Object> requirement){
		if (requirement == null || requirement.isEmpty()){
			return true;
		}
		if (config == null){
			return false;
		}
		for (Map.Entry<String, Object> e : requirement.entrySet()){
			Object v = config.get(e.getKey());
			if (v == null || !v.equals(e.getValue())){
				return false;
			}
		}
		return true;
	}

	public void parseMatcherExpression(String expression) {
		this.matcherExpression = expression;
		if (expression != null){
			if (this.matcherExpression.indexOf("$.") > -1){
				isDynamic = true;
			}
			int dspos = expression.indexOf(ON_DEMAND_SEPRATOR);
			if (dspos >-1){
				String ondemandDef = expression.substring(0, dspos);
				expression = expression.substring(dspos + ON_DEMAND_SEPRATOR.length()).trim();
				ondemandConfigRequirement = new HashMap<String, Object>();
				for (String item : ondemandDef.split("\\;")){
					int ep = item.indexOf('=');
					String key = item.substring(0, ep);
					String valuestr = item.substring(ep+1);
//					if (valuestr.indexOf("$c") > -1){
//						try {
//							ondemandConfigRequirement.put(key, TypeInfoPool.getDefault().resolvePropertityValue(new Object(){
//								@SuppressWarnings("unused")
//								Map<String, Object> $c = config;
//							}, valuestr));
//						} catch (Throwable e) {
//						} 
//					}else{
						ondemandConfigRequirement.put(key, valuestr);
//					}
					
				}
			}
		}
		if (expression == null || expression.length() == 0){
			if (ondemandConfigRequirement != null && !ondemandConfigRequirement.isEmpty()){
				innerMatcher = OnDemandInnerMatcher.single;
			}else{
				innerMatcher = MATCHALL_INNER_MATCHER;
			}
		}else {
			if (expression.startsWith(".")) { //static method matcher
				if (hostClass == null){
					throw new IllegalStateException("please setHostClass first before call this method");
				}
				try {
					final Method matcherMethod = hostClass.getDeclaredMethod(expression.substring(1), new Class[] {Map.class, Map.class});
					innerMatcher = new MethodInnerMatcher(matcherMethod); 
				} catch (Throwable e) {
					throw new IllegalStateException("illegal matcher " + this.matcherExpression +", the matcher method must be static and like:\n public static boolean mymatch(Map<String, Object> sourceProperties, Map<String, Object> targetProperties)", e);
				} 
			}else { //simple expression
				innerMatcher = new ExpressionInnerMatcher(expression);
			}
		}
	}

	public String getMatcherExpression() {
		return matcherExpression;
	}
	
	public String toString(){
		return matcherExpression;
	}

	public boolean isDynamic() {
		return isDynamic;
	}
}
