# Lightstreamer - Multiplayer Darts - Java Adapter
<!-- START DESCRIPTION bananadarts-adapter-java -->

Multiplayer, [Leap Motion](https://www.leapmotion.com/) controlled, dart game running in the browser and using 
[Lightstreamer](http://www.lightstreamer.com) for its real-time communication needs.  

<!-- END DESCRIPTION bananadarts-adapter-java -->
## Details
As example of a client using this adapter, you may refer to the [Multiplayer Darts - HTML (LeapJS, Three.js) Client](https://github.com/Weswit/BananaDarts-client-javascript) and view the corresponding [Live Demo](http://demos.lightstreamer.com/BananaDarts).

## Install

* Download and install Lightstreamer Vivace (make sure you use Vivace edition, otherwise you will see a limit on the event rate; Vivace comes with a free non-expiring demo license for 20 connected users).
* Get the `deploy.zip` file of the [latest release](https://github.com/Weswit/BananaDarts-adapter-java/releases), unzip it and copy the just unzipped `Dart` folder into the `adapters` folder of your Lightstreamer Server installation.
* Download [croftsoft](http://sourceforge.net/projects/croftsoft/files/) library and compile a `croftsoft-math.jar` version. Please make sure to include: applet, io, inlp, lang and math packages.
* Copy the just compiled `croftsoft-math.jar` file in the `BananaDart/lib` folder.
* Launch Lightstreamer Server.
* Launch a client like the [Multiplayer Darts - HTML (LeapJS, Three.js) Client](https://github.com/Weswit/BananaDarts-client-javascript).

## Build
To build your own version of `LS_darts_adapters.jar`, instead of using the one provided in the `deploy.zip` file, follow these steps:

* Clone this project
* Get the `ls-adapter-interface.jar`, `ls-generic-adapters.jar`, and `log4j-1.2.15.jar` from the [Lightstreamer distribution](http://www.lightstreamer.com/download) and copy them into the `lib` folder..
* Download [croftsoft](http://sourceforge.net/projects/croftsoft/files/) library and compile a `croftsoft-math.jar` version. Please make sure to include: applet, io, inlp, lang and math packages.
* Put the just compiled `croftsoft-math.jar` file in the `lib` folder.
* Build the java source files in the `src` folder into a `LS_darts_adapters.jar` file. Here is an example for that:
```
 > javac -classpath ./lib/croftsoft-math.jar;./lib/ls-adapter-interface.jar;./lib/ls-generic-adapters.jar;./lib/log4j.jar -d ./classes ./src/com/lightstreamer/adapters/Dart/*.java ./src/com/lightstreamer/adapters/Dart/engine3D/*.java ./src/com/lightstreamer/adapters/Dart/room/*.java
 > jar cvf LS_darts_adapters.jar -C tmp_classes com
```
* Copy the just compiled `LS_darts_adapters.jar` in the `adapters/Dart/lib` folder of your Lightstreamer Server installation.

## See Also

### Clients Using This Adapter
<!-- START RELATED_ENTRIES -->

* [Lightstreamer - Multiplayer Darts - HTML (LeapJS, Three.js) Client](https://github.com/Weswit/BananaDarts-client-javascript)

<!-- END RELATED_ENTRIES -->

## Lightstreamer Compatibility Notes

* Compatible with Lightstreamer SDK for Java Adapters since 5.1
