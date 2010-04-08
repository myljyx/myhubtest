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
package org.xfeep.asura.core.test.factory;

import static junit.framework.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xfeep.asura.core.Component;
import org.xfeep.asura.core.ComponentContext;
import org.xfeep.asura.core.ComponentFactoryService;
import org.xfeep.asura.core.ComponentFactoryServiceImp;
import org.xfeep.asura.core.ComponentInstance;
import org.xfeep.asura.core.ComponentManager;
import org.xfeep.asura.core.ComponentType;
import org.xfeep.asura.core.CoreConsts;
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
import org.xfeep.asura.core.config.Configuration;
import org.xfeep.asura.core.console.CommandLineService;
import org.xfeep.asura.core.match.Matcher;
import org.xfeep.asura.core.match.SmartMatcher;
import org.xfeep.asura.core.test.eager.EagerComponentTestSet.WriteService;
import org.xfeep.asura.core.test.pre.TestHelper;




public class FactoryComponentTestSet {
	
	ComponentManager manager;
	TestHelper helper;
	
	@Before
	public void init(){
		helper = new TestHelper();
		manager = new ComponentManager(helper.getExecutorService());
		helper.appServiceSpace = manager.getApplicationServiceSpace();
		manager.add(ConfigAdminService.class);
		manager.add(ConfigServiceImp.class);
	}
	
	//test single eager component's life cycle
	
	
	@Service(type=ComponentType.FACTORY)
	public static class SimpleLifeCycleFactoryComponent {
		
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
	public void testFactorySimpleLifeCycleComponent() {
		manager.add(SimpleLifeCycleFactoryComponent.class);
		manager.add(ComponentFactoryServiceImp.class);
		manager.openApplicationServiceSpace();
		helper.sleepUntilNoTask();
		//test get factory sample instance
		Component c = manager.getApplicationServiceSpace().findComponent(SimpleLifeCycleFactoryComponent.class);
		ComponentInstance sampleInstance = c.getInstance(SmartMatcher.FACTORY_SAMPLE_INSTANCE_MATCHER);
		assertNotNull(sampleInstance);
		ComponentFactoryService componentFactoryService = manager.getApplicationServiceSpace().findService(ComponentFactoryService.class);
		Map<String, Object> goodConfig = new HashMap<String, Object>();
		goodConfig.put("name", "good");
		componentFactoryService.newService(sampleInstance.getService(), "config", goodConfig);
		helper.sleepUntilNoTask();
		
		SimpleLifeCycleFactoryComponent s = manager.getApplicationServiceSpace().findService(SimpleLifeCycleFactoryComponent.class);
		assertNotNull(s);
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
		assertNull(manager.getApplicationServiceSpace().findService(SimpleLifeCycleFactoryComponent.class));
		
		//test enable again
		c.enable();
		sampleInstance = c.getInstance(SmartMatcher.FACTORY_SAMPLE_INSTANCE_MATCHER);
		assertNotNull(sampleInstance);
		componentFactoryService.newService(sampleInstance.getService(), goodConfig);
		helper.sleepUntilNoTask();
		s = manager.getApplicationServiceSpace().findService(SimpleLifeCycleFactoryComponent.class);
		assertNotNull(s);
		assertTrue(s.activated);
		assertTrue(s.inited);
		assertFalse(s.deactivated);
		assertFalse(s.destroyed);
		assertEquals("good", s.same("good"));
		
		//test service space close
		manager.getApplicationServiceSpace().close();
		assertTrue(s.deactivated);
		assertTrue(s.destroyed);
		assertNull(c.getInstance(null));
		assertNull(manager.getApplicationServiceSpace().findService(SimpleLifeCycleFactoryComponent.class));
		assertNull(manager.getApplicationServiceSpace().findComponent(SimpleLifeCycleFactoryComponent.class));
		
	}
	
	//test simple service and service client
	public static interface SimpleEchoFactoryService {
		public String echo(String name);
	}
	
	@Service(type=ComponentType.FACTORY)
	public static class SimpleEchoFactoryComponent implements SimpleEchoFactoryService{
		
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
		SimpleEchoFactoryService echoService;
		boolean activated = false;
		
		public String clientCall(String name){
			return "client:" + echoService.echo(name);
		}
		
		public SimpleEchoFactoryService getEchoService() {
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
	
	
	public static class Task {
		boolean isFinished = false;
	}
	
//test newInstance of factory component
	public static interface InturnWorker {
		public boolean work(Task task);
		public String getUID();
	}
	
	public static class WorkerConfig {
		public String uid;
		public String name;
		public String getUid() {
			return uid;
		}
		public void setUid(String uid) {
			this.uid = uid;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		
		
	}
	
	@Service(type=ComponentType.FACTORY)
	public static class InturnWorkerImp  implements InturnWorker {
		public boolean work(Task task) {
			return task.isFinished = true;
		}
		
		String uid;
		String name;
		
		@Activate
		public void start(ComponentContext ctx){
			WorkerConfig config = (WorkerConfig)ctx.getProperties().get("config");
			if (config != null){
				uid = config.uid;
			}
		}

		public String getUID() {
			return uid;
		}
		
	}

	
	@Service
	public static class WorkerTaskDispacther {
		
		@Ref(matcher="config.uid='1'")
		InturnWorker worker;
		
		public void dispatch(Task task){
			worker.work(task);
		}
		
	}
	
	@Test
	public void testDynamicReplace(){
		manager.add(ComponentFactoryServiceImp.class);
		manager.add(InturnWorkerImp.class);
		manager.add(WorkerTaskDispacther.class);
		
		manager.openApplicationServiceSpace();
		
		helper.sleepUntilNoTask();
		WorkerTaskDispacther workerTaskDispacther = manager.getApplicationServiceSpace().findService(WorkerTaskDispacther.class);
		assertNull(workerTaskDispacther);
		
		ComponentFactoryService componentFactoryService = manager.getApplicationServiceSpace().findService(ComponentFactoryService.class);
		InturnWorker sampleWorker = manager.getApplicationServiceSpace().findService(InturnWorker.class, SmartMatcher.FACTORY_SAMPLE_INSTANCE_MATCHER);
		WorkerConfig config = new WorkerConfig();
		config.uid = "1";
		ComponentInstance ci1 =componentFactoryService.newService(sampleWorker,"config", config);
		helper.sleepUntilNoTask();
		
		workerTaskDispacther = manager.getApplicationServiceSpace().findService(WorkerTaskDispacther.class);
		assertNotNull(workerTaskDispacther);
		assertNotNull(workerTaskDispacther.worker);
		assertEquals("1", workerTaskDispacther.worker.getUID());
		
		//test dispose
		ci1.dispose();
		helper.sleepUntilNoTask();
		assertNull(workerTaskDispacther.worker);
		
		ComponentInstance ci2 =componentFactoryService.newService(sampleWorker, "config", config);
		assertNotNull(ci2);
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
	
	@Service(type=ComponentType.FACTORY)
	public static class WriteServiceFacotryImp implements WriteService {
		
		String name;
		
		public String write(String text) {
			return name + " :" + text;
		}
		
		@Activate
		public void start(ComponentContext ctx){
			name = ((WorkerConfig) ctx.getProperties().get("config") ).name;
		}
	}
	
	@Service
	public static class WriteCreatorClient {
		@Ref(matcher=CoreConsts.FACTORY_SAMPLE_INSTANCE_MATCHER)
		WriteService sample;
		
		@Ref
		ComponentFactoryService componentFactoryService;
		
		@Activate
		public void start() {
			WorkerConfig tomConfig = new WorkerConfig();
			tomConfig.name = "Tom";
			ComponentInstance tomci = componentFactoryService.newService(sample, "config", tomConfig);
			if (!tomci.isActive() || tomci.getService() == null){
				System.out.println("fail");
			}
			WorkerConfig jackConfig = new WorkerConfig();
			jackConfig.name = "Jack";
			componentFactoryService.newService(sample, "config", jackConfig);
		}
		
	}
	
	@Service
	public static class WriteBoss  {
		
		@Ref(matcher="config.name='Tom'")
		WriteService tom;
		
		@Ref(matcher="config.name='Jack'")
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
		manager.add(ComponentFactoryServiceImp.class);
		manager.add(WriteServiceFacotryImp.class);
		manager.add(WriteBoss.class);
		manager.add(WriteCreatorClient.class);
		manager.openApplicationServiceSpace();
		helper.sleepUntilNoTask();
		
		WriteBoss boss = manager.getApplicationServiceSpace().findService(WriteBoss.class);
		if (boss == null){
			System.out.println("fail");
		}
		assertNotNull(boss);
		assertEquals("Tom :look!", boss.tom.write("look!"));
		assertEquals("Jack :look!", boss.jack.write("look!"));
	}
	
	//test config
	
	
	@Service(type=ComponentType.FACTORY)
	@Config("")
	public static class WriteServiceFacotryWithConfig implements WriteService {
		
		@Property
		String name;
		
		public String write(String text) {
			return name + " :" + text;
		}
		
	}
	
	@Service
	public static class WriteCreatorWithConfigClient {
		@Ref(matcher=CoreConsts.FACTORY_SAMPLE_INSTANCE_MATCHER)
		WriteService sample;
		
		@Ref
		ComponentFactoryService componentFactoryService;
		
		@Activate
		public void start() {
			componentFactoryService.newService(sample, "name", "Tom");
			componentFactoryService.newService(sample, "name", "Jack");
		}
		
	}
	
	@Service
	public static class WriteBossWithConfig  {
		
		@Ref(matcher="name='Tom'")
		WriteService tom;
		
		@Ref(matcher="name='Jack'")
		WriteService jack;
		
		@Activate
		public void start() {
			String text = "who?!";
			System.out.println(tom.write(text));
			System.out.println(jack.write(text));
		}
		
	}
	
	@Test
	public void testReferenceWithMatcherAndConfig() {
		manager.add(ConfigAdminService.class);
		manager.add(ConfigServiceImp.class);
		manager.add(CommandLineService.class);
		manager.add(ComponentFactoryServiceImp.class);
		manager.add(WriteServiceFacotryWithConfig.class);
		manager.add(WriteBossWithConfig.class);
//		manager.add(WriteCreatorWithConfigClient.class);
		manager.openApplicationServiceSpace();
		helper.sleepUntilNoTask();
		
		manager.add(WriteCreatorWithConfigClient.class);
		
		helper.sleepUntilNoTask();
		
		WriteBossWithConfig boss = manager.getApplicationServiceSpace().findService(WriteBossWithConfig.class);
		assertNotNull(boss);
		assertEquals("Tom :look!", boss.tom.write("look!"));
		assertEquals("Jack :look!", boss.jack.write("look!"));
	}
	
	
	@Test
	public void testReferenceWithMatcherAndDynamicConfig() {
		manager.add(ConfigAdminService.class);
		manager.add(ConfigServiceImp.class);
		manager.add(CommandLineService.class);
		manager.add(ComponentFactoryServiceImp.class);
		manager.add(WriteServiceFacotryWithConfig.class);
		manager.add(WriteBossWithConfig.class);
//		manager.add(WriteCreatorWithConfigClient.class);
		manager.openApplicationServiceSpace();
		helper.sleepUntilNoTask();
		
		WriteBossWithConfig boss = manager.getApplicationServiceSpace().findService(WriteBossWithConfig.class);
		assertNull(boss);
		
		ComponentFactoryService componentFactoryService = manager.getApplicationServiceSpace().findService(ComponentFactoryService.class);
		WriteService sample = manager.getApplicationServiceSpace().findService(WriteService.class, CoreConsts.FACTORY_SAMPLE_INSTANCE_MATCHER);
		assertNotNull(sample);
		
		ConfigAdminService configAdminService = manager.getApplicationServiceSpace().findService(ConfigAdminService.class);
		Map<String, Object> writeTomConfig = new HashMap<String, Object>();
		writeTomConfig.put("name", "Tom");
		configAdminService.put("WriteService.tom-config", writeTomConfig);
		Map<String, Object> writeJackConfig = new HashMap<String, Object>();
		writeJackConfig.put("name", "Jack");
		configAdminService.put("WriteService.jack-config", writeJackConfig);
		helper.sleepUntilNoTask();
		
		ComponentInstance tomComponentInstance = componentFactoryService.newService(sample, Matcher.ON_DEMAND_CONFIG_ID, "WriteService.tom-config");
		helper.sleepUntilNoTask();
		WriteService tomService = manager.getApplicationServiceSpace().findService(WriteService.class, "name='Tom'");
		assertNotNull(tomService);
		boss = manager.getApplicationServiceSpace().findService(WriteBossWithConfig.class);
		assertNull(boss);
			
		ComponentInstance jackComponentInstance = componentFactoryService.newService(sample, Matcher.ON_DEMAND_CONFIG_ID, "WriteService.jack-config");
		helper.sleepUntilNoTask();
		WriteService jackService = manager.getApplicationServiceSpace().findService(WriteService.class, "name='Jack'");
		assertNotNull(jackService);
		
		boss = manager.getApplicationServiceSpace().findService(WriteBossWithConfig.class);
		assertNotNull(boss);
		assertEquals("Tom :look!", boss.tom.write("look!"));
		assertEquals("Jack :look!", boss.jack.write("look!"));
		
		jackComponentInstance.dispose();
		helper.sleepUntilNoTask();
		jackService = manager.getApplicationServiceSpace().findService(WriteService.class, "name='Jack'");
		assertNull(jackService);
		assertNull(boss.jack);
		assertNull(boss.tom);
		boss = manager.getApplicationServiceSpace().findService(WriteBossWithConfig.class);
		assertNull(boss);
		
		tomComponentInstance.dispose();
		helper.sleepUntilNoTask();
		tomService = manager.getApplicationServiceSpace().findService(WriteService.class, "name='Tom'");
		assertNull(tomService);
		
		tomComponentInstance = componentFactoryService.newService(sample, Matcher.ON_DEMAND_CONFIG_ID, "WriteService.tom-config");
		jackComponentInstance = componentFactoryService.newService(sample, Matcher.ON_DEMAND_CONFIG_ID, "WriteService.jack-config");
		helper.sleepUntilNoTask();
		boss = manager.getApplicationServiceSpace().findService(WriteBossWithConfig.class);
		assertNotNull(boss);
		assertEquals("Tom :look!", boss.tom.write("look!"));
		assertEquals("Jack :look!", boss.jack.write("look!"));
	}
	
	
	//test foctory with dynamic reference
	@Service(type=ComponentType.FACTORY)
	@Config("WriteServiceFacotryWithDynamicRef_globalConfig")
	public static class WriteServiceFacotryWithDynamicRef implements WriteService {
		
		@Property
		String wname;
		
		@Property
		String wid;
		
		@Ref(matcher="_name=$.gwname")
		InturnWorker gworker;
		
		
		@Ref(matcher="_name=$.wname")
		InturnWorker worker;
		
		public String write(String text) {
			return wname + " :" + text;
		}
		
	} 
	
	@Service(name="Tom")
	public static class TomWorker implements InturnWorker {
		
		public String getUID() {
			return "Tom";
		}

		public boolean work(Task task) {
			return true;
		}
	}
	
	@Service(name="Jack")
	public static class JackWorker implements InturnWorker {
		
		public String getUID() {
			return "Jack";
		}

		public boolean work(Task task) {
			return true;
		}
	}
	
	
	@Service(name="Wood")
	public static class WoodWorker implements InturnWorker {
		
		public String getUID() {
			return "Wood";
		}

		public boolean work(Task task) {
			return true;
		}
	}
	
	
	@Service
	@Config("WriteBossWithDynamicRef_config")
	public static class WriteBossWithDynamicRef  {
		
		@Ref(matcher="wid = $.ws1")
		WriteService ws1s;
		
		@Ref(matcher="wid = $.ws2")
		WriteService ws2s;
		
	}
	
	@Test
	public  void testFacotryWithDynamicRef () {
		manager.add(ConfigAdminService.class);
		manager.add(ConfigServiceImp.class);
		manager.add(CommandLineService.class);
		manager.add(ComponentFactoryServiceImp.class);
		manager.add(WriteServiceFacotryWithDynamicRef.class);
		manager.add(TomWorker.class);
		manager.add(JackWorker.class);
		manager.add(WoodWorker.class);
		manager.add(WriteBossWithDynamicRef.class);
//		manager.add(WriteCreatorWithConfigClient.class);
		manager.openApplicationServiceSpace();
		helper.sleepUntilNoTask();
		
		ComponentFactoryService componentFactoryService = manager.getApplicationServiceSpace().findService(ComponentFactoryService.class);
		WriteService sample = manager.getApplicationServiceSpace().findService(WriteService.class, CoreConsts.FACTORY_SAMPLE_INSTANCE_MATCHER);
		assertNull(sample);
		
		Map<String, Object> writeTomConfig = new HashMap<String, Object>();
		writeTomConfig.put("gwname", "Tom");
		writeTomConfig.put("wid", "000");
		ConfigAdminService configAdminService = manager.getApplicationServiceSpace().findService(ConfigAdminService.class);
		configAdminService.create("WriteServiceFacotryWithDynamicRef_globalConfig", writeTomConfig);
		helper.sleepUntilNoTask();
		
		sample = manager.getApplicationServiceSpace().findService(WriteService.class, CoreConsts.FACTORY_SAMPLE_INSTANCE_MATCHER);
		assertNotNull(sample);
		
		ComponentInstance ci = componentFactoryService.newService(sample, "wname", "Jack", "wid", "001");
		helper.sleepUntilNoTask();
		assertEquals("001", ci.getProperties().get("wid"));
		WriteServiceFacotryWithDynamicRef dynamicRefWriteService= (WriteServiceFacotryWithDynamicRef) manager.getApplicationServiceSpace().findService(WriteService.class);
		assertNotNull(dynamicRefWriteService);
		assertEquals("Jack :good!", dynamicRefWriteService.write("good!"));
		assertEquals("Jack :good!", dynamicRefWriteService.write("good!"));
		assertEquals("Tom", dynamicRefWriteService.gworker.getUID());
		assertEquals("Jack", dynamicRefWriteService.worker.getUID());
		assertEquals("001", dynamicRefWriteService.wid);
		
		Map<String, Object> bossConfig = new HashMap<String, Object>();
		bossConfig.put("ws1", "001");
		bossConfig.put("ws2", "002");
		configAdminService.create("WriteBossWithDynamicRef_config", bossConfig);
		helper.sleepUntilNoTask();
		
		Component writeBossComponent = manager.getApplicationServiceSpace().findComponent(WriteBossWithDynamicRef.class);
		assertNotNull(writeBossComponent);
		WriteBossWithDynamicRef boss = manager.getApplicationServiceSpace().findService(WriteBossWithDynamicRef.class);
		assertNull(boss);
		
		ci = componentFactoryService.newService(sample, "wname", "Wood", "wid", "002");
		helper.sleepUntilNoTask();
		boss = manager.getApplicationServiceSpace().findService(WriteBossWithDynamicRef.class);
		assertNotNull(boss);
		
		assertEquals("001", ( (WriteServiceFacotryWithDynamicRef) boss.ws1s).wid);
		assertEquals("002", ( (WriteServiceFacotryWithDynamicRef) boss.ws2s).wid);
		
	}
	
	@After
	public void release() {
		manager.getApplicationServiceSpace().close();
		helper.destory();
	}
}


