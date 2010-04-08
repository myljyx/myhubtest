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

import java.util.Map;
import java.util.concurrent.Callable;

public class LazyConfigInfo {

	protected String id;
	protected Callable<Map<String, Object>> configFetcher;

	public LazyConfigInfo() {
		super();
	}
	
	public LazyConfigInfo(String id, Callable<Map<String, Object>> configFetcher) {
		super();
		this.id = id;
		this.configFetcher = configFetcher;
	}

	public String getId() {
		return id;
	}



	public void setId(String id) {
		this.id = id;
	}



	public Callable<Map<String, Object>> getConfigFetcher() {
		return configFetcher;
	}



	public void setConfigFetcher(Callable<Map<String, Object>> configFetcher) {
		this.configFetcher = configFetcher;
	}


}