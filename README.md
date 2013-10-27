java-Hadoop-AdditionalNetwork
=============================

This library is a simple package for solving regularized linear classification.
This currently supports:
- Pegasos SVM (L2-regulalized L1-loss linear SVM for binary classification)
- SVM + Dual Coordinate Descent (L2-regulalized L1/L2-loss linear SVM for binary classiffication)
- Maximum Entropy + Stochastic Gradient Descent (L2-regulalized multi-class logistic regression)

For parallel learning, AllReduce on Hadoop (Map Reduce) is implemented.

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
- Nikos Karampatziakis and John Langford. (2010). Importance Weight Aware Gradient Updates. CoRR.
- Daniel Golovin, D. Sculley, H. Brendan McMahan and Michael Young. (2013). Large-Scale Learning with Less RAM via Randomization. CoRR.
- H. Brendan McMahan, Gary Holt, D. Sculley, Michael Young, Dietmar Ebner, Julian Grady, Lan Nie, Todd Phillips, Eugene Davydov, Daniel Golovin, Sharat Chikkerur, Dan Liu, Martin Wattenberg, Arnar Mar Hrafnkelsson, Tom Boulos, Jeremy Kubica. (2013). Ad Click Prediction: a View from the Trenches. KDD 2013.
- L\'{e}on Bottou. (2012). Stochastic Gradient Tricks. Neural Networks, Tricks of the Trade, Reloaded. http://leon.bottou.org/papers/bottou-tricks-2012

AUTHOR
------

TAGAMI Yukihiro <tagami.yukihiro@gmail.com>

LICENSE
-------

This library is distributed under the term of the Apache license version 2.0.
http://www.apache.org/licenses/LICENSE-2.0

