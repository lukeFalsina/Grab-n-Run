.. docgrabnrun documentation master file, created by
   sphinx-quickstart on Tue Sep 23 09:59:33 2014.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Welcome to Grab'n Run documentation!
====================================

*Grab'n Run* (aka **GNR**) is a **simple** and **effective** Java Library that you can add to your Android projects to secure *dynamic class loading* operations.

For a **quick start** on how to include the library and how to use it in your projects give a look at :doc:`tutorial`.

For a brief explanation on the **issue** of *insecure dynamic class loading* and on Grab'n Run purpose check the :doc:`security` section.

A concise **example** of use of the library is provided into an Android toy-application `here <https://github.com/lukeFalsina/Grab-n-Run/tree/master/example>`_. A *full explanation* of key extracts of this code is given into the :doc:`example` section.

For a description on Grab'n Run **API** in *JavaDoc* style please refer to the `API documentation <https://rawgit.com/lukeFalsina/Grab-n-Run/master/docs/javaDoc/index.html>`_. 

For those willing for more **technicalities** and **advanced features** implemented in *Grab'n Run*, the section on :doc:`complementary` is a *must-read*. This part of the documentation can also be used for **reference** as it presents how to handle properly some **tricky situations** that may occur while using *GNR*.

For an introduction on how to use the POC script for rewriting your application automatically to use the secure Grab'n Run API instead of the regualr ones for dynamic 
code loading, check out the :doc:`repackaging` section.

.. toctree::
   :maxdepth: 2

   tutorial
   security
   example
   complementary
   repackaging

Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`

