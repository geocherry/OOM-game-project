package com.oom.game.main.environment.blocks;

import com.oom.game.main.entities.Entity;
import com.oom.game.main.environment.utils.Block;
import com.oom.game.main.environment.utils.OpenTop;
import com.oom.game.main.environment.utils.Walkable;

/*
    Notice that grass block is not stackable. This means that it can only be used as floor (z-coord = 0), which makes sense
    Grass can't grow on any other blocks.
    But you can put other blocks on it
 */
public class Grass extends Block implements Walkable, OpenTop {
    //FIXME add this interface to UML
    private Entity hiddenEntity; //might be redundant, added this member just for example (some item mey be hidden in the grass)
    /*
        this is DISGUISTING. Might need to create a seperate class that implements OpenTop
        and should extend from that class. ATM it's not essential
     */
    private Block blockOnTop;

    /**
     * @param texture {@link Block}
     * @param hiddenEntity the entity (item in most cases) that is hidden in the grass. It gets revealed after you walk on the grass
     * @param blockOnTop the block that sits on top of this grass block
     */
    Grass(int texture, Entity hiddenEntity, Block blockOnTop){
        super(texture);
        this.hiddenEntity = hiddenEntity;
        this.blockOnTop = blockOnTop;
    }

    /**
     *
     * {@link Walkable}
     */
    @Override
    public int getBaseWalkingSpeed() {
        return Walkable.DEFAULT_WALKING_SPEED;
    }

    /**
     * {@link OpenTop}
     */
    @Override
    public Block getBlockOnTop() {
        return blockOnTop;
    }
}