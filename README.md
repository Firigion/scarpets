## Scarpets
Scarpet scripts for small utilities.

Files with .sc format are meant to be used with `/script load <script_name> [global]` (see [this](https://github.com/gnembon/fabric-carpet/wiki/Installing-carpet-scripts-in-your-world) tutorial to learn how to install them). Files with .mccmd format are a thing I came up with as a reminder that they are not apps per se. They are a very long command that needs to be pasted into an (impulse) command block and powered once. I did this because this way you can easily test stuff in a server and don't need access to server files to upload the .sc file.

## Contents
* [Flowerify](https://github.com/Firigion/scarpets/blob/master/README.md#flowerify) (Deprecated)
* [Skip night](https://github.com/Firigion/scarpets/blob/master/README.md#skip-night)
* [Shapes](https://github.com/Firigion/scarpets/blob/master/README.md#shapes)
* [Cover](https://github.com/Firigion/scarpets/blob/master/README.md#cover)
* [Soft replace](https://github.com/Firigion/scarpets/blob/master/README.md#soft-replace)
* [Nether portal POI display](https://github.com/Firigion/scarpets/blob/master/README.md#nether-portal-poi-display)
* [Spawning spheres](https://github.com/Firigion/scarpets/blob/master/README.md#spawning-spheres) (Deprecated)
* [Stack potion effects](https://github.com/Firigion/scarpets/blob/master/README.md#stack-potion-effects)
* [Curves](https://github.com/Firigion/scarpets/blob/master/README.md#curves)
* [Storage tech aid](https://github.com/Firigion/scarpets/blob/master/README.md#storage-tech-aid)

# Flowerify
[NOTE: this is deprecated in favour of a better app, available at the [official scarpet app store](https://github.com/gnembon/scarpet).]

Load with `/script load flowerify global`. It has two very simple functionalities: by running `/flowerify toggle_flowerify_continuous`, it will generate all flowers centered around the topmost grass block under the player in an 11x11 square. Move around to generate all of them. Optionally, you can use `/flowerify set_pos 1` and `flowerily set_pos 2` to define a volume in which to generate flowers using the command `/flowerify flowerify_area`. You can hide/show the position markers with `flowerily set_pos [0|1]`. Go watch this dated [video](https://www.youtube.com/watch?v=1T9oyzhIn1k) showcasing an old version of this.

# Skip night
Load with `/script load skip-night`. A tiny app to skip the night without the need for a bed bot. It comes in two modes: bed mode and command mode. You can toggle between them by editing the configuration inside the app. Bed mode will skip the night as long as there is a bed with `skip` in its name placed somewhere in the overworld, no need for it to be in a loaded chunk. Command mode will not track beds, but creates a command `/skip-night toggle` to toggle between skipping on and off.

Both modes will check daytime every 100 game ticks by default, can be configured inside the app. When skipping the night, time is _added_ to the current game time, so day and local difficulty will not be affected, as shown in [the showcase video](https://youtu.be/FY0PwGE0g68).

# Shapes
To load it either run `/script load shapes` or paste the code inside `shapes.mccmd` in a command block and power it. Then, all functions will be available through `/shapes <function_name>` or `/script invoke <function_name>`, respectively.  It's an app to draw shapes in-game, but, unlike the usual shape-drawing tools you see in Minecraft, this is centered around setting three points in space and drawing the shape defined but them. All shapes are drawn with the command `/shapes draw_<shape> material width`, where `<shape>` is replaced by the shapes name. For example, `/shapes draw_plane white_concrete 3`. Available shapes are:
* **Plane**: it will draw a plane passing through the three defined points, so long as they are not colinear. If they are, it will probably give a divide by zero error, or something like that. Three colinear points don't define a plane anyway, so why should it work?
* **Sphere**: it will draw the smallest sphere passing through the three defined points. Why would you want a bigger one? Note that passing width as 1 will give a slightly different behaviour from any other number, because it uses a different function in the backend. Width is counted from the radius towards the inside.
* **Ring**: it will draw the ring (thicc circle) passing through the three defined points. This is the reason I made this script for: do you know how hard it is to draw circles in any orientation in three dimensional space freehand? `width` will increase width of the shape in all directions, not only radialy.
* **Disc**: like ring, but full.
* **Line**: it's like intersecting two planes. Literally, that's how it's done in the app. Line will only use the positions 1 and 2 to draw, ignoring position 3.
* **Fast line**: this is an alternative line algorithm. It's way faster for very big lines, so if you need to draw a 200 block line, use this. It might even work into unloaded chunks, not sure. Downside is, the `width` parameter does nothing. I kept it to keep the signatures of all shapes even.
* **tube elipse**: this draws a tube with an elliptic profile between p1 and p2 (hollow center) with a major radius defined by the position of p3 where p3 is inline with the inner edge and a minor radius defined by argument, width is also defined by argument.
* **tube circle**: this draws a tube between p1 and p2 (hollow center) with a radius defined by the position of p3 where p3 is inline with the inner edge and a width defined by argument.
* **cylinder elliptic**: this draws a cylindroid between p1 and p2 with a major radius defined by the position of p3 where p3 is inline with the edge and a minor radius is defined by argument.
* **cylinder**: this draws a cylinder between p1 and p2 with a major radius defined by the position of p3 where p3 is inline with the edge and a minor radius defined by argument.

There's an extra line feature that creates a straight line from position 1 and into the direction the player is looking. The signature is `line_sight(length, material)`. You can also query the distance between positions 1 and 2 via the `distance` command and delete connected regions by clicking them with a snowball.

### Positions
To set positions, grab a golden sword and left click, right click or shift right click on a block with it, to set positions 1, 2 and 3, respectively. You can also do it by running `/shapes set_position <number>`. A marker will appear in the block the player is looking at. If no block is within reach, the marker will appear at the layers feet (you can't left click ari, sadly). These markers are the positions the shapes will use as reference. You can also `get_pos()` to get the coordinates of all set positions and toggle the rendering of the markers with `show_pos(<true|false>)`. Note that while markers are not rendered, positions are still recorded and you can set them, the `draw` commands will. If you log out or something and markers are not deleted (it happens sometimes), run `/script run remove_all_markers()`.

You can watch a video with some of the features [here](https://www.youtube.com/watch?v=F0MCtPvy46Q&t=3s), and another video with the rest of the features [here](https://www.youtube.com/watch?v=PMY4L_zKggc&t=12s). The snowball and straight lines are featured in [this "building a tetrahedron" video](https://youtu.be/iTFD0bVmLQk). Sorry, I recorded during development, so this is not well ordered.

# Cover

Cover is a small utility app oriented at creative mode decoration. To use it, put [cover.sc](https://github.com/Firigion/scarpets/blob/master/cover.sc) into your `/scripts` folder in your world save and run `/script load cover`. Its main function is to cover a block type with some other block, the idea being this helps spawnproof decorations and buildings. It can also generate random patterns from a block pool.

To select what block to cover with what, place the block to cover in your offhand and the block to cover it _with_ in your main hand when running the command. If you want to do many block pairs at once, place shulker box with the blocks to cover in the offhand and another one with the corresponding blocks in the main hand. Block pairs (covered-coveree) will be made by matching slots in the shulker box's inventory. Blocks will only get placed replacing air.

The app has four main modes: continuous, sphere, region and random. 

In continuous mode, a box will follow the player, and every tick all blocks in the box that match one of the pairs will get covered. Toggle continuous mode on and off with `/cover continuous` and set the box size with `/cover set_size <dx> <dy> <dz>` and it's vertical offset from the player's feet with `/cover set_offset <y_offset>`. Defaults to `/cover set_size 20 8 20` and `/cover set_offset 3`.

In sphere mode, you just need to run `/cover sphere <radius>`, where `<radius>` should be the radius of the decires sphere, as you'd imagine.

In region mode, you first make a selection to define the area to affect, and then cover the blocks with `/cover region`. To select the volume use an **iron sword** and right and left click to define the corners of the rectangle encompassing the region. Use `/cover reset_positions` to remove the selection.

Random mode uses the same selection as region mode. It uses the items in a shulker box in the main hand to generate a pool of random blocks to place. To alter the weight of each item, just occupy more slots in the box with that item. If your offhand is empty, it will just fill the whole region. If has an item or shulker box, the items in the box will act as a filter and blocks will only be placed on blocks matching the items. You can run it with `/cover random` or `/cover random_cover`, the latter being version with slightly different behaviour, making it more similar to the cover functions.

To use blocks like air or liquids, there are some aliases: 
```
water bucket -> water
lava bucket -> lava
feather -> air
ender_eye -> end_portal
flint_and_steel -> nether_portal
```

You can toggle between asynchronous behaviour and synchronous behaviour for all functions except continuous mode using `toggle_parallel`. The former is the default and will run all commands in a separate thread, which will result in a slower execution of the command, but it should not lag the game at all. If you are running something on a large area, be patient, it _is_ running, even if the MSPT didn't jump. The latter forces the game to run the commands synchronously, which will force the game to do stuff as fast as it can, but for larger regions, that might mean a very large lag spike and temporarily freezing the game. Use with care. You can only do one job at a time. Continuous mode always runs in synch.

All modes support an undo functionality, read [this](https://github.com/Firigion/scarpets/#undo) for more details. Take into account that continuous mode will record each successful tick as a separate action, so you might need to undo a lot of actions. 

The [video](https://youtu.be/i3YxwoTCOVM) for this one is a bit long, but it showcases most things described above plus a nice trick you can use this for. The shpere and random modes are showcase [here](https://youtu.be/iUyGJdmje8U).

# Soft Replace
Soft replace is a small utility app oriented at creative mode decoration (just like the last one!). To use it, put [soft_replace.sc](https://github.com/Firigion/scarpets/blob/master/soft_replace.sc) into your `/scripts` folder in your world save and run `/script load soft_replace`. This app will help you replace blocks just like the vanilla replace command, but keeping their block properties. This means that if you made a complex structure out of stair blocks and decide that you want to try using birch instead of diorite, running soft replace will replace all diorite stairs by birch stairs, preserving their orientation.

To do so, just hold the block you want to replace (diorite stairs, in this case) in your offhand and the block you want to replace _with_ (birch stairs) in your main hand. Then, to select the area you want to affect, grab a **stone sword** and right and left click to select the corners of a rectangle defining the volume. Finally, run `/soft_replace region` to execute the operation.

If you want to, say, replace only the top stairs with birch ones, but the bottom ones with sandstone, you can use `/soft_repalce region_filt <property> <value>`, where in this case you'd replace `<property>` with `half` and `<property>` with `bottom`. This way you filter out only the blocks that have that property set to that value.

Here's a [video](https://www.youtube.com/watch?v=_iWv2vvnj8o) to showcase the app in action. I recorded it during development, so don't pay attention at how the commands are called or the fact that I use a golden sword instead of a stone one.

### Hollowify
I added an extra bit to let you hollow you creations, because I don't know how to keep myself from bundling a bunch of stuff in the same app. `hollowify` will replace the interior of a structure within the selected region with air, where interior is defined as "anything not touching air". `hollowify_replace` will do the same, but replace it with the block in the main hand, instead of air. If you also put a block in your off hand, only the blocks matching that will be counted as "interior" blocks and get replaced.

If the description is too confusing, watch [this video](https://youtu.be/pdL9AKlZypc) showcasing how it works. Probably the worst showcase in this whole repo.

# Nether portal POI display
Super simple app that you load using `/script load poi_show_md global`. For once, the app is fully compatible with multiplayer (and with multiple players using it at the same time, in particular, which is the important part). To use it, just hold an eye of ender in your main or offhand. A marker will appear in every nether portal POI around you in a 40 block radius by default. You can change that value using `/poi_show_md set_range <range>`. Please be sensible about the settings, because I didn't add any checks when setting new values.

The markers will update in real time, which means the game is constantly checking for new POIs popping up or old POIs disappearing. By default, the check is done every 5 game ticks. If this is too often for what your system can handle, use `/poi_show_md set_refresh_rate <value>` to something higher than 5. Again, be sensible with values. If you use some non integer value, it will never update. I think. haven't tried.

The app also adds two simple commands to place and remove nether portal POIs from the world, without affecting the nether portal block: just look at the block the POI is in and run `/poi_show_md portalles_poi` to set one and `/poi_show_md remove_poi` to remove it. Sidenote: `remove_poi` will remove _any_ POI, not only nether portals. I forgot to check for the type, upsi.

For once, the [video](https://www.youtube.com/watch?v=Q6GULuQjgxQ&t=1s) is fully up to date with the features.

# Spawning spheres
[NOTE: this is deprecated in favour of the built-in overlay app.]

To use this app, put the .sc file int your scripts folder and run `/script load spawning_spheres`. It adds an option to render spawning sphere around a position with one click. Right click with a wool block and a double sphere of the corresponding colour will appear. Inner sphere shows minimum distance to the player for mobs to spawn. Outer ones shows where mobs instantly despawn. Right click again to move the sphere, or click with another colour to create a second double sphere. Right click with a glass block to delete all renders.

If for some reason you need a video to see this in action, [here](https://youtu.be/5V6gqe8OHm4) it is.

# Stack potion effects

Pretty much what it ways on the tin: when you drink a potion, instead of overriding the duration you had left for that same effect, it adds the potion length to the old one. To use it, put [stack_potion_effects.sc](https://github.com/Firigion/scarpets/blob/master/stack_potion_effects.sc) into your `/scripts` folder inside your world save. You might want to have this app automatically load when you launch your world or server. To do that, follow [these](https://github.com/gnembon/fabric-carpet/wiki/Installing-carpet-scripts-in-your-world#keeping-scripts-loaded) instructions.


# Curves
The curves app is a creative mode-oriented script that includes a few commands to generate 3D curves of different types. To use it, put [curves.sc](https://github.com/Firigion/scarpets/blob/master/curves.sc) into your `/scripts` folder in your world save and run `/script load curves`. All commands in this app should be available with `/curves <command>`. A playlist with all the videos relevant to this app can be found [here](https://www.youtube.com/playlist?list=PL8lLKEt66RqslmFunrXESEZeu9HS6LJnd).

The app includes four [shapes or curve types][1], has a [settings UI][2], a [help command][3], a way to make [selections][4] in-world and [undo][5] your actions. It also includes the [soft replace app][7]. 

### Help

This is pretty self-explanatory: run `curves help` and you will be presented with a clickable menu that lets you browse the information in this article in a more condensed way. There are also some category-specific help commands, which you can run by hand if you like, or just click your way to them. They are all called `help_<category_name>`, you can tab autocomplete them.

### Settings

This one is also simple: run `curves settings` and you will be presented with a clickable menu that lets you set all the settings in the curves app and shows the current values. When clicking a setting, a new command will be suggested. You can also type the setting commands by hand, they are called either `toggle_<setting>` or `set_<setting>`. All the settings for each aspect of the app will be listed under the description of that category, so go to [undo][5] to see the undo related settings, for example. The usage of settings is showcased [here](https://www.youtube.com/watch?v=Im9Do0zVg8w), but I think it's pretty self-explanatory.

### Selections and position setting

While most drawing commands in this app work fine placing a block along a curve, you will find it's also very useful to create a template that gets pasted along the curve, allowing for more complex and interesting shapes. A template is defined by a cuboid area selection determined by its two corners. To define those corners, grab a golden sword and right and left click on a block or in mid air. A faint white box will appear once the selection is complete. Bear in mind that the templates are dimension specific, so if you try to make a selection in the overworld and go to the end to paste it, it will not work. 

You will also find all shapes have a center or origin. That block is usually the players feet, but if you would like to have a repeatable center position, or be able to watch the shape from afar when creating it, you can set your third position using shift and right click. All shapes will use that one as origin.

You can erase all positions with `reset_pos` and query them with `get_pos` and, if for some reason you want to, you can set them via commands instead of with the golden sword. For that, just run `curves set_pos <i>`, where `<i>` should be either 1, 2 or 3, depending on the position you want to set. You can also toggle rendering of positions and selection on and off with the setting `toggle_show_pos`. If you need to see a video of this working for some reason, [here](https://youtu.be/o8xU7nN55jI) it is.

### Undo

All actions done with this app are saved into history. You can at any time undo any or all of them. To do so, you have two commands available: `undo <n>` and `go_back_stories <n>`, both of which take a number as a parameter. The first one will undo as many actions as requested, so `curves undo 4` will undo your last four actions. The second one is a bit stranger: `curves go_back_stories <n>` will skip your last 3 actions and undo only the thing you did 4 actions ago. Bear in mind, this might have some odd behaviours, because all the `undo` functions do is paste back whatever the action replaced. So if you make a spiral, then replace some of it with another spiral and then undo only the first one, it will put back in place whatever *it* replaced, cutting through the new spiral.

There is one setting pertinent to the `undo` functions, that set how many actions to save into history. It defaults to 100, but you can set it to whatever you like with `set_undo_history_size <size>`. You can see this features being used in [this bad showcase video](https://www.youtube.com/watch?v=qeGa9eZy8PA) using spirals.

### Brush

There is also an obscure and somewhat useless feature: if you click with a blaze rod, you will paste your template wherever you are looking, as far as the horizon reaches. Or 200 blocks, whatever comes first.

### Soft replace

This is just a port of the [replace app][6] (which replaces one block with another, keeping the block properties) to the curves app, because it can be useful to edit the template and the selection method is the same. The only difference with how you operate it is that the commands are called `soft_replace`, which replaces the block in the off hand with the one in the main hand, and `soft_replace_filt <property> <value>`, which does the same, but only for blocks with a specific property-value pair.

### Curves

All the curves have about the same signature: `/curves <curve_name> <param1> <param2> ... <paramN> <material>`. Each curve type requires a different number of parameters and all take a material as last input. The material can either be any Minecraft block name, or `'template'`. Some curve types also have a few variations, each one having its own command. For example, doing an anti multi spiral with radius 20, pitch 11, size 80 and 4 turns out of prismarine would look like 
```/curves antimultispiral 20 11 80 4 prismarine``` 

If at some point you don't know what parameters each shape needs, just run it without parameters and the error message will include the expected signature. There are a number of settings available for all shapes to share:

* `paste_with_air`: when pasting a template, this setting decides if said template includes or excludes air. Default: `false`.
* `replace_block`: if enabled, generated curves will only be placed replacing the block you have in the off hand. For stuff you can't easily get in item form, there are a few aliases: hold a feather to replace air, an eye of ender to replace end portals, a flint and steel to replace nether portals and buckets for the corresponding liquids. Default: `false`.
* `max_template_size`: a safeguard to avoid using templates that would kill your game, because you misclicked the position selector. This limit counts the actual template volume if `paste_with_air` is `true`, or only the non air blocks, if it's `false`, meaning you can still paste a very large very sparse template. Default is a rather conservative value; you can increase it a bunch if you trust your computer's power. Default: `100`.
* `max_operations_per_tick`: limits the number of operations per tick, as it says in the name. This usually is not needed, but for some slower systems or shapes that have an inefficient algorithm, not having this can be inconvenient. Default: 10000

Other than those, all shapes have a "direction" in which they generate. For all circle-based curves (that's all of them, except for waves), `circle_axis` defines the axis perpendicular to the circle in question and defaults to `y`, and for waves, `wave_axis` defines the direction along which and into which it generates, defaults into `yx`.

#### Spirals

Spirals are defined by three parameters: 

* `radius`:	the circle radius,
* `pitch`: the rate at which it grows. With the `slope_mode` setting you can make this setting be interpreted as slope, rather than pitch, the former being how many blocks per step the spiral advances, and the latter how many blocks per full revolution. Default values is `false`, meaning pitch mode.
* `size`: the total size (if `circle_axis` is `y`, this would be total height, for example)

`spirals` by default spin counter-clockwise; to get a clockwise spiral, use `antispiral`. Both this commands have a variation in `multispiral` and `antimultispiral`, which create multiple spirals uniformly distributed along the circle. Both variations take a fourth parameter before material, it being how many spirals to generate. For example, to generate three concentric anticlockwise spirals out of the template with radius 30, pitch 11 and total 100 blocks, each 120 degrees away from the next:
```
/curves multispiral 30 11 100 3 template
```
I made some videos showcasing spirals. Since it was the first curve I worked on, spiral development is entangled with the development of other features: [this video](https://youtu.be/WL9Pl3eaFaU) shows  all the functions, except multi spiral, which you can see in this [other video](https://youtu.be/sMGKnUiST6E).

#### Waves

Waves are defined by three parameters:

* `wavelength`: how many blocks does it take the wave to complete a full oscillation.
* `amplitude`: how far from the baseline does one oscillation reach. 
* `size`: how many blocks does the wave cover in total.

You can think of waves as going into one direction and oscillating into another, both of which are defined in the `wave_axis` setting. For example, a wave going into (positive) `x` oscillating up and down would have the default value of `xy`. Note: to have waves be symmetrical about the max and min point of each oscillation, the wavelength is actually rounded to the nearest multiple of four. So when setting a wavelength, do know the actual value will be plus or minus two.

You can find the video briefly showcasing this right [here](https://youtu.be/9qSo_WL8Rok).

#### Circular waves (cwaves)

Please note the algorithm to generate these is not as refined as the ones for the other curves, so it's quite a bit heavier to execute. It creates a wave along a circle (duh) with the wave plane being coplanar with the circle (planar waves) or perpendicular to the circle (transverse waves). You have the option to make a full circle or just a partial one, which means you have a total four functions: `cwaves_planar`, `cwaves_transverse`, `cwaves_planar_partial` and `cwaves_transverse_partial`. They have the following arguments:

* `radius`: the radius of the circle
* `amplitude`: the amplitude of the wave
* `cycles`: amount of cycles the wave does in one full turn of the circle. Whole numbers will mean the wave meets itself at the end, resulting in a closed curve. You can play around with non whole numbers for more interesting results.
* `material`: material out of which the curve is made. Use `template` to create the wave using the selection.

`partial` functions also have the `from` and `to` arguments, in degrees. Note you can make more than one full revolution. Having fractional `cycle`s and multiple turns makes for some interesting patterns. You can also use `from` as a phase value to rotate the shape by some amount. Just make sure to set `to` to `from + 360` to have the whole shape be rotated, like so:
```
/curves cwaves_transverse_partial 40 8 10 45 405 white_stained_glass 
```

As usual, the bad video showcasing these things (plus a few tricks) can be found [here](https://youtu.be/gP_p0lJbOjc).

#### Stars

It defines a star by taking N points on an outer circle, N points on an inner circle, and connecting them with straight lines. The parameters are:

* `outer_radius`: radius of the outer circle.
* `inner_radius`: radius of the inner circle.
* `n_points`: the number of points in the star.
* `phase`: a phase or rotation, so that you can point your stars in any direction you want.
* `material`: material out of which the curve is made. Use `template` to create the star using the selection.

Because the star is built by connecting points, if you set inner and outer radius to the same value, you will get a polygon of 2N sides. Also, 2 and 3 pointed stars are not a thing, but they make good rhombi and triangle/irregular hexagon tools.

Here's a [video](https://www.youtube.com/watch?v=XT4XJngGZRM) showing this functionality and some tricks.

#### Polygons

It defines a polygon by taking N points on a circle and connecting them with straight lines. The parameters are:

* `radius`: radius of the outer circle.
* `n_points`: the number of points in the star.
* `rotation`: a phase or rotation, so that you can point your polygons in any direction you want.
* `material`: material out of which the curve is made. Use `template` to create the star using the selection.

Did I shamelessly copy this description from the stars one? yes. But they are basically the same. You can make odd-sided (regular) polygons with this.

Here's a [video](https://youtu.be/HCePbkaB8Vk) showing this functionality.


[1]: https://github.com/Firigion/scarpets#curves-1
[2]: https://github.com/Firigion/scarpets#selections-and-position-setting
[3]: https://github.com/Firigion/scarpets#help
[4]: https://github.com/Firigion/scarpets#selection
[5]: https://github.com/Firigion/scarpets#undo
[6]: https://github.com/Firigion/scarpets#replace
[7]: https://github.com/Firigion/scarpets#soft_replace

# Storage tech aid

This app (get it [here](https://raw.githubusercontent.com/Firigion/scarpets/master/storagetech_aid.sc)) is a bundle of crude features designed to help you in different aspects of storage tech designig. It has a few features ranging from reading signal strength level from an inventory and setting it's contents to some level, to generating a row of chests or hoppers so that you don't have to pre fill them by hand yourself.

### Magic hoppers and chests

Upon loading up the app (`/script load storagetech_aid`), you will recieve three chests and two hoppers. If you hover over them they will tell you what each does, but for clarity, here's a brakedown:
* `Double chests full of boxes` will place a row of double chests in the direction you are looking full of shulker boxes full of items. Each chest will contain a uinque item type.
* `Double chests full of items` will place a row of double chests in the direction you are looking full of stacks of items. Each chest will contain a uinque item type.
* `Hopper full of single item type` will place a row of hoppers in the direction you are looking and conserving the orientation of the hopper you place. The hoppers will be full of one item type each, useful for doublespeed sorters.
* `Signal strength defined sorter` will place a row of hoppers in the direction you are looking and conserving the orientation of the hopper you place. The hoppers will have the settings required for an item filter: the item to filter in the first slot and the the rest of the slots with dummy (blocker) items. You can set the ammount of items for the first slot using `/storagetech_aid set_fst_slot_fill_level <number>` and set the signal srength of the filter with `/storagetech_aid set_hopper_ss <number>`. That means, the hopper will have enough items such that, when it recieves a new item, the signal strength will raise to the requested value. Default values are 41 items and ss3, what's needed for a standard overflow proof item sorter. If the signal strength value and first slot stack size you specify are not compatible, all dummy items stacks will be set to one and the first slot value will be the default.
* `Double chests configured for hex encoders` will place a row of double chests in the direction you are looking. Each chest will me set up for a traditional item encoder system, where if you take out one item from it, the signal strength value reading from it will decrease by one. The ss value can be set with `/storagetech_aid set_chest_ss <number>`. The items are generated from files, see [below](https://github.com/Firigion/scarpets#encoded_chest_files).

### Items lists and files

The default magic chests and hoppers will use a hardcoded list of items, which you can find in the first line of the code. Feel free to replace it with your own list to suit your needs. The list will skip any misspelled items and is not caps sensitive (placing a magic hopper or chest will tell you which entries are being skipped).

If you don't feel like modifying the code, or think that you need to constantly modify your list or have many lists at hand, you can use the file-based magic hoppers and chests. To get them, run `/storagetech_aid <type> <file>`, where `<type>` can be:
* `chest_from_file` to get the chest full of items
* `chest_shulkers_from_file` to get the chest full of shulkers full of items
* `hopper_full_from_file` to get the hopper full of items
* `hopper_from_file` to get the hopper filter maker

The files in question have to be located in a folder named `item_lists` which in turn has to be inside a folder named `storagetech_aid.data`. This folder needs to be in the same directory the app `storagetech_aid.sc` is. The files can be in either `JSON` or plain `text` format. For `JSON` files the format is as follows:
```json
[
"stone",
"dirt",
"grass_block",
"cobblestone"
]
```
while for `text` files, they should look like this:
```
stone
dirt
grass_block
cobblestone
```

Depending on how you generate these files, you might prefer one format or the other. The app will only list files of one type at a time. To decide which type to show, use `/storagetech_aid set_safe_mode [text|json]`, defaults to text.

### Encoded chest files

Files to define the encoded chests need to be inside a folder named `encoders`, again,  inside `storagetech_aid.data`. Formatting and file types works the same as for item lists. The files will be loaded "in order", so I recommend naming them numbers of increasing order or something like that (`1.json`, `2.json`, etc). Each file will correspond to one chest, and the contents of the chest will be ordered in the same way the entries in the file are. If there are not enough entries to fill the double chest, empty slots will be padded with dummy items. If there are too many, only the first 54 entries will be used. 

### Signal strength

The app also has a functionality to return the signal strength of a cointainer. This is especially useful for overloaded containers (containers with stacked unstackables, which usually results in a comparator reading signal strength higher than 15), since a redstone dust powered by a comparator reading from this inventory will be at ss15.

To use it, just look at the inventory in question and run `/storagetech_aid ss` or `/storagetech_aid ss <pos>` to specify a position other than the one you are looking at.

To accompany this, you cna also set the contents of an inventory to some specific signal strength level. Again, this is intended for overloaded inventories, so by default it will stack unstackable items until it reaches the needed value. If it can't, it will use stackable items to fine tune the result (for instance, adding an unstackable item to a hopper will increase it's ss in more than one, so stackables are needed).

To use it, look at the inventory in question and run `/storagetech_aid fill_ss <signal_strength>` or `/storagetech_aid fill_ss <signal_strength> <pos>` to set some inventory other than the one you are looking at.

### Others

There are some other odds and ends in this app:

#### Safe mode
The app by default will replace any block in its way when placing hoppers and chests. Thurning on safe mode (with `/storagetech_aid set_safe_mode true`) will make it so that only air blocks are replaced.

#### Chest of stacked shulkers
Just that: get a chest full of stacked shulker boxes, so you don't have to stack them yourself. Run `/storagetech_aid stacked_shulkers_chest` to set the chest at your feet.

#### Some funcy barrels
At some point someone needed a bunch of items named with names going from 1 to some given number, so I added a thing to create a row of barrels in the direction you are looking at willed with stacks of buttons named `1`, `2` and so on until the number specified by `/storagetech_aid bin_barrel <number>`.

#### Video!

Like always, I made a (not so shor this time) [video showcase](https://youtu.be/2PZjUQCN4_k). I think this one is especially bad when compared to the rest of them.
