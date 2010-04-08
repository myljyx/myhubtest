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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CountLine {

	int messageLevel = 6;
	
	public CountLine(int messageLevel) {
		this.messageLevel = messageLevel;
	}
	
	public int[] count(File dir) throws IOException {
		return count(dir, 0);
	}
	
	public int[] count(File dir, int level) throws IOException{
		int c = 0;
		int sc = 0;
		int size = 0;
		for (String f : dir.list()){
			File file = new File(dir, f);
			if (file.isDirectory()){
				int[] sf =  count(file, level+1);
				c += sf[0];
				size += sf[1];
			}
			if (f.endsWith(".java")){
				InputStream in = new FileInputStream(file);
				size += in.available();
				byte[] buf = new byte[4096];
				int rc = 0;
				while ( (rc = in.read(buf)) > 0 ){
					for (int i = 0; i < rc; i++){
						if (buf[i] == '\n'){
							c ++;
							sc ++;
						}
					}
				}
				in.close();
			}
		}
		if (level <= messageLevel){
			System.out.println(c + "\t" + sc + "\t" + size + "\t"+ dir.getAbsolutePath());
		}
		
		return new int[]{ c, size };
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2){
			System.out.println("need two args: level and file");
			return;
		}
		CountLine cl = new CountLine(Integer.parseInt(args[0]));
		try {
//			cl.count(new File("/Applications/eclipse34/workspace/ClassFileTransformer/src"));
//			cl.count(new File("/Applications/eclipse34/workspace/org.knopflerfish.bundle.componet"));
//			cl.count(new File("/Applications/eclipse34/workspace/org.eclipse.equinox.ds"));
			cl.count(new File(args[1]));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
