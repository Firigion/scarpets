
pprint(...things) -> print(player('Firigion'), things);

//// Copy world fucntions
replicate(pos, block) -> (
	if(global_origin == null || within_bounds(pos), // if there's an origin defined, check for bounds
		for(global_offsets,
			newpos = pos + _;
			set(newpos, block);
			for(neighbours(newpos), update(_));
		)
	)
);

update_region() -> (
	base_size = 5;
	[lower, upper] = global_corners;

	ppos = player()~'pos';

	if(global_origin == null, // the matrix is following the player
		corners = map(make_corners(), map(_, if(_==null, base_size, abs(_) )) );
		lower = ppos - corners:0;
		upper = ppos + corners:1;
		,
	//else
		o = map(lower, if(_==null, ppos:_i, global_origin:_i));

		lower = if(lower == null,
			map(range(3), o-base_size),
			map(lower, if(_==null, o:_i - base_size, _));
		);
		upper = if(upper==null,
			map(range(3), o+base_size),
			map(upper, if(_==null, o:_i + base_size, _));
		);
	);

	volume(lower, upper, replicate(pos(_), _));
);

within_bounds(pos) -> (
	[lower, upper] = global_corners;
	all(pos, (lower:_i <= _ && _ <= upper:_i) || lower:_i == null)
);

//// Global lists to keep track of the grid

reset_offsets() -> (
	global_x = [0];
	global_y = [0];
	global_z = [0];
);

reset_axis(ax) -> (
	if(
		ax=='x', global_x = [0],
		ax=='y', global_y = [0],
		ax=='z', global_z = [0],
	);
	make_offsets();
);

reset_and_clear() -> (
	reset_offsets();
	reset_fix();
	reset_box();
	make_offsets();
);

make_offsets() -> (
	update_corners();
	global_offsets = [];

	for(global_x,
		x = _;
		for(global_y,
			y = _;
			for(global_z,
				z = _;
				global_offsets += [x, y, z]
			)
		)
	);
	draw_lines()
);


//// Make the offset lists for the fixed mode
make_box(pos1, pos2, x_repeats, y_repeats, z_repeats) -> (
	global_corners = [
		map(pos1, min(_, pos2:_i)),
		map(pos1, max(_, pos2:_i)), //the +1 is there to make sure to include the upper block in the range
	];
	global_origin = map(pos1+pos2, ceil(_/2)); 

	if( x_repeats != null,
		[x_step, y_step, z_step] = map(global_corners:1 - global_corners:0, _) + 1;
		x_offset(x_repeats, x_step);
		y_offset(y_repeats, y_step);
		z_offset(z_repeats, z_step);
	)
);

define_fix(pos) -> (
	global_origin = pos;

	update_corners();
	draw_lines();
);

// corners stores the upper most and lower most block in the tracked region
update_corners() -> (
	global_corners = make_corners();
	// if none of the offsets were set, remove corners
	if(all(global_corners:0, _==null), global_corners = null);
);

//abstracting this to make use of it in update_region()
make_corners() -> (
	corners = [[], []];
	for([global_x, global_y, global_z],
		//skip directions we don't draw in
		if(length(_)==1, 
			corners:0:_i = null;
			corners:1:_i = null;
			continue() 
		);

		step  = get_step(_i);
		upper = ceil(step/2) - 1;
		lower = floor(step/2);
		o = global_origin;

		corners:0:_i = o:_i - lower;
		corners:1:_i = o:_i + upper;
	);

	return(corners)
);

reset_fix() -> (
	global_origin = null;

	update_corners();
	draw_lines();
);

reset_box() -> global_corners = null;

//// Make the offset lists for the following mode
x_repeats(repeats) -> x_repeats_double(repeats, repeat);
y_repeats(repeats) -> y_repeats_double(repeats, repeat);
z_repeats(repeats) -> z_repeats_double(repeats, repeat);
xz_repeats(repeats) -> xz_repeats_double(repeats, repeat);

x_repeats_double(plus_count, minus_count) -> (
	if(global_corners == null, _error('You don\'t have a region defined'));

	step = global_corners:1:0 - global_corners:0:0;
	x_offset_double(plus_count, minus_count, step)
);

y_repeats_double(plus_count, minus_count) -> (
	if(global_corners == null, _error('You don\'t have a region defined'));

	step = global_corners:1:1 - global_corners:0:1;
	y_offset_double(plus_count, minus_count, step)
);

z_repeats_double(plus_count, minus_count) -> (
	if(global_corners == null, _error('You don\'t have a region defined'));

	step = global_corners:1:2 - global_corners:0:2;
	z_offset_double(plus_count, minus_count, step)
);

xz_repeats_double(plus_count, minus_count, ) -> (
	x_repeats_double(plus_count, minus_count);
	z_repeats_double(plus_count, minus_count)
);


x_offset(repeats, every) -> x_offset_double(repeats, repeats, every);
y_offset(repeats, every) -> y_offset_double(repeats, repeats, every);
z_offset(repeats, every) -> z_offset_double(repeats, repeats, every);
xz_offset(repeats, every) -> xz_offset_double(repeats, repeats, every);

x_offset_double(plus_count, minus_count, step) -> (
	global_x = map(range(-minus_count, plus_count+1), step * _);
	make_offsets()
);

y_offset_double(plus_count, minus_count, step) -> (
	global_y = map(range(-minus_count, plus_count+1), step * _);
	make_offsets()
);

z_offset_double(plus_count, minus_count, step) -> (
	global_z = map(range(-minus_count, plus_count+1), step * _);
	make_offsets()
);

xz_offset_double(plus_count, minus_count, step) -> (
	x_offset_double(plus_count, minus_count, step);
	z_offset_double(plus_count, minus_count, step)
);


//// Draw shapes

//this code is ungood, but it works, oh well
global_draw = false;
global_base_size = 4;
draw_lines() -> (
	// if we were already rendering lines, remove them, else, start drawing
	if(global_draw,
		// set the duration of all lines to 0 to turn them off
		for(global_lines,
			_:1 = 0;
		);
		draw_shape(global_lines),
	//else
		global_draw = true
	);

	all_offsets = [global_x, global_y, global_z];
	// the size of the affected area in each direction
	sizes = map(all_offsets, abs( (_:0) - (_:(-1))  ) + get_step(_i));
	// the ammount of affected areas in each direction
	lengths = map(all_offsets, length(_));

	// number of directions that have been registered to draw
	drawing_directions = reduce(lengths, _a + (_>1), 0);
	if(
		// if there are no copying directions, stop drawing
		drawing_directions == 0 || global_settings:'lines' == 'hide',
			global_draw = false,
	
		// grid in only one direction
		drawing_directions == 1,
			lines = if(global_settings:'lines' == 'dense',  lines_1d_dense(),  lines_1d_sparse() ),
		drawing_directions == 2,
			lines = lines_2d(),
		drawing_directions == 3,
			lines = lines_3d();
	);

	global_lines = lines;
	draw_lines_tick();

);

draw_lines_tick() -> (
	if(global_draw,
		draw_shape(global_lines);
		schedule(1, 'draw_lines_tick')
	);
);

// grabs a 2-ple and a singleton and packs them into a 3-ple
// keeping the singleton in the index given by direction
make_triple(li, b, direction) -> (
	if(
		direction == 0, [b, ...li],
		direction == 1, [li:0, b, li:1],
		direction == 2, [...li, b],
	)
);

// constructs the start and end point of lines that form
// either the vertical or horizontal part of a grid
// I abstrated this because I wanted to use it in the 2d 
// configuration, but ended up not needing it
make_grid_lines(a, b, step, direction, count) -> (
	map(range(-count, count+1),
		from = make_triple(a + b * _, step, direction);
		to = make_triple(-1 * a + b * _, step, direction);
		[from, to]
	);
);

// constructs a grid with length u_size and width v_size
make_grid(u_size, v_size, inx) -> (
	step = -1 * get_half_step(inx);

	u_grid_lines = make_grid_lines([u_size, 0], [0,1], step, inx, v_size);
	v_grid_lines = make_grid_lines([0, v_size], [1,0], step, inx, u_size);
	[...u_grid_lines, ...v_grid_lines];
);

// gets the start and end points for a cross perpendicular to the inx axis
make_cross(inx) -> (
	step = -1 * get_half_step(inx);

	[ 
		[ make_triple([-1,0], step, inx), make_triple([1,0], step, inx)], 
		[ make_triple([0,-1], step, inx), make_triple([0,1], step, inx);]
	]
);

make_line_data(from, to) -> (
	if(global_origin == null,
		//follow the player
		{'player' -> player(), 'from' -> from, 'to' -> to, 'follow' -> player(), 'snap' -> 'dxdydz', 'color' -> 0xFF0000FF},
	//else
		//only follow the player in the directions that are noe being drawn
		directions = map([global_x, global_y, global_z], length(_) > 1);
		o = map(global_origin, if(directions:_i, _, 0)); //a vector that has zeros in the directions we are not drawing
		attribute_map = {'from' -> from + o, 'to' -> to + o, 'color' -> 0xFF0000FF};

		// if we are not drawing in at least one direction, add a follow player in that direction
		if(!all(directions, _), 
			ds = filter(['dx', 'dy', 'dz'], !(directions:_i));
			attribute_map:'follow' = player();
			attribute_map:'snap' = join('', ds);
		);

		attribute_map
	)
);

get_step(inx) -> (
	all_offsets = [global_x, global_y, global_z];
	abs(all_offsets:inx:1 - all_offsets:inx:0);
);

get_half_step(inx) -> (
	floor(get_step(inx)/2);
);

extend_offsets(offsets) -> (
	step = offsets:1 - offsets:0;
	one_more = offsets:(-1) + step;
	[...offsets, one_more]
);

// this will return the two indexes that are not i, ordered
// [1,2], [0,2], [0,1]
get_rotating_index(i) -> sort([(i+1)%3, (i+2)%3]);


lines_1d_sparse() -> (

	all_offsets = [global_x, global_y, global_z];
	repeats = map(all_offsets, length(_));
	inx = repeats ~ (filter(repeats, _ > 1):0); //the index we are drawing on
	cross_lines = make_cross(inx);

	// replicate the cross at each step in the matrix
	lines = [];
	offsets = extend_offsets(global_offsets);
	for(cross_lines,
		[from, to] = _;
		for(offsets,
			line_data = make_line_data(from+_, to+_);
			lines += ['line', 5, line_data];
		)
	);
	// add one extra line crossing everything
	hs = make_triple([0, 0], get_half_step(inx), inx);
	line_data = make_line_data(offsets:0 - hs, offsets:(-1) - hs);
	lines += ['line', 5, line_data];

	return(lines)
);

lines_1d_dense() -> (
	
	all_offsets = [global_x, global_y, global_z];
	repeats = map(all_offsets, length(_));
	inx = repeats ~ (filter(repeats, _ > 1):0); //the index we are drawing on
	grid_lines = make_grid(global_base_size, global_base_size, inx);

	// replicate the grid at each step in the matrix
	lines = [];
	offsets = extend_offsets(global_offsets);
	for(grid_lines,
		[from, to] = _;
		for(offsets,
			line_data = make_line_data(from+_, to+_);
			lines += ['line', 5, line_data];
		)
	);
	return(lines)
);


lines_2d() -> (

	all_offsets = [global_x, global_y, global_z];
	// the size of the affected area in each direction
	sizes = map(all_offsets, abs( (_:0) - (_:(-1))  ) + get_step(_i));
	// the ammount of times the palced blocks is clones in each direction
	repeats = map(all_offsets, length(_));

	inx = repeats ~ (filter(repeats, _ == 1):0); //the index we are not drawing on
	sizes:inx = if(global_settings:'lines' == 'dense', global_base_size, 0);

	lines = [];
	half_steps = map(get_rotating_index(inx), get_half_step(_));
	half_steps = make_triple(half_steps, 0, inx);

	for(sizes,
		i = _i;
		l = _;

		// get the offsets and haklf steps corresponding to the other two directions
		[j, k] = get_rotating_index(i);

		u_offset = all_offsets:j;
		v_offset = all_offsets:k;

		u_hs = -half_steps:j;
		v_hs = -half_steps:k;

		from = make_triple([u_hs, v_hs], -floor(l/2), i);
		to = make_triple([u_hs, v_hs], ceil(l/2), i);
		
		// // this is for even denser lines
		// if(i==inx,
		// 	// for the axes we are drawing in
		// 	for(extend_offsets(u_offset),					
		// 		u = _;
		// 		eo = extend_offsets(v_offset);
		// 		for(range(eo:0, eo:(-1) + 1),
		// 			v = _;
		// 			if(i==inx, pprint(u, v));
		// 			offset = make_triple([u, v], 0, i);
		// 			line_data = make_line_data(from+offset, to+offset);
		// 			lines += ['line', 5, line_data];
		// 		);
		// 	);
		// 	for(extend_offsets(v_offset),					
		// 		v = _;
		// 		eo = extend_offsets(v_offset);
		// 		for(range(eo:0, eo:(-1) + 1),
		// 			u = _;
		// 			if(i==inx, pprint(u, v));
		// 			offset = make_triple([u, v], 0, i);
		// 			line_data = make_line_data(from+offset, to+offset);
		// 			lines += ['line', 5, line_data];
		// 		);
		// 	),
		// 	// for the axes we are not drawing in
		// 	for(extend_offsets(u_offset),
		// 		u = _;
		// 		for(extend_offsets(v_offset),
		// 			v = _;
		// 			if(i==inx, pprint(u, v));
		// 			offset = make_triple([u, v], 0, i);
		// 			line_data = make_line_data(from+offset, to+offset);
		// 			lines += ['line', 5, line_data];
		// 		)
		// 	)
		// );
		for(extend_offsets(u_offset),
			u = _;
			for(extend_offsets(v_offset),
				v = _;
				offset = make_triple([u, v], 0, i);
				line_data = make_line_data(from+offset, to+offset);
				lines += ['line', 5, line_data];
			)
		)
		
	);
	return(lines)
);


lines_3d() -> (

	all_offsets = [global_x, global_y, global_z];
	// the size of the affected area in each direction
	sizes = map(all_offsets, abs( (_:0) - (_:(-1))  ) + get_step(_i));

	lines = [];
	half_steps = map(range(3), get_half_step(_));

	for(sizes,
		i = _i;
		l = _;

		// get the offsets and haklf steps corresponding to the other two directions
		[j, k] = get_rotating_index(i);

		u_offset = all_offsets:j;
		v_offset = all_offsets:k;
		
		u_hs = -half_steps:j;
		v_hs = -half_steps:k;

		from = make_triple([u_hs, v_hs], -floor(l/2), i);
		to = make_triple([u_hs, v_hs], ceil(l/2), i);
		
		for(extend_offsets(u_offset),
			u = _;
			for(extend_offsets(v_offset),
				v = _;
				offset = make_triple([u, v], 0, i);
				line_data = make_line_data(from+offset, to+offset);
				lines += ['line', 5, line_data];
			)
		)
	);
	return(lines)
);


//// Settings

set_setting(val, what) -> (
	if(global_settings:what == val,
		_msg(str('%s was already set to %s', what, val)),
	//else
		_msg(str('Set %s to %s', what, val));
		global_settings:what = val;
		save_settings();	
	);
);

save_settings() -> (
	write_file(settings_file_name(), 'json', global_settings)
);

default_settings() -> (
	global_settings = {
		'lines' -> 'sparse', 

	}
);

load_settings() -> (
	settings = read_file(settings_file_name(), 'json');

	//if there was no file saved, use defaults
	if(settings == null, default_settings());
);

settings_file_name() -> str('%s_settings', player());

set_lines(val) -> (
	schedule(0, 'draw_lines');
	set_setting(val, 'lines')
);

//// Config and such

__config() -> {
	'commands' -> {
		'x <repeats> every <every>' -> 'x_offset',
		'x <repeats> <negative_repeats> every <every>' -> 'x_offset_double',
		'x <repeats>' -> 'x_repeats',
		'x <repeats> <negative_repeats>' -> 'x_repeats_double',

		'y <repeats> every <every>' -> 'y_offset',
		'y <repeats> <negative_repeats> every <every>' -> 'y_offset_double',
		'y <repeats>' -> 'y_repeats',
		'y <repeats> <negative_repeats>' -> 'y_repeats_double',

		'z <repeats> every <every>' -> 'z_offset',
		'z <repeats> <negative_repeats> every <every>' -> 'z_offset_double',
		'z <repeats>' -> 'z_repeats',
		'z <repeats> <negative_repeats>' -> 'z_repeats_double',

		'flat <repeats> every <every>' -> 'xz_offset',
		'flat <repeats> <negative_repeats> every <every>' -> 'xz_offset_double',
		'flat <repeats>' -> 'xz_repeats',
		'flat <repeats> <negative_repeats>' -> 'xz_repeats_double',

		'fix <pos>' -> 'define_fix',
		'follow' -> 'reset_fix',

		'<first_pos> <second_pos>' -> ['make_box', null, null, null],
		'<first_pos> <second_pos> <repeats>' -> _(p1, p2, r) -> make_box(p1, p2, r, r, r), //shorthand for a matrix that's hogenous in all directions
		'<first_pos> <second_pos> <repeats> <y_repeats> <z_repeats>' -> 'make_box',

		'update' -> 'update_region',
		//'update inventories' -> 'update_inv',
		'reset' -> 'reset_and_clear',
		'reset <axis>' -> 'reset_axis',

		'settings reset' -> 'default_settings',
		'settings line <howmuch>' -> 'set_lines',
	},
	'arguments' -> {
		'every' -> {'type' -> 'int', 'min' -> 1, 'suggest' -> [3, 7]},
		'repeats' -> {'type' -> 'int', 'min'-> 0, 'suggest' -> [0, 2, 5]},

		'howmuch' -> {'type' -> 'term', 'options' -> ['hide', 'sparse', 'dense']},
		'axis' -> {'type' -> 'term', 'options' -> ['x', 'y', 'z']},
	}
};

_msg(msg) -> print(player(), format('gi ' + msg));
_error(msg) -> (
	print(player(), format('r ' + msg));
	exit()
);

//// register events that modify the world
__on_player_breaks_block(player, block) -> replicate(pos(block), 'air');
__on_player_interacts_with_block(player, hand, block, face, hitvec) -> replicate(pos(block), block);
__on_player_places_block(player, item_tuple, hand, block) -> replicate(pos(block), block);

reset_offsets();
reset_fix();
reset_box();
default_settings();