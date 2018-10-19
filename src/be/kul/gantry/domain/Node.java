package be.kul.gantry.domain;

public class Node {

    private Slot slot;          //slots in de nodes.

    private Node leftchild;
    private Node leftParrent;
    private Node rightchild;
    private Node rightParrent;


    public Node(){
        this.slot=null;
        this.leftchild = null;
        this.leftParrent = null;
        this.rightchild = null;
        this.rightParrent = null;
    }

    public Node(Slot slot, Node leftchild, Node leftParrent, Node rightchild, Node rightParrent) {
        this.slot = slot;
        this.leftchild = leftchild;
        this.leftParrent = leftParrent;
        this.rightchild = rightchild;
        this.rightParrent = rightParrent;
    }

 /*   public Node(Slot slot, int hoogte){
        this.slot = slot;
        this.leftchild =

    }
*/
    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public Node getLeftchild() {
        return leftchild;
    }

    public void setLeftchild(Node leftchild) {
        this.leftchild = leftchild;
    }

    public Node getLeftParrent() {
        return leftParrent;
    }

    public void setLeftParrent(Node leftParrent) {
        this.leftParrent = leftParrent;
    }

    public Node getRightchild() {
        return rightchild;
    }

    public void setRightchild(Node rightchild) {
        this.rightchild = rightchild;
    }

    public Node getRightParrent() {
        return rightParrent;
    }

    public void setRightParrent(Node rightParrent) {
        this.rightParrent = rightParrent;
    }

  /*  public void initialiseerroot(int aantalx){
        this.slot = null;
        this.rightParrent = null;
        this.leftParrent = null;

        for(int i=0; i<)
        this.leftchild =

    }
*/
    @Override
    public String toString() {
        return "Node{" +
                "slot=" + slot +
                ", leftchild=" + leftchild +
                ", leftParrent=" + leftParrent +
                ", rightchild=" + rightchild +
                ", rightParrent=" + rightParrent +
                '}';
    }
}
