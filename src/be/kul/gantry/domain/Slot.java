package be.kul.gantry.domain;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Wim on 27/04/2015.
 */
public class Slot implements Comparable<Slot>{

    private Slot leftParent;                    //ieder slot heeft 2 kinderen en 2 parrents, bovenste slots hebben geen parrents
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

    public Slot(Slot slot) {
        this.id = slot.id;
        this.centerX = slot.centerX;
        this.centerY = slot.centerY;
        this.xMin = slot.xMin;
        this.xMax = slot.xMax;
        this.yMin = slot.yMin;
        this.yMax = slot.yMax;
        this.z = slot.z;
        this.item = null;
        this.type = slot.type;
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
        if(this.item == null || item == null) this.item = item;
        else if (this.item != item) throw new SlotAlreadyHasItemException(String.valueOf(item.getId()));
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
        return String.format("Slot %d (%d,%d,%d)\n%s",id,centerX,centerY,z, item);
    }

    @Override
    public int compareTo(Slot o) {
        // sort y value first, followed by z value (height) and finally x value
        if(this.centerY != o.centerY) return o.centerY - this.centerY;
        if(this.z != o.z) return o.z - this.z;
        else return o.centerX - this.centerX;
    }

    public boolean willNotCollapse(){
        // check if item in slot will not fall down, all slots below need to contain items
        return (leftChild == null && rightChild == null) || (
                (leftChild != null && leftChild.willNotCollapse() && leftChild.getItem() != null) &&
                ((rightChild != null && rightChild.willNotCollapse() && rightChild.getItem() != null) || rightChild == null)
        );
    }

    public boolean isBuried(){
        // check if the slots above contain items
        return ((this.leftParent != null && this.leftParent.getItem() != null) || (this.rightParent != null && this.rightParent.getItem() != null));
    }

    public int getPriority(){
        // the priority is the lowest value for priority of all slots below
        int priority = Integer.MAX_VALUE;
        if(this.leftChild != null) priority = Math.min(priority, leftChild.getPriority());
        if(this.rightChild != null) priority = Math.min(priority, rightChild.getPriority());
        if(this.item != null) priority = Math.min(priority, this.item.getPriority());
        return priority;
    }

    public List<Slot> getAbove(){
        //returns list of slots above the current slot
        List<Slot> above = new LinkedList<>();
        if(this.leftParent != null && this.leftParent.getItem() != null){
            above.add(this.leftParent);
            above.addAll(this.leftParent.getAbove());
        }
        if(this.rightParent != null && this.rightParent.getItem() != null){
            above.add(this.rightParent);
            above.addAll(this.rightParent.getAbove());
        }
        return above;
    }

    public boolean hasChild(Slot ignore) {
        return leftChild == ignore || rightChild == ignore ||
                (leftChild != null && leftChild.hasChild(ignore)) ||
                (rightChild != null && rightChild.hasChild(ignore));
    }

    public static enum SlotType {
        INPUT,
        OUTPUT,
        STORAGE
    }
}
