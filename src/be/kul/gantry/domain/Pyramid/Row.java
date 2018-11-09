package be.kul.gantry.domain.Pyramid;

import be.kul.gantry.domain.Slot;

import java.util.*;

public class Row {

    // key: x coord
    private SlotMap top;
    private int maxHeight;

    public Row(List<Slot> slots, int maxHeight, int minX, int maxX) {
        // make tow from list of Slots
        this.top = new SlotMap(minX, maxX, maxHeight);
        this.maxHeight = maxHeight;

        LinkedList<Slot> sortedSlots;
        (sortedSlots = (LinkedList<Slot>) slots).sort(Comparator.naturalOrder());
        Slot temp;
        while (!sortedSlots.isEmpty()){
            temp = sortedSlots.removeFirst();
            if(temp.getZ() == maxHeight - 1) top.addTopSlot(temp.getCenterX(), temp.getZ(), temp);
            else {
                top.add(temp);
            }
        }
    }
}
