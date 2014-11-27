/*******************************************************************************
 * Copyright 2014 Luca Falsina
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package it.necst.grabnrun;

import java.io.File;
import java.io.FileFilter;

/**
 * A simple implementation of the {@link FileFilter} interface which will accept
 * only those files whose name and extension match the required one
 * stated during object creation.
 * 
 * @author Luca Falsina
 */
final class FileFilterByName implements FileFilter {

	private String name;
	private String extension;

	/**
	 * Instantiate a {@link FileFilterByName} which will look
	 * for files with the provided name and extension.
	 * 
	 * @param name
	 *  the file name.
	 * @param extension
	 *  the file extension.
	 */
	FileFilterByName(String name, String extension) {
		
		this.name = name;
		this.extension = extension;
	}
	
	@Override
	public final boolean accept(File file) {
		
		// If the file is a directory is not a
		// certificate for sure..
		if (file.isDirectory())
			return false;
		else if (file.isFile()) {
					
			// On the contrary if this is a normal file and its name is
			// the desired one and it ends with one of the 
			// approved extensions then it's fine.
			if (file.getName().equals(name + extension))
		    	  return true;
		}
				
		// Used for any other kind of weird stuff reaching the filter..
		return false;
	}

}
