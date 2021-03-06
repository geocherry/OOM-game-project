package com.oom.game.main.process;

import com.oom.game.main.entities.WorldItem;
import com.oom.game.main.entities.items.Axe;
import com.oom.game.main.entities.items.Backpack;
import com.oom.game.main.entities.items.Sword;
import com.oom.game.main.entities.mobs.Wolf;
import com.oom.game.main.entities.player.Player;
import com.oom.game.main.entities.player.commands.EnchantWeaponRandCommand;
import com.oom.game.main.entities.player.commands.PickUpItemCommand;
import com.oom.game.main.environment.Position;
import com.oom.game.main.environment.World;
import com.oom.game.main.environment.blocks.Barrel;
import com.oom.game.main.environment.blocks.StoneTileFloor;
import com.oom.game.main.process.render.GUIRenderable;
import com.oom.game.main.process.render.MainRenderable;
import com.oom.game.main.process.render.WorldRenderable;
import com.oom.game.main.process.utils.control.DefaultNPCMovement;
import com.oom.game.main.process.utils.control.PlayerControl;
import com.oom.game.main.utils.SerializationFacade;
import com.oom.game.main.utils.command.GameCommand;
import com.oom.game.main.utils.GameKeyActionManager;
import com.oom.game.main.utils.SystemKeyEventManager;
import com.oom.game.main.utils.command.NoneCommand;
import com.oom.game.main.utils.command.TestLoggingCommand;
import gameCore.Game;
import gameCore.Renderer;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Process{
    /*
        FIXME add normal docs to Process class
     */
    private MainRenderable mainRenderable = null;
    private World world;

    //FIXME maybe create constructor with custom values (graphics / renderer / game)
    /**
     *
     * Default constructor for Process
     */
    public Process(){
        /*
            FIXME Not clear how to use renderer and Graphics, a lot of work to do,
            FIXME however i don't know what exactly should be done to make it look normal and work accordingly
            FIXME upd 18.05.2020: Basic BufferedImage graphics. This approach is still quite disguisting,
            FIXME but at least there is not NullPointerException
         */
        Renderer defaultRenderer = new Renderer(
                (new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB))
                .getGraphics()
        );

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        //DO NOT ADD ANY ENTITIES TO WORLD BEFORE creating mainRenderable

        GUIRenderable guiRenderable = new GUIRenderable();
        //FIXME Set gui to main menu mode
        this.mainRenderable = new MainRenderable(null, guiRenderable);
        mainRenderable.setRenderer(defaultRenderer);

        GameKeyActionManager actionManager = GameKeyActionManager.getInstance();
        actionManager.setCommandOnPress('S', new GameCommand() {
            @Override
            public void execute() {
                run();
            }
        });



        //FIXME if world is smaller than the window, than adjust the window

        //Launching the game. In this class we have refs to all necessary Objects
        Game game = new Game(
                "OOM GAME",
                (int) screenSize.getWidth() / 2,
                (int) screenSize.getHeight() / 2,
                30,
                mainRenderable, //renderable
                30,
                mainRenderable, //updatable
                SystemKeyEventManager.getInstance()
        );



        SerializationFacade sf = new SerializationFacade();

        actionManager.setCommandOnPress('L', new GameCommand() {
            @Override
            public void execute() {
                World w = sf.load(game.getParent());
                if (w != null){
                    run(w);
                }
            }
        });


        actionManager.setCommandOnPress('P', new GameCommand() {
            @Override
            public void execute() {
                sf.save(world);
            }
        });

    }

    public static void setupBasicPlayerControl(PlayerControl playerControl){
        GameKeyActionManager actionManager = GameKeyActionManager.getInstance();
        //Setting up controls
        actionManager.setCommandOnPress('A', new GameCommand() {
            @Override
            public void execute() {
                playerControl.activateDirection(PlayerControl.DIRECTIONS.LEFT);
            }
        });
        actionManager.setCommandOnPress('S', new GameCommand() {
            @Override
            public void execute() {
                playerControl.activateDirection(PlayerControl.DIRECTIONS.DOWN);
            }
        });
        actionManager.setCommandOnPress('D', new GameCommand() {
            @Override
            public void execute() {
                playerControl.activateDirection(PlayerControl.DIRECTIONS.RIGHT);
            }
        });
        actionManager.setCommandOnPress('W', new GameCommand() {
            @Override
            public void execute() {
                playerControl.activateDirection(PlayerControl.DIRECTIONS.UP);
            }
        });
        actionManager.setCommandsOnRelease('A', new GameCommand() {
            @Override
            public void execute() {
                playerControl.disableDirection(PlayerControl.DIRECTIONS.LEFT);
            }
        });
        actionManager.setCommandsOnRelease('S', new GameCommand() {
            @Override
            public void execute() {
                playerControl.disableDirection(PlayerControl.DIRECTIONS.DOWN);
            }
        });
        actionManager.setCommandsOnRelease('D', new GameCommand() {
            @Override
            public void execute() {
                playerControl.disableDirection(PlayerControl.DIRECTIONS.RIGHT);
            }
        });
        actionManager.setCommandsOnRelease('W', new GameCommand() {
            @Override
            public void execute() {
                playerControl.disableDirection(PlayerControl.DIRECTIONS.UP);
            }
        });
    }

    /**
     *
     * @param world world to derive data from
     */
    public void run(World world){
        mainRenderable.getGuiRenderable().toggle(GUIRenderable.MODES.IN_GAME);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        GameKeyActionManager actionManager = GameKeyActionManager.getInstance();

        this.world = world;
        Player player = world.getPlayer();

        WorldRenderable worldRenderable = new WorldRenderable(
                this.world,
                (int) screenSize.getWidth() / 2,
                (int) screenSize.getHeight() / 2
        );
        mainRenderable.setWorldRenderable(worldRenderable);
        mainRenderable.getWorldRenderable().updatePosition(mainRenderable.getWorldRenderable().getPosition());

        PlayerControl playerControl = new PlayerControl(2);
        setupBasicPlayerControl(playerControl);
        world.assignMovement(player, playerControl);
        playerControl.enable();

        actionManager.setCommandOnPress('E', new EnchantWeaponRandCommand(player));
        actionManager.setCommandOnPress('F', new GameCommand() {
            @Override
            public void execute() {
                (new PickUpItemCommand(world, player)).execute();
                mainRenderable.getGuiRenderable().updateInventoryGUI(player.getInventory());
            }
        });
        actionManager.setCommandOnPress('I', new GameCommand() {
            @Override
            public void execute() {
                mainRenderable.getGuiRenderable().updateInventoryGUI(player.getInventory());
                mainRenderable.getGuiRenderable().toggle(GUIRenderable.MODES.INVENTORY);
            }
        });
    }

    /**
     * method is responsible for executing world / game logic and adding stuff to it, like a playground
     */
    public void run() {
        /*
            Mocking normal game process here
         */
        mainRenderable.getGuiRenderable().toggle(GUIRenderable.MODES.IN_GAME);

        GameKeyActionManager actionManager = GameKeyActionManager.getInstance();
        actionManager.setCommandOnPress('s', new NoneCommand());

        /*
            Creating some random world
         */
        this.world = World.generateDefaultWorld();
        Player player = Player.generateDefaultPlayer();
        world.addPlayer(player);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        WorldRenderable worldRenderable = new WorldRenderable(
                world,
                (int) screenSize.getWidth() / 2,
                (int) screenSize.getHeight() / 2
        );

        mainRenderable.setWorldRenderable(worldRenderable);

        for (int i = 0; i < world.getBlockCountY(); i++){
            for (int j = 0; j < world.getBlockCountX(); j++){
                if ((i + j) % 2 == 1){
                    world.removeBlock(i, j);
                    world.addBlock(i, j, new StoneTileFloor());
                }
            }
        }

        world.addBlock(2, 1, new Barrel());
        world.addBlock(5, 5, new Barrel());

        world.removeBlock(4, 4);

        PlayerControl playerControl = new PlayerControl(2);
        setupBasicPlayerControl(playerControl);
        world.assignMovement(player, playerControl);
        playerControl.enable();

        actionManager.setCommandOnPress('E', new EnchantWeaponRandCommand(player));
        actionManager.setCommandOnPress('F', new GameCommand() {
            @Override
            public void execute() {
                (new PickUpItemCommand(world, player)).execute();
                mainRenderable.getGuiRenderable().updateInventoryGUI(player.getInventory());
            }
        });
        actionManager.setCommandOnPress('I', new GameCommand() {
            @Override
            public void execute() {
                mainRenderable.getGuiRenderable().updateInventoryGUI(player.getInventory());
                mainRenderable.getGuiRenderable().toggle(GUIRenderable.MODES.INVENTORY);
            }
        });
        actionManager.setCommandOnPress(27, new GameCommand() {
            @Override
            public void execute() {
                mainRenderable.getGuiRenderable().toggle(GUIRenderable.MODES.IN_GAME);
            }
        });


        Wolf wolf1 = new Wolf(new Position(8, 8, true));
        world.addEntity(wolf1);

        Wolf wolf2 = new Wolf(new Position(18, 6, true));
        world.addEntity(wolf2);

        Wolf wolf3 = new Wolf(new Position(25, 6, true));
        world.addEntity(wolf3);

        WorldItem swordItem1 = new WorldItem(new Position(20, 120), "Sword", new Sword());
        world.addItem(swordItem1);
        WorldItem swordItem2 = new WorldItem(new Position(40, 140), "Sword", new Sword());
        world.addItem(swordItem2);
        WorldItem axeItem1 = new WorldItem(new Position(200, 300), "Axe", new Axe());
        world.addItem(axeItem1);
        WorldItem axeItem2 = new WorldItem(new Position(250, 450), "Axe", new Axe());
        world.addItem(axeItem2);
        WorldItem backpack1 = new WorldItem(new Position(150, 30), "Backpack", new Backpack());
        world.addItem(backpack1);
        WorldItem backpack2 = new WorldItem(new Position(180, 30), "Backpack", new Backpack());
        world.addItem(backpack2);



        DefaultNPCMovement wolfMovement = new DefaultNPCMovement(2);
        world.assignMovement(wolf1, wolfMovement);

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        wolfMovement.enable();
                    }
                },
                2000
        );
    }



}
