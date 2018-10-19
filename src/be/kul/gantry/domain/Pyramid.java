package be.kul.gantry.domain;

import java.util.List;

public class Pyramid {
    private List<Node> nodelist;


    //aantalbedenx = aantal containers onderste laag van rij.
    //slotlijst is de lijst met slots voor z = 3, in volgorde van x klein naar groot
    public Pyramid(int aantalbenedenx, int hoogte, List<Slot> slotlijst) {
        //aantalbenedenx-hoogte IDEE DAT nog kan gebruikt worden als niet werkt
        for(int i=0; i<slotlijst.size(); i++){
            Node node = new Node();
            node.setSlot(slotlijst.get(i));                         //slotlijst normaal even groot dan geen probleem met out of bounds
            nodelist.add(node);


        }
    }
}
