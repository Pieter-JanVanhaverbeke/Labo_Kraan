package be.kul.gantry.domain;

/**
 * Created by Wim on 12/05/2015.
 */
public class Item {

    private final int id;
    private Slot slot;
    private int priority;

    public Item(int id) {
        this.id = id;
        priority = Integer.MAX_VALUE; //begint met laagste prioriteit, dus hoogste nummer
    }

    public int getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority){
        this.priority = priority;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) throws SlotAlreadyHasItemException{
        // bidirectionally link item and slot in case slot is not output slot
        if (this.slot != null) this.slot.setItem(null);
        this.slot = slot;
        if (this.slot != null && slot.getType() != Slot.SlotType.OUTPUT && slot.getItem() != this) slot.setItem(this);
    }

    @Override
    public String toString() {
        return String.format("item: %d, priority: %d", id, priority);
    }
}
