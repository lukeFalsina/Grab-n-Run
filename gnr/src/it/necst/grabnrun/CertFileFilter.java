package it.necst.grabnrun;

import java.io.File;
import java.io.FileFilter;

/**
 * CertFileFilter is a filter for checking local files in a directory and 
 * verify whether these element are certificate files by holding one of 
 * the correct extensions (.pem). Moreover it also verifies that the name 
 * of the certificate matches the one provided during the constructor
 * invocation.
 * 
 * @author Luca Falsina
 */
public final class CertFileFilter implements FileFilter {

	private final String[] okCertsExtensions = new String[] {".pem"};
	
	private String certificateName;
	
	/**
	 * A constructor for the filter which receives the 
	 * name of the desired certificate as a parameter.
	 * 
	 * Do not provide the extension of the certificate
	 * file but only the name!
	 * 
	 * @param certificateName
	 */
	CertFileFilter(String certificateName) {
		
		this.certificateName = certificateName;
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
			for (String extension : okCertsExtensions) {
				
				if (file.getName().equals(certificateName + extension))
		    	  return true;
		    }
		}
		
		// Used for any other kind of weird stuff reaching the filter..
		return false;
	}

}
