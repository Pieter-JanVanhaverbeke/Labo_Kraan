package be.kul.gantry.domain;

import java.util.List;

/**
 * Created by Wim on 27/04/2015.
 */
public class Slot implements Comparable<Slot>{

    //TODO Linker, rechter en parent node
    //TODO Slots in boomstructuur

    private Slot leftParent;
    private Slot rightParent;
    private Slot leftChild;
    private Slot rightChild;

    private final int id;
    private final int centerX, centerY, xMin, xMax, yMin, yMax,z;
    private Item item;
    private final SlotType type;

    public Slot(int id, int centerX, int centerY, int xMin, int xMax, int yMin, int yMax, int z, SlotType type, Item item) {
        this.id = id;
        this.centerX = centerX;
        this.centerY = centerY;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.z = z;
        this.item = item;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getZ() {
        return z;
    }

    public int getXMin() {
        return xMin;
    }

    public int getXMax() {
        return xMax;
    }

    public int getYMin() {
        return yMin;
    }

    public int getYMax() {
        return yMax;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) throws SlotAlreadyHasItemException{
        if(this.item == null) this.item = item;
        else throw new SlotAlreadyHasItemException(String.valueOf(item.getId()));
    }

    public SlotType getType() {
        return type;
    }

    public boolean isInputSlot() { return type == SlotType.INPUT; }

    public boolean isOutputSlot() { return type == SlotType.OUTPUT; }

    public boolean isStorageSlot() { return type == SlotType.STORAGE; }

    /**
     * Remove item from slot and return it.
     * @return item in this slot
     */
    public Item removeItem(){
        Item returnItem = item;
        item = null;
        return returnItem;
    }

    public Slot getLeftParent() {
        return leftParent;
    }

    public void setLeftParent(Slot leftParent) {
        this.leftParent = leftParent;
    }

    public Slot getRightParent() {
        return rightParent;
    }

    public void setRightParent(Slot rightParent) {
        this.rightParent = rightParent;
    }

    public Slot getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(Slot leftChild) {
        this.leftChild = leftChild;
    }

    public Slot getRightChild() {
        return rightChild;
    }

    public void setRightChild(Slot rightChild) {
        this.rightChild = rightChild;
    }

    public void setParents(List<Slot> parents){
        this.rightParent = parents.get(0);
        parents.get(0).setLeftChild(this);
        if(parents.size() > 1){
            this.leftParent = parents.get(1);
            parents.get(1).setRightChild(this);
        }
    }

    @Override
    public String toString() {
        return String.format("Slot %d (%d,%d,%d)",id,centerX,centerY,z);
    }

    @Override
    public int compareTo(Slot o) {
        if(this.centerY != o.centerY) return o.centerY - this.centerY;
        if(this.z != o.z) return o.z - this.z;
        else return o.centerX - this.centerX;
    }

    public static enum SlotType {
        INPUT,
        OUTPUT,
        STORAGE
    }
}
