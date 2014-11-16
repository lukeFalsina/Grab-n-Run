Why should I use Grab'n Run?
============================

A significant question that every developer faces every time that (s)he adds a new library to his/her project is the following: "*Is it worthy to add this library? Does it provide something really necessary to my project?*"

In this specific case those two questions can be reformulated as follows:
"*Aren't Android API already able to handle dynamic class loading? Why should I use for example* ``SecureDexClassLoader`` *in stead of the regular* ``DexClassLoader`` *? Does it really enhance the standard class with something relevant?*".

To answer all of these questions let us consider the case of choosing ``SecureDexClassLoader``, one of the classes provided by **Grab'n Run**, in stead of `DexClassLoader <http://developer.android.com/reference/dalvik/system/DexClassLoader.html>`_, the standard class provided by the Android API, to *dynamically load* **.dex classes** into your Android application at run time.

First of all ``SecureDexClassLoader`` provides a couple of slight but significant **improvements** over the standard class in terms of **functionalities**:

* ``SecureDexClassLoader`` lets you retrieve *dynamically* also classes from *.jar* and *.apk* containers which are **not located directly on the phone** running the application as long as you simply provide a **valid remote URL** for the resource, while ``DexClassLoader`` is only able to cache classes from containers stored on the phone.

* ``SecureDexClassLoader`` is a **thread-safe** library and so, after that you have created your ``SecureDexClassLoader`` instance on the main thread of your application, you can launch different threads, each one performing dynamic class loading on the very same ``SecureDexClassLoader`` instance **without** incurring in nasty **race conditions** and **concurrencies exceptions**.

In addition and above all ``SecureDexClassLoader`` ensures relevant **security features** on classes that you dynamically load that are not possible to check or implemented with ``DexClassLoader`` like:

* **Fetch remote code in a secure way**: ``SecureDexClassLoader`` retrieves remote code either via HTTP or HTTPS protocol. In both cases it **always verifies** and validates the downloaded containers **before actually loading classes** inside of them.

* **Store containers in secure application-private locations**: `SecureDexClassLoader`` also **prevents** your application from being a possible target of **code injection attacks**. This attack becomes feasible whenever you use the standard ``DexClassLoader`` and you provide as an optimized cache folder for *dex* files a directory which is located in a **world writable area** of your phone (i.e. external storage) or you decide to load classes from a container which is, once again, stored in a world writable folder. ``SecureDexClassLoader`` on the other hand manages this situation for you by choosing *application-private directories* for caching *dex* files and storing containers and certificates. This strategy represents an **effective** way to make the **attack infeasible**.

* **Developer authentication**: for each package containing classes to be loaded dynamically it is possible to ensure authentication of the developer who coded those classes though a *check of the signature on the container* of the classes against the *certificate of the developer* (which could possibly be even *self-signed*). **Non-signed** classes or those who were not signed by that required certificate will be rejected and **prevented from being loaded**.  

* **Integrity**: during the *signature verification* process whenever one of the entries inside the container results to be *incorrectly signed*, ``SecureDexClassLoader`` recognized a **possible tampered or repackaged container** and it prevents your application from running the code of any classes inside this invalid and possible malicious container.

And these improvements come with a **convenient overhead** on the **performance** :)

This is possible since ``SecureDexClassLoader`` was implemented with an accurate **caching system** that, from one side, *prevents* your application **from continuously downloading** the same *jar* and *apk* **containers and certificates** and, from the other side, **avoid** it from **verifying every time** *the signature and the integrity* of already **checked containers**.

Moreover, for even *more performance concerned developers*, it is also possible to set the **strategy** which is going to be used by ``SecureDexClassLoader`` to **validate classes** before attempting to load them. In particular **two** options are provided:

1. **Lazy Strategy**: this mode implies that the **signature and integrity** of each container will be **evaluated only when** the ``loadClass()`` method will be invoked on *one of the classes*, whose package name is linked to this container. An ideal case of use for this mode is when you have quite a lot of containers and just a couple of classes to load, which may also vary from one execution to another and so validating all the containers in this case may be a waste of time.

2. **Eager Strategy**: in this mode the process of **signature and integrity** will be carried out on **all** the containers **immediately** before returning an instance of ``SecureDexClassLoader``. This choice implies that you will have to pay an **initial penalty** on time of execution but then the time required for a ``loadClass()`` operation becomes almost equal to the corresponding operation performed with standard ``DexClassLoader``.

**By default lazy strategy** is applied but developers can *pick* the *eager version* by adding a final ``false`` attribute to the ``createDexClassLoader()`` method invocation
in ``SecureLoaderFactory``. An example of use is shown in the following snippet of code (a slight modification of one of the calls of what you may have seen in :doc:`tutorial` )::

		SecureDexClassLoader mSecureDexClassLoader = mSecureLoaderFactory.createDexClassLoader(	jarContainerPath, 
													null, 
													packageNamesToCertMap, 
													getClass().getClassLoader(),
													false);

