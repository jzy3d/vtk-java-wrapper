

# Instructions for VTK

You should be able to compile VTK for Mesa by turning on VTK_OPENGL_HAS_OSMESA and turning off VTK_USE_X. Then make sure the OSMESA-related cmake variable are set (library and include dirs).

I could not find a specific option to force JOGL to use Mesa, so I think you just have to install the Mesa driver on Ubuntu and make sure it is used by checking the output of glxinfo. If glxinfo uses mesa then you JOGL application will use it too. You can check the graphics configuration at runtime in your JOGL application using the following piece of code:

```java
GraphicsEnvironment graphEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
GraphicsDevice[] graphDevsOrig = graphEnv.getScreenDevices();
GraphicsConfiguration gcDef = graphDevsOrig[0].getDefaultConfiguration(); // Printing this should show mesa opengl is used
```

## Install and build Mesa

This installer is helpful to build [OffScreen Mesa](https://docs.mesa3d.org/osmesa.html)

```shell
git clone git@github.com:devernay/osmesa-install.git
```

Needs extra dependencies

```shell
sudo apt-get install autoconf
sudo apt-get install libtool
```

Requires build settings

```shell
# define path fpr MESA and LLVM
export OSMESA_PREFIX=/home/martin/Dev/jzy3d/external/osmesa
export LLVM_PREFIX=/home/martin/Dev/jzy3d/external/llvm

# Disable GL symbol mangling
export MANGLED=0

# Produce a shared library that VTK will use for linking
export SHARED=1

# Set the below variable to 1 only the first time to download and build
# the appropriate version LLVM, which may take more than ten minutes.
export LLVM_BUILD=1  
```

Then go (expect 5 minutes excluding LLVM which will make build longer than that the first time)

```
./osmesa-install.sh -release
```

WARNING : macOS 10.12 may require to manually download 

https://github.com/llvm/llvm-project/releases/download/llvmorg-6.0.1/llvm-6.0.1.src.tar.xz

in current folder

The outcome is in `OSMESA_PREFIX`


## CPU ONSCREEN rendering : Run VTK With OS Mesa

Edit CMake script so that

| Name                       | Value                                      |
|----------------------------|--------------------------------------------|
| `VTK_USE_X`               | true                                       |
| `VTK_OPENGL_HAS_OSMESA`  | false                                     |
| `VTK_WRAP_JAVA`           | true                                      |


Run DemoVTKCanvas (hence not using JOGL) with the following environment variables

```
export LD_LIBRARY_PATH=<MESA_INSTALL_PREFIX>:$LD_LIBRARY_PATH
export LIBGL_ALWAYS_SOFTWARE=true
```

Not using these variables will use GPU rendering instead of CPU rendering


## CPU OFFSCREEN rendering : Build VTK With OS Mesa

Edit CMake script so that

| Name                          | Value                                      |
|-------------------------------|--------------------------------------------|
| `VTK_USE_X`                  | false                                      |
| `VTK_OPENGL_HAS_OSMESA`    | true                                       |
| `OSMESA_INCLUDE_DIR`        | `/path/to/osmesa/include/`             |
| `OSMESA_LIBRARY`            | `/path/to/osmesa/lib/libOSMesa32.so`  |
| `OPENGL_xmesa_INCLUDE_DIR` | `/path/to/osmesa/include/`              |
| `JOGL_LIB`                   | `/path/to/jogl-all.jar`                 |
| `JOGL_LIB`                   | `/path/to/gluegen-rt.jar`               |



Add POSITION_INDEPENDENT_CODE to Cmake (https://cmake.org/cmake/help/latest/variable/CMAKE_POSITION_INDEPENDENT_CODE.html)







## Other links & info


### Install on Ubuntu

https://gist.github.com/SiyuanQi/600d1ce536791b7a3bd2e59fdbe69e66

https://docs.mesa3d.org/osmesa.html

* Download [Mesa](https://docs.mesa3d.org/download.html)
* Install [Meson](https://github.com/mesonbuild/meson) with `pip3 install meson` NO, with `sudo apt install meson`
* Compile [Mesa](https://docs.mesa3d.org/install.html)

export MESA_INSTALLDIR=/home/martin/Programs/mesa-21.3.3

https://community.khronos.org/t/installing-opengl-mesa-on-ubuntu/68797/2

### Install on MacOS

brew install mesa
ls /usr/local/Cellar/mesa/21.1.2/lib
