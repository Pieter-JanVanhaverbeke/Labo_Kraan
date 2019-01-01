package be.kul.gantry.domain;

import be.kul.gantry.domain.Pyramid.Row;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

import static be.kul.gantry.domain.Slot.SlotType.*;

/**
 * Created by Wim on 27/04/2015.
 */
public class Problem {

    private PrintWriter outputWriter;

    private final int minX, maxX, minY, maxY;
    private final int maxLevels;
    private final List<Item> items;
    private final List<Job> inputJobSequence;
    private final List<Job> outputJobSequence;

    private final List<Gantry> gantries;
    private final List<Slot> slots;
    private final int safetyDistance;
    private final int pickupPlaceDuration;

    private int time;
    private List<Slot> toIgnore;

    private HashMap<Integer, Row> rows;
    private EmptySlotComparator comparator;

    private Slot outputSlot;
    private Slot inputSlot;

    public Problem(int minX, int maxX, int minY, int maxY, int storageMinX, int storageMaxX, int maxLevels,
                   List<Item> items, List<Gantry> gantries, List<Slot> slots,
                   List<Job> inputJobSequence, List<Job> outputJobSequence, int gantrySafetyDist, int pickupPlaceDuration) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.maxLevels = maxLevels;
        this.items = new ArrayList<>(items);
        this.gantries = new ArrayList<>(gantries);
        this.slots = new ArrayList<>(slots);
        this.inputJobSequence = new ArrayList<>(inputJobSequence);
        this.outputJobSequence = new ArrayList<>(outputJobSequence);
        this.safetyDistance = gantrySafetyDist;
        this.pickupPlaceDuration = pickupPlaceDuration;

        this.rows = new HashMap<>();
        LinkedList<Slot> sortedSlots, temp = new LinkedList<>(slots);
        while(temp.size() > 2){
            sortedSlots = new LinkedList<>();
            int y = temp.getFirst().getCenterY();
            for(Slot slot: temp){
                if (slot.getType() == INPUT) inputSlot = slot;
                else if (slot.getType() == OUTPUT) outputSlot = slot;
                else if(slot.getCenterY() == y) sortedSlots.add(slot);
            }
            temp.removeAll(sortedSlots);
            this.rows.put(y, new Row(sortedSlots, maxLevels, storageMinX, storageMaxX));
        }
        comparator = new EmptySlotComparator();
        toIgnore = new LinkedList<>();
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxLevels() {
        return maxLevels;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<Job> getInputJobSequence() {
        return inputJobSequence;
    }

    public List<Job> getOutputJobSequence() {
        return outputJobSequence;
    }

    public List<Gantry> getGantries() {
        return gantries;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public int getSafetyDistance() {
        return safetyDistance;
    }

    public int getPickupPlaceDuration() {
        return pickupPlaceDuration;
    }

    public void writeJsonFile(File file) throws IOException {
        JSONObject root = new JSONObject();

        JSONObject parameters = new JSONObject();
        root.put("parameters",parameters);

        parameters.put("gantrySafetyDistance",safetyDistance);
        parameters.put("maxLevels",maxLevels);
        parameters.put("pickupPlaceDuration",pickupPlaceDuration);

        JSONArray items = new JSONArray();
        root.put("items",items);

        for(Item item : this.items) {
            JSONObject jo = new JSONObject();
            jo.put("id",item.getId());

            items.add(jo);
        }


        JSONArray slots = new JSONArray();
        root.put("slots",slots);
        for(Slot slot : this.slots) {
            JSONObject jo = new JSONObject();
            jo.put("id",slot.getId());
            jo.put("cx",slot.getCenterX());
            jo.put("cy",slot.getCenterY());
            jo.put("minX",slot.getXMin());
            jo.put("maxX",slot.getXMax());
            jo.put("minY",slot.getYMin());
            jo.put("maxY",slot.getYMax());
            jo.put("z",slot.getZ());
            jo.put("type",slot.getType().name());
            jo.put("itemId",slot.getItem() == null ? null : slot.getItem().getId());

            slots.add(jo);
        }

        JSONArray gantries = new JSONArray();
        root.put("gantries",gantries);
        for(Gantry gantry : this.gantries) {
            JSONObject jo = new JSONObject();

            jo.put("id",gantry.getId());
            jo.put("xMin",gantry.getXMin());
            jo.put("xMax",gantry.getXMax());
            jo.put("startX",gantry.getStartX());
            jo.put("startY",gantry.getStartY());
            jo.put("xSpeed",gantry.getXSpeed());
            jo.put("ySpeed",gantry.getYSpeed());

            gantries.add(jo);
        }

        JSONArray inputSequence = new JSONArray();
        root.put("inputSequence",inputSequence);

        for(Job inputJ : this.inputJobSequence) {
            JSONObject jo = new JSONObject();
            jo.put("itemId",inputJ.getItem().getId());
            jo.put("fromId",inputJ.getPickup().getSlot().getId());

            inputSequence.add(jo);
        }

        JSONArray outputSequence = new JSONArray();
        root.put("outputSequence",outputSequence);

        for(Job outputJ : this.outputJobSequence) {
            JSONObject jo = new JSONObject();
            jo.put("itemId",outputJ.getItem().getId());
            jo.put("toId",outputJ.getPlace().getSlot().getId());

            outputSequence.add(jo);
        }

        try(FileWriter fw = new FileWriter(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            fw.write(gson.toJson(root));
        }

    }

    public static Problem fromJson(File file) throws IOException, ParseException {


        JSONParser parser = new JSONParser();

        try(FileReader reader = new FileReader(file)) {
            JSONObject root = (JSONObject) parser.parse(reader);

            List<Item> itemList = new ArrayList<>();
            List<Slot> slotList = new ArrayList<>();
            List<Gantry> gantryList = new ArrayList<>();
            List<Job> inputJobList = new ArrayList<>();
            List<Job> outputJobList = new ArrayList<>();

            JSONObject parameters = (JSONObject) root.get("parameters");
            int safetyDist = ((Long)parameters.get("gantrySafetyDistance")).intValue();
            int maxLevels = ((Long)parameters.get("maxLevels")).intValue();
            int pickupPlaceDuration = ((Long)parameters.get("pickupPlaceDuration")).intValue();

            JSONArray items = (JSONArray) root.get("items");
            for(Object o : items) {
                int id = ((Long)((JSONObject)o).get("id")).intValue();

                Item c = new Item(id);
                itemList.add(c);
            }


            int overallMinX = Integer.MAX_VALUE, overallMaxX = Integer.MIN_VALUE;
            int storageMinX = Integer.MAX_VALUE, storageMaxX = Integer.MIN_VALUE;
            int overallMinY = Integer.MAX_VALUE, overallMaxY = Integer.MIN_VALUE;

            JSONArray slots = (JSONArray) root.get("slots");
            for(Object o : slots) {
                JSONObject slot = (JSONObject) o;

                int id = ((Long)slot.get("id")).intValue();
                int cx = ((Long)slot.get("cx")).intValue();
                int cy = ((Long)slot.get("cy")).intValue();
                int minX = ((Long)slot.get("minX")).intValue();
                int minY = ((Long)slot.get("minY")).intValue();
                int maxX = ((Long)slot.get("maxX")).intValue();
                int maxY = ((Long)slot.get("maxY")).intValue();
                int z = ((Long)slot.get("z")).intValue();

                overallMinX = Math.min(overallMinX,minX);
                overallMaxX = Math.max(overallMaxX,maxX);
                overallMinY = Math.min(overallMinY,minY);
                overallMaxY = Math.max(overallMaxY,maxY);

                Slot.SlotType type = Slot.SlotType.valueOf((String)slot.get("type"));
                Integer itemId = slot.get("itemId") == null ? null : ((Long)slot.get("itemId")).intValue();
                Item c = itemId == null ? null : itemList.get(itemId);

                Slot s = new Slot(id,cx,cy,minX,maxX,minY,maxY,z,type,c);
                // added -----------------------------------------------------------------------------------------------
                if (c != null && s.getType() == STORAGE) {
                    try {
                        c.setSlot(s);
                    } catch (SlotAlreadyHasItemException e) {
                        e.printStackTrace();
                    }
                }
                if(s.getType() == STORAGE){
                    storageMaxX = Math.max(storageMaxX, maxX);
                    storageMinX = Math.min(storageMinX, minX);
                }
                // added ----- -----------------------------------------------------------------------------------------
                slotList.add(s);
            }


            JSONArray gantries = (JSONArray) root.get("gantries");
            for(Object o : gantries) {
                JSONObject gantry = (JSONObject) o;


                int id = ((Long)gantry.get("id")).intValue();
                int xMin = ((Long)gantry.get("xMin")).intValue();
                int xMax = ((Long)gantry.get("xMax")).intValue();
                int startX = ((Long)gantry.get("startX")).intValue();
                int startY = ((Long)gantry.get("startY")).intValue();
                double xSpeed = ((Double)gantry.get("xSpeed")).doubleValue();
                double ySpeed = ((Double)gantry.get("ySpeed")).doubleValue();

                Gantry g = new Gantry(id, xMin, xMax, startX, startY, xSpeed, ySpeed, pickupPlaceDuration);
                gantryList.add(g);
            }

            JSONArray inputJobs = (JSONArray) root.get("inputSequence");
            int jid = 0;
            for(Object o : inputJobs) {
                JSONObject inputJob = (JSONObject) o;

                int iid = ((Long) inputJob.get("itemId")).intValue();
                int sid = ((Long) inputJob.get("fromId")).intValue();

                Job job = new Job(jid++,itemList.get(iid),slotList.get(sid),null);
                inputJobList.add(job);
            }

            JSONArray outputJobs = (JSONArray) root.get("outputSequence");
            for(Object o : outputJobs) {
                JSONObject outputJob = (JSONObject) o;

                int iid = ((Long) outputJob.get("itemId")).intValue();
                int sid = ((Long) outputJob.get("toId")).intValue();

                Job job = new Job(jid++,itemList.get(iid),null, slotList.get(sid));

                outputJobList.add(job);
                //adding priority to item
                itemList.get(iid).setPriority(outputJobList.size());
            }


            return new Problem(
                    overallMinX,
                    overallMaxX,
                    overallMinY,
                    overallMaxY,
                    storageMinX,
                    storageMaxX,
                    maxLevels,
                    itemList,
                    gantryList,
                    slotList,
                    inputJobList,
                    outputJobList,
                    safetyDist,
                    pickupPlaceDuration);

        }

    }

    public void setOutputWriter(PrintWriter printWriter){
        this.outputWriter = printWriter;
        for(Gantry gantry: gantries) gantry.setOutputWriter(printWriter);
    }

    public void solve(){

        // set time to 0 -----------------------------------------------------------------------------------------------
        time = 0;

        // print starting positions ------------------------------------------------------------------------------------
        for(Gantry gantry: gantries) gantry.print();

        // tick time ---------------------------------------------------------------------------------------------------
        while (!gantries.get(0).getTodo().isEmpty() || !gantries.get(1).getTodo().isEmpty() ||
                !outputJobSequence.isEmpty()) {
            try {
                tick();
            } catch (SlotAlreadyHasItemException | NoSlotAvailableException | SlotUnreachableException e) {
                System.out.println("algorithm error");
                System.exit(-1);
            }
        }
        outputWriter.close();
    }

    /**
     * Method to simulate the time flow, will call actions on gantries.
     */
    private void tick() throws SlotAlreadyHasItemException, NoSlotAvailableException, SlotUnreachableException {

        // find total time gantries can safely move --------------------------------------------------------------------
        // gantries moving in the same direction can collide -----------------------------------------------------------
        // check if they might collide ---------------------------------------------------------------------------------
        Slot firstGantrySlot = getNextFirstGantrySlot();
        Slot secondGantrySlot = getNextSecondGantrySlot();

        if (firstGantrySlot == null && secondGantrySlot == null) return;

        if (firstGantrySlot == null) {
            // move other gantry out of the way ------------------------------------------------------------------------
            if (gantries.get(0).getCurrentX() > secondGantrySlot.getCenterX() - safetyDistance) {
                gantries.get(0).moveTo(
                        secondGantrySlot.getCenterX() - safetyDistance,
                        gantries.get(0).getCurrentY(),
                        Integer.MAX_VALUE
                );
            }

            // handle second gantry ------------------------------------------------------------------------------------
            gantries.get(1).moveTo(secondGantrySlot);
            gantries.get(1).pickupDrop(secondGantrySlot);
            gantries.get(1).getTodo().removeFirst();
            toIgnore.remove(secondGantrySlot);
        } else if (secondGantrySlot == null) {
            // move other gantry out of the way ------------------------------------------------------------------------
            if (gantries.get(1).getCurrentX() < firstGantrySlot.getCenterX() + safetyDistance) {
                gantries.get(1).moveTo(
                        firstGantrySlot.getCenterX() + safetyDistance,
                        gantries.get(0).getCurrentY(),
                        Integer.MAX_VALUE
                );
            }

            // handle second gantry ------------------------------------------------------------------------------------
            gantries.get(0).moveTo(firstGantrySlot);
            gantries.get(0).pickupDrop(firstGantrySlot);
            gantries.get(0).getTodo().removeFirst();
            toIgnore.remove(firstGantrySlot);
        } else if (gantries.get(0).getCurrentX() < firstGantrySlot.getCenterX() &&
                gantries.get(1).getCurrentX() > secondGantrySlot.getCenterX() &&
                secondGantrySlot.getCenterX() - firstGantrySlot.getCenterX() <= safetyDistance) {

            // move shortest action and make other gantry wait ---------------------------------------------------------
            if (Math.abs(firstGantrySlot.getCenterX() - gantries.get(0).getCurrentX()) <
                    Math.abs(secondGantrySlot.getCenterX() - gantries.get(1).getCurrentX())) {
                time += avoidCollision(gantries.get(0), firstGantrySlot, gantries.get(1), secondGantrySlot);
            } else {
                time += avoidCollision(gantries.get(1), secondGantrySlot, gantries.get(0), firstGantrySlot);
            }
        } else {
            // move until shortest action completed --------------------------------------------------------------------
            if (gantries.get(0).actionTime(
                    firstGantrySlot.getCenterX(),
                    firstGantrySlot.getCenterY()
            ) < gantries.get(1).actionTime(
                    secondGantrySlot.getCenterX(),
                    secondGantrySlot.getCenterY()
            )) {
                time += handle(gantries.get(0), firstGantrySlot, gantries.get(1), secondGantrySlot);
            } else {
                time += handle(gantries.get(1), secondGantrySlot, gantries.get(0), firstGantrySlot);
            }
        }
    }

    private int handle(Gantry movingGantry, Slot movingGantrySlot, Gantry waitingGantry, Slot waitingGantrySlot)
            throws SlotAlreadyHasItemException, NoSlotAvailableException {

        int timeToAct;
        //complete shortest action and move other gantry -----------------------------------------------------------
        timeToAct = movingGantry.moveTo(movingGantrySlot);
        timeToAct += movingGantry.pickupDrop(movingGantrySlot);
        movingGantry.getTodo().removeFirst();
        toIgnore.remove(movingGantrySlot);
        if (waitingGantry.moveFor(timeToAct, waitingGantrySlot.getCenterX(), waitingGantrySlot.getCenterY())) {
            // drop off or pick up item and move other gantry ------------------------------------------------------
            waitingGantry.print();
            int timeNeeded =
                    (int) waitingGantry.actionTime(waitingGantrySlot.getCenterX(), waitingGantrySlot.getCenterY()) -
                            timeToAct -
                            waitingGantry.pickupDrop(waitingGantrySlot);
            waitingGantry.getTodo().removeFirst();
            toIgnore.remove(waitingGantrySlot);
            movingGantrySlot = getNextFirstGantrySlot();
            if (movingGantrySlot != null) {
                movingGantry.moveFor(timeNeeded, movingGantrySlot.getCenterX(), movingGantrySlot.getCenterY());
            }
            timeToAct += timeNeeded;
        }
        return timeToAct;
    }

    /**
     * Method to handle an action with focus on avoiding collision. One gantry specified will be able to fully complete
     * it's action while the other one will need to wait to move until the second gantry is done.
     *
     * @param movingGantry      the gantry that's is allowed to move
     * @param movingGantrySlot  the slot the moving gantry will handle
     * @param waitingGantry     the gantry that will be forced to wait
     * @param waitingGantrySlot the slot to be handled by the waiting gantry
     *
     * @return                  the time elapsed
     */
    private int avoidCollision(Gantry movingGantry, Slot movingGantrySlot, Gantry waitingGantry, Slot waitingGantrySlot)
            throws SlotAlreadyHasItemException {
        int timeToAct;
        int timeNeeded;

        timeToAct = movingGantry.moveTo(movingGantrySlot);
        timeNeeded = waitingGantry.moveTo(
                waitingGantrySlot.getCenterX() + movingGantry.getId() == 0 ? -safetyDistance : safetyDistance,
                waitingGantrySlot.getCenterY(),
                timeToAct
        );
        // drop item at first gantry -------------------------------------------------------------------------------
        timeToAct += movingGantry.pickupDrop(movingGantrySlot);
        movingGantry.getTodo().removeFirst();
        toIgnore.remove(movingGantrySlot);
        waitingGantry.waitForOther(timeToAct - timeNeeded);

        return timeToAct;
    }

    /**
     * Move items from slots above the slot containing the needed item to empty closest slots. Will add jobs to the
     * gantry selected to un-bury the item.
     *
     * @param slot      bottom slot to un-bury
     * @param gantry    gantry to un-bury item
     *
     * @throws SlotUnreachableException thrown when the slot cannot be reached by the gantry, debug only for one gantry.
     */
    private void unBury(Slot slot, Gantry gantry) throws SlotUnreachableException{

        // temporary variables -----------------------------------------------------------------------------------------
        List<Slot> toMove = slot.getAbove();
        toMove.sort(Comparator.naturalOrder());
        List <Slot> checked = new LinkedList<>();
        LinkedList<int[]> sequence = new LinkedList<>();

        // ignore all slots above current slot -------------------------------------------------------------------------
        toIgnore.add(slot);

        // move item away from all slots above buried slot -------------------------------------------------------------
        for(Slot slotToMove: toMove){
            if (checked.contains(slotToMove)) continue;
            checked.add(slotToMove);

            if(slotToMove.getItem() != null) {
                sequence.addLast(new int[]{
                        slotToMove.getItem().getId(),
                        slotToMove.getId(),
                        slotToMove.getItem().getPriority()
                });
                sequence.addLast(new int[]{
                        slotToMove.getItem().getId(),
                        -1,
                        slotToMove.getItem().getPriority()
                });
            }
        }
        gantry.addPriorityTodo(slot.getId(), sequence, toMove);
    }

    private Slot findEmpty(int centerX, int centerY, Item toPlace, int minX, int maxX) throws NoSlotAvailableException{

        // set comparator settings and sort ----------------------------------------------------------------------------
        comparator.setCenterX(centerX);
        comparator.setCenterY(centerY);
        slots.sort(comparator);
        Slot best = null;

        // start looking for slot --------------------------------------------------------------------------------------
        findEmpty:
        for (Slot slot: slots) {
            for (Slot ignore: toIgnore) if (ignore == slot || slot.hasChild(ignore)) continue findEmpty;
            /*
             condition to be empty +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                - have no item
                - don't be in the ignore list if it exists
                - be sure to not collapse
                - be a storage slot
                - don't be on the same x and y coordinates
                ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
              */
            if(slot.getItem() == null &&
                    slot.getCenterX() > minX && slot.getCenterX() < maxX &&
                    slot.willNotCollapse() && slot.getType() == STORAGE &&
                    (toPlace.getSlot() == null ||
                            (slot.getCenterX() != toPlace.getSlot().getCenterX() &&
                            slot.getCenterY() != toPlace.getSlot().getCenterY()))
                    ){
                best = slot;

                // no higher priority item buried -> return ------------------------------------------------------------
                if(best.getPriority() > toPlace.getPriority()) break;
                // else look for better location -----------------------------------------------------------------------
            }
        }

        // return slot if slot still available -------------------------------------------------------------------------
        if(best != null) {
            toIgnore.add(best);
            return best;
        }
        throw new NoSlotAvailableException();
    }

    /**
     * Method to call to decide which slot the first gantry should move to next.
     *
     * @return  the slot the gantry will move to
     * @throws SlotAlreadyHasItemException  thrown to detect algorithmic errors
     * @throws NoSlotAvailableException     thrown to detect algorithmic errors
     */
    private Slot getNextFirstGantrySlot() throws SlotAlreadyHasItemException, NoSlotAvailableException {
        Slot firstGantrySlot = null;
        // all inputs can be immediately handled -----------------------------------------------------------------------
        if (!inputJobSequence.isEmpty()) {
            inputJobSequence.get(0).getItem().setSlot(
                    inputJobSequence.get(0).getPickup().getSlot()
            );
            // add job to gantry's to do list --------------------------------------------------------------------------
            gantries.get(0).addJobTodo(new int[]{
                    inputJobSequence.get(0).getItem().getId(),
                    inputJobSequence.get(0).getPickup().getSlot().getId(),
                    inputJobSequence.get(0).getItem().getPriority()
            });
            gantries.get(0).addJobTodo(new int[]{
                    inputJobSequence.get(0).getItem().getId(),
                    -1,
                    -1
            });
            inputJobSequence.remove(0);
        }

        if (!gantries.get(0).getTodo().isEmpty()) {
            if (gantries.get(0).getTodo().getFirst()[1] == -1) {
                firstGantrySlot = findEmpty(
                        0,
                        0,
                        items.get(gantries.get(0).getTodo().getFirst()[0]),
                        items.get(gantries.get(0).getTodo().getFirst()[0]).getPriority() == Integer.MAX_VALUE ?
                                gantries.get(0).getXMin() : gantries.get(1).getXMin(),
                        gantries.get(0).getXMax()
                );
            } else {
                firstGantrySlot = items.get(gantries.get(0).getTodo().getFirst()[0]).getSlot();
                firstGantrySlot.setItem(items.get(gantries.get(0).getTodo().getFirst()[0]));
            }
        }
        return firstGantrySlot;
    }

    private Slot getNextSecondGantrySlot() throws SlotUnreachableException, NoSlotAvailableException {
        Slot secondGantrySlot;
        if (gantries.get(1).getTodo().isEmpty()) {
            // get new job from output job sequence if any left --------------------------------------------------------
            if (!outputJobSequence.isEmpty() &&
                    items.get(outputJobSequence.get(0).getItem().getId()).getSlot() != null) {
                gantries.get(1).addJobTodo(new int[]{
                        outputJobSequence.get(0).getItem().getId(),
                        items.get(outputJobSequence.get(0).getItem().getId()).getSlot().getId(),
                        outputJobSequence.get(0).getItem().getPriority()
                });
                gantries.get(1).addJobTodo(new int[]{
                        outputJobSequence.get(0).getItem().getId(),
                        outputJobSequence.get(0).getPlace().getSlot().getId(),
                        outputJobSequence.get(0).getItem().getPriority()
                });
                outputJobSequence.remove(0);
            } else return null;
        }
        if (gantries.get(1).getTodo().getFirst()[1] == outputSlot.getId()) {
            secondGantrySlot = outputSlot;
        } else if (gantries.get(1).getTodo().getFirst()[1] == -1) {
            secondGantrySlot = findEmpty(
                    gantries.get(1).getCurrentX(),
                    gantries.get(1).getCurrentY(),
                    gantries.get(1).getItem(),
                    gantries.get(1).getXMin(),
                    gantries.get(1).getXMax()
            );
        } else {
            secondGantrySlot = items.get(gantries.get(1).getTodo().getFirst()[0]).getSlot();
            if (secondGantrySlot.isBuried()) {
                unBury(secondGantrySlot, gantries.get(1));
                secondGantrySlot = items.get(gantries.get(1).getTodo().getFirst()[0]).getSlot();
            }
        }
        return secondGantrySlot;
    }
}