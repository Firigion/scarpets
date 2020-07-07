## Scarpets
Scarpet scripts for small utilities.

Files with .sc format are meant to be used with `/script load <script_name> [global]` (see [this](https://github.com/gnembon/fabric-carpet/wiki/Installing-carpet-scripts-in-your-world) tutorial to learn how to install them). Files with .mccmd format are a thing I came up with as a reminder that they are not apps per se. They are a very long command that needs to be pasted into an (impulse) command block and powered once. I did this because this way you can easly test stuff in a server and don't need access to server files to upload the .sc file.

# Flowerify
Load with `/script load flowerify global`. It has two very simple functionalities: by running `/flowerify toggle_flowerify_continuous`, it will generate all flowers centered around the topmost gass block under the player in an 11x11 square. Move around to generate all of them. Optionaly, you can use `/flowerify set_pos 1` and `lowerify set_pos 2` to define a volume in which to generate flowers using the command `/flowerify flowerif_area`. You can hide/show the position markers with `lowerify set_pos [0|1]`. Go watch this dated [video](https://www.youtube.com/watch?v=1T9oyzhIn1k) showcasing an old version of this.

# Shapes
To load it either run `/script load shapes` or paste the code inside `shapes.mccmd` in a command block and power it. Then, all functions will be available through `/shapes <function_name>` or `/script invoke <function_name>`, respectively.  It's an app to draw shapes in-game, but, unlike the usual shape-drawing tools you see in Minecraft, this is centered around setting three points in space and drawing the shape defined but them. All shapes are drawn with the command `/shapes draw_<shape> material width`, where `<shape>` is replaced by the shapes name. For example, `/shapes draw_plane white_concrete 3`. Available shapes are:
* **Plane**: it will draw a plane passing through the three defined points, so long as they are not colinear. If they are, it will probably give a divide by zero error, or something like that. Three colinear points don't define a plane anyway, so why should it work?
* **Sphere**: it will draw the smallest sphere passing through the three defined points. Why would you want a bigger one? Note that passing width as 1 will give a slightly different behaviour from any other number, because it uses a different function in the backend. Width is counted from the radious towards the inside.
* **Ring**: it will draw the ring (thicc circle) passing through the three defined points. This is the reason I made this script for: do you know how hard it is to draw circles in any orientation in three dimensional space freehand? `width` will increase width of the shape in all directions, not only radialy.
* **Disc**: like ring, but full.
* **Line**: it's like intersecting two planes. Literaly, that's how it's done in the app. Line will only use the positions 1 and 2 to draw, ignoring position 3.
* **Fast line**: this is an alternative line algorithm. It's way faster for verly big lines, so if you need to draw a 200 block line, use this. It might even work into unloaded chunks, not sure. Downside is, the `width` paremeter does nothing. I kept it to keep the signatures of all shapes even.

You can also query the distance between positions 1 and 2 via the `distance` command.

### Positions
To set positions, grab a golden sword and left click, right clcik or shift right click on a block with it, to set positions 1, 2 and 3, respectively. You can also do it by running `/shapes set_position <number>`. A marker will appear in the block the player is looking at. If no block is within reach, the marker will appear at the layers feet (you can't left click ari, sadly). These markers are the positions the shapes will use as reference. You can also `get_pos()` to get the coordinates of all set positions and toggle the rendering of the markers with `show_pos(<true|false>)`. Note that while markers are not rendered, positions are still recorded and you can set them, the `draw` commands will. If you log out or something and markers are not deleted (it happens sometimes), run `/script run remove_all_markers()`.
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
A nice app to make spirals with a few options. To use it, put `spirals.sc` in your scrpits folder and load it with `/script load spirals`. After that, all functions will be available with the command `/spirals`. If you can't access the scripts folder of your world save, copy the contents of `spirals.mccmd` into a command block and power it. All functions will be available with `/script run <function_name>`.

### Commands
* `spiral`: takes radius, separation between cycles of the spiral, total height and material. It will produce a spiral centered around the player. For example, to make a stone spiral with radius 10 and step 5 that makes 10 total cycles to reacha  height of 50: `/spirals spiral 10 5 50 stone`. If you loaded with the command block, you'd do `/script run spiral(10, 5, 50, 'stone')`.
* `antispiral`: the same as spiral, but it turns the other way.
* `multispiral`: same as spiral, but takes one extra argument (before material) to decide how many spirals to draw.
* `antimultispiral`: you know, like multi spiral, but anti.

You have the option to use a special material. If instead of a Minecraft block name you enter `template` as material, it will take in a template and copy it around to make the spiral. To select the template, grab a golden sword and use left and right click to define the area. Read about the replace app to see how to use it. Of course, these commands dont take a `material` parameter. If you don't want to stand in the place you want to be the center of the spiral, you can also define a center with the third position. To set it, shift right click with your sword where you want it to be (if you aren't looking at a block, it will be set at your feet).

### Settings
You also have a bunch of options to further customize your spiral making experience. To avoid cluttering the commands, these are done via settings. To access the settings menu run `settings`, and click your way through the options. Some of them are togglable and the corresponding command is `toggle_<option_name>`, some of them take an argument to set, which you can do with `set_<option_name> <value>`.

Current settings available are:
* `show_pos`: toggle to show/hide markers and box showing selection. Default: `true`.
* `paste_with_air`: toggle to generate spiral from template counting or disregarding air. Default: `false`.
* `replace_block`: toggle to generate complete spiral, or generate it only replacing certain block. Instead of using the block to replace as an argument, when this option is enabled, the app wil take whatever block you have in your offhand and replace that one. For stuff you can't easily get in item form, there are a few aliases: hold a feather to replace air, an eye of ender to replace end portals, a flint and steel to replace nether portals and the corresponding buckets for liquids. Default: `false`.
* `slope_mode`: toggle between slope mode (value `true`) and pitch mode (value`false`). The second argument of the spiral making commands defines how fast the spiral grows. In pitch mode, the number represents the height diference between full revolutions of a spiral, while in slope mode, it represents how many blocks it should go up for every block moved horizontaly. You can use floats like 0.5 to move one block up every second horizontal block. Default: `false`.
* `axis`: one of `x`, `y` and `z`. Defines the axis about which the spiral is generated. Default: `'y'`.
* `max_template_size`: a safeguard to avoid using templates that would kill your game, because you misclicked the position selector. This limit counts the actual temaplte volume if `paste_with_air` is `true`, or only the non air blocks, if it's `false`, meaning you can still paste a very large very saprse tempalte. Default is a rather conservative value, you can increase it a bunch if you trust your computer's power. Default: `100`.
* `undo_history_size`: sets the amount of stories the undo command saves, efectively defining how many mistakes you can fix. Default: `100`.

### Undo
We have an `undo` command. It takes one argument defining how many steps back you want to go. To undo your last three spirals made , just use `undo 3`. Ez pz. Note that multispirals save to separate stories, so to completly undo a double spiral, you ened to use `undo 2`. 

The undo functionality comes with one extra command: `go_to_story <number>`. This works like `undo`, but instead of undoing the last `<number>` actions, it skips all of them but the last one. So, if you made three spirals and do `go_to_story 3`, it will undo the first spiral you made. Bare in mind, this might have some odd behaviours, because all the `undo` functions do is paste back whatever the spiral replaced. So if you make a spiral, then replace some of it with another spiral and then undo only the first one, it will put back in place whatever *it* replcaed, cutting through the new spiral.

### Videos
Made a [video](https://youtu.be/WL9Pl3eaFaU) for all the functions, except multi spiral, which you can see in this [other video](https://youtu.be/sMGKnUiST6E). Also made a third [video](https://youtu.be/o8xU7nN55jI) showing the shift + right click thing. After that I did a separate [video](https://www.youtube.com/watch?v=Im9Do0zVg8w) on some of the settings and [another one](https://www.youtube.com/watch?v=qeGa9eZy8PA) about the undo functions. I make them as i go, don't judge me.

# Waves
A nice app to make waves with a few options. To use it, put `waves.sc` in your scrpits folder and load it with `/script load waves`. After that, all functions will be available with the command `/waves`. If you can't access the scripts folder of your world save, copy the contents of `spirals.mccmd` into a command block and power it. All functions will be available with `/script run <function_name>`.

This app is a direct derivation of the spirals app, so most of the features and settings of that app are directly imported here, so go read about that to learn how to use it. The main difference is of course that the funtion to actualy draw the sapes is `wave`, instead of `spiral`. `waves` have four parameters: `wavelength`, `amplitude`, `size`, `material`. `size` defines how long (or tall, deoending on direction) the wave is going to be. Bare in mind that the actual size the wave is generated might be up to three blocks off of `size`. This was done this way to preserve the symmetricness (?) of the curve.

Other than that, all of the settings are the same as in the spirals app, except for the `axis` one, which in this case has six options instead of three: `xy`, `xz`, `yx`, `yz`, `zx` and `zy`. In all of them, the first character indicates the axis along which the wave will be created, and the second one the axis _into_ which it will me created. So, `xy` makes a vertical wave going along the x axis.

For a very short showcase, see [this video](https://youtu.be/9qSo_WL8Rok), or go watch the videos on spirals for extra info on how to use the tools.

# Curves
Curves combines **spirals**, **waves** and **replace** into one single app. All of them work basicaly the same, except for the fact that some settings are separated into categories and the replace commnands are now `/curves soft_replace` and `/curves soft_replace_filt <property> <value>`.

Curves also includes a new curve type: circular waves (or cwaves). Please note the algorithm to generate these is not as refined as the ones for spirals and waves, so it's quite a bit heavier to excecute. It creates a wave along a circle (duh) with the wave plane being coplanar with the circle (planar waves) or perpendicular to the circle (transverse waves). You have the option to make a full circle or just a partial one and, like always, these curves support template pasting, undo functionality and settings. It shares `axis` settings with spirals. The functions are simply called `cwaves_planar`, `cwaves_transverse`, `cwaves_planar_partial` and `cwaves_transverse_partial`. They have the following arguments:

* `radius`: the radius of the circle
* `amplitude`: the amplitude of the wave
* `cycles`: ammount of cycles the wave does in one full turn of the circle. Whole numbers will mean the wave meets itself at the end, resulting in a closed curve. You can play around with non whole numbers for more interesting results.
* `material`: material out of which the curve is made. Use `template` to create the wave using the selection.

`partial` functions also have the `from` and `to` arguments, in degrees. Note you can make more than one full revolution. Having fractional `cycle`s and multiple turns makes for some interesting patterns. You can also use `from` as a phase value to rotate the shape by some ammount. Just make sure to set `to` to `from + 360` to have the whole shape be rotated.

An extra very small thing added to curves.sc is a world-edit-style brush. Make a selection with your golden sword, grab an endrod and just right click around. It will paste the selection as far as your (probably not elven) eyes can see.

As usual, the bad video showcasing these things can be fount [here](https://youtu.be/gP_p0lJbOjc).


# Spawning shperes
To use this app, put the .sc file int your scripts fodler and run `/script load spawning_spheres`. It adds a an option to render spawning sphere around a position with one click. Right click with a wool block and a double sphere of the corresponding colour will appear. Inner sphere shows minimum distnace to the player for mobs to spawn. Outer ones shows where mobs instantly despawn. Right click again to move the sphere, or click with another colour to create a second double sphere. Right click with a glass block to delete all renders.

If for some reason you need a video to see this in action, [here](https://youtu.be/5V6gqe8OHm4) it is.
