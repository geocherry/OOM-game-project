package com.oom.game.main.process.render;

import com.oom.game.main.environment.Position;
import com.oom.game.main.environment.blocks.utils.BlockTextures;
import com.oom.game.main.environment.utils.Block;
import com.oom.game.main.process.render.NodeRenderable;
import com.oom.game.main.utils.GameObservable;
import com.oom.game.main.utils.GameObserver;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BlockRenderable extends NodeRenderable {
    /*
        FIXME add this class to UML
     */
    private Block block;

    /**
     *
     * @param block the block that the image defines this renderable
     * @param position position on frame
     */
    public BlockRenderable(Block block, Position position) {
        super(null, position);
        this.image = BlockTextures.getTextureByState(block.getState());
        this.block = block;
    }


    /**
     * function to toggle display the block on top of the one that is saved in BlockRenderable as a member
     */
    public void displayTopBlock(){
        if (block.hasBlockOnTop()){
            BufferedImage bottom = BlockTextures.getTextureByState(block.getState());
            BufferedImage top = BlockTextures.getTextureByState(block.getBlockOnTop().getState());
            BufferedImage combined = new BufferedImage(bottom.getWidth(), bottom.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = combined.getGraphics();
            g.drawImage(bottom, 0, 0, null);
            g.drawImage(top, 0, 0, null);
            g.dispose();
            this.image = combined;
        }
    }

    @Override
    protected String getNodeType() {
        return "Block";
    }
}
