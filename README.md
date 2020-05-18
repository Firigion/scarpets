## Scarpets
Scarpet scripts for small utilities.

Files with .sc format are mant to be used with `/script load <script_name> [global]`. Files with .mccmd format are a thing I came up with as a reminder that they are not apps per se. They are a very long command that needs to be pasted into an (impulse) command block and powered once. I did this because this way you can easly test stuff in a server and don't need access to server files to upload the .sc file.

# Flowerify
Load with `/script load flowerify global`. It has two very simple functionalities: by running `/flowerify toggle_flowerify_continuous`, it will generate all flowers centered around the topmost gass block under the player in an 11x11 square. Move around to generate all of them. Optionaly, you can use `/flowerify set_pos 1` and `lowerify set_pos 2` to define a volume in which to generate flowers using the command `/flowerify flowerif_area`. You can hide/show the position markers with `lowerify set_pos [0|1]`. Go watch this dated [video](https://www.youtube.com/watch?v=1T9oyzhIn1k) showcasing an old version of this.

# Shapes
To load, paste the code in a command block and power it. Then, all functions will be available through `/script run <function_name>`. This file is still called linalg.mccmd, for some reason; ignore that. It's an app to draw shapes in-game, but, unlike the usual shape-drawing tools you see in Minecraft, this is centered around setting three points in space and drawing the shape defined but them. All shapes are drawn with the command `/script run dra_<shape>(mateeial, width)`, where `<shape>` is replaced byt the shapes name. For example, `/script run draw_plane('white_concrete', 3)`. Available shapes are:
* **Plane**: it will draw a plane passing through the three defined points, so long as they are not colinear. If they are, it will probably give a divide by zero error, or something like that. Three colinear points don't define a plane anyway, so why should it work?
* **Sphere**: it will draw the smallest sphere passing through the three defined points. Why would you want a bigger one? Note that passing width as 1 will give a slightly different behaviour from any other number, because it uses a different function in the backend. Width is counted from the radious towards the inside.
* **Ring**: it will draw the ring (thicc circle) passing through the three defined points. This is the reason I made this script for: do you know how hard it is to draw circles in any orientation in three dimensional space freehand? `width` will increase width of the shape in all directions, not only radialy.
* **Disc**: like ring, but full.
* **Line**: it's like intersecting two planes. Literaly, that's how it's done in the app. Line will only use the positions 1 and 2 to draw, ignoring position 3.
* **Fast line**: this is an alternative line algorithm. It's way faster for verly big lines, so if you need to draw a 200 block line, use this. It might even work into unloaded chunks, not sure. Downside is, the `width` paremeter does nothing. I kept it to keep the signatures of all shapes even.
