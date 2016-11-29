# Frequently Asked Questions 

 - **Something is broken/Something doesn't work/I get werid results/..** Use Github's issue tracker and open a ticket. We will try to address it as soon as possible. 
 - **Can I contribute?** Of course! We always welcome extera help! You can fork the Github project, and send us Pull-Request. If you want to discuss the suggested changes/improvment/etc, you can use the issue tracker. 
 - **Some of the tests that I have not touched failed for some mysterious reasons** Some tests have extra reqirements. For example the [`inference`](inference#frequently-asked-questions) and [`curator`](curator/README.md#frequently-asked-questions) modules. Unless you're changing a very core part of the code-base, you can ignore failures in other modules. If you want to test only a single module you can use `-pl` option. For example:  `mvn -pl pos` tests only the module(s) inside the `pos` folder. 
 
