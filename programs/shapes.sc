//__command() -> print(player(), str('Use a %s to set positions, use the draw commands to draw shapes, and distance to querry distance between positions 1 and 2. You can throw a snow ball to erase a connected object.', global_tool));

global_debug = false;
global_show_pos = true;
global_tool = 'golden_sword';
global_delete_item = 'snowball';
global_max_delete_radius_sq = 625;


/////// Utilities ////////

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


_sq_distance(a, b) -> reduce(a-b, _a + _*_, 0);


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
__proj(v1, v2) -> ( //projection of v1 onto v2
  v2*__dot_prod(v1,v2)/__dot_prod(v2,v2);
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


debug_mark_sphere(p1, p2, p3, c) -> (
	if(global_debug, 
		for([p1, p2, p3], __set_and_save(_, 'diamond_block') );
		set(c, 'emerald_block');
	);
);

//////// Shapes ///////

sphere(p1, p2, p3, material, width) -> (
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
				__set_and_save(_, material)
			)
		)
		//run(str('/draw ball %s %s %s %s %s', c1, c2, c3, r, material) )
	);

	debug_mark_sphere(p1, p2, p3, c);
	
	dim = player() ~ 'dimension';
	__put_into_history(global_this_story, dim); 
);


plane(p1, p2, p3, material, width) -> (
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
		if(__tha_plane(pos(_)-l(a1, a2, a3)), __set_and_save(_, material) )
	);
	

	if(global_debug, (
		for([ [a1, a2, a3], [b1, b2, b3] ], __set_and_save(_, 'gold_block') );

		for([p1, p2, p3], __set_and_save(_, 'diamond_block') );
		);
	);
	
	dim = player() ~ 'dimension';
	__put_into_history(global_this_story, dim); 
);


disc(p1, p2, p3, material, width) -> (

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
			__set_and_save(_, material)
		);
	);

	debug_mark_sphere(p1, p2, p3, c);
	
	dim = player() ~ 'dimension';
	__put_into_history(global_this_story, dim); 
);


ring(p1, p2, p3, material, width) -> (

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
			__set_and_save(_, material)
		);
	);

	debug_mark_sphere(p1, p2, p3, c);
	
	dim = player() ~ 'dimension';
	__put_into_history(global_this_story, dim); 
);


line_fast(p1, p2, material) -> (
 	m = p2-p1;
	max_size = max(map(m, abs(_)));
	t = l(range(max_size))/max_size;
	for(t, 
 		b = block(m * _ + p1);
 		__set_and_save(b, material);
 	);
	
	dim = player() ~ 'dimension';
	__put_into_history(global_this_story, dim); 
);


line(p1, p2, material, width) -> (
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
			__set_and_save(_, material)
		);
	);

	dim = player() ~ 'dimension';
	__put_into_history(global_this_story, dim); 

	// run(str('script fill %d %d %d %d %d %d %d %d %d "__tha_plane1(x, y, z) && __tha_plane2(x, y, z) " %s', 
			// p1:0, p1:1, p1:2, p1:0, p1:1, p1:2, p2:0, p2:1, p2:2, material
		// ));

);

distance(pos1, pos2) -> (
	if( pos1 != null && pos2 != null,
		print( str('Distance between markers is %.2f', __norm(pos1-pos2) ) ),
		print('Need to set all three positions first.')
	);
	return('')
);

line_sight(p1, material, length) -> (
	dim = player() ~ 'dimension';
	pos1 = global_positions:dim:0;
	
	if( pos1 == null , print('Need to set all three positions first.'); return('') );

	facing = player()~'facing';
	end_point = pos_offset(pos1, facing, length);
	
	line(pos1, end_point, material, 1);
);

tube_elipse(p1,p2,p3,material,width,rminor) ->(
  v1 = p2-p1;
  v3 = p3-p1;
  vmajor = v3 - __proj(v3,v1);
  cylinder=false;
  rmajor = __norm(vmajor);
  if(rminor == null,rminor=rmajor);
  if(width == null,cylinder=true);
  magv1=__norm(v1);
  cmin=[0,0,0];//initilizes corners for volume builtin
  cmax=[0,0,0];
  //detrmines bounding box arround shape so that it can be itterated over by the volume builtin
  loop(3,
    dn = sin(acos(get(v1,_)/magv1))*(max(rmajor,rminor)+max(width,0));
    if(v1:_>=0,
      put(cmin, _, floor(p1:_-dn));
      put(cmax, _, ceil(p2:_+dn));
      ,
      put(cmin, _, floor(p2:_ - dn));
      put(cmax, _, ceil(p1:_ + dn));
    );
  );
  //the h in hminor and hmajor represent the hat notation for unit vectors
  hminor = __normalize(__cross_prod(vmajor,v1));
  hmajor = __normalize(vmajor);
  //print(str(hminor)+','+str(hmajor));
  //computing some values before the loop is run to reduce loop time
  //these are largely the denominator in the functions for drawing circles
  rmajorWp2=(rmajor+width)^2;
  rminorWp2=(rminor+width)^2;
  rmajorp2=rmajor^2;
  rminorp2=rminor^2;
  cylinder = min([rmajor+width,rminor+width])<=0;//checks if it will be solid
  //this is needed because if rX + width = 0 then we get a devide by zero error and 
  //if its less than zero we get the effective value increasing again since it gets squared
  
  volume(cmin,cmax,
    tv=pos(_)-p1;//create a 'test vector' relative to p1
    tp=pos(_); //create a tp 
    //cacluates the magnitude of the test vector in the directions of the major and minor axis
    magProjMajor = __dot_prod(tv,hmajor);
    magProjMinor = __dot_prod(tv,hminor);
    magProjMajorp2=magProjMajor^2;
    magProjMinorp2=magProjMinor^2;
    
    //these functions define where the shape gets drawn they work for width greater than zero 
    //inside=((magProjMajor^2)/(rmajor^2)+(magProjMinor^2)/(rminor^2))>=1;
    //outside=((magProjMajor)^2/(rmajor+width)^2+(magProjMinor)^2/(rminor+width)^2)<=1;
    //started = (__dot_prod(tv,v1))>=0;
    //notended = (__dot_prod(tp-p2, -1*v1))>=0;
    //the if statment here checks for the width condition so that the inner wall can become the outer wall and vice versa
    if(width>=0,
      if( ((magProjMajorp2/rmajorp2+magProjMinorp2/rminorp2)>=1 &&
            (cylinder||(magProjMajorp2/rmajorWp2+magProjMinorp2/rminorWp2)<=1) 
            && (__dot_prod(tv,v1))>=0 && (__dot_prod(tp-p2, -1*v1))>=0 ),
        set(pos(_),material),
        continue()
      );
    ,
      if( (((magProjMajorp2)/rmajorp2+(magProjMinorp2)/rminorp2)<=1 &&
            (cylinder||(magProjMajorp2/rmajorWp2+magProjMinorp2/rminorWp2)>=1) 
            && (__dot_prod(tv,v1))>=0 && (__dot_prod(tp-p2, -1*v1))>=0 ),
        set(pos(_),material),
        continue()
      );    
    );
  );
);


////// Snowball eraser //////

global_trace_distance = 100;
global_limit = 10000;

_remove_projectiles(player) -> (
	sb = filter(entity_selector(str('@e[type=%s]', global_delete_item)), _sq_distance(pos(player), pos(_)) );
	for(sb, modify(_, 'remove') );
);

_sq_distance(a, b) -> reduce(a-b, _a + _*_, 0);

_flood_delete(start) -> (
	interior_block = block(start);
	set(start, 'air');
	
	visited = {start->null};
	queue = [start];
	
	while(length(queue)>0, global_limit,
		current_pos = queue:0;
		delete(queue, 0);
		
		for(neighbours(current_pos),
			current_neighbour = pos(_);
			// check neigbours, add the non visited ones to the visited set
			if(!has(visited, current_neighbour),
				visited:current_neighbour = null;
				// if the block is not too far and is itnerior, delete it and add to queue to check neighbours later
				if( _sq_distance(current_neighbour, start) < global_max_delete_radius_sq && _==interior_block,
					queue:length(queue) = current_neighbour;
					set(current_neighbour, 'air')
				);
			);
		);
	);
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

//get_armor_stands() -> print(global_armor_stands);

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
	if(player~'holds':0 == global_tool,
		set_pos(1);
	);
);

// set position 2 if player right clicks with a golden sword
__on_player_uses_item(player, item_tuple, hand) -> (
	if(
	// if tool, set positions
	item_tuple:0 == global_tool,
		if(query(player(), 'sneaking'),
			set_pos(3),
			set_pos(2)
		),
	
	//else, if projectile tool
	item_tuple:0 == global_delete_item,
				
		schedule(0, '_remove_projectiles', player);
		
		target = query(player, 'trace', global_trace_distance);
		if(target==null, return('') );
		
		_flood_delete(pos(target));
		
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



////// Undo stuff ///////

global_history = {
					'overworld' -> [] ,
					'the_nether' -> [] ,
					'the_end' -> [] ,
				};

__set_and_save(block, material) -> ( //defaults to no replace
	global_this_story:length(global_this_story) = [pos(block), block];
	set(block , material);
);

__put_into_history(story, dim) -> (	
	print(str('Set %d blocks', length(story) ));
	global_history:dim += story;
	return('');
);

__undo(index, dim) -> (
	// iterate over the story backwards
	for(range(length(global_history:dim:index)-1, -1, -1),
		print(global_history:dim:index:_);
		set(global_history:dim:index:_:0, global_history:dim:index:_:1); // (position, block) pairs
	);
	// remove used story
	delete(global_history:dim, index);
);

go_back_stories(num) -> (
	//check for valid input
	if( type(num) != 'number' || num <= 0, 
		print(format('rb Error: ', 'y Need a positive number of steps to go to'));
		return('')
	);
	
	dim = player() ~ 'dimension';

	index = length(global_history:dim)-num;
	if(index<0, 
		print(format('rb Error: ', str('y You only have %d actions available to undo', length(global_history:dim) ) ));
		return('')
	);
	
	__undo(index, dim);
	print(str('Undid what you did %s actions ago', num ));	
);

undo(num) -> (
	//check for valid input
	if( type(num) != 'number' || num <= 0, 
		print(format('rb Error: ', 'y Need a positive number of steps to undo'));
		return('')
	);

	dim = player() ~ 'dimension';
	
	index = length(global_history:dim)-num;
	if(index<0, 
		print(format('rb Error: ', str('y You only have %d actions to undo available', length(global_history:dim) ) ));
		return('')
	);
	
	loop(num, __undo(length(global_history:dim)-1, dim) );
	print(str('Undid the last %d actions', num) );
);


__config() -> {
	'commands' -> {
		'ring <block>' -> _(b) -> __callif('ring', b, 0.5),
		'ring <block> <width>' -> _(b, w) -> __callif('ring', b, w/2),

		'disc <block>' -> _(b) -> __callif('disc', b, 0.5),
		'disc <block> <width>' -> _(b, w) -> __callif('disc', b, w/2),

		'sphere <block>' -> _(b) -> __callif('sphere', b, 1),
		'sphere <block> <width>' -> _(b, w) -> __callif('sphere', b, w),

		'plane <block>' -> _(b) -> __callif('plane', b, 0.5),
		'plane <block> <width>' -> _(b, w) -> __callif('plane', b, w/2),

		'line <block>' -> _(b) -> __callif('line', b, 0.5),
		'line <block> <width>' -> _(b, w) -> __callif('line', b, w/2),
		'line <block> fast' -> _(b) -> __callif('line_fast', b),
		'line <block> sight <length>' -> _(b, l) -> __callif('line_sight', b, l),
    'tube <block> <thickness> <rminor>' -> _(b, w, r) -> __callif('tube_elipse', b, w, r),
    'tube <block> <thickness>' -> _(b, w) -> __callif('tube_elipse', b, w,null),
		'distance' -> _() -> __callif('distance'),

		'undo <actions>' -> 'undo',
		'undo jump <actions>' -> 'go_back_stories',
    
		'markers reset' -> 'reset_positions',
		'markers toggle' -> 'toggle_show_pos',
		'markers set <index>' -> 'set_pos',
		'markers get' -> 'get_pos',

	},
	'arguments' -> {
		'actions' -> {'type'->'int', 'min'->1, 'suggest'->[1,3,5]},
		'width' -> {'type' -> 'int', 'min' -> 1, 'suggest'->[1,3,5]},
		'length' -> {'type' -> 'int', 'min' -> 1, 'suggest'->[1,12,24]},
		'index' -> {'type' -> 'int', 'min' -> 1, 'max'->3, 'suggest'->[1,2,3]},
    'rminor' -> {'type' -> 'float', 'min' -> 0, 'suggest'->[5,8.5,12.4]},
    'thickness' -> {'type' -> 'float', 'suggest'->[1,2,3,-2]}
	},
};


__callif(fun, ...args) -> (
	dim = player() ~ 'dimension';
	global_this_story = [];

	ammount = global_points_ammount:fun;

	positions = [];
	loop(ammount, 
		pos = global_positions:dim:_;
		if(pos==null,
			print(format('r Need to set '+ammount+' positions first'));
			exit();
		);
		positions += pos;
	);
	//if positions were set, call function
	call(fun, ...positions, ...args);
);

// how many set positions does that function need?
global_points_ammount = {
	'ring' -> 3,
	'disc' -> 3,
	'sphere' -> 3,
	'plane' -> 3,
	'line' -> 2,
	'line_fast' -> 2,
	'line_sight' -> 1,
	'distance' -> 2,
  'tube_circle' -> 3,
  'tube_elipse' -> 3,
  'cylinder' -> 3,
  'cylinder_elliptic' -> 3
};

