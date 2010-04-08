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

public interface JAXBConfService {

	public static final String ASURA_CONFIG_JAXB_HOME = "asura_config_jaxb_home";

	public abstract <C> C getConfig(final String name,
			final Class<C> configObjectType);

	public abstract File getConfigHome();

}