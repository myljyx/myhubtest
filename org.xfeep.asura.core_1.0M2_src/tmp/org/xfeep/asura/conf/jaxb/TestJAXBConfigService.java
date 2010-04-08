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
package org.xfeep.asura.conf.jaxb;


import java.io.File;
import java.net.URISyntaxException;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xfeep.asura.core.ComponentManager;
import org.xfeep.asura.core.test.pre.TestHelper;

import static junit.framework.Assert.*;

public class TestJAXBConfigService {

	ComponentManager manager;
	TestHelper helper;
	
	@Before
	public void init() throws URISyntaxException{
		helper = new TestHelper();
		manager = new ComponentManager(helper.getExecutorService());
		System.setProperty(JAXBConfService.ASURA_CONFIG_JAXB_HOME, new File(this.getClass().getResource("TestJAXBConfigService.class").toURI()).getParentFile().getAbsolutePath());
		
	}

	@XmlRootElement(name="HttpConfig")
	@XmlType(propOrder={"serverName", "port", "maxThread", "minThread", "sessionTimeout"})
	public static class JAXBHttpConfig {
		
		String serverName;
		int port;
		int maxThread;
		int minThread;
		int sessionTimeout;
		
		public String getServerName() {
			return serverName;
		}
		public void setServerName(String serverName) {
			this.serverName = serverName;
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
		public int getMaxThread() {
			return maxThread;
		}
		public void setMaxThread(int maxThread) {
			this.maxThread = maxThread;
		}
		public int getMinThread() {
			return minThread;
		}
		public void setMinThread(int minThread) {
			this.minThread = minThread;
		}
		public int getSessionTimeout() {
			return sessionTimeout;
		}
		public void setSessionTimeout(int sessionTimeout) {
			this.sessionTimeout = sessionTimeout;
		}
		
		
	}
	
	@Test
	public void testGetSimpleConfig() {
		manager.add(JAXBConfServiceImp.class);
		manager.openApplicationServiceSpace();
		JAXBConfService jcs = manager.getApplicationServiceSpace().findService(JAXBConfService.class);
		JAXBHttpConfig config = jcs.getConfig("http", JAXBHttpConfig.class);
		assertNotNull(config);
		assertEquals("MyHttpServer", config.getServerName());
		assertEquals(8080, config.getPort());
		assertEquals(120, config.getMaxThread());
		assertEquals(2, config.getMinThread());
		assertEquals(20, config.getSessionTimeout());
	}
	
	@After
	public void release() {
		manager.getApplicationServiceSpace().close();
		helper.destory();
	}

}
