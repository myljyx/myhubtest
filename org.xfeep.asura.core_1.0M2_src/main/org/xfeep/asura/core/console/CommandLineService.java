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
package org.xfeep.asura.core.console;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;

import org.xfeep.asura.core.Component;
import org.xfeep.asura.core.ComponentContext;
import org.xfeep.asura.core.ComponentInstance;
import org.xfeep.asura.core.DynamicReference;
import org.xfeep.asura.core.Reference;
import org.xfeep.asura.core.ServiceSpace;
import org.xfeep.asura.core.annotation.Activate;
import org.xfeep.asura.core.annotation.Deactivate;
import org.xfeep.asura.core.annotation.Service;

@Service
public class CommandLineService {

	ServiceSpace applicationServiceSpace;
	Thread consoleThread;
	
	@Activate
	public void start(ComponentContext ctx) {
		applicationServiceSpace = ctx.getComponent().getServiceSpace();
//		if (System.console() != null){
		consoleThread = new Thread() {
				public void run() {
					BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
					String cmd = null;
					try {
						while ( (cmd = reader.readLine()) != null ){
							if ("ss".equalsIgnoreCase(cmd)){
								printServices();
							}else if ("shutdown".equalsIgnoreCase(cmd)){
								applicationServiceSpace.close();
//								applicationServiceSpace.getExecutorService().awaitTermination(5, TimeUnit.SECONDS);
							}
						}
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			};
//		}
		consoleThread.setDaemon(true);
		consoleThread.start();
	}
	
	public void printServices(){
		printServices(applicationServiceSpace, System.out, 0);
	}
	
	public void printServices(ServiceSpace space, PrintStream out, int level){
		out.println("ServiceSpace[id="+space.getId()+"], level="+level);
		for (Component c : space.getAllComponent()){
			out.print("\tcomponent["+c.getDefinition().getImplement().getName()+"{");
			for (Class s : c.getDefinition().getInterfaces()){
				out.print(s.getName());
				out.print(",");
			}
			List<ComponentInstance> cis = c.getAllInstancesForDebugInfo();
			out.println("}, status=" + c.getStatus()+", registered " + cis.size()+"]");
			for (Reference r : c.getReferences()){
				out.println("\t\tref[name="+ r.getDefinition().getName() + ", service="
						+r.getServiceClass().getName() +", matcher=" + r.getDetailMatcher()
						+", isSatisfied=" + r.isSatisfied()
						+"]");
			}
			for (ComponentInstance ci : cis){
				out.println("\t\tservice["+ci.getCurrentService()+"], config="+ci.getProperties());
				if (ci.getDynamicReferences() != null){
					out.println("\t\tdynamic references");
					for (DynamicReference dr : ci.getDynamicReferences()){
						out.println("\t\t\tdref[name="+ dr.getDefinition().getName() + ", service="
								+dr.getServiceClass().getName() +", matcher=" + dr.getDetailMatcher()
								+", isSatisfied=" + dr.isSatisfied()
								+"]");
					}
				}
			}
		}
		if (space.getChildren() != null){
			for (ServiceSpace cs : space.getChildren().values()){
				printServices(cs, out, level+1);
			}
		}
	}
	
	
	@Deactivate
	public void shutdown(){
		if (consoleThread != null){
//			System.console().
			//we really can not kill a uninterruptable thread.
//			consoleThread.stop();
		}
	}
	
	
	
	
	
}
