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
    private int xMin,xMax;
    private final int startX,startY;
    private final double xSpeed,ySpeed;
    private final int pickupPlaceDuration;

    // other fields ----------------------------------------------------------------------------------------------------
    private double currentX,currentY;
    private double currentTime;
    private Item item;

    private LinkedList<int[]> todo;

    private boolean dropping;
    private int dropTime;

    private Slot nextSlot;

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
        this.currentTime = 0;

        dropping = false;
        nextSlot = null;
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

    public void setxMin(int xMin) {
        this.xMin = xMin;
    }

    public void setxMax(int xMax) {
        this.xMax = xMax;
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

    public double getxSpeed() {
        return xSpeed;
    }

    public double getySpeed() {
        return ySpeed;
    }

    public int getPickupPlaceDuration() {
        return pickupPlaceDuration;
    }

    public int getxMin() {
        return xMin;
    }

    public int getxMax() {
        return xMax;
    }

    public double getCurrentX() {
        return currentX;
    }

    public void setCurrentX(double currentX) {
        this.currentX = currentX;
    }

    public double getCurrentY() {
        return currentY;
    }

    public void setCurrentY(double currentY) {
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

    public boolean isDropping() {
        return dropping;
    }

    public void setDropping(boolean dropping) {
        this.dropping = dropping;
    }

    public int getDropTime() {
        return dropTime;
    }

    public void setDropTime(int dropTime) {
        this.dropTime = dropTime;
    }

    public Slot getNextSlot() {
        return nextSlot;
    }

    public void setNextSlot(Slot nextSlot) {
        this.nextSlot = nextSlot;
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
    public double moveTo(Slot slot) {
        double start = currentTime;
        updateTimeDistance(slot.getCenterX(), slot.getCenterY());
        print();
        return currentTime - start;
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
    public double moveTo(double centerX, double centerY, double timeNeeded) {
        double time = moveFor(timeNeeded, centerX, centerY);
        print();
        return time;
    }

    public double moveTo(Slot slot, double timeLeft) {
        return moveTo(slot.getCenterX(), slot.getCenterY(), timeLeft);
    }

    /**
     * Method to print the gantry's current status to file. Output should always be printed as:
     * gantryID:time;centerX:centerY:itemID. in case the gantry does not hold an item null will be printed.
     */
    public void print() {
        outputWriter.println(String.format(
                "%d;%.2f;%.0f;%.0f;%s",
                id,
                currentTime,
                currentX,
                currentY,
                item == null ? "null" : String.valueOf(item.getId())
        ).replace(",", "."));
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
     * @return      time needed
     */
    public double pickupDrop() throws SlotAlreadyHasItemException {
        itemAction();
        currentTime += pickupPlaceDuration;
        print();
        return pickupPlaceDuration;
    }

    private void itemAction() throws SlotAlreadyHasItemException {
        if (item != null) {
            item.setSlot(nextSlot);
            item = null;
        } else {
            item = nextSlot.getItem();
            item.setSlot(null);
        }
    }

    /**
     * Method to call to make a gantry wait a specified amount of time in order for the other gantry to perform it's
     * actions.
     *
     * @param time  time to wait
     */
    public void waitForOther(double time) {
        currentTime += time;
        print();
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

    public double actionTime() {
        return actionTime(nextSlot.getCenterX(), nextSlot.getCenterY());
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
    public double moveFor(double timeToAct, double nextX, double nextY) {
        double startX = currentX;
        double startY = currentY;
        currentX = currentX < nextX ?
                Math.floor(Math.min(currentX + timeToAct * xSpeed, nextX)) :
                Math.ceil(Math.max(currentX - timeToAct * xSpeed, nextX));
        currentY = currentY < nextY ?
                Math.floor(Math.min(currentY + timeToAct * ySpeed, nextY)) :
                Math.ceil(Math.max(currentY - timeToAct * ySpeed, nextY));
        if (currentX == nextX && currentY == nextY) {
            double time = Math.max(
                    Math.abs(currentX - startX) / xSpeed,
                    Math.abs(currentY - startY) / ySpeed
            );
            currentTime += time;
            return time;
        } else {
            currentTime += timeToAct;
            return timeToAct;
        }
    }

    /**
     * Method to append a sequence of jobs to the to do list of the gantry.
     *
     * @param sequence      sequence of jobs to append
     */
    public void addPriorityTodo(LinkedList<int[]> sequence) {
        while (!sequence.isEmpty()) todo.addFirst(sequence.removeLast());
    }

    public int next() {
        return (int) (!dropping ? next(currentX, nextSlot.getCenterX(), xSpeed) : currentX);
    }

    public double moveAndDrop() throws SlotAlreadyHasItemException {
        return moveTo(nextSlot) + pickupDrop();
    }

    public void waitTick(int x) {
        currentX = Math.abs(currentX - x) > xSpeed ? currentX + (x > currentX ? xSpeed : -xSpeed) : x;
        currentTime++;
        print();
    }

    public boolean tick() throws SlotAlreadyHasItemException {
        currentTime++;
        if (currentX == nextSlot.getCenterX() && currentY == nextSlot.getCenterY()) {
            dropTime++;
            if (dropTime > pickupPlaceDuration) {
                itemAction();
                print();
                dropTime = -1;
                return true;
            }
        } else{
            currentX = next(currentX, nextSlot.getCenterX(), xSpeed);
            currentY = next(currentY, nextSlot.getCenterY(), ySpeed);
            if (currentX == nextSlot.getCenterX() && currentY == nextSlot.getCenterY()) {
                print();
                dropTime++;
            }
        }
        return false;
    }

    private double next(double current, double next, double speed) {
        return current == next ?
                current :
                current > next ?
                        current - speed < next ?
                                next :
                                current - speed
                        :
                        current + speed > next ?
                                next :
                                current + speed;
    }
}
