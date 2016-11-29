# Frequently Asked Questions 

 - **I'm trying to use your system and getting `Exception in thread "main" java.lang.UnsupportedClassVersionError: a (Unsupported major.minor version 51/52)`** This is becuase you're using an older version of Java. Upgrade your system to use Java 8. 
 - **Some of the tests that I have not touched failed for some mysterious reasons** Some tests have extra reqirements. For example the [`inference`](inference#frequently-asked-questions) and [`curator`](curator/README.md#frequently-asked-questions) modules. Unless you're changing a very core part of the code-base, you can ignore failures in other modules. 
 - **Tests take so much time! How can I run a specific test?**  If you want to test only a single module you can use `-pl` option. For example:  `mvn -pl pos` tests only the module(s) inside the `pos` folder. 
 - **Something is broken/Something doesn't work/I get weird results/..** Use Github's issue tracker and open a ticket. We will try to address it as soon as possible. 
 - **Can I contribute?** Of course! We always welcome extera help! You can fork the Github project, and send us Pull-Request. If you want to discuss the suggested changes/improvment/etc, you can use the issue tracker. 
