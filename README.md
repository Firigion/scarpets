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
### Positions
To set positions use the command `/script run set_position(<index>)` where `index` should be 1, 2 or 3. A marker will appear in the block the player is looking at. If no block is within reach, the marker will appear at the layers feet. These markers are the positions the shapes will use as reference. You can also `get_pos()` to get the coordinates of all set positions and toggle the rendering of the markers with `show_pos(<true|false>)`. Note that while markers are not rendered, positions are still recorded and you can set them, the `draw` commands will. If you log out or something and markers are not deleted (it happens sometimes), run `/script run remove_all_markers()`.
You can watch a video with some of the features [here](https://www.youtube.com/watch?v=F0MCtPvy46Q&t=3s), and another video with the rest of the features [here](https://www.youtube.com/watch?v=PMY4L_zKggc&t=12s). Sorry, I recorded during development, so this is not well ordered.

# Replace
To load, paste the code in `replace.mccmd` into a command block and power it. Then, all functions will be available through `/script run <function_name>`. This app adds two commands to replace blocks with some extra functionality: it will keep the block properties of the pasted block. This means, if you have a to spruce stair facing north, it will be replaced by a top sandstone stair facing north, and if you have a bich log in the z axis, it will be replaced by a striped sruce log in the z axis. 

But I hear you ask, "how can I chose what blocks to replace with what blocks?". Easy: just hold the block to replace in the offhand and the block to replace it with in the main hand. Select the area you want to affect and run `/script run replace_volume()`. To select the area, you define a cuboid by its two corners with `/script run set_pos(<1|2>)`, just like the shapes app. I stole the area selection from there. I also upgraded it a bit by adding functionality to the golden sowrd: if you left click a block or in the air, you will set one position and if you attack a block, you will set the other. Sadly, you can't attack air. This only works when holding a golden sword. If you are in the nether, the once the two corners are defined, a cuboid will actualy render. Why only in the nether, you ask? Excelent question. If you wanna turn rendering of the markers and cuboid on/off, use `/script run toggle_show_pos()`.

After recording the [video](https://www.youtube.com/watch?v=_iWv2vvnj8o) I added another command, so it's not showcased there: you cna now filter by properties: for example, if you only wanna replace logs that are in the x axis, you hold the corresponding logs in main and offhand and run `/script run replace_volume_filt('axis', 'x')`. Remember to put properties and values in single quotes, even stuf like `'true'` and `'5'`.

### tl:dr
Select positions with golden sword left and right click, or by using `/script run set_pos(<index>)` with `index` being 1 or 2. to replace blocks, run `/script run replace_volume()`, which will replace whatever block you ahve in the offhand with whatever block you have in the main hand, keeping the block properties of the replaced block, as long as they are somewhat compatible. You can filter for certain properties by using  `/script run replace_volume_filt(property, value)`.

# Nether portal POI display
Super simple app that you load using `/script load poi_show_md gloabl`. For once, the app is fully compatible with multiplayer (and with mutiple players using it at the same time, in particular, which is the important part). To use it, jsut hold an eye of ender in your main or offhand. A marker will appear in every nether portal POI around you in a 40 block radious by default. You can change that value using `/poi_show_md set_range <range>`. Please be sensible about the settings, because I didn't add any checks when setting new values.

The markers will update in real time, which means the game is constantly checking for new POIs poping up or old POIs disappearing. By default, the check is done every 5 game ticks. If this is too often for what your system can handle, use `/poi_show_md set_refresh_rate <value>` to something higher than 5. Again, be sensible with values. If you uso some non integer value, it will never update. I think. haven't tried.

The app also adds two simple commands to place and remove nether portal POIs from the world, without affecting the nether portal block: just look at the block the POI is in and run `/poi_show_md portalles_poi` to set one and `/poi_show_md remove_poi` to remove it. Sidenote: `remove_poi` will remove _any_ POI, not only nether portals. I forgot to check for the type, upsi.

For once, the [video](https://www.youtube.com/watch?v=Q6GULuQjgxQ&t=1s) is fully up to date with the features.

# Spirals
A nice app to make spirals with a few options. To use it, put `spirals.sc` in your scrpits folder and load it with `/script load spirals global`. After that, all functions will be available with the command `/spirals`. If you can't access the scripts folder, copy the contents of `spirals.mccmd` into a command block and power it. All functions will be available with `/script run <function_name>`.

Available commands:
* `spiral`: takes radius, separation between cycles of the spiral, total height and material. It will produce a spiral centered around the player. For example, to make a stone spiral with radius 10 and step 5 that makes 10 total cycles to reacha  height of 50: `/spirals spiral 10 5 50 'stone`. If you loaded with the command block, you'd do `/script run spiral(10, 5, 50, 'stone')`.
* `antispiral`: the same as spiral, but it turns the other way.
* `multi_spiral`: same as spiral, but takes one extra argument (before material) to decide how many spirals to draw.

This three commands have a counterpart in `spiral_template`, `antispiral_template` and `multi_spiral_template`. These commands, intead of using a material to make a one wide spiral, will take in a template and copy it around to make the spiral. To select the template, graba gold sword and use left and right click to define the area. Read about the replace app to see how to use it. of course, these commands dont take a `material` parameter.

Made a [video](https://youtu.be/WL9Pl3eaFaU) for all these functions, except multi spiral, which you can see in this [other video](https://youtu.be/sMGKnUiST6E).
