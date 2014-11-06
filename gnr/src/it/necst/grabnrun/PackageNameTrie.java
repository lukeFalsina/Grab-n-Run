package it.necst.grabnrun;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

final class PackageNameTrie {

	// Unique identifier used for Log entries
	private static final String TAG_PACKAGE_NAME_TRIE = PackageNameTrie.class.getSimpleName();
	
	// Map which links the package name string to the associated node
	private Map<String, Boolean> packageNameToHasCertificateMap;
	
	PackageNameTrie() {
		
		packageNameToHasCertificateMap = new HashMap<String, Boolean>();
		// Add also the root entry and suppose that it has 
		// always an associated certificate.
		packageNameToHasCertificateMap.put("", true);
	}
	
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
				packageNameToHasCertificateMap.put(currentPackageName, Boolean.valueOf(false));
				
				Log.d(TAG_PACKAGE_NAME_TRIE, "Inserted a new entry for " + currentPackageName);
				
				// Now remove the last part of the package name and then
				// repeat the previous step recursively.
				currentPackageName = getUpALevel(currentPackageName);
			}
		}
	}
	
	// Remove the last part of the package name.
	// If nothing more is left after this removal, return an empty string.
	private final String getUpALevel(String packageName) {
		
		int lastPointIndex = packageName.lastIndexOf('.');
		
		if (lastPointIndex != -1)
			return packageName.substring(0, lastPointIndex);
		else
			return "";
	}
	
	final void setEntryHasAssociatedCertificate(String packageName) {
		
		if (packageNameToHasCertificateMap.containsKey(packageName)) {
			
			packageNameToHasCertificateMap.put(packageName, Boolean.valueOf(true));
			Log.d(TAG_PACKAGE_NAME_TRIE, packageName + " has a certificate associated now.");
		}
	}
	
	final String getPackageNameWithAssociatedCertificate(String packageName) {
		
		String currentPackageName = packageName;
		
		if (!packageNameToHasCertificateMap.containsKey(currentPackageName))
			return "";
		
		while (!packageNameToHasCertificateMap.get(currentPackageName))
			currentPackageName = getUpALevel(currentPackageName);
		
		Log.d(TAG_PACKAGE_NAME_TRIE, currentPackageName + " is the closest package name to the target "
		+ packageName + " with an associated certificate for verification.");
		
		return currentPackageName;
	}
}
