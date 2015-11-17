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

import android.util.Log;

import com.google.common.base.Optional;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link PackageNameTrie} is an helper class used to keep a hierarchy among package names and certificates.
 * <p>
 * In particular when a certificate is associated to a package name, all the descendant package names of the 
 * previous one should be able to verify the signature of their classes with the same certificate.
 * <p>
 * Example:<br>
 * Package name "com.example" has certificate "CertA" associated to it.<br>
 * Package names "com.example.polimi" and "com.example.application.system" can use "CertA" as well.<br>
 * Package names "com" and "it.example" cannot use "CertA" for their classes verification.
 * 
 * @author Luca Falsina
 */
final class PackageNameTrie {

	// Unique identifier used for Log entries
	private static final String TAG_PACKAGE_NAME_TRIE = PackageNameTrie.class.getSimpleName();
	
	// Map which links the package name string to the associated node
	private Map<String, Boolean> packageNameToHasCertificateMap;
	
	/**
	 * Basic constructor that initializes internal data structures.
	 */
	PackageNameTrie() {
		
		packageNameToHasCertificateMap = new HashMap<>();
		// Add also the root entry and suppose that it has 
		// always an associated certificate.
		packageNameToHasCertificateMap.put("", true);
	}
	
	/**
	 * This method generates an internal hierarchy representation of a provided package name.
	 * In particular it will generate all not empty prefixes of the input package name.
	 * <p>
	 * Example execution:<br>
	 * Input: "com.example.polimi.application"<br>
	 * Internal Hierarchy generates the strings:<br>
	 * "com.example.polimi.application", "com.example.polimi", "com.example" and "com".
	 * 
	 * @param packageName
	 *  the package name for which the hierarchical structure of package name prefix will be generated.
	 */
	final void generateEntriesForPackageName(String packageName) {
		
		String currentPackageName = packageName;
		boolean hasFoundAnAlreadyInsertedPackageName = false;
		
		while (!hasFoundAnAlreadyInsertedPackageName) {
			
			if (packageNameToHasCertificateMap.containsKey(currentPackageName)) {
				
				// In this case this entry has been already inserted in the map
				// so the process of package name generation stops here.
				hasFoundAnAlreadyInsertedPackageName = true;
				
			} else {
				
				// Need to insert this entry by populating the map accordingly
				packageNameToHasCertificateMap.put(currentPackageName, false);
				
				Log.d(TAG_PACKAGE_NAME_TRIE, "Inserted a new entry for " + currentPackageName);
				
				// Now remove the last part of the package name and then
				// repeat the previous step recursively.
				currentPackageName = getUpALevel(currentPackageName);
			}
		}
	}
	
	// Remove the last part of the package name.
	// If nothing more is left after this removal, return an empty string.
	private String getUpALevel(String packageName) {
		
		int lastPointIndex = packageName.lastIndexOf('.');
		
		if (lastPointIndex > 0)
			return packageName.substring(0, lastPointIndex);
		else
			return "";
	}
	
	/**
	 * This method simply marks the input package name as one having an
	 * associated certificate that may be used for verifying a container signature.
     * <p>
     * If the input package name has not been generated yet using
     * {@link #generateEntriesForPackageName}, this method has no effect.
	 * 
	 * @param packageName
	 *  the package name to mark as having a certificate associated to it.
	 */
	final void setPackageNameHasAssociatedCertificate(String packageName) {
		
		if (packageNameToHasCertificateMap.containsKey(packageName)) {
			
			packageNameToHasCertificateMap.put(packageName, true);
			Log.d(TAG_PACKAGE_NAME_TRIE, packageName + " has a certificate associated now.");
		}
	}
	
	/**
	 * This method is used to query {@link PackageNameTrie} to obtain the name of the
	 * closest prefix of the input package name, which has been linked to a certificate.
	 * 
	 * @param packageName
	 *  the package name for which a certificate for signature verification must be found
	 * @return
	 *  an optional containing either the longest prefix of the input package name
     *  (possibly the one provided as input), which has an associated certificate
     *  for signature verification, or an absent value if no certificate
	 *  is associated to any of the package names in its hierarchy.
	 */
	final Optional<String> getPackageNameWithAssociatedCertificate(String packageName) {
		
		String currentPackageName = packageName;
		
		if (!packageNameToHasCertificateMap.containsKey(currentPackageName))
			return Optional.absent();
		
		while (!packageNameToHasCertificateMap.get(currentPackageName)) {
            currentPackageName = getUpALevel(currentPackageName);

            if (currentPackageName.equals("")) {
                Log.d(TAG_PACKAGE_NAME_TRIE, "There is no prefix associated with a certificate "
                        + "for package name " + packageName);

                return Optional.absent();
            }
        }

		Log.d(TAG_PACKAGE_NAME_TRIE, currentPackageName + " is the closest package name to the target "
		+ packageName + " with an associated certificate for verification.");
		
		return Optional.of(currentPackageName);
	}
}
