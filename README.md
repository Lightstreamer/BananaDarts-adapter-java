# Lightstreamer - Multiplayer Darts - Java Adapter
<!-- START DESCRIPTION bananadarts-adapter-java -->

The *Multiplayer Darts* implements a multiplayer, [Leap Motion](https://www.leapmotion.com/) controlled, dart game running in the browser and using [Lightstreamer](http://www.lightstreamer.com) for its real-time communication needs.

This project shows the Data Adapter and Metadata Adapters for the *Multiplayer Darts* and how they can be plugged into Lightstreamer Server.

As example of a client using this adapter, you may refer to the [Multiplayer Darts - HTML (LeapJS, Three.js) Client](https://github.com/Weswit/BananaDarts-client-javascript) and view the corresponding [Live Demo](http://demos.lightstreamer.com/BananaDarts).

<!-- END DESCRIPTION bananadarts-adapter-java -->

## Install
If you want to install a version of this demo in your local Lightstreamer Server, follow these steps
* Download Lightstreamer Server Vivace (make sure you use Vivace edition, otherwise you will see a limit on the event rate; Lightstreamer Server comes with a free non-expiring demo license for 20 connected users) from [Lightstreamer Download page](http://www.lightstreamer.com/download.htm), and install it, as explained in the `GETTING_STARTED.TXT` file in the installation home directory.
* Get the `deploy.zip` file of the [latest release](https://github.com/Weswit/BananaDarts-adapter-java/releases), unzip it and copy the just unzipped `BananaDarts` folder into the `adapters` folder of your Lightstreamer Server installation.
* Download [croftsoft](http://sourceforge.net/projects/croftsoft/files/) library and compile a `croftsoft-math.jar` version. Please make sure to include: applet, io, inlp, lang and math packages.
* Copy the just compiled `croftsoft-math.jar` file in the `BananaDarts/lib` folder.
* Launch Lightstreamer Server.
* Launch a client like the [Multiplayer Darts - HTML (LeapJS, Three.js) Client](https://github.com/Weswit/BananaDarts-client-javascript).

## Build
To build your own version of `LS_darts_adapters.jar`, instead of using the one provided in the `deploy.zip` file from the [Install](https://github.com/Weswit/BananaDarts-adapter-java#install) section above, follow these steps:
* Clone this project
* Get the `ls-adapter-interface.jar` file from the [Lightstreamer distribution](http://www.lightstreamer.com/download) and copy it into the `lib` folder.
* Get the `log4j-1.2.17.jar` file from [Apache log4j](https://logging.apache.org/log4j/1.2/) and copy it into the `lib` folder.
* Download [croftsoft](http://sourceforge.net/projects/croftsoft/files/) library and compile a `croftsoft-math.jar` version. Please make sure to include: applet, io, inlp, lang and math packages.
* Put the just compiled `croftsoft-math.jar` file in the `lib` folder.
* Build the java source files in the `src` folder into a `LS_darts_adapters.jar` file. Here is an example for that:
```
 > javac -classpath ./lib/croftsoft-math.jar;./lib/ls-adapter-interface.jar;./lib/log4j-1.2.17.jar -d ./classes ./src/com/lightstreamer/adapters/Dart/*.java ./src/com/lightstreamer/adapters/Dart/engine3D/*.java ./src/com/lightstreamer/adapters/Dart/room/*.java
 > jar cvf LS_darts_adapters.jar -C tmp_classes com
```
* Copy the just compiled `LS_darts_adapters.jar` in the `adapters/Dart/lib` folder of your Lightstreamer Server installation.

## See Also

### Clients Using This Adapter
<!-- START RELATED_ENTRIES -->

* [Lightstreamer - Multiplayer Darts - HTML (LeapJS, Three.js) Client](https://github.com/Weswit/BananaDarts-client-javascript)

<!-- END RELATED_ENTRIES -->

## Lightstreamer Compatibility Notes

* Compatible with Lightstreamer SDK for Java In-Process Adapters since 6.0
- For a version of this example compatible with Lightstreamer SDK for Java Adapters version 5.1, please refer to [this tag](https://github.com/Weswit/BananaDarts-adapter-java/tree/for_Lightstreamer_5.1).
