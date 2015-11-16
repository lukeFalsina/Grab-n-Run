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

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.google.common.annotations.VisibleForTesting;

/**
 * CertificateFileFilterByNameMatch checks local files and verifies whether
 * these elements hold the correct extension (.pem), and they match
 * the expected certificate name provided at construction time.
 * 
 * @author Luca Falsina
 */
final class CertificateFileFilterByNameMatch extends FileFilterByNameMatch {

    @VisibleForTesting final static String PEM_EXTENSION = ".pem";
	
	/**
	 * A constructor for the filter which receives the 
	 * name of the desired certificate as a parameter.
	 * <p>
	 * Do not provide the extension of the certificate
	 * file but only the name!
	 * 
	 * @param certificateName
	 *  the file name of the certificate.
	 */
	CertificateFileFilterByNameMatch(@NonNull String certificateName) {
		super(
                checkNotNull(certificateName, "The input name for the certificate was null"),
                PEM_EXTENSION);
	}
}
