package be.kul.gantry.domain;

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
        if(!canReachSlot(toSlot)) throw new SlotUnreachableException(toSlot.toString());
        if(!canReachSlot(fromSlot)) throw new SlotUnreachableException(fromSlot.toString());
        System.out.println(String.format("%d;%.0f;%d;%d;null", id, currentTime, currentX, currentY));
        updateTime(fromSlot);
        System.out.println(String.format("%d;%.0f;%d;%d;null", id, currentTime, currentX, currentY));
        currentTime+=pickupPlaceDuration;
        System.out.println(String.format("%d;%.0f;%d;%d;%d", id, currentTime, currentX, currentY, item.getId()));
        updateTime(toSlot);
        item.setSlot(toSlot);
        System.out.println(String.format("%d;%.0f;%d;%d;%d", id, currentTime, currentX, currentY, item.getId()));
        currentTime+=pickupPlaceDuration;
    }

    public void updateTime(Slot slot){              //tijd aanpassen voor een beweging naar een slot
        currentTime += Math.max(Math.abs(slot.getCenterX()-currentX)/xSpeed, Math.abs(slot.getCenterY()-currentY)/ySpeed);
        currentX = slot.getCenterX();
        currentY = slot.getCenterY();
    }
}
