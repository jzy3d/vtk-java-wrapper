vtk-java-wrapper
================

This project facilitates the use of VTK in Java applications on multiple operating system (Ubuntu, MacOS and Windows).

You will find here
- Builds of [VTK](https://vtk.org/) for Java (9.1) for multiple operating systems and CPU.
- A [maven configuration example](https://github.com/jzy3d/vtk-java-wrapper/blob/master/pom.xml) showing how to build a Java app linking to VTK and using JOGL 2.4-rc4 for enhanced compatibility with operating systems and hardwares.
- Examples showing how to create simple 3D applications with VTK, derived from the [official examples](https://kitware.github.io/vtk-examples/site/Java/) but verified under multiple platforms.
- Examples showing how to create simple 3D applications with Jzy3D for rendering and VTK for processing.
- Documentation on how to build VTK for Java yourself.

Build it by following the instruction below, or download it [here](https://download.jzy3d.org/vtk/build/).

When downloading prebuilt dylib files, Mac user may need to allow dylib files to be loaded by running this in the dylib folder.

```
sudo xattr -r -d com.apple.quarantine .
```

(other way to solve this [here](https://github.com/Jackett/Jackett/issues/5589))


# Getting started

To get started, read the three sections below
1. Download VTK libraries
1. Configure path to VTK libraries
1. Running an example


## Download VTK native libraries

To be able to run the examples in this project, you add a VTK build for Java in the `lib/{version}` directory.

The table below lists the [existing builds](https://download.jzy3d.org/vtk/build/) of VTK for Java. Pick the one that match your OS.

| OS      | OS Versions   | CPU          | Java       | VTK | Archive                        |
|---------|---------------|--------------|------------|-----|--------------------------------|
| Ubuntu  | 20            | Intel x86_64 | JDK 11     | 9.1 | <a href="https://download.jzy3d.org/vtk/build/9.1.0/vtk-Linux-x86_64-9.1.0-jdk11.zip">vtk-Linux-x86_64-9.1.0-jdk11</a> |                                       
| macOS   | 10.12, 10.15  | Intel x86_64 | JDK 11     | 9.1 | <a href="https://download.jzy3d.org/vtk/build/9.1.0/vtk-Darwin-x86_64-9.1.0-jdk11.zip">vtk-Darwin-x86_64-9.1.0-jdk11</a> |                                       
| macOS   | 11.4          | Apple M1     | JDK 11     | 9.1 | <a href="https://download.jzy3d.org/vtk/build/9.1.0/vtk-Darwin-arm64-9.1.0-jdk11.zip">vtk-Darwin-arm64-9.1.0-jdk11</a> |                                       
| Windows | 10            | Intel x86_64  | JDK 14     | 9.1 | <a href="https://download.jzy3d.org/vtk/build/9.1.0/vtk-Windows-x86_64.zip">vtk-Windows-x86_64</a> |   

If you can't find a suitable version for you or if the JDK is higher than the JRE you intend to use, simply read below some advices for building VTK for Java yourself.

Programs may fail if running with a lower JDK than the one that was used when building VTK. E.g. I experienced that a VTK build for JDK 11 won't properly work for JDK 9. The reason is that VTK refers to AWT native interface which has methods that should link to VTK. And JDK 11 has methods that JDK 9 does not have, which lead to a failure when loading VTK natives on a too low JDK.

## Check VTK java library

VTK Java classes are bundled in `lib/vtk-9.1.0.jar` which was built by following the instructions given in section _Building VTK for Java_. To allow referencing this jar file from the Maven project file, we deployed this jar to a local maven repository in `mvn/` folder. This folder is commited in this Git repository to facilitate bootstrapping. You can also reuse `lib/install-vtk-locally.sh` to deploy it to your own local maven repository.

## Configure path to VTK native libraries

One you have downloaded and unzipped the above package, you must run your program with a reference to the folder containing VTK native libraries, either through system path variables, either through the JVM library path.

An ```Exception in thread "main" java.lang.UnsatisfiedLinkError``` in your application may mean that you forgot to set your current directory to where the native libraries stand.


### Method 1 (prefered) : Define path to VTK in an environment variable

#### Method 1.1 : Append VTK to the operating system environment variables

Depending on your OS, VTK should be added to the following environment variable.

* Linux   : `LD_LIBRARY_PATH   = /home/martin/Dev/jzy3d/vtk-java-wrapper/lib/9.1.0/vtk-Linux-x86_64/:$LD_LIBRARY_PATH`   
* macOS   : `DYLD_LIBRARY_PATH = /Users/martin/Dev/jzy3d/vtk-java-wrapper/lib/9.1.0/vtk-Darwin-arm64/:$DYLD_LIBRARY_PATH`
* Windows : `PATH              = C:/Users/martin/Dev/jzy3d/vtk-java-wrapper/lib/9.1.0/vtk-Windows-x86_64;PATH`

#### Method 1.2 : Append VTK to the IDE run configuration environment variables

Eclipse users can define environment variables from the IDE Run Configurations

`DYLD_LIBRARY_PATH = /Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/9.1.0/vtk-Darwin-arm64:${env_var:DYLD_LIBRARY_PATH}`

<img src="doc/eclipse-settings.png"/>


### Method 2 : Define path to VTK in a JVM argument

This has the drawback of [keeping a reference of the path where the VTK libraries were built](https://discourse.vtk.org/t/shared-libraries-cant-be-redistributed-since-they-refer-to-their-build-path/7892/3).

* -Djava.library.path=./lib/{platform} (preferred)
* -Djava.library.path=/opt/homebrew/Cellar/vtk/9.0.3/lib
* -Djava.library.path=/Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/vtk-Darwin-x86_64
* lib/{platform}/ as runtime directory  

NB : wrappers with Python and Java in their name fail to load. I renamed the dylib so that they end by _POUET to minimize errors in console, but it is still normal to see

```
vtkPythonContext2DJava not loaded
vtkFiltersPythonJava not loaded
vtkCommonPythonJava not loaded
```

## Running an example

Run `DemoVTKPanelJogl.java` will display a Java frame including a VTK rendering Window supported by JOGL.

<img src="doc/demo-simple-vtk.png"/>

Note for VTK veterans : This is merely the `SimpleVTK` example that was formerly [crashing on macOS](https://gitlab.kitware.com/vtk/vtk/-/issues/17831) which is now working both for [Intel and Apple M1 CPU](https://discourse.vtk.org/t/fixed-vtk-java-wrappers-on-macos/7467).

# Compatibility issues with VTK Java and solutions

## When JOGL works and when JOGL crashes

VTK for Java relies on JOGL. JOGL does a great job but sometime hits compatibility issues with a small set of `{OS,CPU,GPU,JDK}` combinations. For example [Ubuntu 18 is known to fail](https://github.com/jzy3d/jzy3d-api/issues/139) for now. The matrix shown below indicates the combinations of `{OS,CPU,GPU,JDK}` that have been tested and proven to support [JOGL 2.4-rc4](https://github.com/jzy3d/jogl-maven-deployer#getting-jogl-24-rc-4-for-macos-bigsur-20211116). Manual tests on JOGL are performed by starting simple Jzy3D charts in an AWT Window.  

<img src="doc/compatibility-matrix.png"/>

Find the online matrix [here](https://docs.google.com/spreadsheets/d/1PsHpJnwug40pwLeX1gk33kmGCUOTzVPbEzn8RjmZXIA/edit?usp=sharing). Each failure is linked to an issue describing the problem in detail with possible workarounds.

## Java based CPU rendering to the rescue  

When JOGL can not allow access to native OpenGL rendering, one solution for Java developers is to avoid using the GPU. Jzy3D provides [EmulGL, a pure Java CPU OpenGL implementation](https://github.com/jzy3d/jzy3d-api/tree/master/jzy3d-emul-gl-awt) that can be used in such situation. In this case, VTK is only used for processing geometries but not for rendering. Jzy3D is used to render 3D with CPU only.

This is a great fallback that works on any JVM and OS. The drawback is a loss of performance, which depend on the number of polygons to render as well as pixel to draw.

<img src="doc/cpu-vs-gpu.png"/>

There are however tricks to circumvent this performance loss
* dynamic level of details, to allow simplifying the data to draw when needing to refresh often.
* decimation, to reduce the number of polygon to draw and only work with visible polygons

This repository contains the following examples.

### EmulGL example : DemoLOD_Cylinder

<img src="doc/demo-emulgl-cylinder-small.png"/>

### EmulGL example : DemoLOD_Slab_Full / DemoLOD_Slab_Part

<img src="doc/demo-emulgl-slab-full.png"/>


### JOGL example : PVTU_Part

<img src="doc/pvtu-part.png"/>




# Building VTK for Java

The following instructions are copied from this [page](https://www.particleincell.com/2011/vtk-java-visualization/) which is provided by the official [VTK instructions page](https://vtk.org/Wiki/VTK/Java_Wrapping)

## VTK Java Wrappers

So how do you couple VTK with Java? It’s very easy. VTK comes with support for Java in the form of Java wrappers. In the following sections we show you how to install and configure the VTK / Java development environment and how to use it to build a simple Java GUI-driven application.

We are assuming you already have JDK and a Java development environment such as Netbeans or Eclipse installed. In addition you will need to download the VTK source and build it. This requires a working C++ compiler. On Windows, you can obtain the Microsoft Visual Studio Express Edition for free. Then follow these steps to get started:

* Download CMake from cmake.org. CMake is a cross-platform configuration tool for VTK. It will query your system for the available compilers and settings, and will generate the appropriate project files needed to build VTK.
* Download VTK source from vtk.org. Make sure to download the Source package and not the Windows Installer.
* Unzip the source to some temporary directory.
* Configure your project files by running CMake. Specify the location to the source files you just unzipped and also a different directory where to build the files. This build folder is used during the compilation stage, however at the end, files will be installed into the appropriate system folders. Click Configure to start the process. CMake will run for a while performing various system tests. It will finish with another screen with additional options highlighted in red.
* Enable Java Wrapping by selecting the appropriate check box (see Figure 1). You will also need to enable the shared libraries. I generally uncheck build tests / examples to reduce the compilation time. Press Configure again. If everything went well, you will see the Generate button become available. Press this button to generate the project files.
cmake customization screen
* Compile VTK by launching the solution file located in the build folder. If you are using Visual Studio on Microsoft Windows, right click on ALL_BUILD and then select Build. The compiler with churn for a while. Depending on your machine, this may take multiple hours. If you are using make, use the appropriate make command (most likely make all but this was not tested).
* Install the system libraries, assuming no errors were encountered, by right clicking on INSTALL and selecting Build. Make sure you run Visual Studio as an administrator for this step. If you do encounter linker errors in the Java wrappers, make sure you are linking against the correct version of Java. My machine is running a 64-bit version of Windows 7. However, I only have the 32-bit C++ compilers. My default Java JDK is the 64-bit version, which resulted in an unresolved symbol “__imp__JAWT_GetAWT@8” linker error in vtkRenderingJava. If you do encounter these types of errors, make sure to download the 32-bit version of Java JDK and link against it by adjusting the input path under Linker->Input property page for the appropriate project.
* Check your PATH. The final piece, and likely the biggest headache, is making sure that your Java program can find the required VTK DLLs. These may in turn depend on other DLLs that may not be in the path. Great tool for checking DLL dependencies is Dependency Walker. It took me a while to get everything set up, and in the end, the following directories did the trick. Of course, your setup will likely differ.
* C:WindowsSystem32;C:Program Files (x86)VTKbin;C:Program Files (x86)Javajdk1.6.0_30lib;C:Program Files (x86)Javajre6bin;C:Program Files (x86)Javajre6binclient;C:Program Files (x86)Microsoft Visual Studio 9.0VCredistx86Microsoft.VC90.CRT;
* Copy vtk.jar to somewhere safe. This file is located in the bin directory in your VTK build folder. It is better to move it out of here in case you later decide to delete the build directory. It is over 1Gb after all and you don’t need it post Install unless you actually plan to modify the VTK libraries themselves.

## Additional notes

Here are my settings for building 9.1.0 on MacOS BigSur with an ARM64 CPU, assuming the JDK was built for ARM64 as well

<img src="./doc/cmake-java-settings.png"/>

Which should avoid error "Undefined symbols for architecture arm64: "_JAWT_GetAWT"".

One may also edit the CMakeCache.Txt to disable AWT link

```
//ADVANCED property for variable: JAVA_AWT_INCLUDE_PATH
JAVA_AWT_INCLUDE_PATH-ADVANCED:INTERNAL=0
//ADVANCED property for variable: JAVA_AWT_LIBRARY
JAVA_AWT_LIBRARY-ADVANCED:INTERNAL=0
```

You may have to finalize the jar manually

```
cd Wrapping/Java/CMakeFiles/vtkjava.dir/
jar --create -f ../../../../lib/java/vtk-9.1.0.jar vtk
```

Then copy
* `{VTK-BUILD-DIR}/lib/java/vtk.jar` to `./lib/vtk-{version}.jar`
* `{VTK-BUILD-DIR}/lib/java/{platform}.jnilib` to `./lib/{version}/{platform}`
* `{VTK-BUILD-DIR}/lib/{platform}.jnilib` to `./lib/{version}/{platform}`



## Troubleshooting

```
java.lang.UnsatisfiedLinkError: /Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/9.1.0/vtk-Darwin-arm64/libvtkCommonCoreJava.jnilib: dlopen(/Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/9.1.0/vtk-Darwin-arm64/libvtkCommonCoreJava.jnilib, 1): no suitable image found.  Did find:
	/Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/9.1.0/vtk-Darwin-arm64/libvtkCommonCoreJava.jnilib: mach-o, but wrong architecture
```

Ensure you have a ARM64 JVM



## Useful flags

Extra CMake options for specifying source & target versions
```
-DVTK_JAVA_SOURCE_VERSION=1.8
-DVTK_JAVA_TARGET_VERSION=1.8
```

## See also

https://github.com/Kitware/VTK/tree/master/Wrapping/Java

# VTK Documentation

* [VTK Guide (online)](https://kitware.github.io/vtk-examples/site/VTKBook/00Preface/)
* [VTK Guide PDF](./doc/VTKTextBook.pdf)
* [VTK 9.1.0 release note](https://gitlab.kitware.com/vtk/vtk/-/blob/master/Documentation/release/9.1.md)
