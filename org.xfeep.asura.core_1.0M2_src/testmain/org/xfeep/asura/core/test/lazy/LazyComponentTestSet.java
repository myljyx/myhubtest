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
package org.xfeep.asura.core.test.lazy;

import static junit.framework.Assert.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xfeep.asura.core.Component;
import org.xfeep.asura.core.ComponentInstance;
import org.xfeep.asura.core.ComponentManager;
import org.xfeep.asura.core.ComponentStatus;
import org.xfeep.asura.core.ComponentType;
import org.xfeep.asura.core.annotation.Activate;
import org.xfeep.asura.core.annotation.Deactivate;
import org.xfeep.asura.core.annotation.Destroy;
import org.xfeep.asura.core.annotation.Init;
import org.xfeep.asura.core.annotation.Ref;
import org.xfeep.asura.core.annotation.Service;
import org.xfeep.asura.core.reflect.FieldReference;
import org.xfeep.asura.core.test.pre.TestHelper;




public class LazyComponentTestSet {
	
	ComponentManager manager;
	TestHelper helper;
	
	@Before
	public void init(){
		helper = new TestHelper();
		manager = new ComponentManager(helper.getExecutorService());
		helper.appServiceSpace = manager.getApplicationServiceSpace();
	}
	
	//test single eager component's life cycle
	
	
	@Service(type=ComponentType.LAZY)
	public static class SimpleLifeCycleLazyComponent {
		
		boolean inited = false;
		boolean activated = false;
		boolean deactivated = false;
		boolean destroyed = false;
		
		@Init
		void init(){
			inited = true;
		}
		
		@SuppressWarnings("unused")
		@Activate
		private void activate() {
			activated = true;
		}
		
		@Deactivate
		protected String deactivate() {
			deactivated = true;
			return "true";
		}
		
		@Destroy
		protected void destroy() {
			destroyed = true;
		}
		
		public String same(String s){
			return s;
		}
		
	}
	
	@Test
	public void testLazySimpleLifeCycleComponent() {
		manager.add(SimpleLifeCycleLazyComponent.class);
		manager.openApplicationServiceSpace();
		
		//test lazy creation of lazy component 
		Component c = manager.getApplicationServiceSpace().findComponent(SimpleLifeCycleLazyComponent.class);
		ComponentInstance ci = c.getInstance(null);
		FieldReference<ComponentInstance, Object> fa = null;
		try {
			fa = new FieldReference<ComponentInstance, Object>(ci, "service");
		} catch (Throwable e) {
			fail(e.getMessage());
		} 
		assertNull(fa.get());
		
		
		SimpleLifeCycleLazyComponent s = manager.getApplicationServiceSpace().findService(SimpleLifeCycleLazyComponent.class);
		assertNotNull(s);
		assertNotNull(fa.get());
		assertTrue(s.activated);
		assertTrue(s.inited);
		assertFalse(s.deactivated);
		assertFalse(s.destroyed);
		assertEquals("good", s.same("good"));
		
		
		//test disable component
		c.disable();
		assertTrue(s.deactivated);
		assertTrue(s.destroyed);
		assertNull(c.getInstance(null));
		assertNull(manager.getApplicationServiceSpace().findService(SimpleLifeCycleLazyComponent.class));
		
		//test enable again
		c.enable();
		s = manager.getApplicationServiceSpace().findService(SimpleLifeCycleLazyComponent.class);
		assertNotNull(s);
		assertTrue(s.activated);
		assertTrue(s.inited);
		assertFalse(s.deactivated);
		assertFalse(s.destroyed);
		assertEquals("good", s.same("good"));
		
		//test ungetService
		ci = c.getInstance(null);
		ci.ungetService(null);
		assertTrue(s.deactivated);
		assertTrue(s.destroyed);
		
		//test enable again
		c.enable();
		
		//test service space close
		manager.getApplicationServiceSpace().close();
		assertTrue(s.deactivated);
		assertTrue(s.destroyed);
		assertNull(c.getInstance(null));
		assertNull(manager.getApplicationServiceSpace().findService(SimpleLifeCycleLazyComponent.class));
		assertNull(manager.getApplicationServiceSpace().findComponent(SimpleLifeCycleLazyComponent.class));
		
	}
	
	//test simple service and service client
	public static interface SimpleEchoLazyService {
		public String echo(String name);
	}
	
	@Service(type=ComponentType.LAZY)
	public static class SimpleEchoLazyComponent implements SimpleEchoLazyService{
		
		static boolean activateWithException = false;
		
		public String echo(String name) {
			return name;
		}
		
		@Activate
		void start() {
			if (activateWithException){
				throw new RuntimeException("test activateWithException");
			}
		}
		
		boolean destroy = false;
		
		@Destroy
		void destroy(){
			destroy = true;
		}
	}
	
	@Service
	public static class SimpleEchoClient {
		
		@Ref
		SimpleEchoLazyService echoService;
		boolean activated = false;
		
		public String clientCall(String name){
			return "client:" + echoService.echo(name);
		}
		
		public SimpleEchoLazyService getEchoService() {
			return echoService;
		}
		
		@Activate
		void start(){
			activated = true;
		}
		
		@Deactivate
		void stop(){
			activated = false;
		}
		
	}
	
	
	@Test
	public void testLazySimpleEchoServiceAndClient() {
		manager.add(SimpleEchoClient.class);
		manager.openApplicationServiceSpace();
		Component clientComponent = manager.getApplicationServiceSpace().findComponent(SimpleEchoClient.class);
		assertEquals(clientComponent.getStatus(), ComponentStatus.IDLE);
		SimpleEchoClient client = manager.getApplicationServiceSpace().findService(SimpleEchoClient.class);
		assertNull(client);
		
		//test dynamic add component after service space started
		manager.add(SimpleEchoLazyComponent.class);
		helper.sleepUntilNoTask();
		assertEquals(clientComponent.getStatus(), ComponentStatus.SATISFIED);
		
		client = manager.getApplicationServiceSpace().findService(SimpleEchoClient.class);
		assertNotNull(client);
		assertTrue(client.activated);
		assertEquals("client:"+"zhangyx", client.clientCall("zhangyx"));
		assertEquals("zhangyx", client.getEchoService().echo("zhangyx"));
		
		//test disable SimpleEchoComponent
		Component serverComponent = manager.getApplicationServiceSpace().findComponent(SimpleEchoLazyComponent.class);
		assertEquals(serverComponent.getStatus(), ComponentStatus.SATISFIED);
		serverComponent.disable();
		helper.sleepUntilNoTask();
		
		assertEquals(serverComponent.getStatus(), ComponentStatus.ASLEEP);
		assertEquals(clientComponent.getStatus(), ComponentStatus.IDLE);
		assertNull(client.getEchoService());
		assertFalse(client.activated);
		
		client = manager.getApplicationServiceSpace().findService(SimpleEchoClient.class);
		assertNull(client);
		
		//test activate with exception
		SimpleEchoLazyComponent.activateWithException = true;
		serverComponent.enable();
		helper.sleepUntilNoTask();
		assertEquals(serverComponent.getStatus(), ComponentStatus.IDLE);
		assertEquals(clientComponent.getStatus(), ComponentStatus.IDLE);
		
		client = manager.getApplicationServiceSpace().findService(SimpleEchoClient.class);
		assertNull(client);
		
		serverComponent.disable();
		//test normal enable
		SimpleEchoLazyComponent.activateWithException = false;
		serverComponent.enable();
		helper.sleepUntilNoTask();
		
		assertEquals(clientComponent.getStatus(), ComponentStatus.SATISFIED);
		
		client = manager.getApplicationServiceSpace().findService(SimpleEchoClient.class);
		assertNotNull(client);
		assertTrue(client.activated);
		assertEquals("client:"+"zhangyx", client.clientCall("zhangyx"));
		assertEquals("zhangyx", client.getEchoService().echo("zhangyx"));
		SimpleEchoLazyComponent echoLazyService = (SimpleEchoLazyComponent) client.getEchoService();
		assertFalse(echoLazyService.destroy);
		//test disable client expect lazy component instance to destroy
		clientComponent.disable();
		helper.sleepUntilNoTask();
		assertTrue(echoLazyService.destroy);
		ComponentInstance ci = serverComponent.getInstance(null);
		FieldReference<ComponentInstance, Object> fa = null;
		try {
			fa = new FieldReference<ComponentInstance, Object>(ci, "service");
		} catch (Throwable e) {
			fail(e.getMessage());
		} 
		assertNull(fa.get());
	}
	
	public static interface LevelService{
		public int getLevel();
	}
	
	//test a complex service space with three levels
	public static interface LevelOneService {
		public int getLevel();
	}
	
	public static interface LevelTwo1Service {
		public int getLevel();
		public LevelOneService getLevelOneService();
	}
	
	public static interface LevelTwo2Service {
		public int getLevel();
		public LevelOneService getLevelOneService();
	}
	
	public static interface LevelThreeService {
		public int getLevel();
		public LevelTwo1Service getLevelTwo1Service();
		public LevelTwo2Service getLevelTwo2Service();
	}
	
	@Service(type=ComponentType.LAZY)
	public static class LevelOneServiceImp implements LevelOneService {
		
		public int getLevel() {
			return 1;
		}
	}
	
	
	@Service
	(type=ComponentType.LAZY)
	public static class LevelTwo1ServiceImp implements LevelTwo1Service {
		
		LevelOneService levelOneService;
		
		public int getLevel() {
			return 2;
		}

		@Ref
		public void setLevelOneService(LevelOneService levelOneService) {
			this.levelOneService = levelOneService;
		}
		
		public LevelOneService getLevelOneService() {
			return levelOneService;
		}
	}
	
	@Service
	public static class LevelTwo2ServiceImp implements LevelTwo2Service {
		
		LevelOneService levelOneService;
		
		public int getLevel() {
			return 2;
		}

		@Ref
		public void setLevelOneService(LevelOneService levelOneService) {
			this.levelOneService = levelOneService;
		}
		
		public LevelOneService getLevelOneService() {
			return levelOneService;
		}
	}
	
	@Service
	public static class LevelThreeServiceImp implements LevelThreeService {

		@Ref
		LevelTwo1Service levelTwo1Service;
		
		@Ref
		LevelTwo2Service levelTwo2Service;
		
		public int getLevel() {
			return 3;
		}

		public LevelTwo1Service getLevelTwo1Service() {
			return levelTwo1Service;
		}

		public LevelTwo2Service getLevelTwo2Service() {
			return levelTwo2Service;
		}
		
		@Activate
		public void start() {
			System.out.println("start LevelThreeServiceImp");
		}
		
	}
	
	
	@Test
	public void testThreeLevelServiceSpace(){
		Class[] imps = {
				LevelOneServiceImp.class,
				LevelThreeServiceImp.class,
				LevelTwo2ServiceImp.class,
				LevelTwo1ServiceImp.class
		};
		for (Class imp : imps){
			manager.add(imp);
		}
		manager.openApplicationServiceSpace();
		Component[] comps = new Component[imps.length];
		for (int i = 0; i < imps.length; i++){
			comps[i] = manager.getApplicationServiceSpace().findComponent(LevelOneServiceImp.class);
		}
		
		for (int i = 0; i < comps.length; i++){
			assertEquals(comps[i].getStatus(), ComponentStatus.SATISFIED);
		}
		
		Object[] services = new Object[comps.length];
		helper.sleepUntilNoTask();//let events complete
		
		for (int i = 0; i < comps.length; i++){
			services[i] = manager.getApplicationServiceSpace().findService( imps[i].getInterfaces()[0] );
			assertNotNull(services[i]);
		}
		
		LevelOneService levelOneService  = (LevelOneService)services[0];
		LevelTwo1Service levelTwo1Service = (LevelTwo1Service) services[3];
		LevelTwo2Service levelTwo2Service = (LevelTwo2Service) services[2];
		LevelThreeService levelThreeService = (LevelThreeService) services[1];
		
		assertEquals(1, levelOneService.getLevel());
		assertEquals(2, levelTwo1Service.getLevel());
		assertEquals(2, levelTwo2Service.getLevel());
		assertEquals(3, levelThreeService.getLevel());
		
		assertEquals(2, levelThreeService.getLevelTwo1Service().getLevel());
		assertEquals(2, levelThreeService.getLevelTwo2Service().getLevel());
		assertEquals(levelTwo1Service, levelThreeService.getLevelTwo1Service());
		assertEquals(levelTwo2Service, levelThreeService.getLevelTwo2Service());
		
		Component  levelTwo2ServiceComponent = comps[2];
		levelTwo2ServiceComponent.disable();
		helper.sleepUntilNoTask();
		
		assertEquals(null, levelThreeService.getLevelTwo1Service());
		assertEquals(null, levelThreeService.getLevelTwo2Service());
		
		
		levelTwo2ServiceComponent.enable();
		helper.sleepUntilNoTask();
		for (int i = 0; i < comps.length; i++){
			services[i] = manager.getApplicationServiceSpace().findService( imps[i].getInterfaces()[0] );
			assertNotNull(services[i]);
		}
		
		levelOneService  = (LevelOneService)services[0];
		levelTwo1Service = (LevelTwo1Service) services[3];
		levelTwo2Service = (LevelTwo2Service) services[2];
		levelThreeService = (LevelThreeService) services[1];
		assertEquals(1, levelOneService.getLevel());
		assertEquals(2, levelTwo1Service.getLevel());
		assertEquals(2, levelTwo2Service.getLevel());
		assertEquals(3, levelThreeService.getLevel());
		
		assertEquals(2, levelThreeService.getLevelTwo1Service().getLevel());
		assertEquals(2, levelThreeService.getLevelTwo2Service().getLevel());
		assertEquals(levelTwo1Service, levelThreeService.getLevelTwo1Service());
		assertEquals(levelTwo2Service, levelThreeService.getLevelTwo2Service());
		helper.sleepUntilNoTask();
//		sleep(1000);
	}
	
	public static class Task {
		boolean isFinished = false;
	}
	
	//test dynamic replace
	public static interface InturnWorker {
		public boolean work(Task task);
	}
	
	@Service
	public static class InturnWorker1 implements InturnWorker {
		public boolean work(Task task) {
			return task.isFinished = true;
		}
	}
	
	@Service(type=ComponentType.LAZY)
	public static class InturnWorker2 implements InturnWorker {
		public boolean work(Task task) {
			return task.isFinished = true;
		}
	}
	
	@Service
	public static class WorkerTaskDispacther {
		
		@Ref
		InturnWorker worker;
		
		public void dispatch(Task task){
			worker.work(task);
		}
		
	}
	
	@Test
	public void testDynamicReplace(){
		manager.add(InturnWorker1.class);
		manager.add(InturnWorker2.class);
		manager.add(WorkerTaskDispacther.class);
		
		manager.openApplicationServiceSpace();
		
		helper.sleepUntilNoTask();
		WorkerTaskDispacther workerTaskDispacther = manager.getApplicationServiceSpace().findService(WorkerTaskDispacther.class);
		InturnWorker firstInturnWorker = workerTaskDispacther.worker;
		assertNotNull(firstInturnWorker);
		
		Component firstInturnWorkerComponent = manager.getApplicationServiceSpace().findComponent(firstInturnWorker.getClass());
		firstInturnWorkerComponent.disable();
		helper.sleepUntilNoTask();
		
		InturnWorker secondInturnWorker = workerTaskDispacther.worker;
		assertNotNull(secondInturnWorker);
		assertNotSame(firstInturnWorker, secondInturnWorker);
		Task task = new Task();
		secondInturnWorker.work(task);
		assertTrue(task.isFinished);
		
		Component secondInturnWorkerComponent = manager.getApplicationServiceSpace().findComponent(secondInturnWorker.getClass());
		secondInturnWorkerComponent.disable();
		helper.sleepUntilNoTask();
		secondInturnWorker = workerTaskDispacther.worker;
		assertNull(secondInturnWorker);
		
		workerTaskDispacther = manager.getApplicationServiceSpace().findService(WorkerTaskDispacther.class);
		assertNull(workerTaskDispacther);
		
		firstInturnWorkerComponent.enable();
		helper.sleepUntilNoTask();
		workerTaskDispacther = manager.getApplicationServiceSpace().findService(WorkerTaskDispacther.class);
		assertNotNull(workerTaskDispacther);
		assertNotNull(workerTaskDispacther.worker);
		assertEquals(firstInturnWorkerComponent.getDefinition().getImplement(), workerTaskDispacther.worker.getClass());
	}
	
	@After
	public void release() {
		manager.getApplicationServiceSpace().close();
		helper.destory();
	}
}


