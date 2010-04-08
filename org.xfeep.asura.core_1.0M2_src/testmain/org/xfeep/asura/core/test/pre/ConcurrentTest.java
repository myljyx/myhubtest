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

import org.xfeep.asura.core.util.ReadWriteProtectedArrayList;
import org.xfeep.asura.core.util.ReadWriteProtectedList;

public class ConcurrentTest {

	
	public static void testIllegeAccessList() {
		
		ReadWriteProtectedList<String> list = new ReadWriteProtectedArrayList<String>();
		list.add("good");
		for (String s : list){
			System.out.println(s);
		}
		
	}
	
	public static void testNormalAccessList() {
		ReadWriteProtectedList<String> list = new ReadWriteProtectedArrayList<String>();
		list.add("good");
		list.getReadLock().lock();
		try{
			for (String s : list){
				System.out.println(s);
			}
		}finally{
			list.getReadLock().unlock();
		}
		System.out.println("ok, test normal ");
	}
	
	public static void main(String[] args) {
		try{
			testIllegeAccessList(); //cause exeption
		}catch (Exception e) {
			System.out.println("ok, get exception " + e);
		}
		testNormalAccessList();
		
	}
	
}
