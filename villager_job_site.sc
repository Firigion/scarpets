import('math','distance');//Doesn't actually work until gnembon accepts https://github.com/gnembon/fabric-carpet/pull/289, till then just get fainter lines cos below resolves to null fro unknown func

__config() -> m( l('stay_loaded', 'true'),l('scope', 'global'));
__on_player_uses_item(player, item_tuple, hand)->(
    if(item_tuple:0=='blaze_rod',
        entity=query(player,'trace',20,'entities')||null;
        if(type(entity)=='entity'&&entity~'type'=='villager',
            brain= entity~'nbt':'Brain';
            mem=brain:'memories';
            pos=mem:'"minecraft:job_site"':'value':'pos'||mem:'"minecraft:job_site"':'pos'||null;
            if(!pos,return());
            l(x,y,z)=nbt(pos):'[]';
            particle_line('happy_villager',pos(entity),pos(player),if(distance(pos(entity),pos(player))<2,0.1,1));
            particle_line('happy_villager',pos(entity),l(x+0.5,y+0.5,z+0.5),if(distance(pos(entity),l(x+0.5,y+0.5,z+0.5))<2,0.1,1));
        ),
    )
)