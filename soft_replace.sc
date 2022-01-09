__command() -> null;

global_tool = 'stone_sword';

/////// Replace stuff

__get_states(b) -> (
	properties = keys(block_state(pos(b)));
	pairs = map(properties, l(_, block_state(pos(b), _)) );
);

__make_properies_string(pairs) -> (
	if( pairs,
		props_str = join(',', map(pairs, str('%s="%s"', _:0, _:1)) ),
		props_str = '',
	);
);

__set_with_state(b, replace) -> (
	pairs = __get_states(b);
	properties_string = __make_properies_string(pairs);
	set(b, block(str('%s[%s]', replace, properties_string)) );
);

__replace_one_block(b, to_replace, replace_with) -> (
	if(b == to_replace, 
		__set_with_state(b, replace_with)
	);
);

__replace_one_block_filt(b, to_replace, replace_with, property, value) -> (
	if(b == to_replace && block_state(b, property)==value, 
		__set_with_state(b, replace_with)
	);
);

region() -> (
	p = player();
	dim = p ~ 'dimension';
	to_replace = query(p, 'holds', 'offhand'):0;
	replace_with = query(p, 'holds', 'mainhand'):0;

	if(!global_all_set:dim, __error(p,'Missing positions. Set two positions before replacing.'); return(''));
	
	print(str('Replacing %s with %s', to_replace, replace_with));
	volume(global_positions:dim:0, global_positions:dim:1,
			__replace_one_block(_, to_replace, replace_with)
	);
);

region_filt(property, value) -> (
	p = player();
	dim = p ~ 'dimension';
	to_replace = query(p, 'holds', 'offhand'):0;
	replace_with = query(p, 'holds', 'mainhand'):0;

	if(!global_all_set:dim, __error(p,'Missing positions. Set two positions before replacing.'); return(''));
	
	print(str('Replacing %s with %s', to_replace, replace_with));
	volume(global_positions:dim:0, global_positions:dim:1,
			__replace_one_block_filt(_, to_replace, replace_with, property, value)
	);
);


/////// Hoolowifier

// replaces interior with air
hollowify() -> (
	p = player();
	dim = p ~ 'dimension';

	if(!global_all_set:dim, __error(p,'Missing positions. Set two positions before replacing.'); return(''));

	list_to_fill = [];
	// replace interior with barrier blocks
	volume(global_positions:dim:0, global_positions:dim:1,
		if( all(neighbours(_), !air(_)),list_to_fill:length(list_to_fill) = _) 
	);
	// fill with air
	for(list_to_fill, set(_, 'air') );
);

// replaces interior with given block
hollowify_replace() -> (
	p = player();
	dim = p ~ 'dimension';
	if(!global_all_set:dim, __error(p,'Missing positions. Set two positions before replacing.'); return(''));
	
	to_replace = query(p, 'holds', 'offhand'):0;
	replace_with = query(p, 'holds', 'mainhand'):0;
	if(replace_with==null, __error(p, 'Need a block to replace with in main hand'); return(''));

	list_to_fill = [];
	// replace interior with barrier blocks
	if(to_replace==null, 
		volume(global_positions:dim:0, global_positions:dim:1,
			if( all(neighbours(_), !air(_)),list_to_fill:length(list_to_fill) = _) 
		),
		volume(global_positions:dim:0, global_positions:dim:1,
			if( all(neighbours(_), _==to_replace) && _==to_replace, list_to_fill:length(list_to_fill) = _) 
		)
	);
	// fill with block
	for(list_to_fill, set(_, replace_with) );
);


/////// Markers stuff

__error(player, msg) -> print(player, format('rb Error: ', str('y %s', msg)));


// Spawn a marker
__mark(i, position, dim) -> (
 	colours = l('red', 'lime', 'light_blue'); 
	e = create_marker('pos' + i, position + l(0.5, 0.5, 0.5), colours:(i-1) + '_concrete', false); // crete the marker
	run(str( //modify some stuff to make it fancier
		'data merge entity %s {Glowing:1b, Fire:32767s}', query(e, 'uuid') 
		));
	global_armor_stands:dim:(i-1) =  query(e, 'id'); //save the id for future use
);

__remove_mark(i, dim) -> (
	e = entity_id(global_armor_stands:dim:(i));
 	if(e != null, modify(e, 'remove'));
);

// set a position
__set_pos(i) -> (
	dim = player() ~ 'dimension';

	// position to be set at the block the player is aiming at, or player position, if there is none
	tha_block = query(player(), 'trace');
	if(tha_block!=null,
		tha_pos = pos(tha_block),
		tha_pos = map(pos(player()), round(_))
	);
	global_positions:dim:(i-1) = tha_pos; // save to global positions
	__all_set(dim); 
	
	print(str('Set your position %d in %s to ',i, dim) + tha_pos);

	// remove previous marker for set positi
	__remove_mark(i-1, dim); //-1 because stupid indexes
	__mark(i, tha_pos, dim);
);

// remove all markers
__reset_positions(dim) -> (
	loop(3, 
		__remove_mark(_, dim);
	);
	global_positions:dim = l(null, null, null);
	global_all_set:dim = false;
	global_armor_stands:dim = l(null, null, null);
);

reset_positions() -> (
	dim = player() ~ 'dimension';
	__reset_positions(dim);
);

// set position 1 if player left clicks with a stone sword
__on_player_clicks_block(player, block, face) -> (
	if(query(player(), 'holds'):0 == global_tool,
		__set_pos(1);
	);
);

// set position 2 if player right clicks with a stone sword
__on_player_uses_item(player, item_tuple, hand) -> (
	if(item_tuple:0 == global_tool && hand == 'mainhand',
		__set_pos(2)
	);
);

__all_set(dim) -> (
	if(all(slice(global_positions:dim, 0, 2), _!=null), global_all_set:dim = true);
	__render_box();
);

__render_box() -> (
	dim = current_dimension();
	if(global_all_set:dim,
		min_pos = map(range(3), min(global_positions:dim:0:_, global_positions:dim:1:_));
		max_pos = map(range(3), max(global_positions:dim:0:_, global_positions:dim:1:_));
		draw_shape('box', 6, 'color', 0xFFFFFF70 , 'fill', 0xFFFFFF20, 'from', min_pos, 'to', max_pos+1 );
		schedule(5, '__render_box')
	);
);

global_positions = m();
global_all_set = m();
global_armor_stands = m();

__reset_positions('overworld');
__reset_positions('the_nether');
__reset_positions('the_end');
