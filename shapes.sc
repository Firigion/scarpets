__command() -> null;

global_debug = false;
global_show_pos = true;

__dot_prod(l1, l2) -> (
	reduce(l1 * l2, _a + _, 0)
);


__cross_prod(l1, l2) -> (
	s = l(
		l1:1 * l2:2 - l2:1 * l1:2,
		l2:0 * l1:2 - l1:0 * l2:2,
		l1:0 * l2:1 - l2:0 * l1:1,
	);
);


__norm(list) -> (
	sqrt(__dot_prod(list, list));
);


__normalize(list) -> (
	list / __norm(list);
);


__min_list(list_of_positions) -> (
	map(list_of_positions,
		i = _i;
		min(map(list_of_positions, _:i));
	);
);


__max_list(list_of_positions) -> (
	map(list_of_positions,
		i = _i;
		max(map(list_of_positions, _:i));
	);
);


__circ(p1, p2, p3) -> (
	v1 = p2-p1;
	v2 = p3-p1;

	v11 = __dot_prod(v1, v1);
	v12 = __dot_prod(v1, v2);
	v22 = __dot_prod(v2, v2);

	b = 1/ (2*(v11 * v22 - v12*v12));
	k1  = b * v22 * (v11 - v12);
	k2  = b * v11 * (v22 - v12);

	center = p1 + k1 * v1 + k2 * v2;
	l(center, __norm(center-p1));
);


__sphere(p1, p2, p3, material, width) -> (
	l(c, r) = __circ(p1, p2, p3);
	if(global_debug, print('c: ' + map(c, floor(_)) + ', r: ' + floor(r)) );
	l(c1, c2, c3) = c;

	if(width < 1,
	// just a shell
		run(str('/draw sphere %s %s %s %s %s', c1, c2, c3, r, material) ),
	// else a thick shell or ball
		if(width > r,
		// a ball
			__tha_sphere(x, y, z, outer(r), outer(width)) -> (
				x*x+y*y+z*z <= r * r
			),
		// a shell
			__tha_sphere(x, y, z, outer(r), outer(width)) -> (
				x*x+y*y+z*z <= r * r && x*x+y*y+z*z > (r-width) * (r-width)
			);
		);
		
		volume(c1+r, c2+r, c3+r, c1-r, c2-r, c3-r,
			l(x, y, z) = pos(_)-c;
			if( __tha_sphere(x, y, z),
				set(_, material)
			)
		)
		//run(str('/draw ball %s %s %s %s %s', c1, c2, c3, r, material) )
	);

	if(global_debug, (
		set(p1, 'diamond_block');
		set(p2, 'diamond_block');
		set(p3, 'diamond_block');

		set(c, 'emerald_block');
		);
	);
);


__plane(p1, p2, p3, material, width) -> (
	v1 = p2-p1;
	v2 = p3-p1;

	l(a1, a2, a3) = __min_list(l(p1, p2, p3));
	l(b1, b2, b3) = __max_list(l(p1, p2, p3));

	n = __normalize(__cross_prod(v1, v2));
	k = __dot_prod(n, p1 - l(a1, a2, a3));
	
	__tha_plane(pos, outer(n), outer(k), outer(width)) -> (
		__dot_prod(pos, n) - k >= -width && __dot_prod(pos, n) - k <= width
	);

	volume(a1, a2, a3, b1, b2, b3,
		if(__tha_plane(pos(_)-l(a1, a2, a3)), set(_, material) )
	);
	

	if(global_debug, (
		set(l(a1, a2, a3), 'gold_block');
		set(l(b1, b2, b3), 'gold_block');

		set(p1, 'diamond_block');
		set(p2, 'diamond_block');
		set(p3, 'diamond_block');
		);
	);
);


__disc(p1, p2, p3, material, width) -> (

	l(c, r) = __circ(p1, p2, p3);
	l(c1, c2, c3) = c;

	__tha_sphere(x, y, z, outer(r)) -> (
		x*x+y*y+z*z <= r * r
	);

	v1 = p2-p1;
	v2 = p3-p1;

	n = __normalize(__cross_prod(v1, v2));
	k = __dot_prod(n, p1 - c);

	__tha_plane(pos, outer(n), outer(k), outer(width)) -> (
		__dot_prod(pos, n) - k >= -width && __dot_prod(pos, n) - k <= width
	);
	volume(c1+r, c2+r, c3+r, c1-r, c2-r, c3-r,
		current = pos(_)-c;
		if( __tha_plane(current) && __tha_sphere(current:0, current:1, current:2),
			set(_, material)
		);
	);

	if(global_debug, (
		set(p1, 'diamond_block');
		set(p2, 'diamond_block');
		set(p3, 'diamond_block');

		set(c, 'emerald_block');
		);
	);
);


__ring(p1, p2, p3, material, width) -> (

	l(c, r) = __circ(p1, p2, p3);
	//c = map(c, floor(_) );
	l(c1, c2, c3) = c;

	__tha_sphere(x, y, z, outer(r), outer(width)) -> (
		x*x+y*y+z*z <= r * r && x*x+y*y+z*z > (r-width) * (r-width)
	);

	v1 = p2-p1;
	v2 = p3-p1;

	n = __normalize(__cross_prod(v1, v2));
	k = __dot_prod(n, p1 - c);

	__tha_plane(pos, outer(n), outer(k), outer(width)) -> (
		__dot_prod(pos, n) - k >= -width && __dot_prod(pos, n) - k <= width
	);
	
	volume(c1+r, c2+r, c3+r, c1-r, c2-r, c3-r,
		current = pos(_)-c;
		if( __tha_plane(current) && __tha_sphere(current:0, current:1, current:2),
			set(_, material)
		);
	);

	if(global_debug, (
		set(p1, 'diamond_block');
		set(p2, 'diamond_block');
		set(p3, 'diamond_block');

		set(c, 'emerald_block');
		);
	);
);


__line_fast(p1, p2, material, width) -> (
 	m = p2-p1;
	max_size = max(map(m, abs(_)));
	t = l(range(max_size))/max_size;
	for(t, 
 		b = m * _ + p1;
 		set(b, material);
 	);
);


__line(p1, p2, material, width) -> (
	v = p2-p1;

	if(v:2 == 0,
		n1 = l(0,0,1),
		n1 = l(1, 1, -(v:0 + v:1)/v:2);
	);
	n1 = __normalize(n1);
	n2 = __normalize(__cross_prod(v, n1));

	// __tha_plane1(x, y, z, outer(n1), outer(width)) -> (
		// __dot_prod(l(x, y, z), n1) >= -width && __dot_prod(l(x, y, z), n1) <= width
	// );
	
	__tha_plane1(pos, outer(n1), outer(width)) -> (
		__dot_prod(pos, n1) >= -width && __dot_prod(pos, n1) <= width
	);

	__tha_plane2(pos, outer(n2), outer(width)) -> (
		__dot_prod(pos, n2) >= -width && __dot_prod(pos, n2) <= width
	);


	// __tha_plane2(x, y, z, outer(n2), outer(width)) -> (
		// __dot_prod(l(x, y, z), n2) >= -width && __dot_prod(l(x, y, z), n2) <= width
	// );

	volume(p1:0, p1:1, p1:2, p2:0, p2:1, p2:2,
		current = pos(_)-c;
		if( __tha_plane1(current-p1) && __tha_plane2(current-p1),
			set(_, material)
		);
	);


	// run(str('script fill %d %d %d %d %d %d %d %d %d "__tha_plane1(x, y, z) && __tha_plane2(x, y, z) " %s', 
			// p1:0, p1:1, p1:2, p1:0, p1:1, p1:2, p2:0, p2:1, p2:2, material
		// ));

);


__drawif(ammount, shape, material, width) -> (
	dim = player() ~ 'dimension';
	if(ammount == 3,
		if(global_all_set:dim,
			call(shape, global_positions:dim:0, global_positions:dim:1, global_positions:dim:2, material, width),
			print('Need to set all three positions first.')
		),
	ammount == 2,
		pos1 = global_positions:dim:0;
		pos2 = global_positions:dim:1;
		if( pos1 != null && pos2 != null,
			call( shape, pos1, pos2, material, width),
			print('Need to set all three positions first.')
		);
	);
);


draw_disc(material, width) -> __drawif(3, '__disc', material, width/2);
draw_ring(material, width) -> __drawif(3, '__ring', material, width/2);
draw_sphere(material, width) -> __drawif(3, '__sphere', material, width);
draw_plane(material, width) -> __drawif(3, '__plane', material, width/2);
draw_line(material, width) -> __drawif(2, '__line', material, width/2);
draw_line_fast(material) -> __drawif(2, '__line_fast', material, none);

distance() -> (
	dim = player() ~ 'dimension';
	pos1 = global_positions:dim:0;
	pos2 = global_positions:dim:1;

	if( pos1 != null && pos2 != null,
		print( str('Distance between markers is %.2f', __norm(pos1-pos2) ) ),
		print('Need to set all three positions first.')
	);
	return('')
);

////// Handle Markers //////

// Spawn a marker
__mark(i, position, dim) -> (
 	colours = l('red', 'lime', 'light_blue'); 
	e = create_marker('pos' + i, position + l(0.5, 0.5, 0.5), colours:(i-1) + '_concrete'); // crete the marker
	run(str( //modify some stuff to make it fancier
		'data merge entity %s {Glowing:1b, Fire:32767s, Marker:1b}', query(e, 'uuid') 
		));
	global_armor_stands:dim:(i-1) =  query(e, 'id'); //save the id for future use
	if(global_debug, print('Set mark') );
);

__remove_mark(i, dim) -> (
	e = entity_id(global_armor_stands:dim:(i));
 	if(e != null, modify(e, 'remove'));
);

get_armor_stands() -> print(global_armor_stands);

// set a position
set_pos(i) -> (
	dim = player() ~ 'dimension';
	
	try( // position index must be 1, 2 or 3 
 		if( !reduce(range(1,4), _a + (_==i), 0),
			throw();
		),
		print(format('rb Error: ', 'y Input must be either 1, 2 or 3 for position to set. You input ' + i) );
		return()
	);
	// position to be set at the block the player is aiming at, or player position, if there is none
	tha_block = query(player(), 'trace');
	if(tha_block!=null,
		tha_pos = pos(tha_block),
		tha_pos = map(pos(player()), round(_))
	);
	global_positions:dim:(i-1) = tha_pos; // save to global positions
	__all_set(dim); 
	
	print(str('Set your position %d in %s to ',i, dim) + tha_pos);

	if(global_show_pos, // remove previous marker for set positi, if aplicable
		__remove_mark(i-1, dim); //-1 because stupid indexes
		__mark(i, tha_pos, dim);
	);

);

// print list of positions
get_pos() -> (
	dim = player() ~ 'dimension';
	for(global_positions:dim, 
 		print(str('Position %d is %s', 
				_i+1, if(_==null, 'not set', _)));
 	)
);

// toggle markers and bounding box visibility
toggle_show_pos() ->(
	dim = player() ~ 'dimension'; 
	global_show_pos = !global_show_pos; 
	if(global_show_pos,
		( // summon the markers
			for(global_positions:dim, 
				if(_!=null, __mark( (_i+1) , _, dim) );
			);
			print('Positions are now shown');
		),
		// else
		( //remove the markers
			for(global_armor_stands:dim, 
				__remove_mark(_i, dim);
			);
			print('Positions are now hidden');
		);
	);
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

__all_set(dim) -> (
	if(all(global_positions:dim, _!=null), global_all_set:dim = true);
);

global_positions = m();
global_all_set = m();
global_armor_stands = m();

__reset_positions('overworld');
__reset_positions('the_nether');
__reset_positions('the_end');