package be.kul.gantry.domain.Pyramid;

import be.kul.gantry.domain.Slot;
import com.google.common.collect.HashBasedTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SlotMap extends ArrayList<HashMap<Integer, Slot>>{

    private int minSlot;
    private int maxSlot;

    public SlotMap(int minSlot, int maxSlot, int maxHeight){
        this.minSlot = minSlot;
        this.maxSlot = maxSlot;

        for(int i = 0; i < maxHeight; i++) this.add(i, new HashMap<>());
    }

    public List<Slot> getSlotsAt(int x, int z){

        List<Slot> solution = new LinkedList<>();
        int leftX = x, rightX = x;
        while((leftX >= minSlot || rightX <= maxSlot) && solution.isEmpty()){
            if(this.get(z).get(rightX) != null) solution.add(this.get(z).get(rightX));
            if(this.get(z).get(leftX) != null && !solution.contains(this.get(z).get(leftX))) solution.add(this.get(z).get(leftX));
            leftX--;
            rightX++;
        }
        if(solution.isEmpty()){
        }
        return solution;
    }

    public void add(Slot slot){
        List<Slot> parents = this.getSlotsAt(slot.getCenterX(), slot.getZ() + 1);
        slot.setParents(parents);
        this.get(slot.getZ()).put(slot.getCenterX(), slot);
    }

    public void addTopSlot(int x, int z, Slot slot){
        this.get(z).put(x, slot);
    }
}
