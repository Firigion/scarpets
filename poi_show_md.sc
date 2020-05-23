__command() -> print('command');


global_range = 40;
global_refresh_rate = 5;
global_markers = m( l('nether', m()), l('overworld', m()) );

__put_marker(pos, dim) ->(
	// Create marker at position and give it useful properties and a tag
	e = create_marker('', pos + l(0.5, 0.5, 0.5), 'purple_stained_glass');
	run(str('data merge entity %s {Glowing:1b, Fire:32767s}', query(e, 'uuid')));
	modify(e, 'tag', 'nether_poi_marker');
	// Add the marker to the global marker list
	global_markers:dim:pos = e;
);

__update_markers(player_list, dim) -> (
	// I'll use the keys of the map as a set
	poi_map = m();
	// For each player, check nether portal pois areound them and put it's coords into the set
	for(player_list, map(filter(poi(pos(_), global_range), (_~ 'nether_portal') == 0), poi_map:(_:2) = null) );
	pois = keys(poi_map);
	// Remove existing markers that were not found when refreshing list
	map( filter( keys(global_markers:dim), pois~_ ==null) , 
			modify(global_markers:dim:_, 'remove');
			delete(global_markers:dim, _)
	);
	// Add marker for new pois
	map( filter(pois, !has(global_markers:dim, _)) , __put_marker(_, dim) );
);

__remove_markers(dim) -> (
	// look for all markers that are nether portal pois
	marker_list = entity_selector('@e[tag=nether_poi_marker]');
	//and remove them
	map(marker_list, modify(_, 'remove'));
	global_markers:dim = m();
);


__on_tick() -> (
	if(!(tick_time%global_refresh_rate) && player('*'), 
		// check for players with ender eyes
		player_list = filter(player('*'), query(_, 'holds', 'mainhand'):0 == 'ender_eye' || query(_, 'holds', 'offhand'):0 == 'ender_eye');
		// if any players found
		if(player_list, 
			// then update markers around them
			__update_markers(player_list, 'overworld'),
			// else, delete all remaining markers, if there are any
			if(global_markers, __remove_markers('overworld') )
		)
	)
);


__on_tick_nether() -> (
	if(!(tick_time%global_refresh_rate) && player('*'), 
		// check for players with ender eyes
		player_list = filter(player('*'), query(_, 'holds', 'mainhand'):0 == 'ender_eye' || query(_, 'holds', 'offhand'):0 == 'ender_eye');
		// if any players found
		if(player_list, 
			// then update markers around them
			__update_markers(player_list, 'nether'),
			// else, delete all remaining markers, if there are any
			if(global_markers, __remove_markers('nether') )
		)
	)
);


// Some set commands, not fool proofed. Be sensible when setting values.
set_refresh_rate(val) -> global_refresh_rate = val;
set_range(val) -> global_range = val;
// Set a nether portal poi without the block
portalles_poi() -> set_poi(pos(query(player(), 'trace', 'blocks')), 'nether_portal');
// Remove whatever poi you are looking at
remove_poi() -> set_poi(pos(query(player(), 'trace', 'blocks')), null);