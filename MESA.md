

# Get MESA

Either download a pre-built mesa or build it yourself

## Download Mesa 

Download [MESA here](http://download.jzy3d.org/mesa/)

If not available for your platform, build it as follow


## Install and build Mesa

This installer is helpful to build [OffScreen Mesa](https://docs.mesa3d.org/osmesa.html)

### Build Mesa on Ubuntu 20

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

### Get pre-built MESA on Windows

1. Install [Msys2](https://www.msys2.org/)
1. Install [Mesa with Msys2](https://packages.msys2.org/package/mingw-w64-x86_64-mesa?repo=mingw64)
1. Install [LibC with Msys2](https://packages.msys2.org/package/mingw-w64-x86_64-libc++)


### Build MESA on Windows


#### Install build tools for Windows

Following the [install guide](https://docs.mesa3d.org/install.html)


* Install Python
* Install Meson (pip install meson)
* Install Mako (pip install Mako)
* Install Flex/Bison for Windows and add its path to PATH variable
* Install Ninja (pip install ninja)

#### Build for Windows

```
meson builddir/ -Dosmesa=true -Dgallium-drivers=swrast -Ddri-drivers="[]" -Dvulkan-drivers="[]" -Dprefix="C:\Users\Martin\Dev\jzy3d\external\osmesa" --reconfigure
ninja -C builddir/ 
ninja -C builddir/ install
```



### Use MESA on Windows

C:\msys64\mingw64\bin

Eclipse path
```
Path=C:\Users\Martin\Dev\jzy3d\private\vtk-java-wrapper\lib\9.1.0\vtk-Windows-x86_64;C:\msys64\mingw64\bin;${env_var:PATH}
```

### Get pre-built MESA on MacOS

```
brew install mesa
```

### Run MESA on MacOS

Run `DemoVTKPanelJoglCPU`

with environment variable
* `DYLD_LIBRARY_PATH=/Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/9.1.0/vtk-Darwin-arm64:/opt/homebrew/Cellar/mesa/21.3.7:${env_var:DYLD_LIBRARY_PATH}`
* `LIBGL_ALWAYS_SOFTWARE=true`


### Build MESA on MacOS


#### Install build tools for MacOS

Following the [install guide](https://docs.mesa3d.org/install.html)

* Install Python
* Install Meson (pip install meson)
* Install Mako (pip install Mako)
* Install Flex (brew install flex)
* Install Bison (brew install bison)
* Install Ninja (pip install ninja)


#### Build for MacOS

Read [this](https://docs.mesa3d.org/macos.html)

```
meson builddir/ -Dosmesa=true -Dglx=xlib -Dgallium-drivers=swrast -Ddri-drivers=[] -Dvulkan-drivers=[] -Dprefix=/Users/Martin/Dev/jzy3d/external/osmesa
ninja -C builddir/ 
ninja -C builddir/ install
```

DYLD_LIBRARY_PATH=/Users/Martin/Dev/jzy3d/external/osmesa

```
export LIBGL_ALWAYS_SOFTWARE=true
martin@osxm1 ~/D/j/external (master)> glxinfo | head -n 50
...
OpenGL vendor string: Apple Inc.
OpenGL renderer string: Apple Software Renderer
OpenGL version string: 2.1 APPLE-18.5.9
OpenGL shading language version string: 1.20

export LIBGL_ALWAYS_SOFTWARE=false
martin@osxm1 ~/D/j/external (master)> glxinfo | head -n 50
...
OpenGL vendor string: Apple
OpenGL renderer string: Apple M1
OpenGL version string: 2.1 Metal - 71.6.4
OpenGL shading language version string: 1.20

otool -L /opt/local/bin/glxgears
/opt/local/bin/glxgears:
	/opt/local/lib/libGL.1.dylib (compatibility version 4.0.0, current version 4.0.0)
	/opt/local/lib/libX11.6.dylib (compatibility version 11.0.0, current version 11.0.0)
	/usr/lib/libSystem.B.dylib (compatibility version 1.0.0, current version 1292.60.1)


/Library/Developer/CommandLineTools/SDKs/MacOSX11.3.sdk/System/Library/Frameworks/OpenGL.framework
/Users/martin/Dev/jzy3d/external/osmesa/include

/Users/martin/Dev/jzy3d/external/osmesa/lib/libGL.dylib
/Users/martin/Dev/jzy3d/external/osmesa/lib/libGLU.dylib
```




# Let VTK use Mesa, either at runtime (prefered) or build time 

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
