.. java:import:: java.io ByteArrayInputStream

.. java:import:: java.io File

.. java:import:: java.io FileInputStream

.. java:import:: java.io FileNotFoundException

.. java:import:: java.io IOException

.. java:import:: java.io InputStream

.. java:import:: java.net MalformedURLException

.. java:import:: java.net URL

.. java:import:: java.security.cert Certificate

.. java:import:: java.security.cert CertificateException

.. java:import:: java.security.cert CertificateExpiredException

.. java:import:: java.security.cert CertificateFactory

.. java:import:: java.security.cert CertificateNotYetValidException

.. java:import:: java.security.cert X509Certificate

.. java:import:: java.util ArrayList

.. java:import:: java.util Enumeration

.. java:import:: java.util HashMap

.. java:import:: java.util Iterator

.. java:import:: java.util List

.. java:import:: java.util Map

.. java:import:: java.util Vector

.. java:import:: java.util.jar JarEntry

.. java:import:: java.util.jar JarFile

.. java:import:: java.util.jar Manifest

.. java:import:: java.util.regex Pattern

.. java:import:: javax.security.auth.x500 X500Principal

.. java:import:: android.content ContextWrapper

.. java:import:: android.content.pm PackageManager

.. java:import:: android.content.pm Signature

.. java:import:: android.util Log

.. java:import:: dalvik.system DexClassLoader

SecureDexClassLoader
====================

.. java:package:: it.necst.grabnrun
   :noindex:

.. java:type:: public class SecureDexClassLoader

   A class that provides an extension of default \ :java:ref:`DexClassLoader`\  provided by the Android system and it is used to load classes from jar and apk container files including a classes.dex entry in a secure way. In order to instantiate this class a call to the method createDexClassLoader from a \ :java:ref:`SecureLoaderFactory`\  object must be performed. \ :java:ref:`SecureDexClassLoader`\  ensures integrity of loaded external remote classes by comparing them with the developer certificate, which is retrieved either by a provided associative map between package names and certificate remote URL or by simply reverting the first two words of the package name of the loaded class and then by adding each following word in the same order and separated by a slash "/". Package name reversion example: Class name = it.necst.grabnrun.example.TestClassImpl Constructed URL = https://necst.it/grabnrun/example Final certificate location = https://necst.it/grabnrun/example/certificate.pem A request is pointed to the final certificate location and if the file is found, it is imported in the local private application directory. Please note that in the current implementation certificates obtained by reverting package name must have been saved at the described location as "certificate.pem". Moreover all the certificates must fit requirements of a standard X.509 certificate, they must be valid in the current time frame and of course they must have been used to sign the jar or apk, which contains the classes to be loaded. If any of these previous requirements is violated no class is loaded and this class immediately returns without executing any class code loading operation.

   :author: Luca Falsina

Constructors
------------
SecureDexClassLoader
^^^^^^^^^^^^^^^^^^^^

.. java:constructor::  SecureDexClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent, ContextWrapper parentContextWrapper)
   :outertype: SecureDexClassLoader

Methods
-------
loadClass
^^^^^^^^^

.. java:method:: public Class<?> loadClass(String className) throws ClassNotFoundException
   :outertype: SecureDexClassLoader

setCertificateLocationMap
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method::  void setCertificateLocationMap(Map<String, String> extPackageNameToCertificateMap)
   :outertype: SecureDexClassLoader

wipeOutPrivateAppCachedData
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void wipeOutPrivateAppCachedData(boolean containerPrivateFolder, boolean certificatePrivateFolder)
   :outertype: SecureDexClassLoader

   Sometimes it may be useful to remove those data that have been cached in the private application folder (basically for performance reason or for saving disk space on the device). A call to this method solves the issue. Please notice that a call to this method with both the parameters set to false has no effect. In any of the other cases the content of the related folder(s) will be erased and since some of the data may have been used by \ :java:ref:`SecureDexClassLoader`\  instances, it is required to the caller to create a new \ :java:ref:`SecureDexClassLoader`\  object through \ :java:ref:`SecureLoaderFactory`\  since the already present object is going to be disabled from loading classes dynamically.

   :param containerPrivateFolder: if the private folder containing jar and apk containers downloaded from remote URL needs to be wiped out
   :param certificatePrivateFolder: if the private folder containing certificates needs to be wiped out

