java-Hadoop-AdditionalNetwork
=============================

This library is a simple package for solving regularized linear classification.
This currently supports:
- Pegasos SVM (L2-regulalized L1-loss linear SVM for binary classification)
- SVM + Dual Coordinate Descent (L2-regulalized L1/L2-loss linear SVM for binary classiffication)
- Maximum Entropy + Stochastic Gradient Descent (L2-regulalized multi-class logistic regression)

For parallel learning using Hadoop Map Reduce, AllReduce is implemented.

GettingStarted
--------------

For building jar file, just using maven.

    $ mvn package

Please also see examples directory.

    $ ls -lR src/main/java/myorg/examples/

References
----------

- Shai Shalev-Shwartz, Yoram Singer and Nathan Srebro. (2007). Pegasos: Primal Estimated sub-GrAdient SOlver for SVM. ICML 2007.
- Cho-Jui Hsieh, Kai-Wei Chang, Chih-Jen Lin, S. Sathiya Keerthi and S Sundararajan. (2008). A Dual Coordinate Descent Method for Large-scale Linear SVM. ICML 2008.
- Alekh Agarwal, Olivier Chapelle, Miroslav Dudik and John Langford. (2011). A Reliable Effective Terascale Linear Learning System. CoRR. 

AUTHOR
------

TAGAMI Yukihiro <tagami.yukihiro@gmail.com>

LICENSE
-------

This library is distributed under the term of the Apache license version 2.0.
http://www.apache.org/licenses/LICENSE-2.0

