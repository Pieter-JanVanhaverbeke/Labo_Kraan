package be.kul.gantry.domain;

import java.io.PrintWriter;

/**
 * Created by Wim on 27/04/2015.
 */
public class Gantry {

    private final int id;
    private final int xMin,xMax;
    private final int startX,startY;
    private final double xSpeed,ySpeed;
    private final int pickupPlaceDuration;

    private int currentX,currentY;
    private double currentTime;

    private PrintWriter outputWriter;

    public Gantry(int id,
                  int xMin, int xMax,
                  int startX, int startY,
                  double xSpeed, double ySpeed,
                  int pickupPlaceDuration) {
        this.id = id;
        this.xMin = xMin;
        this.xMax = xMax;
        this.startX = startX;
        this.startY = startY;
        this.currentX = startX;
        this.currentY = startY;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.pickupPlaceDuration = pickupPlaceDuration;

        this.currentTime = 0;
    }

    public int getId() {
        return id;
    }

    public int getXMax() {
        return xMax;
    }

    public int getXMin() {
        return xMin;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public double getXSpeed() {
        return xSpeed;
    }

    public double getYSpeed() {
        return ySpeed;
    }

    public void setOutputWriter(PrintWriter outputWriter) {
        this.outputWriter = outputWriter;
    }

    public boolean overlapsGantryArea(Gantry g) {   //kijkt of overlap is tussen kranen
        return g.xMin < xMax && xMin < g.xMax;
    }

    public int[] getOverlapArea(Gantry g) {         //geeft de min en max in x richting terug van de kraan

        int maxmin = Math.max(xMin, g.xMin);
        int minmax = Math.min(xMax, g.xMax);

        if (minmax < maxmin)
            return null;
        else
            return new int[]{maxmin, minmax};
    }

    public boolean canReachSlot(Slot s) {//methode dat kijkt of kraan slot kan bereiken
        return xMin <= s.getCenterX() && s.getCenterX() <= xMax;
    }

    public void move(Item item, Slot fromSlot, Slot toSlot) throws SlotAlreadyHasItemException, SlotUnreachableException{
        // check if gantry can reach slots, debug only for one gantry
        if(!canReachSlot(toSlot)) throw new SlotUnreachableException(toSlot.toString());
        if(!canReachSlot(fromSlot)) throw new SlotUnreachableException(fromSlot.toString());

        // move to required slot and pick up item
        updateTime(fromSlot);
        outputWriter.println(String.format("%d;%.0f;%d;%d;null", id, currentTime, currentX, currentY));
        currentTime+=pickupPlaceDuration;
        outputWriter.println(String.format("%d;%.0f;%d;%d;%d", id, currentTime, currentX, currentY, item.getId()));

        // move to next slot and drop off item
        updateTime(toSlot);
        outputWriter.println(String.format("%d;%.0f;%d;%d;%d", id, currentTime, currentX, currentY, item.getId()));
        item.setSlot(toSlot);
        currentTime+=pickupPlaceDuration;
        outputWriter.println(String.format("%d;%.0f;%d;%d;null", id, currentTime, currentX, currentY));
    }

    public void printStart(){
        outputWriter.println(String.format("%d;%.0f;%d;%d;null", id, currentTime, currentX, currentY));
    }

    public void updateTime(Slot slot){
        // update time after moving
        currentTime += Math.max(Math.abs(slot.getCenterX()-currentX)/xSpeed, Math.abs(slot.getCenterY()-currentY)/ySpeed);
        currentX = slot.getCenterX();
        currentY = slot.getCenterY();
    }
}
