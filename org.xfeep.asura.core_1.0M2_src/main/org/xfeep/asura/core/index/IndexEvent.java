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
package org.xfeep.asura.core.index;

import java.util.EventObject;

import javax.lang.model.type.NullType;

public class IndexEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public static final int INSERT = 1;
	public static final int REMOVE = 1 << 1;
	
	int type;
	
	/**
	 * for user defined serialization or clone 
	 */
	public IndexEvent() {
		super(NullType.class);
	}
	
	public IndexEvent(int type, Object source) {
		super(source);
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	
	
}
