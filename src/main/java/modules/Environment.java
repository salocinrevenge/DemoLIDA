package modules;

import edu.memphis.ccrg.lida.environment.EnvironmentImpl;
import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Leaflet;
import ws3dproxy.model.Thing;
import ws3dproxy.model.World;
import ws3dproxy.util.Constants;

public class Environment extends EnvironmentImpl {

    public static final int ENVIRONMENT_WIDTH = 300;
    public static final int ENVIRONMENT_HEIGHT = 150;
    private BufferedImage image;

    private static final int DEFAULT_TICKS_PER_RUN = 100;
    private int ticksPerRun;
    private WS3DProxy proxy;
    private Creature creature;
    private Thing food;
    private Thing jewel;
    private List<Thing> thingAhead;
    private Thing leafletJewel;
    private String currentAction;   
    private Thing deliverySpot;
    private Boolean hasCompleteLeaflet;
    
    public Environment() {
        this.ticksPerRun = DEFAULT_TICKS_PER_RUN;
        this.proxy = new WS3DProxy();
        this.creature = null;
        this.food = null;
        this.jewel = null;
        this.thingAhead = new ArrayList<>();
        this.leafletJewel = null;
        this.currentAction = "rotate";
        this.deliverySpot = null;
        this.hasCompleteLeaflet = false;
        this.image = new BufferedImage(ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT, BufferedImage.TYPE_INT_RGB);
        clearImage();
    }

    private void clearImage() {
        if (image != null) {
            Graphics g = image.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT);
        }
    }

    private void drawAction(String action) {
        clearImage();
        if (action != null && !action.isEmpty() && !action.equals("none")) {
            Graphics g = image.getGraphics();
            g.setColor(Color.BLUE);
            g.setFont(new Font("SansSerif", Font.BOLD, 24));
            // Centralizando o texto basico
            System.out.println("Action: " + action);
            if (action.equals("get")) {
                if(thingAhead != null && thingAhead.get(0).getCategory() == Constants.categoryJEWEL) {
                action = "Get Jewel";
                }
                else
                {
                    action = "Eat Food";
                }
            }
            g.drawString("Action: " + action, 20, ENVIRONMENT_HEIGHT / 2);
        }
    }

    @Override
    public Object getModuleContent(Object... params) {
        return image;
    }

    @Override
    public void init() {
        super.init();
        ticksPerRun = (Integer) getParam("environment.ticksPerRun", DEFAULT_TICKS_PER_RUN);
        taskSpawner.addTask(new BackgroundTask(ticksPerRun));
        
        try {
            System.out.println("Reseting the WS3D World ...");
            proxy.getWorld().reset();
            creature = proxy.createCreature(100, 100, 0);
            World.createDeliverySpot(500.0,500.0);
            creature.start();
            System.out.println("Starting the WS3D Resource Generator ... ");
            World.grow(1);
            Thread.sleep(4000);
            creature.updateState();
            System.out.println("DemoLIDA has started...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class BackgroundTask extends FrameworkTaskImpl {

        public BackgroundTask(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            updateEnvironment();
            performAction(currentAction);
        }
    }

    @Override
    public void resetState() {
        currentAction = "rotate";
        clearImage();
    }

    @Override
    public Object getState(Map<String, ?> params) {
        Object requestedObject = null;
        String mode = (String) params.get("mode");
        switch (mode) {
            case "food":
                requestedObject = food;
                break;
            case "jewel":
                requestedObject = jewel;
                break;
            case "thingAhead":
                requestedObject = thingAhead;
                break;
            case "leafletJewel":
                requestedObject = leafletJewel;
                break;
            case "deliverySpot":
                requestedObject = deliverySpot;
                break;
            case "hasCompleteLeaflet":
                requestedObject = hasCompleteLeaflet;
                break;
            default:
                break;
        }
        return requestedObject;
    }

    
    public void updateEnvironment() {
        creature.updateState();
        food = null;
        jewel = null;
        leafletJewel = null;
        thingAhead.clear();
        deliverySpot = null;
        hasCompleteLeaflet = false;

        // Check if there is any complete leaflet
        for (Leaflet leaflet : creature.getLeaflets()) {
            boolean complete = true;
            for (String color : leaflet.getItems().keySet()) {
                if (leaflet.getTotalNumberOfType(color) > leaflet.getCollectedNumberOfType(color)) {
                    complete = false;
                    break;
                }
            }
            if (complete) {
                hasCompleteLeaflet = true;
                break;
            }
        }
                
        for (Thing thing : creature.getThingsInVision()) {
            if (thing.getCategory() == Constants.categoryDeliverySPOT) {
                deliverySpot = thing;
            } else if (creature.calculateDistanceTo(thing) <= Constants.OFFSET) {
                // Identifica o objeto proximo
                thingAhead.add(thing);
                break;
            } else if (thing.getCategory() == Constants.categoryJEWEL) {
                if (leafletJewel == null) {
                    // Identifica se a joia esta no leaflet
                    for(Leaflet leaflet: creature.getLeaflets()){
                        if (leaflet.ifInLeaflet(thing.getMaterial().getColorName()) &&
                                leaflet.getTotalNumberOfType(thing.getMaterial().getColorName()) > leaflet.getCollectedNumberOfType(thing.getMaterial().getColorName())){
                            leafletJewel = thing;
                            break;
                        }
                    }
                } else {
                    // Identifica a joia que nao esta no leaflet
                    jewel = thing;
                }
            } else if (food == null && creature.getFuel() <= 300.0
                        && (thing.getCategory() == Constants.categoryFOOD
                        || thing.getCategory() == Constants.categoryPFOOD
                        || thing.getCategory() == Constants.categoryNPFOOD)) {
                
                    // Identifica qualquer tipo de comida
                    food = thing;
            }
           
        }
    }
    
    
    
    @Override
    public void processAction(Object action) {
        String actionName = (String) action;
        currentAction = actionName.substring(actionName.indexOf(".") + 1);
        drawAction(currentAction);
    }

    private void performAction(String currentAction) {
        try {
            switch (currentAction) {
                case "rotate":
                    creature.rotate(3.0);
                    break;
                case "gotoFood":
                    if (food != null) 
                        creature.moveto(4.0, food.getX1(), food.getY1());
                    else creature.move(0.0, 0.0, 0.0);
                    break;
                case "gotoDeliverySpot":
                    if (deliverySpot != null)
                        creature.moveto(4.0, 500.0, 500.0);
                    else creature.move(0.0, 0.0, 0.0);
                    break;
                case "gotoJewel":
                    if (leafletJewel != null)
                        creature.moveto(4.0, leafletJewel.getX1(), leafletJewel.getY1());
                    else creature.move(0.0, 0.0, 0.0);
                    break;                    
                case "get":
                    creature.move(0.0, 0.0, 0.0);
                    if (thingAhead != null) {
                        for (Thing thing : thingAhead) {
                            if (thing.getCategory() == Constants.categoryJEWEL) {
                                creature.putInSack(thing.getName());
                            } else if (thing.getCategory() == Constants.categoryFOOD || thing.getCategory() == Constants.categoryNPFOOD || thing.getCategory() == Constants.categoryPFOOD) {
                                creature.eatIt(thing.getName());
                            }
                        }
                    }
                    this.resetState();
                    break;
                default:creature.move(0.0, 0.0, 0.0);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
