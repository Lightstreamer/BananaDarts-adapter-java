# Lightstreamer - Multiplayer Darts - Java Adapter
<!-- START DESCRIPTION bananadarts-adapter-java -->

The *Multiplayer Darts* implements a multiplayer, [Leap Motion](https://www.leapmotion.com/) controlled, dart game running in the browser and using [Lightstreamer](http://www.lightstreamer.com) for its real-time communication needs.

This project shows the Data Adapter and Metadata Adapters for the *Multiplayer Darts* and how they can be plugged into Lightstreamer Server.

As example of a client using this adapter, you may refer to the [Multiplayer Darts - HTML (LeapJS, Three.js) Client](https://github.com/Lightstreamer/BananaDarts-client-javascript) and view the corresponding [Live Demo](http://demos.lightstreamer.com/BananaDarts).

<!-- END DESCRIPTION bananadarts-adapter-java -->

## Install
If you want to install a version of this demo in your local Lightstreamer Server, follow these steps
* Download Lightstreamer Server (Lightstreamer Server comes with a free non-expiring demo license for 20 connected users; this should be preferred to using COMMUNITY edition, otherwise you would see a limit on the event rate) from [Lightstreamer Download page](http://www.lightstreamer.com/download.htm), and install it, as explained in the `GETTING_STARTED.TXT` file in the installation home directory.
* Get the `deploy.zip` file of the [latest release](https://github.com/Lightstreamer/BananaDarts-adapter-java/releases), unzip it and copy the just unzipped `BananaDarts` folder into the `adapters` folder of your Lightstreamer Server installation.
* [Optional] Customize logging settings in log4j configuration file `BananaDarts/classes/log4j2.xml`.
* Launch Lightstreamer Server.
* Launch a client like the [Multiplayer Darts - HTML (LeapJS, Three.js) Client](https://github.com/Lightstreamer/BananaDarts-client-javascript).

## Build
To build your own version of `example-MultiplayerDarts-adapter-java-0.0.1-SNAPSHOT.jar`, instead of using the one provided in the `deploy.zip` file from the [Install](https://github.com/Lightstreamer/BananaDarts-adapter-java#install) section above, you have two options:
either use [Maven](https://maven.apache.org/) (or other build tools) to take care of dependencies and building (recommended) or gather the necessary jars yourself and build it manually.
As a precondition for compiling you need to download [croftsoft](http://sourceforge.net/projects/croftsoft/files/) library and copy the sources of applet, io, jnlp, lang and math packages into `src\main\java\` folder of this project.
For the sake of simplicity only the Maven case is detailed here.

### Maven

You can easily build and run this application using Maven through the pom.xml file located in the root folder of this project. As an alternative, you can use an alternative build tool (e.g. Gradle, Ivy, etc.) by converting the provided pom.xml file.

Assuming Maven is installed and available in your path you can build the demo by running
```sh 
 mvn install dependency:copy-dependencies 
```


## See Also

### Clients Using This Adapter
<!-- START RELATED_ENTRIES -->

* [Lightstreamer - Multiplayer Darts - HTML (LeapJS, Three.js) Client](https://github.com/Lightstreamer/BananaDarts-client-javascript)

<!-- END RELATED_ENTRIES -->

## Lightstreamer Compatibility Notes

- Compatible with Lightstreamer SDK for Java In-Process Adapters since 7.3.
- For a version of this example compatible with Lightstreamer SDK for Java Adapters version 6.0, please refer to [this tag](https://github.com/Lightstreamer/BananaDarts-adapter-java/tree/pre_mvn).
- For a version of this example compatible with Lightstreamer SDK for Java Adapters version 5.1, please refer to [this tag](https://github.com/Lightstreamer/BananaDarts-adapter-java/tree/for_Lightstreamer_5.1).
