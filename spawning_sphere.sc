global_colours = m(
					l( 'white_wool', 0xe4e4e420 ),
					l( 'orange_wool', 0xea7e3520 ),
					l( 'magenta_wool', 0xbe49c920 ),
					l( 'light_blue_wool', 0x6387d220 ),
					l( 'yellow_wool', 0xc2b51c20 ),
					l( 'lime_wool', 0x39ba2e20 ),
					l( 'pink_wool', 0xd9819920 ),
					l( 'gray_wool', 0x41414120 ),
					l( 'light_gray_wool', 0xa0a7a720 ),
					l( 'cyan_wool', 0x26719120 ),
					l( 'purple_wool', 0x7e34bf20 ),
					l( 'blue_wool', 0x25319320 ),
					l( 'brown_wool', 0x56331c20 ),
					l( 'green_wool', 0x364b1820 ),
					l( 'red_wool', 0x9e2b2720 ),
					l( 'black_wool', 0x18141420 ),
				);

global_do_render = m();
map( keys(global_colours), global_do_render:_ = false);
global_markers = m();
map( keys(global_colours), global_markers:_ = l(null, null) );

__on_player_uses_item(player, item_tuple, hand) -> (

	//if it's a wool item, create sphere
	if( has(global_colours, item_tuple:0),
		
		colour_wool = item_tuple:0;

		__delete_sphere(colour_wool);
		global_do_render:colour_wool = true;
		__mark_spot(pos(player), colour_wool);
		__render_shedule(colour_wool),
		
	// else, if glass, delete all
	item_tuple:0 == 'glass',
		for( pairs(global_do_render),
			if( _:1,
				__delete_sphere(_:0);
				global_do_render:(_:0) = false;
			);
		);
	);
);

__mark_spot(pos, colour_wool) -> (
	e = create_marker('', pos, colour_wool);
	global_markers:colour_wool = l(e, pos);
);

__render_shedule(colour) -> (
	if(global_do_render:colour, 
		__render_sphere(global_markers:colour:1, global_colours:colour);
		schedule(5, '__render_shedule', colour);
	);
);

__render_sphere(center, colour) -> (
	draw_shape('sphere', 8, 'color', colour , 'fill', colour, 'center', center, 'radius', 24); // inner spawning sphere
	draw_shape('sphere', 8, 'color', colour , 'center', center, 'radius', 24); // wandering radius
	draw_shape('sphere', 8, 'color', colour , 'fill', colour, 'center', center, 'radius', 128); // outer spawning sphere
);

__delete_sphere(colour) -> (
	if(global_do_render:colour,
		modify(global_markers:colour:0, 'remove');
	);
);