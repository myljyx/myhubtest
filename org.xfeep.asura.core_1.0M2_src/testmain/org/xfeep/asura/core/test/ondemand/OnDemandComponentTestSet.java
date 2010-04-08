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
package org.xfeep.asura.core.test.ondemand;

import static junit.framework.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xfeep.asura.core.Component;
import org.xfeep.asura.core.ComponentContext;
import org.xfeep.asura.core.ComponentFactoryServiceImp;
import org.xfeep.asura.core.ComponentInstance;
import org.xfeep.asura.core.ComponentManager;
import org.xfeep.asura.core.ComponentStatus;
import org.xfeep.asura.core.ComponentType;
import org.xfeep.asura.core.OnDemandComponentInstance;
import org.xfeep.asura.core.annotation.Activate;
import org.xfeep.asura.core.annotation.Config;
import org.xfeep.asura.core.annotation.Property;
import org.xfeep.asura.core.annotation.Deactivate;
import org.xfeep.asura.core.annotation.Destroy;
import org.xfeep.asura.core.annotation.Init;
import org.xfeep.asura.core.annotation.Ref;
import org.xfeep.asura.core.annotation.Service;
import org.xfeep.asura.core.config.ConfigAdminService;
import org.xfeep.asura.core.config.ConfigServiceImp;
import org.xfeep.asura.core.match.Matcher;
import org.xfeep.asura.core.reflect.FieldReference;
import org.xfeep.asura.core.test.pre.TestHelper;




public class OnDemandComponentTestSet {
	
	ComponentManager manager;
	TestHelper helper;
	
	@Before
	public void init(){
		helper = new TestHelper();
		manager = new ComponentManager(helper.getExecutorService());
		helper.appServiceSpace = manager.getApplicationServiceSpace();
	}
	
	//test single on-demand component's life cycle
	
	
	@Service(type=ComponentType.ON_DEMAND)
	public static class SimpleLifeCycleOnDemandComponent {
		
		boolean inited = false;
		boolean activated = false;
		boolean deactivated = false;
		boolean destroyed = false;
		Map<String, Object> onDemandConfig;
		
		@Init
		void init(){
			inited = true;
		}
		
		@SuppressWarnings("unused")
		@Activate
		private void activate(ComponentContext ctx) {
			activated = true;
			onDemandConfig = ctx.getProperties();
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
	public void testOnDemandSimpleLifeCycleComponent() {
		manager.add(SimpleLifeCycleOnDemandComponent.class);
		manager.add(ComponentFactoryServiceImp.class);
		manager.openApplicationServiceSpace();
		
		//test get on demand instance
		Component c = manager.getApplicationServiceSpace().findComponent(SimpleLifeCycleOnDemandComponent.class);
		ComponentInstance ci = c.getInstance(null);
		FieldReference<ComponentInstance, List<OnDemandComponentInstance>> fr = null;
		try {
			fr = new FieldReference<ComponentInstance, List<OnDemandComponentInstance>>(ci, "instances");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		assertEquals(0, fr.get().size());
		SimpleLifeCycleOnDemandComponent s = manager.getApplicationServiceSpace().findService(SimpleLifeCycleOnDemandComponent.class);
		assertNotNull(s);
		assertTrue(s.activated);
		assertTrue(s.inited);
		assertFalse(s.deactivated);
		assertFalse(s.destroyed);
		assertEquals("good", s.same("good"));
		assertEquals(1, fr.get().size());
		
		//test disable component
		c.disable();
		assertTrue(s.deactivated);
		assertTrue(s.destroyed);
		assertNull(c.getInstance(null));
		assertNull(manager.getApplicationServiceSpace().findService(SimpleLifeCycleOnDemandComponent.class));
		
		//test enable again
		c.enable();
		ci = c.getInstance(null);
		try {
			fr = new FieldReference<ComponentInstance, List<OnDemandComponentInstance>>(ci, "instances");
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(0, fr.get().size());
		s = manager.getApplicationServiceSpace().findService(SimpleLifeCycleOnDemandComponent.class);
		assertNotNull(s);
		assertTrue(s.activated);
		assertTrue(s.inited);
		assertFalse(s.deactivated);
		assertFalse(s.destroyed);
		assertEquals("good", s.same("good"));
		assertEquals(1, fr.get().size());
		
		//test service space close
		manager.getApplicationServiceSpace().close();
		assertTrue(s.deactivated);
		assertTrue(s.destroyed);
		assertNull(c.getInstance(null));
		assertNull(manager.getApplicationServiceSpace().findService(SimpleLifeCycleOnDemandComponent.class));
		assertNull(manager.getApplicationServiceSpace().findComponent(SimpleLifeCycleOnDemandComponent.class));
		
	}
	
	//test simple on-demand service and service client without matcher and on-demand requirement
	public static interface SimpleEchoOnDemandService {
		public String echo(String name);
	}
	
	@Service(type=ComponentType.ON_DEMAND)
	public static class SimpleEchoOnDemandComponent implements SimpleEchoOnDemandService{
		
		static volatile boolean activateWithException = false;
		
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
		SimpleEchoOnDemandService echoService;
		
		boolean activated = false;
		
		public String clientCall(String name){
			return "client:" + echoService.echo(name);
		}
		
		public SimpleEchoOnDemandService getEchoService() {
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
	public void testOnDemandSimpleEchoServiceAndClient() {
		manager.add(SimpleEchoClient.class);
		manager.openApplicationServiceSpace();
		Component clientComponent = manager.getApplicationServiceSpace().findComponent(SimpleEchoClient.class);
		assertEquals(clientComponent.getStatus(), ComponentStatus.IDLE);
		SimpleEchoClient client = manager.getApplicationServiceSpace().findService(SimpleEchoClient.class);
		assertNull(client);
		
		//test dynamic add component after service space started
		manager.add(SimpleEchoOnDemandComponent.class);
		helper.sleepUntilNoTask();
		assertEquals(clientComponent.getStatus(), ComponentStatus.SATISFIED);
		
		client = manager.getApplicationServiceSpace().findService(SimpleEchoClient.class);
		assertNotNull(client);
		assertTrue(client.activated);
		assertEquals("client:"+"zhangyx", client.clientCall("zhangyx"));
		assertEquals("zhangyx", client.getEchoService().echo("zhangyx"));
		
		//test disable SimpleEchoComponent
		Component serverComponent = manager.getApplicationServiceSpace().findComponent(SimpleEchoOnDemandComponent.class);
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
		SimpleEchoOnDemandComponent.activateWithException = true;
		serverComponent.enable();
		helper.sleepUntilNoTask();
		assertEquals(serverComponent.getStatus(), ComponentStatus.SATISFIED);
		assertEquals(clientComponent.getStatus(), ComponentStatus.IDLE);
		
		client = manager.getApplicationServiceSpace().findService(SimpleEchoClient.class);
		assertNull(client);
		
		serverComponent.disable();
		//test normal enable
		SimpleEchoOnDemandComponent.activateWithException = false;
		serverComponent.enable();
		helper.sleepUntilNoTask();
		
		assertEquals(clientComponent.getStatus(), ComponentStatus.SATISFIED);
		
		client = manager.getApplicationServiceSpace().findService(SimpleEchoClient.class);
		assertNotNull(client);
		assertTrue(client.activated);
		assertEquals("client:"+"zhangyx", client.clientCall("zhangyx"));
		assertEquals("zhangyx", client.getEchoService().echo("zhangyx"));
		SimpleEchoOnDemandComponent echoLazyService = (SimpleEchoOnDemandComponent) client.getEchoService();
		assertFalse(echoLazyService.destroy);
//		//test disable client expect lazy component instance to destroy
//		clientComponent.disable();
//		helper.sleepUntilNoTask();
//		assertTrue(echoLazyService.destroy);
//		ComponentInstance ci = serverComponent.getInstance(null);
//		FieldReference<ComponentInstance, Object> fa = null;
//		try {
//			fa = new FieldReference<ComponentInstance, Object>(ci, "service");
//		} catch (Throwable e) {
//			fail(e.getMessage());
//		} 
//		assertNull(fa.get());
	}
	
	
	public static class Task {
		boolean isFinished = false;
	}
	

	//test simple matcher and simple on-demand requirement
	public static interface InturnWorker {
		public boolean work(Task task);
		public String getUID();
	}
	
	@Service(type=ComponentType.ON_DEMAND, name="InturnWorkerImp")
	public static class InturnWorkerImp implements InturnWorker {
		public boolean work(Task task) {
			return task.isFinished = true;
		}
		
		String uid;
		
		@Activate
		public void start(ComponentContext ctx){
			Map<String, Object> config = ctx.getProperties();
			if (config != null){
				uid = (String)config.get("uid");
			}
		}

		public String getUID() {
			return uid;
		}
		
	}

	
	@Service
	public static class WorkerTaskDispacther {
		
		@Ref(matcher="uid=1" + Matcher.ON_DEMAND_SEPRATOR + "_name = 'InturnWorkerImp'")
		InturnWorker worker;
		
		public void dispatch(Task task){
			worker.work(task);
		}
		
	}
	
	@Test
	public void testSimpleOnDemandRequirement(){
		manager.add(WorkerTaskDispacther.class);
		manager.getApplicationServiceSpace().setExecutorService(null);
		System.out.println("tmp setExecutorService(null);");
		manager.openApplicationServiceSpace();
		manager.add(InturnWorkerImp.class);
		helper.sleepUntilNoTask();
		WorkerTaskDispacther workerTaskDispacther = manager.getApplicationServiceSpace().findService(WorkerTaskDispacther.class);
		if (workerTaskDispacther == null){
			System.out.println("for debug fail");
		}
		assertNotNull(workerTaskDispacther);
		assertNotNull(workerTaskDispacther.worker);
		assertEquals("1", workerTaskDispacther.worker.getUID());
		
		//test disable
		Component c = manager.getApplicationServiceSpace().findComponent(InturnWorkerImp.class);
		c.disable();
		helper.sleepUntilNoTask();
		assertNull(workerTaskDispacther.worker);
		
		c.enable();
		helper.sleepUntilNoTask();
		
		workerTaskDispacther = manager.getApplicationServiceSpace().findService(WorkerTaskDispacther.class);
		assertNotNull(workerTaskDispacther);
		assertNotNull(workerTaskDispacther.worker);
		assertEquals("1", workerTaskDispacther.worker.getUID());
	}
	
	//test reference with matcher
	public static interface WriteService {
		public String write(String text);
	}
	
	@Service(type=ComponentType.ON_DEMAND)
	public static class WriteServiceOnDemandImp implements WriteService {
		
		String name;
		
		public String write(String text) {
			return name + " :" + text;
		}
		
		@Activate
		public void start(ComponentContext ctx){
			name = (String)ctx.getProperties() .get("name");
		}
	}
	
	
	@Service
	public static class WriteBoss  {
		
		@Ref(matcher="name=Tom" + Matcher.ON_DEMAND_SEPRATOR)
		WriteService tom;
		
		@Ref(matcher="name=Jack" + Matcher.ON_DEMAND_SEPRATOR)
		WriteService jack;
		
		@Activate
		public void start() {
			String text = "who?!";
			System.out.println(tom.write(text));
			System.out.println(jack.write(text));
		}
		
	}
	
	@Test
	public void testReferenceWithMatcher() {
		manager.add(WriteServiceOnDemandImp.class);
		manager.add(WriteBoss.class);
		manager.openApplicationServiceSpace();
		helper.sleepUntilNoTask();
		
		WriteBoss boss = manager.getApplicationServiceSpace().findService(WriteBoss.class);
		assertNotNull(boss);
		assertEquals("Tom :look!", boss.tom.write("look!"));
		assertEquals("Jack :look!", boss.jack.write("look!"));
		
		Component c = manager.getApplicationServiceSpace().findComponent(WriteServiceOnDemandImp.class);
		c.disable();
		helper.sleepUntilNoTask();
		assertNull(boss.tom);
		assertNull(boss.jack);
		boss = manager.getApplicationServiceSpace().findService(WriteBoss.class);
		assertNull(boss);
		
		c.enable();
		helper.sleepUntilNoTask();
		boss = manager.getApplicationServiceSpace().findService(WriteBoss.class);
		assertNotNull(boss);
		assertEquals("Tom :look!", boss.tom.write("look!"));
		assertEquals("Jack :look!", boss.jack.write("look!"));
	}
	
	
	@Service(type=ComponentType.ON_DEMAND)
	public static class WriteServiceOnDemandExceptionImp implements WriteService {
		
		static boolean exception = true;
		String name;
		
		public String write(String text) {
			return name + " :" + text;
		}
		
		@Activate
		public void start(ComponentContext ctx){
			if (exception) {
				throw new RuntimeException("I'm tired :)");
			}
			name = (String)ctx.getProperties() .get("name");
		}
	}
	
	@Test
	public void testReferenceWithMatcherException() {
		manager.add(WriteServiceOnDemandExceptionImp.class);
		manager.add(WriteBoss.class);
		manager.openApplicationServiceSpace();
		helper.sleepUntilNoTask();
		
		WriteBoss boss = manager.getApplicationServiceSpace().findService(WriteBoss.class);
		assertNull(boss);
		
		Component c = manager.getApplicationServiceSpace().findComponent(WriteServiceOnDemandExceptionImp.class);
		c.disable();
		helper.sleepUntilNoTask();
		WriteServiceOnDemandExceptionImp.exception = false;
		c.enable();
		helper.sleepUntilNoTask();
		

		boss = manager.getApplicationServiceSpace().findService(WriteBoss.class);
		assertNotNull(boss);
		assertEquals("Tom :look!", boss.tom.write("look!"));
		assertEquals("Jack :look!", boss.jack.write("look!"));
	}
	
	
	@Service(type=ComponentType.ON_DEMAND)
	@Config("")
	public static class WriteServiceOnDemandImpWithConfig implements WriteService {
		
		@Property
		String name;
		
		public String write(String text) {
			return name + " :" + text;
		}
		
		@Activate
		public void start(ComponentContext ctx){
//			name = (String)ctx.getConfig() .get("name");
		}
	}
	
	
	@Test
	public void testReferenceWithMatcherAndConfig() {
		manager.add(ConfigAdminService.class);
		manager.add(ConfigServiceImp.class);
		manager.openApplicationServiceSpace();

		helper.sleepUntilNoTask();
		
		manager.add(WriteServiceOnDemandImpWithConfig.class);
		manager.add(WriteBoss.class);

		helper.sleepUntilNoTask();
		
		WriteBoss boss = manager.getApplicationServiceSpace().findService(WriteBoss.class);
		assertNotNull(boss);
		assertEquals("Tom :look!", boss.tom.write("look!"));
		assertEquals("Jack :look!", boss.jack.write("look!"));
		
		Component c = manager.getApplicationServiceSpace().findComponent(WriteServiceOnDemandImpWithConfig.class);
		c.disable();
		helper.sleepUntilNoTask();
		assertNull(boss.tom);
		assertNull(boss.jack);
		boss = manager.getApplicationServiceSpace().findService(WriteBoss.class);
		assertNull(boss);
		
		c.enable();
		helper.sleepUntilNoTask();
		boss = manager.getApplicationServiceSpace().findService(WriteBoss.class);
		assertNotNull(boss);
		assertEquals("Tom :look!", boss.tom.write("look!"));
		assertEquals("Jack :look!", boss.jack.write("look!"));
	}
	
	
	
	@After
	public void release() {
		manager.getApplicationServiceSpace().close();
		helper.destory();
	}
}


