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

# Cover

Cover is a small utility app oriented at createive mode decoration. To use it, put [cover.sc](https://github.com/Firigion/scarpets/blob/master/cover.sc) into your `/scripts` folder in your wrold save and run `/script load cover`. Its main function is to cover a block type with some other block, the idea being this helps spawnproof decorations and buildings.

To select what block to cover with what, place the block to cover in your offhand and the block to cover it _with_ in your main hand when running the command. If you want to do many block paris at once, place shulker box with the blocks to cover in the offhand and another one with the corresponding blocks in the main hand. Block pairs (covered-coveree) will be made by matching slots in the shulker box's inventoy. Blocks will only get palced replacing air.

The app has two main modes: continuous and region. 

In continuos mode, a box will follow the player, and every tick all blocks in the box that match one of the pairs will get covered. Toggle continuous mode on and off with `/cover continuous` and set the box size with `/cover set_size <dx> <dy> <dz>` and it's vertical offset from the player's feet with `/cover set_offset <y_offset>`. Defaults to `/cover set_size 20 8 20` and `/cover set_offset 3`.

In region mode, you first make a selection to define the area to affect, and then cover the blocks with `/cover region`. To select the volume use an **iron sword** and right and left click to define the corners of the rectangle encompassing the region. Use `/cover reset_positions` to remove the selection.

Both modes support an undo funtionality, read [this](https://github.com/Firigion/scarpets/#undo) for more details. Take into account that continuos mode will record each successful tick as a separate action, so you might need to undo a lot of actions. 

The [video](https://youtu.be/i3YxwoTCOVM) for this one is a bit long, but it whocases all the things described above plus a nice trick you can use thi for.

# Soft Replace
Soft replace is a small utility mod oriented at createive mode decoration (just like the last one!). To use it, put [soft_replace.sc](https://github.com/Firigion/scarpets/blob/master/soft_replace.sc) into your `/scripts` folder in your wrold save and run `/script load soft_replace`. This app will help you replace blocks just like the vanilla replace command, but keeping their block properties. This means that if you made a complex structure out of stair blocks and decide that you want to try using birch instead of diorite, running soft replace will replace all diorite stairs by birch stairs, preserving their orientation.

To do so, just hold the block you want to replace (diorite staris, in this case) in your offhand and the block you want to replace _with_ (birch stairs) in your main hand. Then, to select the area you want to affect, grab a **stone sword** and right and left click to select the corners of a rectangke defining the volume. Finaly, run `/soft_replace region` to excecute the operation.

If you want to, say, replace only the top stairs with birch ones, but the bottom ones with sandstone, you can use `/soft_repalce region_filt <propery> <value>`, where in this case you'd replace `<property>` with `half` and `<property>` with `bottom`. This way you filter out only the blocks that have that propery set to that value.

Here's a [video](https://www.youtube.com/watch?v=_iWv2vvnj8o) to showcase the app in action. I recorded it during develepment, so don't pay attention at how the commands are called or the fact that I use a golden sword instead of a stone one.

# Nether portal POI display
Super simple app that you load using `/script load poi_show_md gloabl`. For once, the app is fully compatible with multiplayer (and with mutiple players using it at the same time, in particular, which is the important part). To use it, jsut hold an eye of ender in your main or offhand. A marker will appear in every nether portal POI around you in a 40 block radious by default. You can change that value using `/poi_show_md set_range <range>`. Please be sensible about the settings, because I didn't add any checks when setting new values.

The markers will update in real time, which means the game is constantly checking for new POIs poping up or old POIs disappearing. By default, the check is done every 5 game ticks. If this is too often for what your system can handle, use `/poi_show_md set_refresh_rate <value>` to something higher than 5. Again, be sensible with values. If you uso some non integer value, it will never update. I think. haven't tried.

The app also adds two simple commands to place and remove nether portal POIs from the world, without affecting the nether portal block: just look at the block the POI is in and run `/poi_show_md portalles_poi` to set one and `/poi_show_md remove_poi` to remove it. Sidenote: `remove_poi` will remove _any_ POI, not only nether portals. I forgot to check for the type, upsi.

For once, the [video](https://www.youtube.com/watch?v=Q6GULuQjgxQ&t=1s) is fully up to date with the features.

# Spawning shperes
To use this app, put the .sc file int your scripts fodler and run `/script load spawning_spheres`. It adds a an option to render spawning sphere around a position with one click. Right click with a wool block and a double sphere of the corresponding colour will appear. Inner sphere shows minimum distnace to the player for mobs to spawn. Outer ones shows where mobs instantly despawn. Right click again to move the sphere, or click with another colour to create a second double sphere. Right click with a glass block to delete all renders.

If for some reason you need a video to see this in action, [here](https://youtu.be/5V6gqe8OHm4) it is.

# Stack potion effects

Pretty much what it ways on the tin: when you drink a potion, intead of overriding the duration you had left for that same effect, it adds the potion length to the old one. To use it, put [stack_potion_effects.sc](https://github.com/Firigion/scarpets/blob/master/stack_potion_effects.sc) into your `/scripts` folder inside your world save. You might want to have this app automaticaly load when you launch your world or server. To do that, follow [these](https://github.com/gnembon/fabric-carpet/wiki/Installing-carpet-scripts-in-your-world#keeping-scripts-loaded) instructions.


# Curves
The curves app is a crateive mode-oriented script that includes a few commands to generate 3D curves of diferent types. To use it, put [curves.sc](https://github.com/Firigion/scarpets/blob/master/curves.sc) into your `/scripts` folder in your wrold save and run `/script load curves`. All commands in this app should be available with `/curves <command>`. A playlist with all the videos relevant to this app can be found [here](https://www.youtube.com/playlist?list=PL8lLKEt66RqslmFunrXESEZeu9HS6LJnd).

The app includes four [shapes or curve types][1], has a [settings UI][2], a [help command][3], a way to make [selections][4] in-world and [undo][5] your actions. It also includes the [soft replace app][7]. 

### Help

This is pretty self-explanatory: run `curves help` and you will be presented with a clickable menu that lets you browse te information in this article in a more condensed way. There are also some category-specific help commands, which you can run by hand if you like, or just click your way to them. They are all called `help_<category_name>`, you can tab autocomplete them.

### Settings

This one is also simple: run `curves settings` and you will be presented with a clickable menu that lets you set all the settings in the curves app and shows the current values. When clicking a setting, a new command will be suggested. You can also type the setting commands by hand, they are called either `toggle_<setting>` or `set_<setting>`. All the settings for each aspect of the app will be listed under the description of that category, so go to [undo][5] to see the undo related settings, for example. The usage of settings is shocased [here](https://www.youtube.com/watch?v=Im9Do0zVg8w), but I think it's pretty self-explanatory.

### Selections and position setting

While most drawing commands in this app work fine placing a block along a curve, you will find it's also very useful to create a template that gets pasted along the curve, allowing for more complex and interesting shapes. A template is defined by a cuboid area selection determined by its two corners. To define those corners, grab a golden sword and right and left click on a block or in mid air. A faint white box will apprear once the selection is complete. Bare in mind that the templates are dimension specific, so if you try to make a selection in the overworld and go to the end to paste it, it will not work. 

You will alos find all shapes have a center or origin. That block is usualy the players feet, but if you would like ot have a repeateble center position, or be able to watch the sahpe from afar when creating it, you can set your third position using shift and right click. All shapes will use that one as origin.

You can erase all positions with `reset_pos` and query them with `get_pos` and, if for some reason you want to, you can set them via commands instead of with the golden sword. For that, just run `curves set_pos <i>`, where `<i>` should be either 1, 2 or 3, depending on the position you want to set. You can also toggle rendering of positions and selection on and off with the setting `toggle_show_pos`. If you need to see a video of this working for some reason, [here](https://youtu.be/o8xU7nN55jI) it is.

### Undo

All actions done with this app are saved into history. You can at any time undo any or all of them. To do so, you have two commands available: `undo <n>` and `go_back_stories <n>`, both of which take a number as a parameter. The first one will undo as many actions as requested, so `curves undo 4` will undo your last four actions. The second one is a bit more strange: `curves go_back_stories <n>` will skip your last 3 actions and undo only the thing you did 4 actions ago. Bare in mind, this might have some odd behaviours, because all the `undo` functions do is paste back whatever the action replaced. So if you make a spiral, then replace some of it with another spiral and then undo only the first one, it will put back in place whatever *it* replaced, cutting through the new spiral.

There is one setting pertinent to the `undo` functions, that set how many actios to save into history. It defautls to 100, but you can set ti to whatever you like with `set_undo_history_size <size>`. You can see this features being used in [this bad showcase video](https://www.youtube.com/watch?v=qeGa9eZy8PA) using spirals.

### Brush

There is also an obscure and somewhat usless freature: if you click with a blaze rod, you will paste your template wherever you are looking, as far as the horizon reaches. Or 200 blocks, whatever comes first.

### Soft replace

This is just a port of the [replace app][6] (which replaces one block with another, keeping the block properties) to the curves app, because it can be useful to edit the template and the selection method is the same. The only difference with how you operate it is that the commands are called `soft_replace`, which replaces the block in the off hand with the one in the main hand, and `soft_replace_filt <property> <value>`, which does the same, but only for blocks with a specific property-value pair.

### Curves

All the curves have about the same signature: `/curves <curve_name> <param1> <param2> ... <paramN> <material>`. Each curve type requires a different ammount of parameters and all take a material as last input. The material can either be any minecraft block name, or `'template'`. Some curve types also have a few variations, each one having its own command. For example, doing an anti multi spiral with radius 20, pitch 11, size 80 and 4 turns out of prismarine would look like 
```/curves antimultispiral 20 11 80 4 prismarine``` 

If at some point you don't know what parameters each shape needs, just run it without parameters and the error message will include the expected signature. There are a number of settings available for all shapes to share:

* `paste_with_air`: when pasting a tempalte, this setting decides if said template includes or excludes air. Default: `false`.
* `replace_block`: if enabled, generated curves will only be placed replacing the block you have in the off hand. For stuff you can't easily get in item form, there are a few aliases: hold a feather to replace air, an eye of ender to replace end portals, a flint and steel to replace nether portals and buckets for the corresponding liquids. Default: `false`.
* `max_template_size`: a safeguard to avoid using templates that would kill your game, because you misclicked the position selector. This limit counts the actual temaplte volume if `paste_with_air` is `true`, or only the non air blocks, if it's `false`, meaning you can still paste a very large very saprse tempalte. Default is a rather conservative value, you can increase it a bunch if you trust your computer's power. Default: `100`.
* `max_operations_per_tick`: limits the ammount of operations per tick, as it says in the name. This usualy is not needed, but for some slower systems or shapes that have an inefficient algorithm, not having this can be inconvinient. Default: 10000

Other than those, all shapes have a "directio" in which they generate. For all circle-based curves (that's all of them, except for waves), `circle_axis` defines the axis perpendicular to the circle in question and defaults to `y`, and for waves, `wave_axis` defines the diretion along which and into which it generates, defaults into `yx`.

#### Spirals

Spirals are defined by three parameters: 

* `radius`:	the circle radius,
* `pitch`: the rate at which it grows. With the `slope_mode` setting you can make this setting be interpreted as slope, rather than pitch, the former being how many blocks per step the spiral advances, and the latter how many blocks per full revolution. Defalut values is `false`, meaning pitch mode.
* `size`: the total size (if `circle_axis` is `y`, this would be total height, for example)

`spirals` by default spin counterclockwise; to get a clockwise spiral, use `antispiral`. Both this commands have a variation in `multispiral` and `antimultispiral`, which create multiple spirals uniformly distributed along the circle. Both variations take a fourth parameter before material, it being how many spirals to generate. For example, to generate three concentric anticlockwise spirals out of the template with radius 30, pitch 11 and total 100 blocks, each 120 degrees away from the next:
```
/curves multispiral 30 11 100 3 template
```
I made some videos showcasing spirals. Since it was the frst curve I worked on, spiral development is entangled with the development of other fetures: [this video](https://youtu.be/WL9Pl3eaFaU) shows  all the functions, except multi spiral, which you can see in this [other video](https://youtu.be/sMGKnUiST6E).

#### Waves

Waves are defined by three parameters:

* `wavelength`: how many blocks does it take the wave to complete a full oscilation
* `amplitude`: how far from the baseline does one oscilation reach 
* `size`: how many blocks does the wave fover in total

You can think of waves as goin into one direction and oscilating into another, both of which are defined in the `wave_axis` setting. For example, a wave going into (positive) `x` oscilating up and down would have the default value of `xy`. Note: to have waves be symmetrical about the max and min point of each oscilation, the wavelength is actualy rounded to the nearest multiple of four. So when setting a wavelength, do know the actual value will be plus or minus two.

You can find the video breefly shocasing this right [here](https://youtu.be/9qSo_WL8Rok).

#### Circular waves (cwaves)

Please note the algorithm to generate these is not as refined as the ones for the other curves, so it's quite a bit heavier to excecute. It creates a wave along a circle (duh) with the wave plane being coplanar with the circle (planar waves) or perpendicular to the circle (transverse waves). You have the option to make a full circle or just a partial one, which means you have a total four functions: `cwaves_planar`, `cwaves_transverse`, `cwaves_planar_partial` and `cwaves_transverse_partial`. They have the following arguments:

* `radius`: the radius of the circle
* `amplitude`: the amplitude of the wave
* `cycles`: ammount of cycles the wave does in one full turn of the circle. Whole numbers will mean the wave meets itself at the end, resulting in a closed curve. You can play around with non whole numbers for more interesting results.
* `material`: material out of which the curve is made. Use `template` to create the wave using the selection.

`partial` functions also have the `from` and `to` arguments, in degrees. Note you can make more than one full revolution. Having fractional `cycle`s and multiple turns makes for some interesting patterns. You can also use `from` as a phase value to rotate the shape by some ammount. Just make sure to set `to` to `from + 360` to have the whole shape be rotated, like so:
```
/curves cwaves_transverse_partial 40 8 10 45 405 white_stained_glass 
```

As usual, the bad video showcasing these things (plus a few tricks) can be found [here](https://youtu.be/gP_p0lJbOjc).

#### Stars

It defines a star by taking N points on an outer circle, N points on an inner circle, and connecting them with straingth lines. The parameters are:

* `outer_radius`: radius of the outer circle.
* `inner_radius`: radius of the inner circle.
* `n_points`: the ammount of points in the star.
* `phase`: a phase or rotation, so that you can point your stars in any direction you want.
* `material`: material out of which the curve is made. Use `template` to create the star using the selection.

Because the star is build by connecting points, if you set inner and outer radius to the same value, you will get a polygon of 2N sides. Also, 2 and 3 pointed stars are not a thing, but they make good rhombi and triangle/iregular hexagon tools.

Here's a [video](https://www.youtube.com/watch?v=XT4XJngGZRM&t=4s) showing this functionality and some tricks.

[1]: https://github.com/Firigion/scarpets#curves-1
[2]: https://github.com/Firigion/scarpets#selections-and-position-setting
[3]: https://github.com/Firigion/scarpets#help
[4]: https://github.com/Firigion/scarpets#selection
[5]: https://github.com/Firigion/scarpets#undo
[6]: https://github.com/Firigion/scarpets#replace
[7]: https://github.com/Firigion/scarpets#soft_replace
