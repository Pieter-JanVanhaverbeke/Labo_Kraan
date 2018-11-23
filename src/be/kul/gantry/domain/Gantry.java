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

    private Item item;

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

    public int getxMin() {
        return xMin;
    }

    public int getxMax() {
        return xMax;
    }

    public double getxSpeed() {
        return xSpeed;
    }

    public double getySpeed() {
        return ySpeed;
    }

    public int getPickupPlaceDuration() {
        return pickupPlaceDuration;
    }

    public int getCurrentX() {
        return currentX;
    }

    public void setCurrentX(int currentX) {
        this.currentX = currentX;
    }

    public int getCurrentY() {
        return currentY;
    }

    public void setCurrentY(int currentY) {
        this.currentY = currentY;
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(double currentTime) {
        this.currentTime = currentTime;
    }

    public PrintWriter getOutputWriter() {
        return outputWriter;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
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

    /*
        // check if gantry can reach slots, debug only for one gantry
        if(!canReachSlot(toSlot)) throw new SlotUnreachableException(toSlot.toString());
        if(!canReachSlot(fromSlot)) throw new SlotUnreachableException(fromSlot.toString());

        // move to required slot and pick up item
        updateTimeDistance(fromSlot);
        outputWriter.println(String.format("%d;%.0f;%d;%d;null", id, currentTime, currentX, currentY));
        currentTime+=pickupPlaceDuration;
        outputWriter.println(String.format("%d;%.0f;%d;%d;%d", id, currentTime, currentX, currentY, item.getId()));

        // move to next slot and drop off item
        updateTimeDistance(toSlot);
        outputWriter.println(String.format("%d;%.0f;%d;%d;%d", id, currentTime, currentX, currentY, item.getId()));
        item.setSlot(toSlot);
        currentTime+=pickupPlaceDuration;
        outputWriter.println(String.format("%d;%.0f;%d;%d;null", id, currentTime, currentX, currentY));
     */

    public void move(Slot toSlot) throws SlotUnreachableException{

        // safety check if slots are reachable, all should be reachable ------------------------------------------------
        if(toSlot != null && !canReachSlot(toSlot)) throw new SlotUnreachableException(toSlot.toString());

        // print moves and update time ---------------------------------------------------------------------------------
        outputWriter.println(String.format(
                "%d;%.0f;%d;%d;%s",
                id,
                currentTime,
                currentX,
                currentY,
                item == null ? "null" : Integer.toString(item.getId()))
        );
        updateTimeDistance(toSlot);
        outputWriter.println(String.format(
                "%d;%.0f;%d;%d;%s",
                id,
                currentTime,
                currentX,
                currentY,
                item == null ? "null" : Integer.toString(item.getId()))
        );
    }

    /**
     * Method to pick up an item, provide null item to drop off item currently in gantry.
     *
     * @param item item to pick up
     */
    public void pickDropItem(Item item){

        if(item != null && item.getId() == 1578){
            System.out.println("break");
        }
        if(this.item != null && this.item.getId() == 1578){
            System.out.println("break");
        }

        // drop if gantry has item -------------------------------------------------------------------------------------
        if(item == null){
            currentTime+=pickupPlaceDuration;
            outputWriter.println(String.format("%d;%.0f;%d;%d;null", id, currentTime, currentX, currentY));
            this.item = null;
        }
        // pickup if gantry doesn't have item --------------------------------------------------------------------------
        else {
            try {
                this.item = item;
                currentTime += pickupPlaceDuration;
                outputWriter.println(String.format("%d;%.0f;%d;%d;%d", id, currentTime, currentX, currentY, item.getId()));
            } catch (NullPointerException e){
                System.out.println("error");
            }
        }
    }

    public void waitForTime(){
        outputWriter.println(String.format(
                "%d;%.0f;%d;%d;%d",
                id,
                currentTime,
                currentX,
                currentY,
                item != null ? item.getId() : null)
        );
    }

    public void printStart(){
        outputWriter.println(String.format("%d;%.0f;%d;%d;null", id, currentTime, currentX, currentY));
    }

    public void updateTimeDistance(Slot slot){
        // update time after moving
        currentTime += Math.max(Math.abs(slot.getCenterX()-currentX)/xSpeed, Math.abs(slot.getCenterY()-currentY)/ySpeed);
        currentX = slot.getCenterX();
        currentY = slot.getCenterY();
    }
}
