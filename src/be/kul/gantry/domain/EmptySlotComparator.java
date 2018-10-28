package be.kul.gantry.domain;

import java.util.Comparator;

public class EmptySlotComparator implements Comparator<Slot>{

    private int centerX;
    private int centerY;

    @Override
    public int compare(Slot slot1, Slot slot2) {

        return
                ((centerX - slot2.getCenterX()) + (centerY - slot2.getCenterY())) -
                ((centerX - slot1.getCenterX()) + (centerY - slot1.getCenterY()));

    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }
}
