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
package org.xfeep.asura.core.test.eager;

import static junit.framework.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xfeep.asura.core.Component;
import org.xfeep.asura.core.ComponentInstance;
import org.xfeep.asura.core.ComponentManager;
import org.xfeep.asura.core.ComponentStatus;
import org.xfeep.asura.core.annotation.Activate;
import org.xfeep.asura.core.annotation.Config;
import org.xfeep.asura.core.annotation.Property;
import org.xfeep.asura.core.annotation.Deactivate;
import org.xfeep.asura.core.annotation.Destroy;
import org.xfeep.asura.core.annotation.Init;
import org.xfeep.asura.core.annotation.Ref;
import org.xfeep.asura.core.annotation.Service;
import org.xfeep.asura.core.annotation.ConstProperty;
import org.xfeep.asura.core.annotation.ConstPropertySet;
import org.xfeep.asura.core.config.ConfigAdminService;
import org.xfeep.asura.core.config.ConfigServiceImp;
import org.xfeep.asura.core.console.CommandLineService;
import org.xfeep.asura.core.match.Matcher;
import org.xfeep.asura.core.match.SmartMatcher;
import org.xfeep.asura.core.test.pre.TestHelper;




public class EagerComponentTestSet {
	
	ComponentManager manager;
	TestHelper helper;
	
	@Before
	public void init(){
		helper = new TestHelper();
		manager = new ComponentManager(helper.getExecutorService());
		helper.appServiceSpace = manager.getApplicationServiceSpace();
	}
	
	//test single eager component's life cycle
	
	
	@Service(name="SimpleLifeCycleComponent")
	public static class SimpleLifeCycleComponent {
		
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
	public void testSimpleLifeCycleComponent() {
		manager.add(SimpleLifeCycleComponent.class);
		manager.openApplicationServiceSpace();
		
		SimpleLifeCycleComponent s = manager.getApplicationServiceSpace().findService(SimpleLifeCycleComponent.class);
		assertNotNull(s);
		assertTrue(s.activated);
		assertTrue(s.inited);
		assertFalse(s.deactivated);
		assertFalse(s.destroyed);
		assertEquals("good", s.same("good"));
		
		//test disable component
		Component c = manager.getApplicationServiceSpace().findComponent(SimpleLifeCycleComponent.class);
		c.disable();
		assertTrue(s.deactivated);
		assertTrue(s.destroyed);
		assertNull(c.getInstance(null));
		assertNull(manager.getApplicationServiceSpace().findService(SimpleLifeCycleComponent.class));
		
		//test enable again
		c.enable();
		s = manager.getApplicationServiceSpace().findService(SimpleLifeCycleComponent.class);
		assertNotNull(s);
		Object sn = manager.getApplicationServiceSpace().findService("SimpleLifeCycleComponent");
		assertEquals(sn, s);
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
		assertNull(manager.getApplicationServiceSpace().findService(SimpleLifeCycleComponent.class));
		assertNull(manager.getApplicationServiceSpace().findComponent(SimpleLifeCycleComponent.class));
		
	}
	
	//test simple service and service client
	public static interface SimpleEchoService {
		public String echo(String name);
	}
	
	@Service
	public static class SimpleEchoComponent implements SimpleEchoService{
		
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
		
	}
	
	@Service
	public static class SimpleEchoClient {
		
		@Ref
		SimpleEchoService echoService;
		boolean activated = false;
		
		public String clientCall(String name){
			return "client:" + echoService.echo(name);
		}
		
		public SimpleEchoService getEchoService() {
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
	public void testSimpleEchoServiceAndClient() {
		manager.add(SimpleEchoClient.class);
		manager.openApplicationServiceSpace();
		Component clientComponent = manager.getApplicationServiceSpace().findComponent(SimpleEchoClient.class);
		assertEquals(clientComponent.getStatus(), ComponentStatus.IDLE);
		SimpleEchoClient client = manager.getApplicationServiceSpace().findService(SimpleEchoClient.class);
		assertNull(client);
		
		//test dynamic add component after service space started
		manager.add(SimpleEchoComponent.class);
		helper.sleepUntilNoTask();
		assertEquals(clientComponent.getStatus(), ComponentStatus.SATISFIED);
		
		client = manager.getApplicationServiceSpace().findService(SimpleEchoClient.class);
		assertNotNull(client);
		assertTrue(client.activated);
		assertEquals("client:"+"zhangyx", client.clientCall("zhangyx"));
		assertEquals("zhangyx", client.getEchoService().echo("zhangyx"));
		
		//test disable SimpleEchoComponent
		Component serverComponent = manager.getApplicationServiceSpace().findComponent(SimpleEchoComponent.class);
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
		SimpleEchoComponent.activateWithException = true;
		serverComponent.enable();
		helper.sleepUntilNoTask();
		assertEquals(serverComponent.getStatus(), ComponentStatus.IDLE);
		assertEquals(clientComponent.getStatus(), ComponentStatus.IDLE);
		
		client = manager.getApplicationServiceSpace().findService(SimpleEchoClient.class);
		assertNull(client);
		
		serverComponent.disable();
		//test normal enable
		SimpleEchoComponent.activateWithException = false;
		serverComponent.enable();
		helper.sleepUntilNoTask();
		
		assertEquals(clientComponent.getStatus(), ComponentStatus.SATISFIED);
		
		client = manager.getApplicationServiceSpace().findService(SimpleEchoClient.class);
		assertNotNull(client);
		assertTrue(client.activated);
		assertEquals("client:"+"zhangyx", client.clientCall("zhangyx"));
		assertEquals("zhangyx", client.getEchoService().echo("zhangyx"));
		
	}
	
	//test given  service interface
	
	public static interface  SimpleEchoInterface {
		public String echo2(String name);
	}
	
	@Service(SimpleEchoService.class)
	public static class SimpleEchoImp2 implements SimpleEchoService, SimpleEchoInterface {

		public String echo(String name) {
			return name;
		}

		public String echo2(String name) {
			return name+"2";
		}
		
	}
	
	@Test
	public void testGivenServiceInterface() {
		manager.add(SimpleEchoImp2.class);
		manager.openApplicationServiceSpace();
		helper.sleepUntilNoTask();
		
		assertNotNull(manager.getApplicationServiceSpace().findService(SimpleEchoService.class));
		assertNull(manager.getApplicationServiceSpace().findService(SimpleEchoInterface.class));
		
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
	
	@Service
	public static class LevelOneServiceImp implements LevelOneService {
		
		public int getLevel() {
			return 1;
		}
	}
	
	
	@Service
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
		helper.sleepUntilNoTask();
		Component[] comps = new Component[imps.length];
		for (int i = 0; i < imps.length; i++){
			comps[i] = manager.getApplicationServiceSpace().findComponent(imps[i]);
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
	
	@Service
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
	
	//test reference with matcher
	public static interface WriteService {
		public String write(String text);
	}
	
	@Service(name="Tom")
	public static class TomWriteService implements WriteService {
		public String write(String text) {
			return "Tom :" + text;
		}
	}
	
	@Service(name="Jack")
	public static class JackWriteService implements WriteService {
		public String write(String text) {
			return "Jack :" + text;
		}
	}
	
	@Service
	public static class WriteBoss  {
		
		@Ref(matcher="_name = 'Tom'")
		WriteService tom;
		
		@Ref(matcher="_name = 'Jack'")
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
		manager.add(TomWriteService.class);
		manager.add(WriteBoss.class);
		manager.add(JackWriteService.class);
		manager.openApplicationServiceSpace();
		helper.sleepUntilNoTask();
		
		WriteBoss boss = manager.getApplicationServiceSpace().findService(WriteBoss.class);
		assertNotNull(boss);
		assertEquals("Tom :look!", boss.tom.write("look!"));
		assertEquals("Jack :look!", boss.jack.write("look!"));
		
		Component tomComponent = manager.getApplicationServiceSpace().findComponent(TomWriteService.class);
		tomComponent.disable();
		helper.sleepUntilNoTask();
		assertNull(boss.tom);
		assertNull(boss.jack);
		boss = manager.getApplicationServiceSpace().findService(WriteBoss.class);
		assertNull(boss);
		
		tomComponent.enable();
		helper.sleepUntilNoTask();
		boss = manager.getApplicationServiceSpace().findService(WriteBoss.class);
		assertNotNull(boss);
		assertEquals("Tom :look!", boss.tom.write("look!"));
		assertEquals("Jack :look!", boss.jack.write("look!"));
	}
	
	
	@Service
	public static class StaticMatcherWriteBoss  {
		
		@Ref(matcher=".mymatchTom")
		WriteService tom;
		
		@Ref(matcher=".mymatchJack")
		WriteService jack;
		
		@Activate
		public void start() {
			String text = "who?!";
			System.out.println(tom.write(text));
			System.out.println(jack.write(text));
		}
		
		public static boolean mymatchTom(Map<String, Object> sourceProperties, Map<String,Object> targetProperties) {
			return "Tom".equals(targetProperties.get("_name"));
		}
		
		public static boolean mymatchJack(Map<String, Object> sourceProperties, Map<String,Object> targetProperties) {
			return "Jack".equals(targetProperties.get("_name"));
		}
		
	}
	
	@Test
	public void testReferenceWithStaticMethodMatcher() {
		

		manager.add(TomWriteService.class);
		manager.add(StaticMatcherWriteBoss.class);
		manager.add(JackWriteService.class);
		manager.openApplicationServiceSpace();
		helper.sleepUntilNoTask();
		
		StaticMatcherWriteBoss boss = manager.getApplicationServiceSpace().findService(StaticMatcherWriteBoss.class);
		assertNotNull(boss);
		assertEquals("Tom :look!", boss.tom.write("look!"));
		assertEquals("Jack :look!", boss.jack.write("look!"));
	
		
	}
	
	//test multiple reference
	@Service
	public static class WriteServiceMonitor {
		
		List<WriteService> currentList = new ArrayList<WriteService>();
		
		@Ref(required=false)
		public void catchWriteServices(List<WriteService> currentServices){
			currentList.clear();
			if (currentServices != null){
				currentList.addAll( currentServices );
			}
		}
		
	}
	
	@Test
	public void testMultipleReference() {
		
		manager.add(TomWriteService.class);
		manager.add(JackWriteService.class);
		manager.add(WriteServiceMonitor.class);
		manager.openApplicationServiceSpace();
		
		helper.sleepUntilNoTask();
		
		WriteServiceMonitor monitor = manager.getApplicationServiceSpace().findService(WriteServiceMonitor.class);
		assertNotNull(monitor);
		assertEquals(2, monitor.currentList.size());
		TomWriteService tomWriteService = null;
		JackWriteService jackWriteService = null;
		for (WriteService ws : monitor.currentList){
			if (ws instanceof TomWriteService) {
				tomWriteService = (TomWriteService) ws;
			}else {
				jackWriteService = (JackWriteService) ws;
			}
		}
		assertNotNull(tomWriteService);
		assertNotNull(jackWriteService);
		
		Component tomWriteServiceComponent = manager.getApplicationServiceSpace().findComponent(TomWriteService.class);
		tomWriteServiceComponent.disable();
		helper.sleepUntilNoTask();
		monitor = manager.getApplicationServiceSpace().findService(WriteServiceMonitor.class);
		assertNotNull(monitor);
		assertEquals(1, monitor.currentList.size());
		assertEquals(JackWriteService.class, monitor.currentList.get(0).getClass());
		
		Component jackWriteServiceComponent = manager.getApplicationServiceSpace().findComponent(JackWriteService.class);
		jackWriteServiceComponent.disable();
		helper.sleepUntilNoTask();
		monitor = manager.getApplicationServiceSpace().findService(WriteServiceMonitor.class);
		assertNotNull(monitor);
		assertEquals(0, monitor.currentList.size());
		
		jackWriteServiceComponent.enable();
		helper.sleepUntilNoTask();
		monitor = manager.getApplicationServiceSpace().findService(WriteServiceMonitor.class);
		assertNotNull(monitor);
		assertEquals(1, monitor.currentList.size());
		assertEquals(JackWriteService.class, monitor.currentList.get(0).getClass());
		
		tomWriteServiceComponent.enable();
		helper.sleepUntilNoTask();
		monitor = manager.getApplicationServiceSpace().findService(WriteServiceMonitor.class);
		assertNotNull(monitor);
		assertEquals(2, monitor.currentList.size());
		tomWriteService = null;
		jackWriteService = null;
		for (WriteService ws : monitor.currentList){
			if (ws instanceof TomWriteService) {
				tomWriteService = (TomWriteService) ws;
			}else {
				jackWriteService = (JackWriteService) ws;
			}
		}
		assertNotNull(tomWriteService);
		assertNotNull(jackWriteService);
	}
	
	@After
	public void release() {
		manager.getApplicationServiceSpace().close();
		helper.destory();
	}
	
	//test config 
	@Service
	@Config("writeservice.config")
	public static class WriteServiceWithConfig implements WriteService {
		
		static volatile int activeCount = 0;
		
		@Property
		String writer;
		
		public String write(String text) {
			return writer + ":" + text;
		}
		
		@Activate
		public void activate(){
			activeCount ++;
		}
	}
	
	@Test
	public void testConfigProperty() {
		WriteServiceWithConfig.activeCount = 0;
		manager.add(ConfigAdminService.class);
		manager.add(ConfigServiceImp.class);
		manager.add(WriteServiceWithConfig.class);
		manager.add(CommandLineService.class);
		
		manager.openApplicationServiceSpace();
		
		helper.sleepUntilNoTask();
		ConfigAdminService configAdminService = manager.getApplicationServiceSpace().findService(ConfigAdminService.class);
		assertNotNull(configAdminService);
		WriteService writeService = manager.getApplicationServiceSpace().findService(WriteService.class);
		assertNull(writeService);
		Map<String, Object> config = new HashMap<String, Object>();
		config.put("writer", "zhangyx");
		configAdminService.put("writeservice.config", config);
		
		helper.sleepUntilNoTask();
		writeService = manager.getApplicationServiceSpace().findService(WriteService.class);
		assertNotNull(writeService);
		assertEquals("zhangyx:hello!", writeService.write("hello!"));
		
		//test to use low level API to remove a configuration
		ComponentInstance configComponentInstance = manager.getApplicationServiceSpace().findComponent(ConfigServiceImp.class).getInstance(SmartMatcher.getMatcher(Matcher.ON_DEMAND_CONFIG_ID + "='writeservice.config'"));
		assertNotNull(configComponentInstance);
		configComponentInstance.dispose();
		helper.sleepUntilNoTask();
		writeService = manager.getApplicationServiceSpace().findService(WriteService.class);
		assertNull(writeService);
		
		config.put("writer", "lisi");
		configAdminService.put("writeservice.config", config);
		
		helper.sleepUntilNoTask();
		writeService = manager.getApplicationServiceSpace().findService(WriteService.class);
		assertNotNull(writeService);
		assertEquals("lisi:hello!", writeService.write("hello!"));
		assertEquals(2, WriteServiceWithConfig.activeCount);
		
		//test to use ConfigServiceAdmin new API to remove a configuration
		assertTrue(configAdminService.remove("writeservice.config"));
		helper.sleepUntilNoTask();
		writeService = manager.getApplicationServiceSpace().findService(WriteService.class);
		assertNull(writeService);
		
		config.put("writer", "wangwu");
		assertNotNull(configAdminService.create("writeservice.config", config));
		
		helper.sleepUntilNoTask();
		writeService = manager.getApplicationServiceSpace().findService(WriteService.class);
		assertNotNull(writeService);
		assertEquals("wangwu:hello!", writeService.write("hello!"));
		assertEquals(3, WriteServiceWithConfig.activeCount);
		
	}
	
	//test dynamic ref
	
	@Service
	@Config("WriteBossWithDynamicRef_config")
	public static class WriteBossWithDynamicRef  {
		
//		@Property
//		String ws1;
//		
//		@Property
//		String ws2;
		
		@Property("ws1s._name")
		String ws2sWriter;
		
		@Ref(matcher="_name = $.ws1")
		WriteService ws1s;
		
		@Ref(matcher="_name = $.ws2")
		WriteService ws2s;
		
	}
	
	@Test
	public void testDynamicReference() {
		
		manager.add(ConfigAdminService.class);
		manager.add(ConfigServiceImp.class);
		manager.add(CommandLineService.class);
		manager.add(WriteBossWithDynamicRef.class);
		manager.add(TomWriteService.class);
		manager.add(JackWriteService.class);
		manager.openApplicationServiceSpace();
		helper.sleepUntilNoTask();
		
		ConfigAdminService configAdminService = manager.getApplicationServiceSpace().findService(ConfigAdminService.class);
		assertNotNull(configAdminService);
		WriteBossWithDynamicRef boss = manager.getApplicationServiceSpace().findService(WriteBossWithDynamicRef.class);
		assertNull(boss);
		Map<String, Object> config = new HashMap<String, Object>();
		config.put("ws1", "Tom");
		config.put("ws2", "Jack");
		configAdminService.create("WriteBossWithDynamicRef_config", config);
		helper.sleepUntilNoTask();
		
		boss = manager.getApplicationServiceSpace().findService(WriteBossWithDynamicRef.class);
		assertNotNull(boss);
		
		assertEquals("Tom", boss.ws2sWriter);
		assertEquals("Tom :look!", boss.ws1s.write("look!"));
		assertEquals("Jack :look!", boss.ws2s.write("look!"));
		
		configAdminService.remove("WriteBossWithDynamicRef_config");
		helper.sleepUntilNoTask();
		boss = manager.getApplicationServiceSpace().findService(WriteBossWithDynamicRef.class);
		assertNull(boss);
		
		configAdminService.create("WriteBossWithDynamicRef_config", config);
		helper.sleepUntilNoTask();
		
		boss = manager.getApplicationServiceSpace().findService(WriteBossWithDynamicRef.class);
		assertNotNull(boss);
		assertEquals("Tom :look!", boss.ws1s.write("look!"));
		assertEquals("Jack :look!", boss.ws2s.write("look!"));
		
		

		Component tomComponent = manager.getApplicationServiceSpace().findComponent(TomWriteService.class);
		tomComponent.disable();
		helper.sleepUntilNoTask();
		assertNull(boss.ws1s);
		assertNull(boss.ws2s);
		boss = manager.getApplicationServiceSpace().findService(WriteBossWithDynamicRef.class);
		assertNull(boss);
		
		tomComponent.enable();
		helper.sleepUntilNoTask();
		boss = manager.getApplicationServiceSpace().findService(WriteBossWithDynamicRef.class);
		assertNotNull(boss);
		assertEquals("Tom :look!", boss.ws1s.write("look!"));
		assertEquals("Jack :look!", boss.ws2s.write("look!"));
	}
	
	
	public static interface DictionaryService {
		public String getLanguage();
		public String lookup(String word);
	}
	
	
	@Service
	@Config("english_dictionary_config")
	public static class EnglishDictionaryServiceImp implements DictionaryService {
		
		@Property
		Map<String, String> wordsMap;

		@Property
		String language;// = "english";
		
		public String getLanguage() {
			return language;
		}

		public String lookup(String word) {
			return wordsMap.get(word);
		}
	}

	@Service
	@Config("chinese_dictionary_config")
	public static class ChineseDictionaryServiceImp implements DictionaryService {

		@Property
		Map<String, String> wordsMap;
		
		@Property
		String language;// = "chinese";

		
		public String getLanguage() {
			return language;
		}

		public String lookup(String word) {
			return wordsMap.get(word);
		}
		
	}
	
	@Service
	@Config("LanguageToolService_config")
	public static class LanguageToolService {

		@Property
		String lang;
	
		// we want to get a DictionaryService whose language is the same with the lang property which is injected by configuration
		@Ref(matcher="language=$.lang")  
		DictionaryService dictionaryService;
		
		public DictionaryService getDictionaryService() {
			return dictionaryService;
		}

	}
	
	@Test
	public void testDynamicReferenceAndConfig() {
		manager.add(ConfigAdminService.class);
		manager.add(ConfigServiceImp.class);
		manager.add(CommandLineService.class);
		manager.add(EnglishDictionaryServiceImp.class);
		manager.add(ChineseDictionaryServiceImp.class);
		manager.add(LanguageToolService.class);
		manager.openApplicationServiceSpace();
		helper.sleepUntilNoTask();
		
		ConfigAdminService configAdminService = manager.getApplicationServiceSpace().findService(ConfigAdminService.class);
		assertNotNull(configAdminService);
		
		Map<String, String> englishWordsMap = new HashMap<String, String>();
		englishWordsMap.put("good", "not bad");
		englishWordsMap.put("xfeep asura", "a agile service framework ");
		configAdminService.create("english_dictionary_config", "language", "english", "wordsMap", englishWordsMap);
		
		Map<String, String> chineseWordsMap = new HashMap<String, String>();
		englishWordsMap.put("好", "不坏");
		englishWordsMap.put("xfeep asura", "一个敏捷的服务框架");
		configAdminService.create("chinese_dictionary_config", "language", "chinese", "wordsMap", chineseWordsMap);
		
		configAdminService.create("LanguageToolService_config", "lang", "english");
		
		helper.sleepUntilNoTask();
		
		LanguageToolService languageToolService = manager.getApplicationServiceSpace().findService(LanguageToolService.class);
		assertNotNull(languageToolService);
		assertEquals("english", languageToolService.lang);
		assertEquals("not bad", languageToolService.getDictionaryService().lookup("good"));
		
		configAdminService.update("LanguageToolService_config", "lang", "chinese");
		helper.sleepUntilNoTask();
		
		//so far , we have not implement ContractType.COINCIDENT
//		assertNull(languageToolService.lang);
//		languageToolService = manager.getApplicationServiceSpace().findService(LanguageToolService.class);
		assertEquals("chinese", languageToolService.lang);
		assertEquals("一个敏捷的服务框架", languageToolService.getDictionaryService().lookup("xfeep asura"));
		
	}
	
	@Service
	@Config("english_dictionary_config")
	@ConstPropertySet(@ConstProperty(name="language", value="english"))
	public static class EnglishDictionaryServiceImp2 implements DictionaryService {
		
		@Property
		Map<String, String> wordsMap;
		
		
		public String getLanguage() {
			return "english";
		}

		public String lookup(String word) {
			return wordsMap.get(word);
		}
	}

	@Service
	@Config("chinese_dictionary_config")
	@ConstPropertySet(@ConstProperty(name="language", value="chinese"))
	public static class ChineseDictionaryServiceImp2 implements DictionaryService {

		@Property
		Map<String, String> wordsMap;
		

		
		public String getLanguage() {
			return "chinese";
		}

		public String lookup(String word) {
			return wordsMap.get(word);
		}
		
	}
	
	@Test
	public void testDynamicReferenceAndConfigAndStaticProperty() {
		manager.add(ConfigAdminService.class);
		manager.add(ConfigServiceImp.class);
		manager.add(CommandLineService.class);
		manager.add(EnglishDictionaryServiceImp2.class);
		manager.add(ChineseDictionaryServiceImp2.class);
		manager.add(LanguageToolService.class);
		manager.openApplicationServiceSpace();
		helper.sleepUntilNoTask();
		
		ConfigAdminService configAdminService = manager.getApplicationServiceSpace().findService(ConfigAdminService.class);
		assertNotNull(configAdminService);
		
		Map<String, String> englishWordsMap = new HashMap<String, String>();
		englishWordsMap.put("good", "not bad");
		englishWordsMap.put("xfeep asura", "a agile service framework ");
		configAdminService.create("english_dictionary_config", "wordsMap", englishWordsMap);
		
		Map<String, String> chineseWordsMap = new HashMap<String, String>();
		englishWordsMap.put("好", "不坏");
		englishWordsMap.put("xfeep asura", "一个敏捷的服务框架");
		configAdminService.create("chinese_dictionary_config", "wordsMap", chineseWordsMap);
		
		configAdminService.create("LanguageToolService_config", "lang", "english");
		
		helper.sleepUntilNoTask();
		
		LanguageToolService languageToolService = manager.getApplicationServiceSpace().findService(LanguageToolService.class);
		assertNotNull(languageToolService);
		assertEquals("english", languageToolService.lang);
		assertEquals("not bad", languageToolService.getDictionaryService().lookup("good"));
		
		configAdminService.update("LanguageToolService_config", "lang", "chinese");
		helper.sleepUntilNoTask();
		
		//so far , we have not implement ContractType.COINCIDENT
//		assertNull(languageToolService.lang);
//		languageToolService = manager.getApplicationServiceSpace().findService(LanguageToolService.class);
		assertEquals("chinese", languageToolService.lang);
		assertEquals("一个敏捷的服务框架", languageToolService.getDictionaryService().lookup("xfeep asura"));
		
	}
	
}


