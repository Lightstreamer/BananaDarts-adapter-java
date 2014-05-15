# Lightstreamer - Multiplayer Darts - Java Adapter #
<!-- START DESCRIPTION bananadarts-adapter-java -->

Multiplayer, [Leap Motion](https://www.leapmotion.com/) controlled, dart game running in the browser and using 
[Lightstreamer](http://www.lightstreamer.com) for its real-time communication needs.  

<!-- END DESCRIPTION bananadarts-adapter-java -->

# Build #

To build the adapter follow these steps:

* Get the ls-adapter-interface.jar and ls-generic-adapters.jar from the Lightstreamer distribution.
* Get a log4j 1.2 jar somewhere (there is one in the Lightstreamer distribution) 
* Download [croftsoft](http://sourceforge.net/projects/croftsoft/files/) library and compile a croftsoft-math.jar version. Please make sure to include: applet, io, inlp, lang and math packages.

Put the downloaded jars in a lib folder inside this project.

Then create a classes folder and run

```
javac -classpath ./lib/croftsoft-math.jar;./lib/ls-adapter-interface.jar;./lib/ls-generic-adapters.jar;./lib/log4j.jar -d ./classes ./src/com/lightstreamer/adapters/Dart/*.java ./src/com/lightstreamer/adapters/Dart/engine3D/*.java ./src/com/lightstreamer/adapters/Dart/room/*.java
```

Then go into the classes folder and run

```
jar cf ../deploy/LS_darts_adapters.jar ./com
```

# Deploy #

* Download and install Lightstreamer Vivace (make sure you use Vivace edition, otherwise you will see a limit on the event rate; Vivace comes with a free non-expiring demo license for 20 connected users).
* Go to the "adapters" folder of your Lightstreamer Server installation. Create a new folder and call it "Darts". Create a "lib" folder inside it.
* Copy the "ls-adapter-interface.jar" file from "Lightstreamer/lib" in the newly created "lib" folder.
* Copy the "croftsoft-math.jar" file from "Lightstreamer/lib" in the newly created "lib" folder.
* Copy the jar of the adapter compiled in the previous section in the newly created "lib" folder.
* Copy the "adapters.xml" file from the Deployment_LS folder of this project inside the "LeapDemo" folder.

Launch Lightstreamer.

# See Also #

## Clients Using This Adapter ##
<!-- START RELATED_ENTRIES -->

* [Lightstreamer - Multiplayer Darts - HTML (LeapJS, Three.js) Client](https://github.com/Weswit/BananaDarts-client-javascript)

<!-- END RELATED_ENTRIES -->

# Lightstreamer Compatibility Notes #

* Compatible with Lightstreamer SDK for Java Adapters since 5.1
