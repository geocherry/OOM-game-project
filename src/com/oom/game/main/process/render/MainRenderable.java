package com.oom.game.main.process.render;

import com.oom.game.main.entities.Creature;
import com.oom.game.main.environment.World;
import com.oom.game.main.utils.GameObservable;
import gameCore.IRenderable;
import gameCore.IUpdatable;
import gameCore.Renderer;

/*
    This class has to deal with all system events like key strokes, clicks, etc.
    It then gives data to its correspondent Process with the help of observers
 */
public class MainRenderable implements IRenderable, IUpdatable {

    private Renderer renderer = null; //FIXME encapsulate renderer
    private WorldRenderable worldRenderable = null;
    private GUIRenderable guiRenderable = null;
    /**
     * This observable is mainly used to let subscribers know about tick / frame updates in game (currently 30 timer per second)
     */
    private final GameObservable<MainRenderable> observable = new GameObservable<>();
    /**
     * This list has
     */

    public MainRenderable(WorldRenderable worldRenderable, GUIRenderable guiRenderable){
        this.worldRenderable = worldRenderable;
        this.guiRenderable = guiRenderable;
    }


    /**
     * {@link IRenderable}
     */
    @Override
    public void render(Renderer renderer) {
        if (worldRenderable != null){
            worldRenderable.render(renderer);
        }
        if (guiRenderable != null){
            guiRenderable.render(renderer);
        }
    }

    /**
     * {@link IRenderable}
     */
    public void render(){
        render(this.renderer);
    }

    /*
     *  FIXME might not need to call this method here
     *  FIXME might need to put it only to NodeRenderable to update only those components that have actually changed
     */
    /**
     * {@link IUpdatable}
     *
     */
    @Override
    public void update(long elapsedMillis) {
        this.observable.notifyObservers(this);
        if (worldRenderable != null) {
            World world = worldRenderable.getWorld();
            Creature[] creatures = world.getCreaturesWithMovement();
            for (int i = 0; i < creatures.length; i++) {
                world.getMovement(creatures[i]).update(world, creatures[i]);
            }
        }


        render(this.renderer);
    }

    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public WorldRenderable getWorldRenderable() {
        return worldRenderable;
    }

    public void setWorldRenderable(WorldRenderable worldRenderable) {
        this.worldRenderable = worldRenderable;
    }

    public GUIRenderable getGuiRenderable() {
        return guiRenderable;
    }

    public void setGuiRenderable(GUIRenderable guiRenderable) {
        this.guiRenderable = guiRenderable;
    }

    public GameObservable<MainRenderable> getObservable() {
        return observable;
    }

}
