// item list to fill shulker with:
global_item_list = ['dirp', 'Stone', 'glass', 'ecc', 'grass_block', 'bricks', 'stone_bricks', 'poppy'];
global_item_list = map(global_item_list, lower(_));

// global settings
global_safe_set = false; // if true, only set blocks if old block is air; if false, set regarless
global_1st_slot_fill = 41; // a number that will be used in min(stack_size, number)
global_hopper_ss_level = 3; // signal strength level the hoppers should have when it revices one more item
global_chest_ss_level = 2; // signal strength level the encoder chest has, such that if you remove one item, it goes down
global_file_type = 'text';

// offset the second half of the chest has to be placed into to make it double
global_offset = {'south'->[-1, 0, 0], 'west'->[0,0, -1], 'north'->[1, 0, 0], 'east'->[0, 0, 1]};


// check item list
_test_items(list) -> (
    wrong = [];
    out = map(list, 
            try(
                stack_limit(_);
                _,
//catch
            'unknown_item',
            wrong += [_, _i];
            continue()            
            )
        );
    if(wrong, print(player(), 'Ignored:'); for(wrong, print(player(), str('  %d: %s', _:1, _:0))));
    out
);
global_item_list = _test_items(global_item_list);


get_offset() -> (
    facing = query( player(), 'facing');
    if( (global_offset ~ facing)== null, facing = query( player(), 'facing', 1));
    [facing, global_offset:facing];
);

get_nbt(item) -> (
    slot_map(i, outer(item)) -> {'Slot'->i, 'id'->str('minecraft:%s', item), 'Count'->stack_limit(item)};
    nbt_map = {'BlockEntityTag' -> {'Items'-> map(range(27), slot_map(_))}};
    nbt = encode_nbt(nbt_map);
);

__command() -> null;
__config() -> {
    'commands'->{
        'bin_barrel <int>'->'set_bin_barrels',
        'hopper_from_file <file>'->['container_form_file', 'hopper_ss', 'hopper'],
        'hopper_full_from_file <file>'->['container_form_file', 'hopper_double', 'hopper'],
        'chest_from_file <file>'->['container_form_file', 'chest_singles', 'chest'],
        'chest_shulkers_from_file <file>'->['container_form_file', 'chest_shulkers', 'chest'],
        'ss' -> _() -> print(p = player(), inventory_ss(p~'trace')),
        'ss <pos>' -> _(p) -> print(player(), inventory_ss(p)),
        'set_file_type <filetype>' -> _(ft) -> (global_file_type = ft; print(player(), 'Set file type to ' + ft) ),
        'set_hopper_ss <ss>' -> _(ss) -> (global_hopper_ss_level = ss; print(player(), 'Set hopper signal strength level to ' + ss) ),
        'set_chest_ss <ss>' -> _(ss) -> (global_chest_ss_level = ss; print(player(), 'Set chest signal strength level to ' + ss) ),
        'set_fst_slot_fill_level <filllevel>' -> _(fl) -> (global_1st_slot_fill = fl; print(player(), 'Set fill level of first slot of hoppers to ' + fl) ), 
        'set_safe_mode <bool>' -> _(b) -> global_safe_set = b,
        'list_files' -> _() -> for(list(), print(player(), str('%s (%s file)', _, global_file_type))),
        'fill_ss <overloaded>' -> ['fill_inventory', null],
        'fill_ss <overloaded> <pos>' -> 'fill_inventory',
        'stacked_shulkers_chest' -> 'stacked_shulker_chest',
        //'list_from_world <fst_pos>, <scnd_pos>' -> 'scan_world'
        },
    'arguments' -> {
            'file' -> {'type'->'term', 'suggester'-> _(trash)-> list()},
            'filetype' -> {'type'->'term', 'options'->['text', 'json']},
            'filllevel' -> {'type'->'int', 'min'->1, 'max'->64, 'suggest'->[1, 16, 64]},
            'ss' -> {'type'->'int', 'min'->1, 'max'->15, 'suggest'->[1,3]},
            'overloaded'-> {'type'->'int', 'min'->1, 'max'->897, 'suggest'->[1,3]},
        }
};

list() -> map(list_files('item_lists', global_file_type), split('/', _):1);

////// For chests ///////
set_double_chest(position, facing, offset) -> (
    if(!global_safe_set || (global_safe_set && air(position) && air(position+offset))   , 
        if(!global_safe_set, set(position, 'stone');, set(position+offset, 'stone'));
        set( position, 'chest', 'type', 'left', 'facing', facing);
        set( position + offset, 'chest', 'type', 'right', 'facing', facing)
    )
);

fill_chest(item, position, type) -> (
    if(type=='chest_shulkers',
        nbt = get_nbt(item);
        loop(54, inventory_set(position, _,  1, 'shulker_box', nbt)),
        type=='chest_singles',
        loop(54, inventory_set(position, _,  stack_limit(item), item))
    );  
);

set_and_fill_chest(item, position, facing, offset, type) -> (
    success = set_double_chest(position, facing, offset);
    if(success, 
        fill_chest(item, position, type);   
    )
);

set_and_fill_all_chests(position, type, extra) -> (
    [facing, offset] = get_offset();
    if(type~'encoder',
        for(list_files('encoders', global_file_type),
            pos = pos_offset(position, facing, _i);
            set_double_chest(pos, facing, offset);
            fill_encoder_chest(_, pos);
            game_tick(50);
        ),
        //else, not encoder
        list = get_list(extra);
        for(list,
            set_and_fill_chest( _, pos_offset(position, facing, _i), facing, offset, type);
            game_tick(50);
        );  
    )
);

// Encoder chest stuff

inventory_ss(pos) -> (
    inv_size = inventory_size(pos);
    items = map(range(inv_size), item = inventory_get(pos, _); if(item==null, continue()); [item:0, item:1]);
    r = floor(14/inv_size * reduce(items, 
        (_:1)/min(64, stack_limit(_:0)) + _a,
        0)+ min(1, length(items)));
);

fill_encoder_chest(file, pos) -> (
    list = _test_items(read_file(file, global_file_type));
    if(length(list)>54, 
        print(player(), str('List too long, ignoring the last %s entries for chest at %s', length(list)-54, pos) );
        list = slice(list, 0, 54);
    );
    for(list,
        inventory_set(pos, _i, 2, _);
        last = _i;
    );
    if(last<53, loop(53-last, inventory_set(pos, _+last+1, 2, 'sea_pickle', '{display:{Name:\'{"text":"╚═Dummy═╝"}\'}}')));
    while(ss<global_chest_ss_level, is = inventory_size(pos),
        slot = is-_-1;
        item_tuple = inventory_get(pos, slot);
        while(inventory_ss(pos) < global_chest_ss_level, stack_limit(item_tuple:0)-item_tuple:1,
            inventory_set(pos, slot, item_tuple:1+1, item_tuple:0, item_tuple:2);
            item_tuple:1 += 1;
        )
    )
);

fill_inventory(target_ss, pos) -> (
    if(pos==null, 
        if( (look_at=query(player(), 'trace', 5, 'blocks'))==null,
            print(player(), format('rb You need to be looking at an inventory')); exit(),
            pos = pos(look_at);
        );
    );

    while(inventory_ss(pos) <target_ss, inventory_size(pos),
        slot = _;
        while(inventory_ss(pos) < target_ss, 64,
            inventory_set(pos, slot, _+1, 'totem_of_undying', '{display:{Name:\'{"text":"╚═Dummy═╝"}\'}}');
        )
    );

    if( (ss = inventory_ss(pos))>target_ss,
        item_tuple = inventory_get(pos, slot);
        inventory_set(pos, slot, item_tuple:1 - 1, item_tuple:0, item_tuple:2);
        
        if(item_tuple:1 > 1, slot +=1);
        if(slot > inventory_size(pos), print(player(), 'Reached end of the inventory with signal strength ' + ss + '. There\'s no more space.'));

        while(inventory_ss(pos) < target_ss, 64,
            inventory_set(pos, slot, _+1, 'sea_pickle', '{display:{Name:\'{"text":"╚═Dummy═╝"}\'}}');
        )
    );
);

////// For barrels ///////

set_barrel(position) -> (
    if( (global_safe_set && air(position)) || !global_safe_set,
        if(!global_safe_set, set(position, 'stone'));
        set(position, 'barrel')
    );
);

set_and_fill_barrel(number, pos, max) -> (
    success = set_barrel(pos);
    if(success,
        loop(27, 
            inventory_set(pos, _, 64, 'acacia_button', str('{display:{Name:\'{"text":"%d"}\'}}', n=_+number*27+1));
            if(n==max, exit());
        );
    );
);

set_bin_barrels(number) -> (
    [facing, offset] = get_offset();
    position = player()~'pos';
    count_chests = ceil(number/27);
    loop(count_chests,
        set_and_fill_barrel( _, pos_offset(position, facing, _), number);
        game_tick(50);
    );
);

////// For hoppers ///////
set_hopper_inv(pos, slot, count) -> inventory_set(pos, slot, count, 'sea_pickle', '{display:{Name:\'{"text":"╚═Dummy═╝"}\'}}');

set_hopper(position, orientation) -> (
    if( (global_safe_set && air(position)) || !global_safe_set,
        if(!global_safe_set, set(position, 'stone'));
        set(position, 'hopper', 'facing', orientation)
    );
);

fill_hopper_double(item, position) -> ( // for doube speed sprters
    loop(5, inventory_set(position, _, stack_limit(item), item) )
);

fill_hopper_ss(item, position) -> ( // for sorters with set ss
    inventory_set(position, 0, min(sl=stack_limit(item), global_1st_slot_fill), item);
    loop(4, set_hopper_inv(position, _+1, 1) );
    set_hopper_inv(position, 1, max(1, get_count_for_stack_size(sl)))
);

set_and_fill_hopper(item, position, orientation, type) -> (
    success = set_hopper(position, orientation);
    if(success,
        if(
            type == 'hopper_double',
            fill_hopper_double(item, position),
            type == 'hopper_ss',
            fill_hopper_ss(item, position),
        )
    )
);

set_and_fill_all_hoppers(position, orientation, type, extra) -> (
    [facing, offset] = get_offset();
    list = get_list(extra);
    for(list,
        set_and_fill_hopper( _, pos_offset(position, facing, _i), orientation, type);
        game_tick(50);
    );  
);

// Signal strengh hopper stuff
signal_strength(stack_size, count_2nd_slot) -> (
    inv_size = 5; // hopper
    floor(14/inv_size * (global_1st_slot_fill / stack_size + count_2nd_slot/64  + 3/64) + 1)
);

get_count_for_stack_size(stack_size) -> (
    ss = 1;
    count_2nd_slot = 1;
    while( ss < global_hopper_ss_level, 64, //no more than 64 iterations
        ss = signal_strength(stack_size, count_2nd_slot);
        count_2nd_slot += 1;
    );
    count_2nd_slot - 2
);

////// Placement ///////

__on_player_places_block(player, item_tuple, hand, block) -> (
    // Place chest chain when correct chest is palced
    if( item_tuple:0 == 'chest' && (tt = item_tuple:2:'Type') != null ,
        set(block, 'air'); // to remove named item placed
        //set chests filled with items or boxes
        set_and_fill_all_chests(pos(block), tt, item_tuple:2:'Extra'),
        // else, place hopper chain when correct hopper is placed
        item_tuple:0 == 'hopper' && (tt = item_tuple:2:'Type') != null  ,
        facing = block_state(block, 'facing'); //save facing direction
        set(block, 'air'); // to remove named item placed
        //set hoppers for souble or single speed sorters
        set_and_fill_all_hoppers(pos(block), facing, tt, item_tuple:2:'Extra'),
    )
);

give_item(item, type, lore, extra) -> (
    run(str('/give %s %s{display:%s,Enchantments:[{}], Type:%s, Extra:%s} 1', player(), item, get_display(lore), type, extra ));
);

get_display(lore) -> {  'Name' -> '\'{"text":"Place me","color":"dark_purple","bold":true}\'',
                        'Lore' -> lore
                    };
                        
global_lores = {                        
    'chest_shulkers'-> ['\'{"text":"Double chests full of boxes","color":"gold"}\''],
    'chest_singles' -> ['\'{"text":"Double chests full of items","color":"aqua"}\''],
    'chest_encoder' -> ['\'{"text":"Double chests configured for hex encoders","color":"red"}\''],
    'hopper_double' -> ['\'{"text":"Hopper full of single item type","color":"green"}\''],
    'hopper_ss' -> ['\'{"text":"Signal strength defined sorter","color":"light_purple"}\''],
};

for( ['chest_shulkers', 'chest_singles', 'chest_encoder', 'hopper_double', 'hopper_ss'],
    give_item(split('_',_):0, _, global_lores:_, null)
);

// Containers from file
global_individual_item_lists = {};
container_form_file(file, type, container) -> (
    items_list = read_file('item_lists/'+file, global_file_type);
    items_list = _test_items(items_list);
    global_individual_item_lists:file = items_list;

    file_lore = str('\'{"text":"generates from file: %s","color":"white"}\'', file);
    lore = global_lores:type;
    lore += file_lore;
    give_item(container, type, lore, file)
);

get_list(file) -> (
    list = if(file=='null', global_item_list, global_individual_item_lists:str(file));
    if(!list, print(player(), format('r Item list is not loaded, try running the get hopper commands, maybe it helps.')); exit());
    list;
);

stacked_shulker_chest() -> (
    pos = player()~'pos';
    set(pos, 'chest');
    loop(27, inventory_set(pos, _, 64, 'white_shulker_box'))
);


//scan_world(pos1, pos2) -> (
//    result_set = {};
//    ignored_set = {};
//    volume(pos1, pos2,
//        try(
//            stack_limit(_);
//            result_set += str(_)
//        //catch
//        'unknown_item',
//            ignored += str(_);
//        );
//    );
//    global_individual_item_lists:'wrold' = keys(result_set);
//
//
//    file_lore = '\'{"text":"generates from world scan","color":"white"}\'';
//    lore = global_lores:type;
//    lore += file_lore;
//    give_item(container, type, lore, file)
//
//
//);
