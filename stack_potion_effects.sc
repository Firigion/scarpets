// No instant health, instant damage (it doesn't make sense to make them stack), or turtle master (need to rewrite stuff)
global_potion_properties = { 'night_vision' -> {	'name' -> 'night_vision',
													'normal' -> {'duration'-> 3600, 'amplifier' -> 0 }, 
													'long' -> {'duration'-> 9600, 'amplifier' -> 0 } 
												},
							 'invisibility' -> {	'name' -> 'invisibility',
													'normal' -> {'duration'-> 3600, 'amplifier' -> 0 }, 
													'long' -> {'duration'-> 9600, 'amplifier' -> 0 } 
												},
							 'leaping' -> {			'name' -> 'jump_boost',
													'normal' -> {'duration'-> 3600, 'amplifier' -> 0 }, 
													'long' -> {'duration'-> 9600, 'amplifier' -> 0 } ,
													'strong' -> {'duration'-> 1800, 'amplifier' -> 1 } 
												},
							 'fire_resistance' -> {	'name' -> 'fire_resistance',
													'normal' -> {'duration'-> 3600, 'amplifier' -> 0 }, 
													'long' -> {'duration'-> 9600, 'amplifier' -> 0 } 
												},
							 'speed' -> {			'name' -> 'speed',
													'normal' -> {'duration'-> 3600, 'amplifier' -> 0 }, 
													'long' -> {'duration'-> 9600, 'amplifier' -> 0 } ,
													'strong' -> {'duration'-> 1800, 'amplifier' -> 1 } 
												},
							 'slowness' -> {		'name' -> 'slowness',
													'normal' -> {'duration'-> 1800, 'amplifier' -> 0 }, 
													'long' -> {'duration'-> 4800, 'amplifier' -> 0 } ,
													'strong' -> {'duration'-> 400, 'amplifier' -> 3 } 
												},
							 'water_breathing' -> { 'name' -> 'water_breathing',
													'normal' -> {'duration'-> 3600, 'amplifier' -> 0 }, 
													'long' -> {'duration'-> 9600, 'amplifier' -> 0 } ,
												},
							 'poison' -> {			'name' -> 'poison',
													'normal' -> {'duration'-> 900, 'amplifier' -> 0 }, 
													'long' -> {'duration'-> 1800, 'amplifier' -> 0 } ,
													'strong' -> {'duration'-> 420, 'amplifier' -> 1 } 
												},
							 'regeneration' -> {	'name' -> 'regeneration',
													'normal' -> {'duration'-> 900, 'amplifier' -> 0 }, 
													'long' -> {'duration'-> 1800, 'amplifier' -> 0 } ,
													'strong' -> {'duration'-> 440, 'amplifier' -> 1 } 
												},
							 'strength' -> {		'name' -> 'strength',
													'normal' -> {'duration'-> 3600, 'amplifier' -> 0 }, 
													'long' -> {'duration'-> 9600, 'amplifier' -> 0 } ,
													'strong' -> {'duration'-> 1800, 'amplifier' -> 1 } 
												},
							 'weakness' -> {		'name' -> 'weakness',
													'normal' -> {'duration'-> 900, 'amplifier' -> 0 }, 
													'long' -> {'duration'-> 4800, 'amplifier' -> 0 } 
												},
							 'luck' -> {			'name' -> 'luck',
													'normal' -> {'duration'-> 900, 'amplifier' -> 0 }, 
												},
							 'slow_falling' -> {	'name' -> 'slow_falling',
													'normal' -> {'duration'-> 900, 'amplifier' -> 0 }, 
													'long' -> {'duration'-> 4800, 'amplifier' -> 0 } 
												},
							};
		
global_apply_potion = false;					

// query effect when player starts using potion
__on_player_uses_item(player, item_tuple, hand) -> (
	// Skip any non potion items
	if(item_tuple:0 != 'potion', return('') );
	
	//obtain potion effect and modifiers
	name = item_tuple:2:'Potion' - 'minecraft:' - '"';
	if( name~'strong' != null, 
			type = 'strong';
			name = name-'strong_',
		name~'long' != null,
			type = 'long';
			name = name-'long_',
		//else
			type = 'normal'
	);

	// if potion is supported, save the effects, if needed
	if(global_potion_properties~name != null, save_effects(player, name, type, amplifier));
);

// apply effects after they finish using potion						
__on_player_finishes_using_item(player, item_tuple, hand) -> (
	// Skip any non potion items or potions that don't need to be stacked
	if(item_tuple:0 == 'potion' && global_apply_potion, modify_effects(player) );
);

// saves current potion properties, if aplicable
save_effects(player, name, type, amplifier) -> (
	// query for the potion effect
	current_effect = query(player, 'effect', global_potion_properties:name:'name');
		
	if(
		// potion matches an existing effect
		current_effect!=null &&
		// amplifiers match
		current_effect:0 == global_potion_properties:name:type:'amplifier' &&
		// and effect will be active after potion is drunk (it takes 32gt)
		current_effect:1 > 32,
			// save the data
			global_current_effect = current_effect;
			global_type = type;
			global_name = name;
			global_apply_potion = true,
		// else reset values (in case previous potion wasn't fully drunk)
			reset_values();
	);
);

// updates player effects
modify_effects(player) -> (
	modify(player, 'effect', 
		global_potion_properties:global_name:'name', 
		global_current_effect:1 + global_potion_properties:global_name:global_type:'duration' - 32,
		global_potion_properties:global_name:global_type:'amplifier'
	);
	
	reset_values();
);

reset_values() -> (
	global_current_effect = null;
	global_type = null;
	global_name = null;
	global_apply_potion = false;
);