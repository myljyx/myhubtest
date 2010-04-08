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
package org.xfeep.asura.core.util;

import java.util.concurrent.Callable;

public class FinishedCallable<V> implements Callable<V> {

	protected V result;
	
	public FinishedCallable() {
	}
	
	
	
	public FinishedCallable(V result) {
		super();
		this.result = result;
	}



	public V getResult() {
		return result;
	}
	
	public void setResult(V result) {
		this.result = result;
	}
	
	public V call() throws Exception {
		return result;
	}

}
