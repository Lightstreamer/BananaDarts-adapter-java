# Lightstreamer - Multiplayer Darts - Java Adapter
<!-- START DESCRIPTION bananadarts-adapter-java -->

Multiplayer, [Leap Motion](https://www.leapmotion.com/) controlled, dart game running in the browser and using 
[Lightstreamer](http://www.lightstreamer.com) for its real-time communication needs.  

<!-- END DESCRIPTION bananadarts-adapter-java -->

## Install

* Download and install Lightstreamer Vivace (make sure you use Vivace edition, otherwise you will see a limit on the event rate; Vivace comes with a free non-expiring demo license for 20 connected users).
* Get the "BananaDart" deploy folder from "deploy.zip" of the [latest release](https://github.com/Weswit/BananaDart-adapter-java/releases) and unzip it.
* Copy the just unzipped BananaDart folder into the "adapters" folder of your Lightstreamer Server installation.
* Copy the "ls-adapter-interface.jar" file from the Lightstreamer distribution in the "BananaDart/lib" folder.
* Download [croftsoft](http://sourceforge.net/projects/croftsoft/files/) library and compile a croftsoft-math.jar version. Please make sure to include: applet, io, inlp, lang and math packages.
* Copy the just compiled "croftsoft-math.jar" file in the "BananaDart/lib" folder.

Launch Lightstreamer.

## Build

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

## See Also

### Clients Using This Adapter
<!-- START RELATED_ENTRIES -->

* [Lightstreamer - Multiplayer Darts - HTML (LeapJS, Three.js) Client](https://github.com/Weswit/BananaDarts-client-javascript)

<!-- END RELATED_ENTRIES -->

## Lightstreamer Compatibility Notes

* Compatible with Lightstreamer SDK for Java Adapters since 5.1
