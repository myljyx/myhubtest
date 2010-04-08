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

import java.util.Map;


public interface ComponentFactoryService {
	
	/**
	 * 
	 * create a service use the same implementation of the given sample and configuration
	 * @param <T>
	 * @param sample a instance of type T which is a service implemented by a factory component.
	 * @param config the configuration of new service
	 * @return the new service with the given configuration
	 * @throws IllegalArgumentException if sample instance isn't a factory component service.
	 */
	public ComponentInstance newService(Object sample, Map<String, Object> config) throws IllegalArgumentException;
	
	public ComponentInstance newService(Object sample, Object... configPairs) throws IllegalArgumentException;
	
}
