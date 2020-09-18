global_tool = 'iron_sword';
__command() -> __help();

__help() -> (
	p = player();
	print(p, '======================');
	print(format('b Welcome to the cover app'));

	print(p, 'Cover x block with y block.');
	print(p, format('w There are four modes available: ', 'b continuous, region, random ', 'w and ', 'b sphere', 'w .'));
	
	print(p, format('w To select the ','by region ','w to affect right and left click with an ',
		str('bg %s ', global_tool),
		'^bg Click to get one!',
		str('!/give %s %s{display:{Name:\'{"text":"Marker","color":"gold"}\',Lore:[\'{"text":"Click to set positions","color":"gray"}\']},Enchantments:[{}]} 1',
			player(), global_tool ),
		'w to slect corners of the volume. Use ', 'b /cover reset_positions ','w to erase selection.'));
		
	print(p, format('w To define size of box around player in ','by continuous ','w mode, use ','b /cover set_size ','w and ','w /cover set_offset',
		'w . Toggle on or off with ','b /cover continuous. '));
	
	print(p, format('w To use ', 'yb sphere ', 'w mode, simply run' , 'b /cover sphere r', 'w , where r is the decired radius.'));
	print(p, 'Place block to cover in offhand and block to cover with in main hand.');
	print(p, 'To create a list of block pairs, put shulker box in each hand. Items in corresponding slots will make cover pairs.');
	
	print(p, format('w To use ','yb random ', 'w mode, select an area and run ', 'b /cover random ', 'w or ', 'b /cover random_cover', 'w .'));
	print(p, 'They have very similar behaviour. To select the random palette and weights, hold a shulker box in the main hand.');
	print(p, 'The ammount of slots with each block type will define the weight of each item.');
	print(p, 'Hold a single item shulker with items in the offhand to filter over what blocks to place the random pattern.');
	
	print(p, 'Undo the last n actions you did with /cover undo n. Use big numbers for continuos mode.');
	print(p, 'For lava, water, air, en portal or nether portal, use lava buckets, water buckets feathers, flint and steel or ender eyes as items in the boxes or offhand.');

	print(p, '');
	print(p, format('g Cover app by Firigion'));
	print(p, '' );
);

////// Make pairs

__make_pairs(player) -> (
	mainhand = query(player, 'holds', 'mainhand');
	offhand = query(player, 'holds', 'offhand');

	if(mainhand:0~'shulker_box' && offhand:0~'shulker_box' && mainhand:2 && offhand:2, 
		//if both are initialized shulkers, look at the contents
		__make_shulker_pairs(mainhand:2, offhand:2),
		//else look at the items themselves
		global_pairs = {__process_alias(offhand:0) -> __process_special_cases(__process_alias(mainhand:0))}
	);
);

__make_shulker_pairs(mainhand_data, offhand_data) -> (
	mainhand_list = __get_item_list(mainhand_data);
	offhand_list = __get_item_list(offhand_data);

	global_pairs = {};
	loop( 27,
		if(mainhand_list:_ != null, global_pairs:(offhand_list:_) = __process_special_cases(mainhand_list:_) ) 
	);
);

global_aliases = {
						'water_bucket' -> 'water' ,
						'lava_bucket' -> 'lava' ,
						'feather' -> 'air' ,
						'ender_eye' -> 'end_portal' ,
						'flint_and_steel' -> 'nether_portal',
						'redstone' -> 'redstone_wire',
						'wheat_seeds' -> 'wheat',
						'beetroot_seeds' -> 'beetroots',
						'melon_seeds' -> 'melon_stem',
						'pumpkin_seeds' -> 'pumpkin_stem',
						'sweet_berries' -> 'sweet_berry_bush',
						'carrot' -> 'carrots',
						'potato' -> 'potatoes',
						
					};

__get_item_list(box_data) -> (
	item_tuple_list = parse_nbt(box_data:'BlockEntityTag':'Items');
	// empty list of 27 elements
	item_list = map(range(27), null);
	// fill with items in their slots
	for(item_tuple_list,
		item = _:'id' - 'minecraft:';
		item_list:(_:'Slot') = __process_alias(item)
	);
	return(item_list)
);

__process_alias(item) -> (
	if(global_aliases:item, item=global_aliases:item);
	return(item)
);
// Uses lists of lists in case some block needs more properties
global_special_cases = {
	'_button' -> [['face', 'floor']],
	'sea_pickle' -> [['waterlogged', 'false']],	
	'snow' -> [['layers', 2]],
};
__process_special_cases(block_name) -> (
	for(keys(global_special_cases), if(block_name~_, key = _) );
	// if block_name was in keys
	print(key);
	if(key,
		// parse properties
		props_str = join(',', map(global_special_cases:key, str('%s="%s"', _:0, _:1)) );
		return( block(str('%s[%s]', block_name, props_str)) ),
		//else, return the block
		return(block_name)
	);
);


////// Genearl utils

__set_and_save(pos, material) -> ( //defaults to no replace
	global_this_story:length(global_this_story) = [pos, block(pos)];
	set(pos , material);
);

__place_if(block) -> (
	cover = global_pairs:str(block);
	if(cover != null,
		p = pos(block) + [0,1,0];
		if(air(p) && __check(block), __set_and_save(p, cover))
	);
);

__check(block) -> (
	if(	
		block ~ '_slab', return(property(block, 'type')=='top'),
		block ~ '_stairs', return(property(block, 'half')=='top'),
		// for any other block, true
		return(true)
	)
);

////// Continuous mode

global_box_halfsize = [10, 4, 10];
global_box_offset = 3;

// Set commands for global mox size
set_size(dx, dy, dz) -> global_box_halfsize = [dx, dy, dz]/2;
set_offset(offset) -> global_box_offset = offset;


__draw_box(player) -> (
	pos = player ~ 'pos';
	from = global_box_halfsize  + 0.5 - [0, global_box_offset + global_box_halfsize:1 -0.5 , 0];
	to = -1 * global_box_halfsize - 0.5- [0, global_box_offset + global_box_halfsize:1, 0];
	draw_shape('box', 2, 'color', 0x059915F0, 'fill', 0x05991550, 'from', from, 'to', to, 'follow', player)
);

global_continous_on = false;
continuous() -> (
	// togle state
	global_continous_on = !global_continous_on;
	if(global_continous_on, 
		p = player();
		dim = p ~ 'dimension';
		__make_pairs(p);
		__cover_player(p, dim) 
	);
	return('')
);

// Cover around the player
__cover_player(player, dim) -> (
	if(global_continous_on,
		global_this_story = [];
		result = scan( pos(player) - [0, global_box_offset + global_box_halfsize:1, 0], global_box_halfsize, __place_if(_));
		if(result, __put_into_history(global_this_story, dim) );

		__draw_box(player);
		schedule(1, '__cover_player', player, dim);
	);
);

// Turn off in a bunch of cases
__on_player_switches_slot(player, from, to) -> global_continous_on = false;

__on_player_swaps_hands(player) -> global_continous_on = false;

__on_player_disconnects(player, reason) -> global_continous_on = false;

__on_player_changes_dimension(player, from_pos, from_dimension, to_pos, to_dimension) -> global_continous_on = false;

__on_player_dies(player) -> global_continous_on = false;


////// Volume mode

region() -> (
	p = player();
	__make_pairs(p);
	
	if(global_all_set,
		task('__cover_region', p),
		print(format('rb Error: ', 'y You must select a region to cover first. Use an iron sword.') );
	);
	return('');
);

__cover_region(player) -> (
	dim = player ~ 'dimension';
	global_this_story = [];

	print(player, 'Covering...');
	result = volume(global_positions:dim:0, global_positions:dim:1, __place_if(_));
	print(player, 'Covered ' + result  + ' blocks');

	__put_into_history(global_this_story, dim);
);

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

// set position 1 if player left clicks with a golden sword
__on_player_clicks_block(player, block, face) -> (
	if(query(player(), 'holds'):0 == global_tool,
		__set_pos(1);
	);
);

// set position 2 if player right clicks with a golden sword
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


////// Random pattern

// builds off of volume mode's selection

random() -> (
	[mainhand_list, offhand_map] = __random_prepair();
	task('__random_place', player(), mainhand_list, offhand_map, 'fill'),
);


random_cover() -> (
	[mainhand_list, offhand_map] = __random_prepair();
	task('__random_place', player(), mainhand_list, offhand_map, 'cover'),
);

__random_prepair() -> (
	p = player();
	mainhand = query(p, 'holds', 'mainhand');
	offhand = query(p, 'holds', 'offhand');

	// return if mainhand is not a box
	if(!(mainhand:0~'shulker_box') || !	mainhand:2, 
		print(format('rb Error: ', 'y Need an initialized shulker to make a random pattern.'));
		return('')
	);
	
	// get all the non empty slots
	mainhand_list = map(__get_item_list(mainhand:2), if(_, _, continue()));
	offhand_map = {};

	if(offhand,
		// if it's an initialized shulker box, create the list, else just the item
		if(offhand:0~'shulker_box' && offhand:2, 
			for(__get_item_list(offhand:2), offhand_map:_=null),
			offhand_map = {offhand:0}
		);
	);
	
	return([mainhand_list, offhand_map])	
);

__random_place(player, mainhand_list, offhand_map, mode) -> (
	dim = player ~ 'dimension';
	global_this_story = [];
	len = length(mainhand_list);
	print(offhand_map);

	if(!global_all_set,
		print(format('rb Error: ', 'y You must select a region to randomize first. Use an iron sword.') );
		return('');
	);

	print(player, 'Randomizing...');
	if(mode=='fill',
		if(offhand_map,
			// place only over items in the map
			result = volume(global_positions:dim:0, global_positions:dim:1, 
				if(has(offhand_map, str(block(pos_offset(_, 'down', 1))) ), __set_and_save(_, mainhand_list:floor(rand(len))) )
			),
			// place everywhere
			result = volume(global_positions:dim:0, global_positions:dim:1, __set_and_save(_, mainhand_list:floor(rand(len))) );
		),
		
		mode=='cover',
		list_to_place = [];
		if(offhand_map,
			// place only over items in the map
			volume(global_positions:dim:0, global_positions:dim:1, 
				p = pos_offset(_, 'up', 1);
				if(has(offhand_map, str(_) ) && air(p) && __check(_), list_to_place:length(list_to_place) = p );
			);
			result = for(list_to_place, __set_and_save(_, mainhand_list:floor(rand(len))) )
			,
			// place everywhere
			volume(global_positions:dim:0, global_positions:dim:1,
				p = pos_offset(_, 'up', 1);
				if(air(p) && __check(_) && !air(_),  list_to_place:length(list_to_place) = p );
			);
			result = for(list_to_place, __set_and_save(_, mainhand_list:floor(rand(len))) )
		)	
	);
	print(player, 'Placed ' + result  + ' blocks');

	__put_into_history(global_this_story, dim);
);

////// Sphere mode

shpere(radius) -> (
	p = player();
	__make_pairs(p);
	task('__cover_sphere', p, radius);
	return('');
);

__cover_sphere(player, radius) -> (
	dim = player ~ 'dimension';
	center = player~'pos';
	global_this_story = [];

	print(player, 'Covering...');
	result = scan(
		center, radius, radius, radius,
		if(__sq_distance(center, pos(_))<= radius*radius, __place_if(_))  
	);
	print(player, 'Covered ' + result  + ' blocks');

	__put_into_history(global_this_story, dim);
);

__sq_distance(p1, p2) -> reduce(p1-p2, _a+_*_, 0);


////// Undo

global_undo_history_size = 12000; // 10 minutes worth of ticks to undo in continuous mode
global_history = {
					'overworld' -> [] ,
					'the_nether' -> [] ,
					'the_end' -> [] ,
				};

__put_into_history(story, dim) -> (
	global_history:dim:length(global_history:dim) = story;
	if(length(global_history:dim) > global_undo_history_size,
		delete(global_history:dim, 0)
	);
);

__undo(index, dim) -> (
	// iterate over the story backwards
	print('Undoing');
	for(range(length(global_history:dim:index)-1, -1, -1),
		set(global_history:dim:index:_:0, global_history:dim:index:_:1); // (position, block) pairs
	);
	// remove used story
	delete(global_history:dim, index);
);

undo(num) -> (
	//check for valid input
	if( type(num) != 'number' || num <= 0, 
		print(format('rb Error: ', 'y Need a positive number of steps to undo'));
		return('')
	);

	p = player();
	dim = p ~ 'dimension';
	
	index = length(global_history:dim)-num;
	if(index<0, 
		print(format('rb Error: ', str('y You only have %d actions to undo available', length(global_history:dim) ) )),
		task('__undo_asynch', num, length(global_history:dim)-1, dim, p )
	);
	return('')	
);

__undo_asynch(ammount, index, dim, player) -> (
	print(player, str('Undoing the last %d actions or ticks', ammount));
	loop(ammount, __undo(index, dim) );
	print(player, 'Done')
);
