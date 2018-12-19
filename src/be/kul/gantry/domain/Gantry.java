package be.kul.gantry.domain;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Wim on 27/04/2015.
 */
public class Gantry {

    // =================================================================================================================
    // fields ==========================================================================================================

    // final fields ----------------------------------------------------------------------------------------------------
    private final int id;
    private final int xMin,xMax;
    private final int startX,startY;
    private final double xSpeed,ySpeed;
    private final int pickupPlaceDuration;

    // other fields ----------------------------------------------------------------------------------------------------
    private int currentX,currentY;
    private double currentTime;
    private Item item;

    private LinkedList<int[]> todo;
    private LinkedList<int[]> priorityTodo;
    private LinkedList<Slot> ignore;
    private int ignoreBoundUpper;
    private int ignoreBoundLower;
    private int unBurySlot;

    // output ----------------------------------------------------------------------------------------------------------
    private PrintWriter outputWriter;

    // =================================================================================================================
    // init ============================================================================================================
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

        this.todo = new LinkedList<>();
        this.priorityTodo = new LinkedList<>();
        this.ignore = new LinkedList<>();
        this.currentTime = 0;
        this.unBurySlot = -1;
        this.ignoreBoundLower = Integer.MIN_VALUE;
        this.ignoreBoundUpper = Integer.MAX_VALUE;
    }

    // =================================================================================================================
    // getters & setters ===============================================================================================
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

    public LinkedList<int[]> getTodo() {
        return todo;
    }

    public void setTodo(LinkedList<int[]> todo) {
        this.todo = todo;
    }

    public LinkedList<int[]> getPriorityTodo() {
        return priorityTodo;
    }

    public void setPriorityTodo(LinkedList<int[]> priorityTodo) {
        this.priorityTodo = priorityTodo;
    }

    public LinkedList<Slot> getIgnore() {
        return ignore;
    }

    public void setIgnore(LinkedList<Slot> ignore) {
        this.ignore = ignore;
    }

    public int getIgnoreBoundUpper() {
        return ignoreBoundUpper;
    }

    public void setIgnoreBoundUpper(int ignoreBoundUpper) {
        this.ignoreBoundUpper = ignoreBoundUpper;
    }

    public int getIgnoreBoundLower() {
        return ignoreBoundLower;
    }

    public void setIgnoreBoundLower(int ignoreBoundLower) {
        this.ignoreBoundLower = ignoreBoundLower;
    }

    public int getUnBurySlot() {
        return unBurySlot;
    }

    public void setUnBurySlot(int unBurySlot) {
        this.unBurySlot = unBurySlot;
    }

    // =================================================================================================================
    // default methods =================================================================================================
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

    // =================================================================================================================
    // algorithm methods ===============================================================================================

    /**
     * Method to move to slot where the next action will be performed.
     *
     * @param slot  slot to move to
     *
     * @return      time taken to move
     */
    public int moveTo(Slot slot) {
        double start = currentTime;
        updateTimeDistance(slot.getCenterX(), currentY);
        print();
        return (int) (currentTime - start);
    }

    /**
     * Method to have gantry move to certain location in order to wait for the other gantry to move out of the way.
     * No collision detection is done as this method will only be called to move a gantry away from a collision
     * collision earlier.
     *
     * @param centerX       the x position to wait at
     * @param centerY       the y position to wait at
     * @param timeNeeded    the time needed for the other gantry to finish
     *
     * @return              time taken to move
     */
    public int moveTo(int centerX, int centerY, int timeNeeded) {
        double start = currentTime;
        moveFor(timeNeeded, centerX, centerY);
        print();
        return (int) (currentTime - start);
    }

    /**
     * Method to print the gantry's current status to file. Output should always be printed as:
     * gantryID:time;centerX:centerY:itemID. in case the gantry does not hold an item null will be printed.
     */
    public void print() {
        outputWriter.println(String.format(
                "%d:%f:%d:%d:%s",
                id,
                currentTime,
                currentX,
                currentY,
                item == null ? "null" : String.valueOf(item.getId())
        ));
    }

    /**
     * @deprecated
     * Method to update a gantry's time and distance to move to a given location.
     *
     * @param slot  slot to move to
     */
    private void updateTimeDistance(Slot slot){
        // update time after moving
        currentTime += Math.max(Math.abs(slot.getCenterX()-currentX)/xSpeed, Math.abs(slot.getCenterY()-currentY)/ySpeed);
        currentX = slot.getCenterX();
        currentY = slot.getCenterY();
    }

    private void updateTimeDistance(int centerX, int centerY) {
        currentTime += Math.max(Math.abs(centerX - currentX) / xSpeed, Math.abs(centerY - currentY) / ySpeed);
        currentX = centerX;
        currentY = centerY;
    }

    /**
     * Method to check if the gantry is available to perform actions. A gantry is available if it has handled all of
     * it's backlogged jobs.
     *
     * @return  true if available
     */
    public boolean isAvailable() {
        return todo.isEmpty();
    }

    /**
     * Method to add a job to a gantry's to do list. Jobs are added as {@link Slot}s. In the list the pickup will
     * need to be added before the drop to enforce the correct ordering of actions.
     *
     * @param job   job to add
     */
    public void addJobTodo(int[] job) {
        this.todo.addLast(job);
    }

    /**
     * Method for gantry to drop or pick up an item at a given slot. Will return the time needed to perform this action
     *
     * @param slot  the specified slot
     *
     * @return      time needed
     */
    public int pickupDrop(Slot slot) throws SlotAlreadyHasItemException {
        if (item != null) {
            item.setSlot(slot);
            item = null;
        } else {
            item = slot.getItem();
            slot.setItem(null);
            if (slot.getId() == unBurySlot) {
                ignore.clear();
                unBurySlot = -1;
                ignoreBoundLower = Integer.MIN_VALUE;
                ignoreBoundUpper = Integer.MAX_VALUE;
            }
        }
        currentTime += pickupPlaceDuration;
        print();
        return pickupPlaceDuration;
    }

    /**
     * Method to call to make a gantry wait a specified amount of time in order for the other gantry to perform it's
     * actions.
     *
     * @param time  time to wait
     */
    public void waitForOther(int time) {
        currentTime += time;
    }

    /**
     * Method to calculate the time needed for a gantry to perform it's next job.
     *
     * @param nextX     the next x coordinate for the gantry
     * @param nextY     the next y coordinate for the gantry
     *
     * @return  the time needed
     */
    public double actionTime(int nextX, int nextY) {
        if (todo.isEmpty()) return 0;
        return Math.max(
                Math.abs(nextX - currentX) / xSpeed,
                Math.abs(nextY - currentY) / ySpeed)
                + pickupPlaceDuration;
    }

    /**
     * Method to have a given gantry move for a specific amount of time. No output will be printed here. If the
     * gantry actually manages to reach it's next destination true will be returned, signifying it can actually drop
     * off it's item.
     *
     * @param timeToAct the time the gantry is allowed to move
     * @param nextX     the next x coordinate for the gantry
     * @param nextY     the next y coordinate for the gantry
     *
     * @return          true if the gantry managed to reach it's pick up or drop off
     */
    public boolean moveFor(int timeToAct, int nextX, int nextY) {
        currentX = (int) (currentX - nextX < 0 ?
                Math.min(currentX + timeToAct * xSpeed, nextX) :
                Math.max(currentX - timeToAct * xSpeed, nextX));
        currentY = (int) (currentY - nextY < 0 ?
                Math.min(currentY + timeToAct * ySpeed, nextY) :
                Math.max(currentY - timeToAct * ySpeed, nextY));
        currentTime += Math.max(Math.abs(nextX - currentX) / xSpeed, Math.abs(nextX - currentY) / ySpeed);
        return currentX == nextX && currentY == nextY;
    }

    /**
     * Method to append a sequence of jobs to the priority to do list of the gantry. Used in combination with the
     * {@link Problem#unBury(Slot, Gantry, List)} method.
     *
     * @param unBurySlot    id of the slot being unburied
     * @param sequence      sequence of jobs to append
     * @param ignore        list of slots to be ignored by other gantry
     */
    public void addPriorityTodo(int unBurySlot, LinkedList<int[]> sequence, List<Slot> ignore) {
        this.unBurySlot = unBurySlot;
        this.ignore.addAll(ignore);
        while (!sequence.isEmpty()) {
            todo.addFirst(sequence.removeLast());
        }
        ignoreBoundLower = Integer.MAX_VALUE;
        ignoreBoundUpper = Integer.MIN_VALUE;
        for (Slot slot: ignore) {
            if (slot.getCenterX() < ignoreBoundLower) ignoreBoundLower = slot.getCenterX();
            if (slot.getCenterX() > ignoreBoundUpper) ignoreBoundUpper = slot.getCenterX();
        }
    }
}
