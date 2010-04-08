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

public class HashCodePerformance {
	
	public static void classHashCodeStringHashCode() {

		int c = 1000000000;
		Class clz = HashCodePerformance.class;
		@SuppressWarnings("unused")
		String cls = clz.getName();
		long s = System.currentTimeMillis();
		for (int i = 0; i < c; i++){
			clz.hashCode();
		}
		System.out.println("class.hashCode() cost " + (System.currentTimeMillis() - s) + "\t  repeat: " + c);
		s = System.currentTimeMillis();
		for (int i = 0; i < c; i++){
			clz.getName().hashCode();
		}
		System.out.println("String.hashCode() cost " + (System.currentTimeMillis() - s) + "\t  repeat: " + c);
	
	}
	
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}

}
