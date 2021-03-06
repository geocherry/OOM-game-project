package com.oom.game.main.process.render;

import com.oom.game.main.entities.Entity;
import com.oom.game.main.environment.Position;
import com.oom.game.main.environment.World;
import com.oom.game.main.environment.utils.Block;
import com.oom.game.main.utils.GameObservable;
import com.oom.game.main.utils.GameObserver;
import gameCore.IRenderable;
import gameCore.Renderer;

/*
    NOTES: this class should invoke updates itself
    updates occur, only when the correspondent World class with data gets changed.
    This one is only responsible for rendering

    World should always be bigger in all dimensions than the renderable
    FIXME might need to fix some issues in above notes
 */

import java.io.Serializable;
import java.util.*;

public class WorldRenderable implements IRenderable, Serializable {
    public static final class WORLD_UPDATES {
        public static final int ADD_ENTITY = 1;
        public static final int REMOVE_ENTITY = 2;
        public static final int ADD_BLOCK = 3;
        public static final int REMOVE_BLOCK = 4;
    }


    private Renderer renderer = null;
    /**
        No setters for world, width, height, because they should not be changed
        as WorldRenderable is a separate static rendering process that only changes
        when blocks / entities on screen change
     */
    private final World world;
    private int width, height;
    /**
        The current renderable field is described by the position (top left corner)
        and its width and height. All stuff in this area should be rendered as specified
        by the current member values
     */
    private Position position;
    /**
        There are separate arrays for saving blocks and creatures only because of optimisation
        We could call World class getBlock method and create new NodeRederables every time
        but it is VERY VERY inefficient (might even cause lag)

        This array of blocks is extended by 1 on x-axis and y-axis.
        There are placeholder EmptyVoids in case the some updates occur and we have to move the rendered component
     */
    private final ArrayList<ArrayList<BlockRenderable> > blockRenderables = new ArrayList<>();
    private final ArrayList<EntityRenderable> entityRenderables = new ArrayList<>();

    private GameObserver<Entity> playerObserver = null;
    private GameObserver<World> worldObserver = null;
    private final GameObservable<WorldRenderable> observable = new GameObservable<>();

    /**
     * Default constructor
     * It is very important that world is larger than the window, in which it will be rendered
     */
    public WorldRenderable(World world, int width, int height){
        this.world = world;
        this.width = width;
        this.height = height;

        this.position = new Position(0, 0, false);
        for (int i = 0; i <= height / World.BLOCK_SIZE; i++){
            ArrayList<BlockRenderable> temp = new ArrayList<>();
            for (int j = 0; j <= width / World.BLOCK_SIZE; j++){
                BlockRenderable cur = new BlockRenderable(
                        world.getBlock(i, j),
                        new Position(j, i, true)
                );
                cur.displayTopBlock();//FIXME this method has a lot of dead code in it, fix this!!!!!!

                temp.add(cur);
            }

            blockRenderables.add(temp);
        }

        worldObserver = new GameObserver<World>() {
            @Override
            public void update(GameObservable<World> observable, World newData) {
                //FIXME implement entity changes and general block changes
            }

            @Override
            public void update(GameObservable<World> observable, World newData, Object specs) {
            }

            @Override
            public void update(GameObservable<World> observable, World newData, Object specs, int id) {
                //FIXME improve type check (do it without dynamic upcast and without instanceof)
                if ((id == WORLD_UPDATES.ADD_BLOCK || id == WORLD_UPDATES.REMOVE_BLOCK)
                        && specs instanceof Position
                ){
                    Position pos = (Position) specs;
                    Position relativePos = pos.difference(position);
                    if (!(
                            relativePos.getX() >= 0 && relativePos.getX() <= width &&
                            relativePos.getY() >= 0 && relativePos.getY() <= height)
                    ){
                        return;
                    }
                    Block b = newData.getBlock(pos);
                    setRenderableByPosition(pos, new BlockRenderable(b, pos));
                    BlockRenderable br = getRenderableByPosition(pos);
                    br.displayTopBlock();
                } else if (id == WORLD_UPDATES.ADD_ENTITY && specs instanceof Entity){
                    int pos = newData.getEntities().size();
                    Entity entity = (Entity) specs;

                    if ((entity.getPosition().getX() >= position.getX()) &&
                            (entity.getPosition().getX() <= position.getX() + width) &&
                            (entity.getPosition().getY() >= position.getY()) &&
                            (entity.getPosition().getY() <= position.getX() + height)
                    ){
                        entityRenderables.add(new EntityRenderable(entity));
                    }
                } else if (id == WORLD_UPDATES.REMOVE_ENTITY && specs instanceof Entity){
                    Entity entity = (Entity) specs;
                    for (int i = 0; i < entityRenderables.size(); i++){
                        if (entity == entityRenderables.get(i).getEntity()){
                            entityRenderables.remove(i);
                            break;
                        }
                    }
                }
            }
        };
        world.getObservable().registerObserver(worldObserver);


        for (int i = 0; i < world.getEntities().size(); i++){
            entityRenderables.add(new EntityRenderable(world.getEntities().get(i)));
        }




        if (world.hasPlayer()){
            entityRenderables.add(new EntityRenderable(world.getPlayer()));
            playerObserver = new GameObserver<>() {
                @Override
                public void update(GameObservable<Entity> observable, Entity newData) {
                    //FIXME character is not exactly in the center of the screen - should be adjusted
                    Position newPosition = new Position(newData.getPosition());
                    int diffStartX = newPosition.getX() - (width / 2);
                    int diffEndX = world.getBlockCountX() * World.BLOCK_SIZE - (width / 2)
                            - newPosition.getX() - newData.getSizeX();
                    int diffStartY = newPosition.getY() - (height / 2);
                    int diffEndY = world.getBlockCountY() * World.BLOCK_SIZE - (height / 2)
                            - newPosition.getY() - newData.getSizeY();

                    Position toUpdate = new Position(position);
                    if (diffStartX > 0 && diffEndX > 0){
                        toUpdate.setX(diffStartX);
                    }
                    if (diffStartY > 0 && diffEndY > 0){
                        toUpdate.setY(diffStartY);
                    }

                    updatePosition(toUpdate);
                }
            };

            world.getPlayer().getObservable().registerObserver(playerObserver);
        }

        //Maybe add placeholder blocks if something goes wrong with visual rendering
    }

    /**
     * This function should update the list of renderables according to the new stuff that should be displayed
     * @param newPosition new position of top left corner of rendering
     * @return true if position was updated successfully, false on problems (mainly boundaries are set wrong)
     */
    public boolean updatePosition(Position newPosition){
        int curStartY = this.position.getBlockY(), newStartY = newPosition.getBlockY();
        int curStartX = this.position.getBlockX(), newStartX = newPosition.getBlockX();

        int curEndY = (this.position.getY() + this.height) / World.BLOCK_SIZE,
                newEndY = (newPosition.getY() + this.height) / World.BLOCK_SIZE;
        int curEndX = (this.position.getX() + this.width) / World.BLOCK_SIZE,
                newEndX = (newPosition.getX() + this.width) / World.BLOCK_SIZE;

        int diffStartY = curStartY - newStartY, diffStartX = curStartX - newStartX;
        int diffEndY = curEndY - newEndY, diffEndX = curEndX - newEndX;

        //FIXME return false here if there are problems with boundaries

        //Top side
        if (diffStartY > 0){
            for (int i = curStartY - 1; i >= newStartY; i--){
                ArrayList<BlockRenderable> temp = new ArrayList<>();
                for (int j = curStartX; j <= curEndX; j++){
                    BlockRenderable cur = new BlockRenderable(
                            world.getBlock(i, j),
                            new Position(j, i, true)
                    );
                    cur.displayTopBlock();

                    temp.add(cur);
                }
                blockRenderables.add(0, temp);
            }
        } else if (diffStartY < 0){
            for (int i = curStartX + 1; i <= newStartX; i++){
                blockRenderables.remove(0);
            }
        }


        //Bottom side
        if (diffEndY > 0){

            for (int i = curEndY - 1; i >= newEndY; i--){
                blockRenderables.remove(blockRenderables.size() - 1);
            }
        } else if (diffEndY < 0){
            for (int i = curEndY + 1; i <= newEndY; i++){
                ArrayList<BlockRenderable> temp = new ArrayList<>();
                for (int j = curStartX; j <= curEndX; j++){
                    BlockRenderable cur = new BlockRenderable(
                            world.getBlock(i, j),
                            new Position(j, i, true)
                    );
                    cur.displayTopBlock();

                    temp.add(cur);
                }
                blockRenderables.add(temp);
            }
        }

        curStartY = newStartY;
        curEndY = newEndY;

        //Left side
        if (diffStartX > 0){
            for (int i = curStartX - 1; i >= newStartX; i--){
                for (int j = curStartY; j <= curEndY; j++){
                    BlockRenderable cur = new BlockRenderable(
                            world.getBlock(j, i),
                            new Position(i, j, true)
                    );

                    cur.displayTopBlock();
                    blockRenderables.get(j - curStartY).add(0, cur);
                }
            }
        } else if (diffStartX < 0) {
            for (int i = curStartX + 1; i <= newStartX; i++){
                for (int j = curStartY; j <= curEndY; j++){
                    blockRenderables.get(j - curStartY).remove(0);
                }
            }
        }

        //Right side
        if (diffEndX > 0){
            for (int i = curEndX - 1; i >= newEndX; i--){
                int size = blockRenderables.get(0).size();
                for (int j = curStartY; j <= curEndY; j++){
                    blockRenderables.get(j - curStartY).remove(size - 1);
                }
            }
        } else if (diffEndX < 0) {
            for (int i = curEndX + 1; i <= newEndX; i++){
                for (int j = curStartY; j <= curEndY; j++){
                    BlockRenderable cur = new BlockRenderable(
                            world.getBlock(j, i),
                            new Position(i, j, true)
                    );

                    cur.displayTopBlock();
                    blockRenderables.get(j - curStartY).add(cur);
                }
            }
        }

        ArrayList<Entity> entities = world.getEntities();
        Set<Entity> entitiesSet = new HashSet<>();
        for (int i = 0; i < entities.size(); i++){
            Entity cur = entities.get(i);
            if ((cur.getPosition().getX() >= position.getX()) &&
                (cur.getPosition().getX() <= position.getX() + width) &&
                (cur.getPosition().getY() >= position.getY()) &&
                (cur.getPosition().getY() <= position.getY() + height)
            ){

                entitiesSet.add(cur);
            }
        }


        int i = 0;




        while(i < entityRenderables.size()){
            EntityRenderable cur = entityRenderables.get(i);
            entitiesSet.remove(cur.getEntity());
            if (cur.getEntity() != world.getPlayer() && entitiesSet.contains(cur.getEntity())){
                entityRenderables.remove(i);
            } else {
                i++;
            }
        }

        for (Entity e : entitiesSet){
            entityRenderables.add(new EntityRenderable(e));
        }



        this.position = newPosition;
        return true;
    }

    /**
     * @param dx change of position by x-axis
     * @param dy change of position by y-axis
     * @return true if moving position was successful and false otherwise
     */
    public boolean movePosition(int dx, int dy){
        Position newPosition = new Position(this.position.getX() + dx, this.position.getY() + dy);
        return this.updatePosition(newPosition);
    }

    /**
     * {@link IRenderable}
     */
    @Override
    public void render(Renderer renderer) {



        /*
            FIXME URGENT sort array of entities and render them together with blocks in such an order,
            FIXME that if a block (on top of another) is in front of the entity, the entity gets rendered before,
            FIXME so that there is and illusion of 3d
            URGENT!!!!!!!!
         */

        //FIXME this is very inefficient! it sorts on every render.
        //FIXME the problem is that positions of entities can change dynamically
        //FIXME so you may not be able to store them in maps (i think so at least). Should think of a better solution

        synchronized (entityRenderables){
            //FIXME i have no idea how synchronized fixed ConcurrentModificationException. But it works for now
            entityRenderables.sort(new Comparator<>() {
                @Override
                public int compare(EntityRenderable o1, EntityRenderable o2) {
                    Position pos1 = o1.getPosition(), pos2 = o2.getPosition();
                    if (pos1.getY() == pos2.getY()){
                        if (pos1.getX() == pos2.getX()){
                            return 0;
                        }

                        if (pos1.getX() < pos2.getX()){
                            return -1;
                        }
                        else{
                            return 1;
                        }
                    }
                    else{
                        if (pos1.getY() < pos2.getY()){
                            return -1;
                        }
                        else{
                            return 1;
                        }
                    }
                }
            });
        }


        for (int i = 0; i < blockRenderables.size(); i++){
            for (int j = 0; j < blockRenderables.get(i).size(); j++){
                BlockRenderable cur = blockRenderables.get(i).get(j);
                cur.render(renderer, cur.getPosition().difference(this.position));

            }
        }



        for (int e = 0; e < entityRenderables.size(); e++){
            EntityRenderable cur = entityRenderables.get(e);
            observable.notifyObservers(this);





            Position relativeEntityPos = cur.getPosition().difference(this.position);
            cur.render(renderer, relativeEntityPos);
            for (int i = relativeEntityPos.getBlockY();
                 i <= (relativeEntityPos.getY() + cur.getEntity().getSizeY()) / World.BLOCK_SIZE;
                 i++
            ){
                if (i < 0 || i >= blockRenderables.size()){
                    continue;
                }
                int blockCenterY = i * World.BLOCK_SIZE + World.BLOCK_SIZE / 2;
                for (int j = relativeEntityPos.getBlockX();
                     j <= (relativeEntityPos.getX() + cur.getEntity().getSizeX()) / World.BLOCK_SIZE;
                     j++
                ){
                    if (j < 0 || j >= blockRenderables.get(i).size()){
                        continue;
                    }
                    if (blockCenterY > (relativeEntityPos.getY() + cur.getEntity().getSizeY())){
                        BlockRenderable curBR = blockRenderables.get(i).get(j);
                        if (curBR.getTopRenderable() == null){
                            continue;
                        }
                        curBR.getTopRenderable()
                                .render(renderer, curBR.getPosition().difference(this.position));
                    }

                }
            }
        }


    }

    /**
     *
     * @param position position of renderable the caller wants to receive
     * @return the wanted BlockRenderable
     */
    public BlockRenderable getRenderableByPosition(Position position){
        //FIXME check if position is in bounds of current WorldRenderable
        Position relative = position.difference(this.position);
        return blockRenderables.get(relative.getBlockY()).get(relative.getBlockX());
    }

    /**
     *
     * @param position position where to set the renderable (relative to the world, NOT to the renderable)
     * @param br the Renderable that is to be set
     * @return true if change was successful, false otherwise
     */
    public boolean setRenderableByPosition(Position position, BlockRenderable br){
        Position relative = position.difference(this.position);
        blockRenderables.get(relative.getBlockY()).set(relative.getBlockX(), br);

        return true;
    }

    /**
     * {@link IRenderable}
     */
    public void render(){
        render(this.renderer);
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public World getWorld() {
        return world;
    }


    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Position getPosition() {
        return position;
    }

    public GameObserver<Entity> getPlayerObserver() {
        return playerObserver;
    }

    public GameObservable<WorldRenderable> getObservable() {
        return observable;
    }
}
