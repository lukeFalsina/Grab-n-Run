Why should I use Grab'n Run?
============================

A significant question that every developer faces every time that (s)he adds a new library to his/her project is the following: "*Is it worthy to add this library? Does it provide something really necessary to my project?*"

In this specific case those two questions can be reformulated as follows:
"*Aren't Android API already able to handle dynamic class loading? Why should I use for example* ``SecureDexClassLoader`` *in stead of the regular* ``DexClassLoader`` *? Does it really enhance the standard class with something relevant?*".

To answer all of these questions let us consider the case of choosing ``SecureDexClassLoader``, one of the classes provided by **Grab'n Run**, in stead of `DexClassLoader <http://developer.android.com/reference/dalvik/system/DexClassLoader.html>`_, the standard class provided by the Android API, to *dynamically load* **.dex classes** into your Android application at run time.

First of all ``SecureDexClassLoader`` provides a couple of slight but significant **improvements** over the standard class in terms of **functionalities**:

* ``SecureDexClassLoader`` lets you retrieve dynamically also classes from *.jar* and *.apk* containers which are **not located directly on the phone** running the application as long as you simply provide a **valid remote URL** for the resource, while ``DexClassLoader`` is only able to cache classes from containers stored on the phone.

* Other functionality.. TODO

In addition and above all ``SecureDexClassLoader`` ensures relevant **security features** on classes that you dynamically load that are not possible to check with ``DexClassLoader`` like:

* **Developer authentication**: for each package containing classes to load dynamically it is possible to ensure authentication of the developer who coded those classes though a *check of the signature on the container* of the classes against the *certificate of the developer* (which could possibly be even *self-signed*). **Non-signed** classes or those who were not signed by that required certificate will be rejected and **prevented from being loaded**.  

* **Integrity**: during the *signature verification* process whenever one of the entries inside the container results to be *incorrectly signed*, ``SecureDexClassLoader`` recognized a **possible tampered or repackaged container** and it prevents your application from running the code of any classes inside this possible malicious container.

And these improvements comes with a **fairly negligible overhead** [NEED TO BE VERIFIED AND EXPLAINED!] on the **performance** :)