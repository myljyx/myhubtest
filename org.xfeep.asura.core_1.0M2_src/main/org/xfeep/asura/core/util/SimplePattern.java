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
package org.xfeep.asura.core.util;

public class SimplePattern implements Comparable<SimplePattern> {
	
	String[] segs;
	String pattern;
	
	public SimplePattern() {
	}
	
	public int length() {
		return pattern.length();
	}

	public SimplePattern(String pattern) {
		segs = pattern.split("\\*");
		this.pattern = pattern;
	}
	
	public int match(String s){
		try {
			if (segs.length == 0){
				return 0;
			}
			int index = 0;
			for (String sp : segs){
				if (sp.length() > 0) {
						index = s.indexOf(sp, index);
						if (index < 0){
							return -1;
						}
				}
			}
			return 0;
		} catch (Throwable e) {
			return -1;
		}
	}
	
	public String getMatchPrefix(int i){
		StringBuilder sb = new StringBuilder();
		for (int k = 0; k < i; k++){
			sb.append(segs[i]);
		}
		return sb.toString();
	}

	public int compareTo(SimplePattern o) {
		return length() >= o.length() ? 1 : -1;
	}
	
}
