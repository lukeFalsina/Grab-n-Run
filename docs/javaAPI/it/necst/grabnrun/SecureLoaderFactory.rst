.. java:import:: java.io File

.. java:import:: java.net MalformedURLException

.. java:import:: java.net URL

.. java:import:: java.util Iterator

.. java:import:: java.util Map

.. java:import:: java.util.regex Pattern

.. java:import:: android.content ContextWrapper

.. java:import:: android.util Log

SecureLoaderFactory
===================

.. java:package:: it.necst.grabnrun
   :noindex:

.. java:type:: public class SecureLoaderFactory

   A Factory class that generates instances of classes used to retrieve dynamic code in a secure way at run time.

   :author: Luca Falsina

Fields
------
RES_DOWNLOAD_DIR
^^^^^^^^^^^^^^^^

.. java:field:: static final String RES_DOWNLOAD_DIR
   :outertype: SecureLoaderFactory

Constructors
------------
SecureLoaderFactory
^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public SecureLoaderFactory(ContextWrapper parentContextWrapper)
   :outertype: SecureLoaderFactory

   Creates a \ ``SecureLoaderFactory``\  used to check and generate instances from secure dynamic code loader classes. It requires a \ :java:ref:`ContextWrapper`\  (i.e. the launching activity) which should be used to manage and retrieve internal directories of the application.

   :param parentContextWrapper: The content wrapper coming from the launching Activity

Methods
-------
createDexClassLoader
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public SecureDexClassLoader createDexClassLoader(String dexPath, String libraryPath, Map<String, String> packageNameToCertificateMap, ClassLoader parent)
   :outertype: SecureLoaderFactory

   Creates a \ :java:ref:`SecureDexClassLoader`\  that finds interpreted and native code in a set of provided locations (either local or remote via HTTP or HTTPS) in dexPath. Interpreted classes are found in a set of DEX files contained in Jar or Apk files and stored into an application-private, writable directory. Before executing one of these classes the signature of the target class is verified against the certificate associated with its package name. Certificates location are provided by filling appropriately \ :java:ref:`packageNameToCertificateMap`\ }; each package name must be linked with the remote location of the certificate that should be used to validate all the classes of that package. It's important that each one of these locations uses HTTPS as its protocol; otherwise this choice will be enforced! If a class package name do not match any of the provided entries in the map, certificate location will be constructed by simply reverting package name and transforming it into a web-based URL using HTTPS. Note that this method returns null if no matching Jar or Apk file is found at the provided dexPath parameter; otherwise a \ :java:ref:`SecureDexClassLoader`\  instance is returned. Dynamic class loading with the returned \ :java:ref:`SecureDexClassLoader`\  will fail whether at least one of these conditions is not accomplished: target class is not found in dexPath or is in a missing remote container (i.e. Internet connectivity is not present), missing or invalid (i.e. expired) certificate is associated with the package name of the target class, target class signature check fails against the associated certificate.

   :param dexPath: the list of jar/apk files containing classes and resources; these paths could be either local URLs pointing to a location in the device or URLs that links to a resource stored in the web via HTTP/HTTPS. In the latter case, if Internet connectivity is available, the resource will be imported in a private-application directory before being used.
   :param libraryPath: the list of directories containing native libraries; it may be null
   :param packageNameToCertificateMap: a map that couples each package name to a URL which contains the certificate that must be used to validate all the classes that belong to that package before launching them at run time.
   :param parent: the parent class loader
   :return: secureDexClassLoader

