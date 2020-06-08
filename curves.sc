__command() -> null;

// to store marker positions and object handles
global_settings = m(
						l( 'show_pos' , true ),
						l( 'paste_with_air' , false ),
						l( 'spiral_axis' , 'y' ),
						l( 'wave_axis' , 'xy' ),
						l( 'replace_block' , false ),
						l( 'rotate' , false ), //TODO
						l( 'slope_mode', false ),
						l( 'max_template_size', 100 ),
						l( 'preview_enabled', false ), //TODO
						l( 'undo_history_size', 100 ),
					);
global_block_alias = m(
						l( 'water_bucket', 'water' ),
						l( 'lava_bucket', 'lava'),
						l( 'feather', 'air'),
						l( 'ender_eye', 'end_portal'),
						l( 'flint_and_steel', 'nether_portal')
					);

global_history = m(
					l( 'overworld', l() ),
					l( 'the_nether', l() ),
					l( 'the_end', l() );
				);


////// Utilities ///////

__get_replace_block() -> (
	block = query(player(), 'holds', 'offhand'):0;
	alias = global_block_alias:block;
	if(alias==null, block, alias)
);

__set_and_save(pos, material) -> ( //defaults to no replace
	global_this_story:length(global_this_story) = l(pos, block(pos));
	set(pos , material);
);

__set_block(pos, material, replace_block) -> __set_and_save(pos, material);

__get_center() -> (
	dim = player() ~ 'dimension';
	if(global_positions:dim:2 == null,
		pos(player()),
		global_positions:dim:2
	)
);

__extend(list, extension) -> (
	len = length(list);
	for(extension, list:(len+_i) = _);
	return(list)
);

__reflect(list) ->  map(range(length(list)-1, -1, -1), list:_ );

// saves selected area, minus air
__make_template() -> (

	dim = player() ~ 'dimension';
	if(!global_all_set:dim,
		print(format('rb Error: ', 'y You need to make a selection first' )); 
		return(true) // dont paste anything 
	);
	
	pos0 = global_positions:dim:0;
	pos1 = global_positions:dim:1;
	
	global_template = l();
	origin = map(range(3), min(pos0:_, pos1:_)); //negative-most corner in all dimensions
	volume(
		pos0:0, pos0:1, pos0:2,
		pos1:0, pos1:1, pos1:2,
		if(global_settings:'paste_with_air',
			global_template:length(global_template) = l(pos(_)-origin, _),
			if(!air(_), global_template:length(global_template) = l(pos(_)-origin, _) ) //save non-air blocks and positions
		);
	);
	// Handle template size warning
	if(length(global_template) > global_settings:'max_template_size',
		print( format(
			'buy Warning',
			'y : ',
			'w Template is too big. Your tried to paste ', 
			str('by %d ', length(global_template)),
			str('w blocks %s, but max size is ', if(global_settings:'paste_with_air', '(counting air)', '(not counting air)') ),
			str('by %d', global_settings:'max_template_size' ),
			'w .\nTry increasing it with ',
			'b [this] ', '^t Click here!', '?/curves set_max_template_size 200',
			'w command.'
		) );
		true, // tempalte too big, don't paste it
		false // paste it
	)
);

// clone template at given position
__clone_template(pos, replace_block) -> (
	for(global_template, __set_block(pos + _:0, _:1, replace_block) );
);


////// Spirals ///////

// ---- Utils ----

// mainly for debug porpuses
_circle(radius, material) -> (
	circ = __make_circle(radius);
	c = pos(player());
	for(circ, 
		set(c + l(_:0, 0, _:1), material); 
		create_marker(str(_i), c + l(_:0, 0, _:1))
	);	
);

__rotated90(list_to_rotate) -> ( //rotates 90 degrees
	map(list_to_rotate, l(_:1, -_:0))
);

__spiral_get_step(circle, perimeter, advance_step, i) ->( //defaults to axis y
	circle_pos = circle:(i%perimeter);
	step = l(circle_pos:0, i * advance_step, circle_pos:1) ;
);

__make_circle(radius) -> (
	z_function(x, outer(radius)) -> round(sqrt(radius * radius - x*x));
	range_val = radius * cos(45); //this spans a quarter circle
	x_range = range(-range_val, range_val);
	
	quarter1 = map(x_range, l(_, z_function(_)) ); // starts with quarter circle
	half = __extend(quarter1, __rotated90(quarter1)); // add a quarter by rotating it 90 degrees
	__extend(half,__rotated90(__rotated90(half))); // rotate the half circle 180 degrees and add it
);

__assert_pitch(pitch) -> (
	if(pitch == 0, 
		print(format('rb Error: ', str('y %s must not be zero', if(global_settings:'slope_mode', 'Slope', 'Pitch')) ) );
		true,
		false
	);
	
);

// ---- Draw funtions ----

__preview_spiral(circle, center, pitch, size, iterations_left) -> (
	
	perimeter = length(circle); // ammount of blocks in one revolution
	advance_step = if(global_settings:'slope_mode', pitch, pitch/perimeter); //pitch encodes slope if slope_mode == true
	
	loop(floor( size / advance_step) - 1 , //loop over the total ammount of spirals
		this_step = __spiral_get_step(circle, perimeter, advance_step, _);
		next_step = __spiral_get_step(circle, perimeter, advance_step, _+1);
		particle_line('end_rod', center + this_step, center + next_step, 1);
	);
	
	if(iterations_left > 0, 
		schedule(10, '__preview_spiral', circle, center, pitch, size, iterations_left-1)
	);
);

preview_spiral(radius, pitch, size, time) -> (
	if(__assert_pitch(pitch), return('') );
	
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	
	iterations = time * 2; // time is a value in seconds, will render every 10 gt
	__preview_spiral(circle, center, pitch, size, iterations);	
);

//main funtion todraw spiral from material
__draw_spiral(circle, center, pitch, size, material) -> (
	
	dim = player() ~ 'dimension';
	
	if(__assert_pitch(pitch), return('') );

	perimeter = length(circle); // ammount of blocks in one revolution
	
	replace_block = __get_replace_block();
	advance_step = if(global_settings:'slope_mode', pitch, pitch/perimeter); //pitch encodes slope if slope_mode == true
	global_this_story = l();
	
	if(material == 'template',
	// make from template
		if(__make_template(), return() ); //tempalte was too big
		offset = map(global_positions:dim:0 - global_positions:dim:1, abs(_)) / 2; //offsets the selection so that it clones it in the center of the block
		
		loop(floor( size / advance_step), //loop over the total ammount of spirals
		this_step =  __spiral_get_step(circle, perimeter, advance_step, _);
		__clone_template( center + this_step - offset, replace_block);
		),
	//else, make out of material
		loop(floor( size / advance_step), //loop over the total ammount of spirals
		this_step =  __spiral_get_step(circle, perimeter, advance_step, _);
		__set_block(center + this_step , material, replace_block);
		);
	);
		
	__put_into_history(global_this_story, dim); //ensure max history size is not exceeded
	print(str('Set %d blocks', length(global_this_story) ));
);


spiral(radius, pitch, size, material) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	__draw_spiral(circle, center, pitch, size, material);
);

antispiral(radius, pitch, size, material) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	circle = __reflect(circle); // to spin the other way around
	__draw_spiral(circle, center, pitch, size, material);

);

multispiral(radius, pitch, size, ammount, material) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	perimeter = length(circle); // ammount of blocks in one revolution
	loop(ammount,
		jump = floor(_ * perimeter/ammount); // by how many places to advance to get to the next circle
		this_circ = __extend(slice(circle, jump), slice(circle, 0, jump) ); // redefine the circle last for this iteration
		__draw_spiral(this_circ, center, pitch, size, material);
	);
);

antimultispiral(radius, pitch, size, ammount, material) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	circle = __reflect(circle); // to spin the other way around
	perimeter = length(circle); // ammount of blocks in one revolution
	loop(ammount,
		jump = floor(_ * perimeter/ammount); // by how many places to advance to get to the next circle
		this_circ = __extend(slice(circle, jump), slice(circle, 0, jump) ); // redefine the circle last for this iteration
		__draw_spiral(this_circ, center, pitch, size, material);
	);
);



////// Waves ///////

// ---- Utils ----

__wave_get_step(wave, len, current_offset, i) -> (
	wave_pos = wave:(i%len);
	this_step = l(wave_pos:0 + current_offset , wave_pos:1, 0) ;
);

__make_curve(L, A) -> (

	// make first quarter
	start_pos = pos( block(pos(player())) ); // to get integer coords
	x = range(L/4 + 1);
	curve = map(x, l(_, floor(A * sin(360 *_ / L)) ));
	curve = __fill_in(curve);
	
	//add seccond quarter
	quarter_size = curve:(length(curve)-1):0;
	reflected_quarter = map(curve, l(quarter_size * 2 + 1- _:0, _:1) ); 
	reflected_quarter = __reflect(reflected_quarter);
	curve = __extend(curve, reflected_quarter);
	
	//add last half
	half_size = curve:(length(curve)-1):0;
	reflected_half = map(curve, l(half_size * 2 - _:0, - _:1) );
	reflected_half = __reflect(reflected_half);
	delete(reflected_half, 0); // to avoid overlap where they meet
	delete(reflected_half, length(reflected_half)-1); // to avoid overlap with the next bit
	curve = __extend(curve, reflected_half);
);

__fill_in(in_list) -> (
	out_list = l();
	len = length(in_list);
	loop( len ,
	
		out_list:length(out_list) = in_list:_;
		
		if(_ < len, // dont run the last one
			d = in_list:(_+1):1 - in_list:_:1;
			i = _;
			loop( d -1 , 
				out_list:length(out_list) = in_list:i + l(0,_+1);
			);
		);
	);
	out_list;
);


// ---- Draw funtions ----

wave(wavelength, amplitude, size, material) -> (

	dim = player() ~ 'dimension';
	start = __get_center();
	
	wave = __make_curve(wavelength, amplitude);
	lenx = wave:(length(wave) - 1):0; //how long the wave is in one dimension
	len = length(wave);
	fractional_fit = size/lenx; //ammount of times the curve fits in target size
	
	replace_block = __get_replace_block();
	global_this_story = l();
	
	if( material == 'template',
	// make with tempalte
		if(__make_template(), return() ); //tempalte was too big
		offset = map(global_positions:dim:0 - global_positions:dim:1, abs(_)) / 2; //offsets the selection so that it clones it in the center of the block
	
		loop( floor( fractional_fit * len ), 
	
			current_period = floor(_ / len);
			this_step = __wave_get_step(wave, len, current_period * lenx, _);

			__clone_template( start + this_step - offset, replace_block);
		)
		,
	// else, make from block material
		loop( floor( fractional_fit * len ), 
		
			current_period = floor(_ / len);
			this_step = __wave_get_step(wave, len, current_period * lenx, _);
			
			__set_block(start + this_step , material, replace_block);
		);
	);
	
	__put_into_history(global_this_story, dim); //ensure max history size is not exceeded
	print(str('Set %d blocks', length(global_this_story) ));
	return('');
	
);


////// Soft replace ///////

__get_states(b) -> (
	properties = block_properties(pos(b));
	pairs = map(properties, l(_, property(pos(b), _)) );
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
	__set_and_save(b, block(str('%s[%s]', replace, properties_string)) );
);

__replace_one_block(b, to_replace, replace_with) -> (
	if(b == to_replace, 
		__set_with_state(b, replace_with)
	);
);

__replace_one_block_filt(b, to_replace, replace_with, property, value) -> (
	if(b == to_replace && property(b, property)==value, 
		__set_with_state(b, replace_with)
	);
);

soft_replace() -> (
	to_replace = __get_replace_block();
	replace_with = query(player(), 'holds', 'mainhand'):0;

	dim = player() ~ 'dimension';
	if(!global_all_set:dim,
		print(format('rb Error: ', 'y You need to make a selection first' )); 
		return('') // dont paste anything 
	);
	
	global_this_story = l();

	pos0 = global_positions:dim:0;
	pos1 = global_positions:dim:1;

	volume(
		pos0:0, pos0:1, pos0:2,
		pos1:0, pos1:1, pos1:2,
		(
			__replace_one_block(_, to_replace, replace_with)
		);
	);	
	print(global_this_story);
	__put_into_history(global_this_story, dim); //ensure max history size is not exceeded
	print(str('Replaced %d %s blocks with %s', length(global_this_story), to_replace, replace_with ));
);

soft_replace_filt(property, value) -> (
	to_replace = __get_replace_block();
	replace_with = query(player(), 'holds', 'mainhand'):0;

	dim = player() ~ 'dimension';
	if(!global_all_set:dim,
		print(format('rb Error: ', 'y You need to make a selection first' )); 
		return('') // dont paste anything 
	);
	
	global_this_story = l();
	
	print(str('Replacing %s with %s', to_replace, replace_with));
	volume(global_positions:0:0, global_positions:0:1, global_positions:0:2,
		 global_positions:1:0, global_positions:1:1, global_positions:1:2,
		(
			__replace_one_block_filt(_, to_replace, replace_with, property, value)
		);
	);
	
	__put_into_history(global_this_story, dim); //ensure max history size is not exceeded
	print(str('Replaced %d blocks', length(global_this_story) ));
);


////// Settings'n stuff ///////

set_max_template_size(value) -> (
	if( type(value) == 'number' && value > 0, 
		global_settings:'max_template_size' = value;
		print(format(str('b Max tempalte size value set to %s', value) ) ),
		print(format('rb Error: ', 'y Max template size should be a positive number') )
	);
	return('')
);

set_spiral_axis(axis) -> (
	if( ( l('x','y','z')~axis ) == null, 
		print(format('rb Error: ', 'y Axis must be one of ', 'yb x, y ', 'y or ', 'yb z'));
		return('')
	);
	global_settings:'axis' = axis;
	if( axis == 'x',
		__spiral_get_step(circle, perimeter, advance_step, i) ->(
			circle_pos = circle:(i%perimeter);
			step = l(i * advance_step, circle_pos:0, circle_pos:1) ;
		),
		axis == 'y',
		__spiral_get_step(circle, perimeter, advance_step, i) ->( //defaults to axis y
			circle_pos = circle:(i%perimeter);
			step = l(circle_pos:0, i * advance_step, circle_pos:1)
		),
		axis == 'z',
		__spiral_get_step(circle, perimeter, advance_step, i) ->(
			circle_pos = circle:(i%perimeter);
			step = l(circle_pos:0, circle_pos:1, i * advance_step) ;
		),
	);
	print(format(str('b Spirals will now generate along the %s axis', axis) ) );
	return('')
);

set_wave_axis(axis) -> (
	if( ( l('xy', 'xz', 'yx' ,'yz','zx', 'zy')~axis ) == null, 
		print(format('rb Error: ', 'y Axis must be one of ', 'yb xy, xz, yx, yz, zx ', 'y or ', 'yb zy'));
		return('')
	);
	global_settings:'axis' = axis;
	if( axis == 'xy',
		__wave_get_step(wave, len, current_offset, i) -> (
			wave_pos = wave:(i%len);
			this_step = l(wave_pos:0 + current_offset , wave_pos:1, 0) ;
		),
		axis == 'xz',
		__wave_get_step(wave, len, current_offset, i) -> (
			wave_pos = wave:(i%len);
			this_step = l( wave_pos:0 + current_offset , 0, wave_pos:1) ;
		),
		axis == 'yx',
		__wave_get_step(wave, len, current_offset, i) -> (
			wave_pos = wave:(i%len);
			this_step = l( wave_pos:1, wave_pos:0 + current_offset , 0) ;
		),
		axis == 'yz',
		__wave_get_step(wave, len, current_offset, i) -> (
			wave_pos = wave:(i%len);
			this_step = l( 0, wave_pos:0 + current_offset , wave_pos:1) ;
		),
		axis == 'zx',
		__wave_get_step(wave, len, current_offset, i) -> (
			wave_pos = wave:(i%len);
			this_step = l( wave_pos:1, 0, wave_pos:0 + current_offset ) ;
		),
		axis == 'zy',
		__wave_get_step(wave, len, current_offset, i) -> (
			wave_pos = wave:(i%len);
			this_step = l( 0, wave_pos:1, wave_pos:0 + current_offset) ;
		),

	);
	print(format(str('b Waves will now generate along the %s axis and into the %s axis', slice(axis, 0, 1), slice(axis, 1)) ) );
	return('')
);


set_undo_histoy_size(value) -> (
	if( type(value) == 'number' && value > 0, 
		global_settings:'undo_history_size' = value;
		print(format(str('b Max undo value set to %s', value) ) ),
		print(format('rb Error: ', 'y Undo history size should be a positive number') )
	);
	index = length(global_history) - global_settings:'undo_history_size';
	if( index>0 , global_history = slice(global_history, index) );
	return('')
);

toggle_paste_with_air() -> (
	global_settings:'paste_with_air' = !global_settings:'paste_with_air';
	if(global_settings:'paste_with_air',
		print('Template will now be pasted with air'),
		print('Template will now be pasted without air')
	);
	return('')
);

toggle_replace_block() -> (
	global_settings:'replace_block' = !global_settings:'replace_block';
	if(global_settings:'replace_block',
		//if replace blocks
		print( format('b Spiral will now only replce block in your offhand.\n',
			'g Hold bucket for liquids, feather for air, ender eye for end portal, and flint and steel for nether portal.') );
		__set_block(pos, material, replace_block) -> if(block(pos) == replace_block, __set_and_save(pos, material) ),
		//else
		print(format( 'b Spiral will paste completly, replacing whatever is there.') );
		__set_block(pos, material, replace_block) -> __set_and_save(pos, material)
	);
	return('')
);

toggle_slope_mode() -> (
	global_settings:'slope_mode' = !global_settings:'slope_mode';
	if(global_settings:'slope_mode',
		print(format('b Second argument of spiral commands is now slope (in blocks)') ),
		print(format('b Second argument of spiral commands is now pitch(separation between revolutions)') )
	);
	return('')
);

// generate interactive string for togglable parameter
__make_toggle_setting(parameter, hover) -> (
	str_list = l(
		str('w * %s: ', parameter), 
		str('^y %s', hover),
	);
	str_list = __extend(str_list, __get_button('true', parameter) );
	str_list = __extend(str_list, __get_button('false', parameter) );
	print(format(str_list))
);

__get_active_button(value) -> (
	l( str('yb [%s] ', value) )
);

__get_inactive_button(value, parameter) -> (
	l( 
		str('g [%s] ', value),
		str('^gb Click to toggle'),
		str('!/curves toggle_%s', parameter)
	)
);

__get_button(value, parameter) -> (
	bool_val = if(bool(value), global_settings:parameter, !global_settings:parameter);
	if( bool_val, __get_active_button(value), __get_inactive_button(value, parameter) )
);

// generate interactive string for parameter with options
__make_value_setting(parameter, hover, options) -> (
	str_list = l(
		str('w * %s: ', parameter), 
		str('^y %s', hover),
	);
	options_list = l();
	map( options, 
			len = length(options_list);
			options_list:len = str('%sb [%s]', if(global_settings:parameter == _, 'y', 'g',), _);
			options_list:(len+1) = '^bg Click to set this value';
			options_list:(len+2) = str('?/curves set_%s %s', parameter, _) 
	);
	print(format( __extend(str_list, options_list) ))
);

// print all settings
settings() -> (
	print(format( 'b General settings:' ));
	__make_toggle_setting('show_pos', 'Shows markers and outlines selection');
	__make_toggle_setting('paste_with_air', 'Includes air when pasting template');
	__make_toggle_setting('replace_block', 'Spirals will only be generated replacing block in offhand');
	__make_value_setting('max_template_size', 'Limits template size to avoid freezing the game if you mess up the selection', l(20, 100, 1200) );
	__make_value_setting('undo_history_size', 'Sets the maximum ammount of actions to undo', l(10, 100, 500) );
	print(format( 'b Spiral specific settings:' ));
	__make_toggle_setting('slope_mode', 'Defines behaviour of second argument in spiral definitions: slope or pitch');
	__make_value_setting('spiral_axis', 'Axis along which spirals are generated', l('x', 'y', 'z') );
	print(format( 'b Waves specific settings:' ));
	__make_value_setting('wave_axis', 'Axis along which and into which waves are generated', l('xy', 'xz', 'yx' ,'yz','zx', 'zy') );
	return('')
);


////// Undo stuff ///////

__put_into_history(story, dim) -> (
	global_history:dim:length(global_history:dim) = story;
	if(length(global_history:dim) > global_settings:'undo_history_size',
		delete(global_history:dim, 0)
	);
);

__undo(index, dim) -> (
	// iterate over the story backwards
	for(range(length(global_history:dim:index)-1, -1, -1),
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


////// Handle Markers //////

// Spawn a marker
__mark(i, position, dim) -> (
 	colours = l('red', 'lime', 'light_blue'); 
	e = create_marker('pos' + i, position + l(0.5, 0.5, 0.5), colours:(i-1) + '_concrete'); // crete the marker
	run(str( //modify some stuff to make it fancier
		'data merge entity %s {Glowing:1b, Fire:32767s, Marker:1b}', query(e, 'uuid') 
		));
	global_armor_stands:dim:(i-1) =  query(e, 'id'); //save the id for future use
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

	if(global_settings:'show_pos', // remove previous marker for set positi, if aplicable
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
	global_settings:'show_pos' = !global_settings:'show_pos'; 
	if(global_settings:'show_pos',
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
	if(all(slice(global_positions:dim, 0, 2), _!=null), global_all_set:dim = true);
	__render_box();
);

__render_box() -> (
	dim = current_dimension();
	if(global_all_set:dim && global_settings:'show_pos',
		min_pos = map(range(3), min(global_positions:dim:0:_, global_positions:dim:1:_));
		max_pos = map(range(3), max(global_positions:dim:0:_, global_positions:dim:1:_));
		particle_rect('end_rod', min_pos, max_pos + l(1, 1, 1));
		schedule(10, '__render_box')
	);
);

global_positions = m();
global_all_set = m();
global_armor_stands = m();

__reset_positions('overworld');
__reset_positions('the_nether');
__reset_positions('the_end');