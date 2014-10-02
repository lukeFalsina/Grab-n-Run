Discussion of an example project
================================

Before digging into this section, you are **strongly** encouraged to read :doc:`tutorial` for an **introductory description** on the features of Grab`n Run library.

The **aim of the sample application** is to give you some *hints on how to use the classes in Grab'n Run and how they will behave across different contexts*. The **source code** of the example can be found `here <http://fill.link.com>`_.

Different extracts of code will be considered and explained in the following sections of this page.

MainActivity.java
-----------------

`MainActivity <http://fill.link.com>`_ is the **entry point** of the sample application. In its overloaded method ``onCreate()`` it initializes through a ``ListView`` a set of buttons used to select the *different test cases* present in the application.

DexClassLoader (.apk) vs SecureDexClassLoader (.apk)
----------------------------------------------------

In this first scenario you will consider how to retrieve an `Activity <http://developer.android.com/reference/android/app/Activity.html>`_ class, whose name is ``NasaDailyImage``, stored in the *.apk* container, called *test.apk*, through the use of `DexClassLoader <http://developer.android.com/reference/dalvik/system/DexClassLoader.html>`_ and ``SecureDexClassLoader``.

The relevant **code** in this case is the one of the two methods ``setUpDexClassLoader()`` and ``setUpSecureDexClassLoader()``, which are triggered by tapping the related two buttons on the ``MainActivity`` view.

setUpDexClassLoader()
~~~~~~~~~~~~~~~~~~~~~

In this method a standard initialization of a ``DexClassLoader`` is applied.
So at first the usual **application-private, writable directory** for caching loaded *.dex* classes must be set up.

Then a ``DexClassLoader`` object is initialized using *test.apk*, a container located directly in the phone external storage ( as described by ``exampleTestAPKPath``), as its *jar path* for the classes to load.

Finally the ``NasaDailyImage`` Activity is loaded. If such an operation is successful the **simple name** of the **loaded class** is shown to the user through a *toast message*; otherwise different **exceptions** are raised and show again through a toast message an appropriate helper message.

setUpSecureDexClassLoader()
~~~~~~~~~~~~~~~~~~~~~~~~~~~

In this method **repeated** ``loadClass()`` **calls** are performed on differently initialized ``SecureDexClassLoader`` instances in order to *show different behaviors* of the loader class while retrieving the usual ``NasaDailyImage`` Activity.

At first a ``SecureLoaderFactory`` object is created. Then this instance is used to generate three ``SecureDexClassLoader`` that covers different cases and ends up with different results on the load operation:

1.	**Load a class through** ``SecureDexClassLoader`` **without providing an associative map for certificates location**

	This first test case shows a **common error** that a developer may encounter when using this library for the first time.
	If you want to have the location of the certificate being computed by `reversing the package name <http://fill.it>`_ you still need to **populate an associative map** with entries like (*"any.package.name"*, **null**) and use it as a parameter of the method ``createDexClassLoader()``. To understand why the class works in this way think of this system as a kind of `white listing <http://en.wikipedia.org/wiki/Whitelist>`_. Only those classes inside packages which are *declared into the associative map* will be considered as possible valid ones, while all classes belonging to a **not listed package** will be **immediately rejected**.

	And this is exactly what happens in this test case where **no associative map is provided** and so all the classes in the two containers, including the target ``NasaDailyImage``, are **prevented from being loaded** since there is *no clue on the certificate location*.

2.	**Load a class through** ``SecureDexClassLoader`` 

DexClassLoader (.jar) vs SecureDexClassLoader (.jar)
----------------------------------------------------

Code presented into DexClassSampleActivity. Aim: enrich GUI with elements taken from ComponentModifier class dynamically.
Difference in the two calls and uses and results

Create PackageContext
---------------------

Coming soon.. More or less ;)