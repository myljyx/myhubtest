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
package org.xfeep.asura.core.test.pre;

import org.xfeep.asura.core.annotation.Activate;
import org.xfeep.asura.core.annotation.Deactivate;
import org.xfeep.asura.core.annotation.Destroy;
import org.xfeep.asura.core.annotation.Init;
import org.xfeep.asura.core.annotation.Service;

@Service
public class HelloClass {

	@Init
	protected void helloInit() {
		System.out.println("helloInit");
	}
	
	@Destroy
	public void HelloDestroy() {
		System.out.println("HelloDestroy");
	}
	
	@SuppressWarnings("unused")
	@Activate
	private void helloActivate() {
		System.out.println("helloActivate");
	}
	
	@Deactivate
	protected void helloDeactivate() {
		System.out.println("helloDeactivate");
	}
	
	public void sayHello(String name) {
		System.out.println("hello, " + name);
	}
	
}
