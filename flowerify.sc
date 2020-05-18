__command() -> print('nothing');

__clear_grass(loc) -> (
	scan(
		loc, l(3, 2, 3), if(
			block(_)=='grass' || block(_)=='tall_grass', 
			set(_, 'air')
		) 
	);
);

__bonemeal(loc) -> (
	place_item('bone_meal', loc)
);

__one_pass(center, range) -> (
	scan(center,
		range, 
		if(_=='grass_block',
			__bonemeal(_);
			__clear_grass(_);
		)
	);
);

__flowerify(center, range, num_iterations) ->(
	loop(num_iterations, __one_pass(center, range))
);

__under_player_flowers() -> (
	pp = pos(player()); 
	i = first(range(pp:1), block(pp:0, (pp:1) - _, pp:2) =='grass_block');
	if(i != null,
		__one_pass(pos_offset(pp, 'down', i), l(5, 2, 5));
	);
);

global_flowerify = false;

toggle_flowerify_continuous() -> (
	global_flowerify = !global_flowerify; 
	if(global_flowerify, 
		print('Changed from off to on'),
		print('Changed from on to off') 
	);
);

__on_tick() -> (
	if(
		global_flowerify, __under_player_flowers() 
	);
);

global_positions = l(null, null);
global_all_set = false;
global_armor_stands = l(null, null);
global_show_pos = true;


__mark(i, position) -> (
 	colours = l('red', 'lime'); 
	e = create_marker('pos' + i, position + l(0.5, 0.5, 0.5), colours:(i-1) + '_concrete');
	run(str(
		'data merge entity %s {Glowing:1b, Fire:32767s, Marker:1b}', query(e, 'uuid')
		));
	put(global_armor_stands, i-1, query(e, 'id'));
);


set_pos(i) -> (
	try(
 		if( !reduce(range(1,3), _a + (_==i), 0),
			throw();
		),
		print('Input must be either 1 or 2 for position to set. You input ' + i);
		return()
	);

	tha_block = query(player(), 'trace');
	if(tha_block!=null,
		tha_pos = pos(tha_block),
		tha_pos = map(pos(player()), round(_))
	);
	global_positions:(i-1) = tha_pos;
	if(all(global_positions, _!=null), global_all_set = true);

	print(str('Set your position %d to ',i) + tha_pos);

	if(global_show_pos,
		e = entity_id(global_armor_stands:(i-1));
 		if(e != null, modify(e, 'remove'));
		__mark(i, tha_pos);
	);

);


get_pos() -> (
	for(global_positions, 
 		print(str('Position %d is %s', 
				_i+1, if(_==null, 'not set', _)));
 	)
);


show_pos(b) ->(
	if(b != global_show_pos,
		if(b,
			for(global_positions, 
				if(_!=null, __mark( (_i+1) , _) ) 
			),
			for(global_armor_stands,
				e = entity_id(_);
				if(e != null, modify(e, 'remove'));
			);
		);
	);
	global_show_pos = b;
);


flowerify_area(num_iterations) ->(
	center =  (global_positions:0 + global_positions:1) / 2 ;
	range = center - global_positions:0 ; 
	loop(num_iterations, one_pass(center, range))
);