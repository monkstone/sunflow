![travis-ci](https://travis-ci.org/monkstone/sunflow.svg)


Sunflow Global Illumination Rendering System
v1.0.0-SNAPSHOT

Sunflow is a rendering system for photo-realistic image synthesis. It is written in Java and built around a flexible ray tracing core and an extensible object-oriented design.

Please consult the LICENSE file for license information.

----------------------------------------------------------------

This is a major refactor see CHANGELOG.md, but relies on much of the original code (but that was 10 years old) so sorely in need of change build now depends on `maven` and `jdk8`, `janino` is now update to `janino-3.0.6` and pulled from maven central

----------------------------------------------------------------

Build instructions:

```bash
mvn package
mvn dependency:copy # installs janino jars with built jar
```
----------------------------------------------------------------

Running instructions:

You need to have copies of janino.jar and commons-compiler.jar (version 3.0.6) in same directory as sunflow.jar
```bash
java -jar sunflow-1.0.jar
```
----------------------------------------------------------------------

Help:

If you need help see a psychiatrist

If you can help submit a PR

---------------------------------------------------------------------

Scene file format:

The SunflowGUI program accepts input in the .sc file format. As this is only a temporary file format, the best documentation for it is SCParser.java. You may also get a feel for what is supported by examining the example scene files provided in the data distribution.

----------------------------------------------------------------

Rendering options:

Here is a quick explanation of the basic rendering options.

Anti-aliasing:

The most important controls are aa.min and aa.max. These control the under/over-sampling of the image. The image will be first sampled at the rate prescribed by aa.min. Then, based on color and surface normal differences, the image will be refined up to the rate prescribed by aa.max.

A value of 0 corresponds to 1 sample per pixel.
A value of -1 corresponds to 1 sample every 2 pixels (1 per 2x2 block)
A value of -2 corresponds to 1 sample every 4 pixels (1 per 4x4 block)
A value of -3 corresponds to 1 sample every 8 pixels (1 per 8x8 block)
...
A value of 1 corresponds to 4 samples per pixel (2x2 subpixel grid)
A value of 2 corresponds to 16 samples per pixel (4x4 subpixel grid)
A value of 3 corresponds to 64 samples per pixel (8x8 subpixel grid)
...

Examples:
    - quick undersampled preview:          -2 0
    - preview with some edge refinement:    0 1
    - final rendering:                      1 2

Filtering:

Image quality is also affected by filtering. If you use oversampling (positive min or max AA), you will want to turn this on. Here are the names of the built-in filters:
  * box
  * triangle
  * gaussian
  * catmull-rom
  * mitchell
  * lanczos
  * blackman-harris
  * sinc

Box and triangle are best for previews as they are small and fast. The other filters are recommended for final image rendering.

Bucket rendering:

Sunflow proceses the image to be rendered in small squares called buckets. The size of these buckets can be controlled by a pixel width. Each rendering thread will be a assigned a single bucket. You may not get the bucket size you expect if you try to make them really small or really big, as there are some hard-coded limits to prevent excessive memory usage or excessive overhead.

The bucket ordering simply affects the order in which the buckets appear. They shouldn't have too much of an effect on overall rendering speed.

Soft shadows:

Area lights automatically create soft shadows in your scene. Once you have the proper meshlight exported, you can control the quality of shadows by changing the samples value. Please note this value is a number of rays PER triangle. Complex meshes made up of many triangles can become quite costly. The best is to restrict yourself to simple quads (2 triangles).

Global illumination:

Sunflow includes several methods to achieve global illumination. The easiest to setup is path tracing.

Path tracing is controlled by a sampling value and a number of bounces. A good starting point is 16 samples and 1 bounce. Increase the samples as needed to make the image less noisy. Note that this technique is not generally usable for complex lighting setups.

Irradiance caching or IGI. Tutorials comming soon.

Caustics:

Caustics are produced by light shining through refractive objects or being bounced by highly reflective materials. The only caustic algorithm currently implemented is via photon mapping. You must first pick a number of photons to shoot into the scene. Each light source will shoot this many photons, so be carefull if you have many lights in the scene. Pay attention to the messages in the console to see exactly what is going on.

Once you have a number of photons to emit, you must pick a way to store them. Only a kd engine is currently available for caustics. You can then set a value for the number of photons to gather at each shading point (start with ~50 to ~100) as well as a maximum search radius. These settings are highly scene dependent so experiment with them until you get satisfactory results.

----------------------------------------------------------------

Third party libraries:

Sunflow makes use of the following libraries, distributed according to the following terms:

Janino - An embedded Java[TM] compiler

Copyright (c) 2001-2016, Arno Unkrig

Copyright (c) 2015-2016  TIBCO Software Inc.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

   1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
   2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
   3. The name of the author may not be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
