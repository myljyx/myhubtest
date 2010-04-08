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

public class CoreConsts {
	
	public static final String FACTORY_SAMPLE_INSTANCE_MATCHER = "factory_sample_instance";
	public static final String MATCH_ALL_MATCHER = "match_all_matcher";
	public static final String NOTHING_MATCHER = "nothing_matcher";
	public static final String CONFIG_SERVICE_REF_NAME = "_c_";
	public static final String CONFIG_ID = "_cid";
	
	public static final String ON_DEMAND_SEPRATOR = "/**/";
	
	public static final String VAR_COMPONENT_NAME = "_name";
	public static final String VAR_COMPONENT_IMP = "_imp";
	public static final String VAR_CONFIG_ID = CONFIG_ID;//"_cid";
	public static final String VAR_SOURCE_PROPERTIES = "$";
	public static final String VAR_SOURCE_PROPERTIES_PREFIX = VAR_SOURCE_PROPERTIES + ".";
	public static final String VAR_SOURCE_CONFIG_ID = "$." + CONFIG_ID;
	public static final int CONFIG_REF_DEFALUT_ORDER = -1024;
	
}
