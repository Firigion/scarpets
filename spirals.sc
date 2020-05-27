__command() -> null;

// to store marker postiions and object handles
global_positions = l(null, null, null);
global_all_set = false;
global_armor_stands = l(null, null, null);
global_show_pos = true;


__rotated90(list_to_rotate) -> ( //rotates 90 degrees
	map(list_to_rotate, l(_:1, -_:0))
);

extend(list, extension) -> (
	len = length(list);
	for(extension, list:(len+_i) = _);
	return(list)
);

__make_circle(radius) -> (
	z_function(x, outer(radius)) -> round(sqrt(radius * radius - x*x));
	range_val = radius * cos(45); //this spans a quarter circle
	x_range = range(-range_val, range_val);
	
	quarter1 = map(x_range, l(_, z_function(_)) ); // starts with quarter circle
	half = extend(quarter1, __rotated90(quarter1)); // add a quarter by rotating it 90 degrees
	extend(half,__rotated90(__rotated90(half))); // rotate the half circle 180 degrees and add it
);

// mainly for debug porpuses
_circle(radius, material) -> (
	circ = __make_circle(radius);
	c = pos(player());
	for(circ, 
		set(c + l(_:0, 0, _:1), material); 
		create_marker(str(_i), c + l(_:0, 0, _:1))
	);	
);

__get_center() -> (
	if(global_positions:2 == null,
		pos(player()),
		global_positions:2
	)
);


////// Material spirals ///////

//main funtion todraw spiral from material
__draw_spiral(circle, center, pitch, height, material) -> (
	l(cx, cy, cz) = center; // center coordiantes
	perimeter = length(circle); // ammount of blocks in one revolution
	
	loop(floor(height/pitch), //loop over the total ammount of spirals
		turn = _; // what turn am I drawng now (spirals ahve many turns)
		for(circle, 
			this_height =  _i * pitch/perimeter + turn * pitch;
			set(cx + _:0, cy + this_height , cz + _:1 , material) 
		)
	);
	// now draw the last bit that is under a full circle
	for(slice(circle, 0, floor( (height/pitch)%1 * perimeter) ),
		this_height =  _i * pitch/perimeter + floor(height/pitch) * pitch;
		set(cx + _:0, cy + this_height , cz + _:1 , material) 
	);
);

spiral(radius, pitch, height, material) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	__draw_spiral(circle, center, pitch, height, material);
);

antispiral(radius, pitch, height, material) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	circle = map(range(length(circle)-1, -1, -1), circle:_); // to spin the other way around
	__draw_spiral(circle, center, pitch, height, material);

);

multispiral(radius, pitch, height, ammount, material) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	perimeter = length(circle); // ammount of blocks in one revolution
	loop(ammount,
		jump = floor(_ * perimeter/ammount); // by how many places to advance to get to the next circle
		this_circ = extend(slice(circle, jump), slice(circle, 0, jump) ); // redefine the circle last for this iteration
		__draw_spiral(this_circ, center, pitch, height, material);
	);
);


////// Template spirals ///////

// saves selected area, minus air
__make_template() -> ( 
	global_template = l();
	origin = map(range(3), min(global_positions:0:_, global_positions:1:_)); //negative-most corner in all dimensions
	volume(
		global_positions:0:0, global_positions:0:1, global_positions:0:2,
		global_positions:1:0, global_positions:1:1, global_positions:1:2,
		if(!air(_), global_template:length(global_template) = l(pos(_)-origin, _) ) //save non-air blocks and positions
		);
);

// clone template at given position
__clone_template(pos) -> (
	for(global_template, set(pos + _:0, _:1) );
);

// main function to draw spirals from template (selected area)
__draw_spiral_from_template(circle, center, pitch, height) -> (
	perimeter = length(circle); // ammount of blocks in one revolution
	__make_template();
	
	loop(floor(height/pitch), //loop over the total ammount of spirals
		turn = _; // what turn am I drawng now (spirals ahve many turns)
		for(circle, 
			this_height =  _i * pitch/perimeter + turn * pitch;
			__clone_template(center + l(_:0, this_height ,  _:1)) 
		)
	);
	// now draw the last bit that is under a full circle
	for(slice(circle, 0, floor( (height/pitch)%1 * perimeter) ),
		this_height =  _i * pitch/perimeter + floor(height/pitch) * pitch;
		__clone_template(center + l(_:0, this_height ,  _:1)) 
	);
);

spiral_template(radius, pitch, height) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	__draw_spiral_from_template(circle, center, pitch, height);
);

antispiral_template(radius, pitch, height) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	circle = map(range(length(circle)-1, -1, -1), circle:_); // to spin the other way around
	__draw_spiral_from_template(circle, center, pitch, height);
);

multispiral_template(radius, pitch, height, ammount) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	perimeter = length(circle); // ammount of blocks in one revolution
	loop(ammount,
		jump = floor(_ * perimeter/ammount); // by how many places to advance to get to the next circle
		this_circ = extend(slice(circle, jump), slice(circle, 0, jump) );
		__draw_spiral_from_template(this_circ, center, pitch, height);
	);
);

////// Handle Markers //////

// Spawn a marker
__mark(i, position) -> (
 	colours = l('red', 'lime', 'light_blue'); 
	e = create_marker('pos' + i, position + l(0.5, 0.5, 0.5), colours:(i-1) + '_concrete'); // crete the marker
	run(str( //modify some stuff to make it fancier
		'data merge entity %s {Glowing:1b, Fire:32767s, Marker:1b}', query(e, 'uuid') 
		));
	put(global_armor_stands, i-1, query(e, 'id')); //save the id for future use
);

// set a position
set_pos(i) -> (
	try( // position index must be 1 or 28done in this convoluted way because it's recycled code)
 		if( !reduce(range(1,4), _a + (_==i), 0),
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
	if(all(slice(global_positions, 0, 2), _!=null), global_all_set = true); 
	
	print(str('Set your position %d to ',i) + tha_pos);

	if(global_show_pos, // remove previous marker for set positi, if aplicable
		e = entity_id(global_armor_stands:(i-1));
 		if(e != null, modify(e, 'remove'); print('removing'));
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
		if(query(player(), 'sneaking'),
			set_pos(3),
			set_pos(2)
		);
	);
);

// display particle cube once per second to select marked volume
__on_tick() -> (
	in_dimension(player(),
		if(global_all_set && global_show_pos && tick_time()%20 == 0, 
			min_pos = map(range(3), min(global_positions:0:_, global_positions:1:_));
			max_pos = map(range(3), max(global_positions:0:_, global_positions:1:_));
			particle_rect('end_rod', min_pos, max_pos + l(1, 1, 1))
		);
	);
);
