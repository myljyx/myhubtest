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

import java.util.concurrent.Executors;

import org.xfeep.asura.core.Component;
import org.xfeep.asura.core.ComponentDefinition;
import org.xfeep.asura.core.ComponentManager;

public class SimpleServicesTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final ComponentManager manager = new ComponentManager(Executors.newFixedThreadPool(5));
		
//		manager.insert((HelloClass.class));
		manager.add(ComponentDefinition.create(HelloServiceImp.class));
		manager.add(HelloServiceClient.class);
		manager.openApplicationServiceSpace();
		
//		HelloClass hello = manager.getApplicationServiceSpace().findService(HelloClass.class);
//		hello.sayHello("zhang yuexiang");
		
		
		HelloService helloService = manager.getApplicationServiceSpace().findService(HelloService.class);
		if (helloService != null){
			System.out.println("from helloService : " + helloService.sayHello("Asura"));
		}
		
		
		
		manager.getApplicationServiceSpace().close();
		System.out.println();
		System.out.println(".............test restart............");
		System.out.println();
		manager.getApplicationServiceSpace().open();
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//		}
		final Component helloServiceComponent = manager.getApplicationServiceSpace().findComponent(HelloServiceImp.class);
		Runnable disableTask = new Runnable(){
			public void run() {
				System.out.println("=======test disable HelloServiceImp");
				helloServiceComponent.disable();
			}
		};
		Thread t = new Thread(disableTask);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		System.out.println();
		System.out.println("=======test enable HelloServiceImp again");
		helloServiceComponent.enable();
		
	}

}
