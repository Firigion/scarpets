__command() -> null;

global_positions = l(null, null);
global_all_set = false;
global_armor_stands = l(null, null);
global_show_pos = true;


spiral(r, h, s, material) -> (
	num = 2 * pi * r ;
	t = l(range( (num+1) * s/h ) )/num;
	pos = pos(player());
	x(t, outer(r)) -> r * cos(360 * t);
	z(t, outer(r)) -> r * sin(360 * t);
	for(t,
		b = pos + l(x(_), h *_ , z(_));
		set(b, material);
	);
);


antispiral(r, h, s, material) -> (
	num = 2 * pi * r ;
	t = l(range( (num+1) * s/h ) )/num;
	pos = pos(player());
	x(t, outer(r)) -> r * cos(-360 * t);
	z(t, outer(r)) -> r * sin(-360 * t);
	for(t,
		b = pos + l(x(_), h *_ , z(_));
		set(b, material);
	);
);


multi_spiral(r, h, s, m, material) -> (
	num = 2 * pi * r ;
	t = l(range( (num+1) * s/h ) )/num;
	pos = pos(player());
	x(t, i, outer(r), outer(m)) -> r * cos(360 * (t + i/m));
	z(t, i, outer(r), outer(m)) -> r * sin(360 * (t + i/m));
	for(range(m),
		i = _;
		for(t,
			b = pos + l(x(_, i), h *_ , z(_, i));
			set(b, material);
		);
	);
);



spiral_template(r, h, s) -> (
	num = 2 * pi * r ;
	t = l(range( (num+1) * s/h ) )/num;
	pos = pos(player());
	x(t, outer(r)) -> r * cos(360 * t);
	z(t, outer(r)) -> r * sin(360 * t);
	offset = map(global_positions:0 - global_positions:1, abs(_));
	for(t,
		b = pos + l(x(_), h *_ , z(_));
		__clone_template(b - offset);
	);
);


antispiral_template(r, h, s) -> (
	num = 2 * pi * r ;
	t = l(range( (num+1) * s/h ) )/num;
	pos = pos(player());
	x(t, outer(r)) -> r * cos(-360 * t);
	z(t, outer(r)) -> r * sin(-360 * t);
	offset = map(global_positions:0 - global_positions:1, abs(_));
	for(t,
		b = pos + l(x(_), h *_ , z(_));
		__clone_template(b - offset);
	);
);


multi_spiral_template(r, h, s, m, material) -> (
	num = 2 * pi * r ;
	t = l(range( (num+1) * s/h ) )/num;
	pos = pos(player());
	x(t, i, outer(r), outer(m)) -> r * cos(360 * (t + i/m));
	z(t, i, outer(r), outer(m)) -> r * sin(360 * (t + i/m));
	offset = map(global_positions:0 - global_positions:1, abs(_));
	for(range(m),
		i = _;
		for(t,
			b = pos + l(x(_, i), h *_ , z(_, i));
			__clone_template(b - offset);
		);
	);
);


__clone_template(pos) -> (
	run( str('clone %d %d %d %d %d %d %d %d %d masked', 
		global_positions:0:0, global_positions:0:1, global_positions:0:2, 
		global_positions:1:0, global_positions:1:1, global_positions:1:2, 
		round(pos:0), round(pos:1), round(pos:2)
	) );
);


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


toggle_show_pos() ->(
	global_show_pos = !global_show_pos;
	if(global_show_pos,
		(
			for(global_positions, 
				if(_!=null, __mark( (_i+1) , _) );
			);
			print('Positions shown');
		),
		(
			for(global_armor_stands,
				e = entity_id(_);
				if(e != null, modify(e, 'remove'));
			);
			print('Positions hidden');
		);
	);
);

__on_player_clicks_block(player, block, face) -> (
	if(query(player(), 'holds'):0 == 'golden_sword',
		set_pos(1);
	);
);

__on_player_uses_item(player, item_tuple, hand) -> (
	if(query(player(), 'holds'):0 == 'golden_sword',
		set_pos(2);
	);
);

__on_tick() -> (
	in_dimension(player(),
		if(global_all_set && global_show_pos && tick_time()%20 == 0, 
			min_pos = map(range(3), min(global_positions:0:_, global_positions:1:_));
			max_pos = map(range(3), max(global_positions:0:_, global_positions:1:_));
			particle_rect('end_rod', min_pos, max_pos + l(1, 1, 1))
		);
	);
);
