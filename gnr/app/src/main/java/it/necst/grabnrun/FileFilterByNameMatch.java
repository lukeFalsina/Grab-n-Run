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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

/**
 * A simple implementation of the {@link FileFilter} interface that will accept
 * only those files whose name and extension match the required one
 * stated during object creation.
 * 
 * @author Luca Falsina
 */
class FileFilterByNameMatch implements FileFilter {
    private static final String DOT_FOLLOWED_BY_THREE_OR_FOUR_CHARACTERS = ".[a-z]{3,4}";
    private final String name;
	private final String extension;

	/**
	 * Instantiate a {@link FileFilterByNameMatch} that will accept
	 * files matching the provided name and extension.
	 * 
	 * @param name
	 *  the file name.
	 * @param extension
	 *  the file extension in the format ".????" (e.g., ".txt", or ".DOCX").
	 */
	FileFilterByNameMatch(@NonNull String name, @NonNull String extension) {
		this.name = checkNotNull(name, "The name of the target file was null");
		checkArgument(!name.isEmpty(), "The file name must not be empty");
		this.extension = checkNotNull(extension, "The extension of the target file was null");
        checkArgument(
                extension.toLowerCase(Locale.US).matches(DOT_FOLLOWED_BY_THREE_OR_FOUR_CHARACTERS),
                "The extension must be one dot, followed by three, " +
                        "or fours alphabetical characters");
	}
	
	@Override
	public final boolean accept(@NonNull File file) {
		checkNotNull(file, "The input file descriptor was null");
		if (file.isDirectory())
			return false;
		else if (file.isFile()) {
			if (file.getName().equals(name + extension))
		    	  return true;
		}
				
		// Used for any other kind of weird stuff reaching the filter..
		return false;
	}
}
