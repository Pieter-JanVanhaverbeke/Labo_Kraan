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

    public List<Slot> getLeftMostSlots(){
        List<Slot> leftmost;

        leftmost = top.getSlotsAt(0, 0);
        leftmost.addAll(leftmost.get(0).getAbove());

        return leftmost;
    }

    public List<Slot> getRightMostSlot(){

        List<Slot> rightMost;

        rightMost = top.getSlotsAt(top.getMinSlot(), 0);
        rightMost.addAll(rightMost.get(0).getAbove());

        return rightMost;
    }

    private List<Slot> getAllAbove(List<Slot> slotList) {

        List<Slot> nextUp;
        if(!slotList.isEmpty()){
            int centerX = slotList.get(0).getCenterX();
            for(int height = 1; height < maxHeight; height++) {
                nextUp = top.getSlotsAt(centerX, height);
                if(slotList.size() == 1) slotList.addAll(nextUp);
            }
        }
        return slotList;
    }

}
