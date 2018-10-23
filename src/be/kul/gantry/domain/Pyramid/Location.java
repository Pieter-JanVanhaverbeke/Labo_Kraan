package be.kul.gantry.domain.Pyramid;

import be.kul.gantry.domain.Slot;

public class Location {

    private Location leftParent;
    private Location rightParent;
    private Location leftChild;
    private Location rightChild;

    private Slot slot;

    public Location(Location leftParent, Location rightParent, Slot slot) {
        this.leftParent = leftParent;
        this.rightParent = rightParent;
        this.slot = slot;
    }

    public Location getLeftParent() {
        return leftParent;
    }

    public void setLeftParent(Location leftParent) {
        this.leftParent = leftParent;
    }

    public Location getRightParent() {
        return rightParent;
    }

    public void setRightParent(Location rightParent) {
        this.rightParent = rightParent;
    }

    public Location getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(Location leftChild) {
        this.leftChild = leftChild;
    }

    public Location getRightChild() {
        return rightChild;
    }

    public void setRightChild(Location rightChild) {
        this.rightChild = rightChild;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }
}
