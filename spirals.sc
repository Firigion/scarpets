__command() -> null;

// to store marker postiions and object handles
global_positions = l(null, null);
global_all_set = false;
global_armor_stands = l(null, null);
global_show_pos = true;

// make a spiral paremetricaly
spiral(r, h, s, material) -> (
	num = 2 * pi * r ; // ammount of blocks per revolution
	t = l(range( (num+1) * s/h ) )/num; // the aprameter running the spiral generation
	pos = pos(player()); //center position
	x(t, outer(r)) -> r * cos(360 * t); //define circles
	z(t, outer(r)) -> r * sin(360 * t); //define circles
	for(t,
		b = pos + l(x(_), h *_ , z(_)); //figure out positions where spiral should be
		set(b, material);
	);
);

// exactly the same, but spiral runs the other way
antispiral(r, h, s, material) -> (
	num = 2 * pi * r ;
	t = l(range( (num+1) * s/h ) )/num;
	pos = pos(player());
	x(t, outer(r)) -> r * cos(-360 * t); //the minus makes it run the other way
	z(t, outer(r)) -> r * sin(-360 * t); //the minus makes it run the other way
	for(t,
		b = pos + l(x(_), h *_ , z(_));
		set(b, material);
	);
);

// again, the same, but it will repeat m times, symmetricaly offset.
multi_spiral(r, h, s, m, material) -> (
	num = 2 * pi * r ;
	t = l(range( (num+1) * s/h ) )/num;
	pos = pos(player());
	x(t, i, outer(r), outer(m)) -> r * cos(360 * (t + i/m)); // i/m is the offset in degrees
	z(t, i, outer(r), outer(m)) -> r * sin(360 * (t + i/m)); // i/m is the offset in degrees
	for(range(m), //repeat for however many spirals are needed
		i = _;
		for(t,
			b = pos + l(x(_, i), h *_ , z(_, i));
			set(b, material);
		);
	);
);


// spirals, again, but instead of using material, uses template defined by selection
spiral_template(r, h, s) -> (
	num = 2 * pi * r ;
	t = l(range( (num+1) * s/h ) )/num;
	pos = pos(player());
	x(t, outer(r)) -> r * cos(360 * t);
	z(t, outer(r)) -> r * sin(360 * t);
	offset = map(global_positions:0 - global_positions:1, abs(_)); //offsets the selection so that it clones it in the center of the block
	for(t,
		b = pos + l(x(_), h *_ , z(_));
		__clone_template(b - offset); //clones template
	);
);

//same, but going the other way
antispiral_template(r, h, s) -> (
	num = 2 * pi * r ;
	t = l(range( (num+1) * s/h ) )/num;
	pos = pos(player());
	x(t, outer(r)) -> r * cos(-360 * t); //the minus makes it run the other way
	z(t, outer(r)) -> r * sin(-360 * t); //the minus makes it run the other way
	offset = map(global_positions:0 - global_positions:1, abs(_));
	for(t,
		b = pos + l(x(_), h *_ , z(_));
		__clone_template(b - offset);
	);
);

// same, but doing many spirals
multi_spiral_template(r, h, s, m, material) -> (
	num = 2 * pi * r ;
	t = l(range( (num+1) * s/h ) )/num;
	pos = pos(player());
	x(t, i, outer(r), outer(m)) -> r * cos(360 * (t + i/m)); // i/m is the offset in degrees 
	z(t, i, outer(r), outer(m)) -> r * sin(360 * (t + i/m)); // i/m is the offset in degrees
	offset = map(global_positions:0 - global_positions:1, abs(_));
	for(range(m), //repeat for however many spirals are needed
		i = _;
		for(t,
			b = pos + l(x(_, i), h *_ , z(_, i));
			__clone_template(b - offset);
		);
	);
);

// clone template at given position
__clone_template(pos) -> (
	run( str('clone %d %d %d %d %d %d %d %d %d masked', 
		global_positions:0:0, global_positions:0:1, global_positions:0:2, 
		global_positions:1:0, global_positions:1:1, global_positions:1:2, 
		pos:0, pos:1, pos:2
	) );
);

// Spawn a marker
__mark(i, position) -> (
 	colours = l('red', 'lime'); 
	e = create_marker('pos' + i, position + l(0.5, 0.5, 0.5), colours:(i-1) + '_concrete'); // crete the marker
	run(str( //modify some stuff to make it fancier
		'data merge entity %s {Glowing:1b, Fire:32767s, Marker:1b}', query(e, 'uuid') 
		));
	put(global_armor_stands, i-1, query(e, 'id')); //save the id for future use
);

// set a position
set_pos(i) -> (
	try( // position index must be 1 or 28done in this convoluted way because it's recycled code)
 		if( !reduce(range(1,3), _a + (_==i), 0),
			throw();
		),
		print('Input must be either 1 or 2 for position to set. You input ' + i);
		return()
	);
	// position to be set at the block the player is aiming at, or player position, if there is none
	tha_block = query(player(), 'trace');
	if(tha_block!=null,
		tha_pos = pos(tha_block),
		tha_pos = map(pos(player()), round(_))
	);
	global_positions:(i-1) = tha_pos; // save to global positions
	if(all(global_positions, _!=null), global_all_set = true); 
	
	print(str('Set your position %d to ',i) + tha_pos);

	if(global_show_pos, // remove previous marker for set positi, if aplicable
		e = entity_id(global_armor_stands:(i-1));
 		if(e != null, modify(e, 'remove'));
		__mark(i, tha_pos);
	);

);

// print list of positions
get_pos() -> (
	for(global_positions, 
 		print(str('Position %d is %s', 
				_i+1, if(_==null, 'not set', _)));
 	)
);

// toggle markers and bounding box visibility
toggle_show_pos() ->(
	global_show_pos = !global_show_pos; 
	if(global_show_pos,
		( // summon the markers
			for(global_positions, 
				if(_!=null, __mark( (_i+1) , _) );
			);
			print('Positions shown');
		),
		// else
		( //remove the markers
			for(global_armor_stands, 
				e = entity_id(_);
				if(e != null, modify(e, 'remove'));
			);
			print('Positions hidden');
		);
	);
);

// set position 1 if player left clicks with a golden sword
__on_player_clicks_block(player, block, face) -> (
	if(query(player(), 'holds'):0 == 'golden_sword',
		set_pos(1);
	);
);

// set position 2 if player right clicks with a golden sword
__on_player_uses_item(player, item_tuple, hand) -> (
	if(query(player(), 'holds'):0 == 'golden_sword',
		set_pos(2);
	);
);

// display particle cube onve per second to select marked volume
__on_tick() -> (
	in_dimension(player(),
		if(global_all_set && global_show_pos && tick_time()%20 == 0, 
			min_pos = map(range(3), min(global_positions:0:_, global_positions:1:_));
			max_pos = map(range(3), max(global_positions:0:_, global_positions:1:_));
			particle_rect('end_rod', min_pos, max_pos + l(1, 1, 1))
		);
	);
);
