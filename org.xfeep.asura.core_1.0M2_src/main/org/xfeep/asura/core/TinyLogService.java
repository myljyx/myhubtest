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
package org.xfeep.asura.core;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xfeep.asura.core.annotation.Service;


@Service
public class TinyLogService {

	
//	Logger log = Logger.getLogger(LogManager.class.getName());
	
//	
//	static LogManager log = new LogManager();
//
//	public static LogManager getInstance() {
//		return log == null ? log = new LogManager() : log;
//	}
	
	public static final String ASURA_LOG_LEVEL = "ASURA_LOG_LEVEL";

	public enum MsgType{ trace, debug,info, warn, error, fatal };
	
	protected MsgType level = MsgType.info;
	
	
	public TinyLogService(){
		String l = System.getProperty(ASURA_LOG_LEVEL);
		if (l != null){
			try{
				level = MsgType.valueOf(l);
			}catch (Exception e) {
				e.printStackTrace();
			}	
		}
	}
	
	public void debug(Object message) {
		message(message, MsgType.debug);
	}

	public void debug(Object message, Throwable t) {
		t.printStackTrace(message(message, MsgType.debug));
	}

	public void error(Object message) {
		message(message, MsgType.error); 
	}

	public void error(Object message, Throwable t) {
		t.printStackTrace(message(message, MsgType.error));
	}

	public void fatal(Object message) {
		message(message, MsgType.fatal);
	}

	public void fatal(Object message, Throwable t) {
		t.printStackTrace(message(message, MsgType.fatal));
	}

	public PrintStream message(Object message, MsgType type){
		if (type.compareTo(level) < 0){
			return System.out;
		}
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuffer s = new StringBuffer();
		StackTraceElement se = null;
		boolean meetCurrentMethod = false;
		for (StackTraceElement si : Thread.currentThread().getStackTrace()){
			if (si.getClassName().equals(TinyLogService.class.getName())){
				meetCurrentMethod = true;
				continue;
			}
			if (meetCurrentMethod){
				se = si;
				break;
			}
		}
		s.append(sf.format(new Date())).append("[").append(type).append("]:")
		.append("[").append(se.getClassName()).append(".").append(se.getMethodName()).append("]:")
		.append(message);
		PrintStream out = System.out;
		if (type != MsgType.debug && type != MsgType.info){
			out = System.err;
		}
		out.println(s);
		return out;
	}
	
	public void info(Object message) {
		message(message, MsgType.info);
	}

	public void info(Object message, Throwable t) {
		message(message, MsgType.info);
		t.printStackTrace(System.out);
	}

	public boolean isDebugEnabled() {
		return level.compareTo(MsgType.debug) <= 0;
	}

	public boolean isErrorEnabled() {
		return level.compareTo(MsgType.error) <= 0;
	}

	public boolean isFatalEnabled() {
		return level.compareTo(MsgType.fatal) <= 0;
	}

	public boolean isInfoEnabled() {
		return level.compareTo(MsgType.info) <= 0;
	}

	public boolean isTraceEnabled() {
		return level.compareTo(MsgType.trace) <= 0;
	}

	public boolean isWarnEnabled() {
		return level.compareTo(MsgType.warn) <= 0;
	}

	public void trace(Object message) {
		message(message, MsgType.trace);
	}

	public void trace(Object message, Throwable t) {
		t.printStackTrace(message(message, MsgType.trace));
	}

	public void warn(Object message) {
		message(message, MsgType.warn);
	}

	public void warn(Object message, Throwable t) {
		t.printStackTrace(message(message, MsgType.warn));
	}


	

}
