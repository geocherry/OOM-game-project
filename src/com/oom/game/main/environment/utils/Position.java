package com.oom.game.main.environment.utils;

/*
    Added this class to better manage creature movement.
    Might need to move back to normal coords afterwards.
    The problem is that coords most likely have to be int.

 */

public class Position {

    //FIXME add this class to UML
    public static final int BLOCK_SIZE = 64, DEFAULT_POSITION = 0;
    private int x = DEFAULT_POSITION, y = DEFAULT_POSITION;


    /**
     *
     * @param x x-coordinate of current object
     * @param y y-coordinate of object
     * @param isBlockPosition true if the given coordinates point to a block position,
     *                        false if the giver coordinates point to exact position (e. g. for entities)
     */
    public Position(int x, int y, boolean isBlockPosition){
        if (isBlockPosition){
            x *= BLOCK_SIZE;
            y *= BLOCK_SIZE;
        }
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    /**
     *
     * @return x-coordinate of block by the given exact x-coordinate
     */
    public int getBlockX(){
        return x / BLOCK_SIZE;
    }

    /**
     *
     * @return y-coordinate of block by the given exact y-coordinate
     */
    public int getBlockY(){
        return x / BLOCK_SIZE;
    }
}