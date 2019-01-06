package be.kul.gantry.domain;

import java.util.Comparator;

public class EmptySlotComparator implements Comparator<Slot>{

    private double centerX;
    private double centerY;

    @Override
    public int compare(Slot slot1, Slot slot2) {
        return (int) (
                (Math.pow((centerX - slot1.getCenterX()), 2) + Math.pow((centerY - slot1.getCenterY()), 2)) -
                (Math.pow((centerX - slot2.getCenterX()), 2) + Math.pow((centerY - slot2.getCenterY()), 2))
        );
    }

    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }
}
