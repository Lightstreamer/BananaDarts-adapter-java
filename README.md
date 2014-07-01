# Lightstreamer - Multiplayer Darts - Java Adapter
<!-- START DESCRIPTION bananadarts-adapter-java -->

Multiplayer, [Leap Motion](https://www.leapmotion.com/) controlled, dart game running in the browser and using 
[Lightstreamer](http://www.lightstreamer.com) for its real-time communication needs.  

<!-- END DESCRIPTION bananadarts-adapter-java -->
## Details
As example of a client using this adapter, you may refer to the [Multiplayer Darts - HTML (LeapJS, Three.js) Client](https://github.com/Weswit/BananaDarts-client-javascript) and view the corresponding [Live Demo](http://demos.lightstreamer.com/BananaDarts).

## Install

* Download and install Lightstreamer Vivace (make sure you use Vivace edition, otherwise you will see a limit on the event rate; Vivace comes with a free non-expiring demo license for 20 connected users).
* Get `deploy.zip` file of the [latest release](https://github.com/Weswit/BananaDarts-adapter-java/releases) and unzip it.
* Copy the just unzipped `Dart` folder into the `adapters` folder of your Lightstreamer Server installation.
* Download [croftsoft](http://sourceforge.net/projects/croftsoft/files/) library and compile a `croftsoft-math.jar` version. Please make sure to include: applet, io, inlp, lang and math packages.
* Copy the just compiled `croftsoft-math.jar` file in the `BananaDart/lib` folder.
* Launch Lightstreamer.

## Build

To build the adapter follow these steps:

* Get the `src` folder from this project, containing the source files to be compiled.
* Create a `compile_libs` folder.
* Get the `ls-adapter-interface.jar`, `ls-generic-adapters.jar`, and `log4j-1.2.15.jar` from the [Lightstreamer distribution](http://www.lightstreamer.com/download) and copy them into the compile_libs folder..
* Download [croftsoft](http://sourceforge.net/projects/croftsoft/files/) library and compile a croftsoft-math.jar version. Please make sure to include: applet, io, inlp, lang and math packages.
* Put the just compiled `croftsoft-math.jar` file in the `compile_libs` folder.
* Build the java source files in the `src` folder into a `LS_darts_adapters.jar` file. Here is an example for that:
```
 > javac -classpath ./compile_libs/croftsoft-math.jar;./compile_libs/ls-adapter-interface.jar;./compile_libs/ls-generic-adapters.jar;./compile_libs/log4j.jar -d ./classes ./src/com/lightstreamer/adapters/Dart/*.java ./src/com/lightstreamer/adapters/Dart/engine3D/*.java ./src/com/lightstreamer/adapters/Dart/room/*.java
 > jar cvf LS_darts_adapters.jar -C tmp_classes com
```
* Copy the just compiled `LS_darts_adapters.jar` in the `adapters/Dart/lib` folder of your Lightstreamer Server.

## See Also

### Clients Using This Adapter
<!-- START RELATED_ENTRIES -->

* [Lightstreamer - Multiplayer Darts - HTML (LeapJS, Three.js) Client](https://github.com/Weswit/BananaDarts-client-javascript)

<!-- END RELATED_ENTRIES -->

## Lightstreamer Compatibility Notes

* Compatible with Lightstreamer SDK for Java Adapters since 5.1
