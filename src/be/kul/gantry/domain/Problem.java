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

    private double time;
    private List<Slot> toIgnore;

    private HashMap<Integer, Row> rows;
    private EmptySlotComparator comparator;

    private int sharedMinX;
    private int sharedMaxX;

    private Slot outputSlot;
    private Slot inputSlot;

    public Problem(int minX, int maxX, int minY, int maxY, int storageMinX, int storageMaxX, int maxLevels,
                   List<Item> items, List<Gantry> gantries, List<Slot> slots,
                   List<Job> inputJobSequence, List<Job> outputJobSequence,
                   int gantrySafetyDist, int pickupPlaceDuration) {
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

        sharedMinX = Integer.MIN_VALUE;
        sharedMaxX = Integer.MAX_VALUE;
        for (Gantry gantry: gantries) {
            gantry.setxMin(gantry.getXMin() + 5);
            gantry.setxMax(gantry.getXMax() - 5);
            if (gantry.getXMax() < sharedMaxX) sharedMaxX = gantry.getXMax();
            else if (gantry.getXMin() > sharedMinX) sharedMinX = gantry.getXMin();
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

    public void solve() throws SlotAlreadyHasItemException, NoSlotAvailableException, SlotUnreachableException {

        // set time to 0 -----------------------------------------------------------------------------------------------
        time = 0;

        // print starting positions ------------------------------------------------------------------------------------
        for(Gantry gantry: gantries) gantry.print();

        // tick time ---------------------------------------------------------------------------------------------------
        boolean done = false;
        load();
        for (Gantry gantry: gantries) getNextSlot(gantry);
        do {
            // check if done -------------------------------------------------------------------------------------------
            done = true;
            for (Gantry g : gantries) if (!g.getTodo().isEmpty() || g.getNextSlot() != null) done = false;
            if (!outputJobSequence.isEmpty()) done = false;
            if (!inputJobSequence.isEmpty()) done = false;
            if (done) break;

            tick();
        } while (!done);
        outputWriter.close();
    }

    private void load() throws SlotAlreadyHasItemException {
        // get new job from output job sequence if any left ------------------------------------------------------------
        if (!outputJobSequence.isEmpty() &&
                items.get(outputJobSequence.get(0).getItem().getId()).getSlot() != null) {
            for (Gantry gantry: gantries) {
                if (gantry.isAvailable() && gantry.canReachSlot(outputJobSequence.get(0).getPlace().getSlot())) {
                    gantry.addJobTodo(new int[]{
                            outputJobSequence.get(0).getItem().getId(),
                            items.get(outputJobSequence.get(0).getItem().getId()).getSlot().getId(),
                            outputJobSequence.get(0).getItem().getPriority()
                    });
                    gantry.addJobTodo(new int[]{
                            outputJobSequence.get(0).getItem().getId(),
                            outputJobSequence.get(0).getPlace().getSlot().getId(),
                            outputJobSequence.get(0).getItem().getPriority()
                    });
                    outputJobSequence.remove(0);
                    break;
                }
            }
        }
        // all inputs can be immediately handled -----------------------------------------------------------------------
        if (!inputJobSequence.isEmpty()) {
            for (Gantry gantry: gantries) {
                if (gantry.isAvailable() && gantry.canReachSlot(inputJobSequence.get(0).getPickup().getSlot())) {
                    // add job to gantry's to do list ------------------------------------------------------------------
                    gantry.addJobTodo(new int[]{
                            inputJobSequence.get(0).getItem().getId(),
                            inputJobSequence.get(0).getPickup().getSlot().getId(),
                            inputJobSequence.get(0).getItem().getPriority()
                    });
                    if (!outputJobSequence.isEmpty() &&
                            outputJobSequence.get(0).getItem().getId() == inputJobSequence.get(0).getItem().getId() &&
                            gantry.canReachSlot(outputSlot)) {
                        gantry.addJobTodo(new int[]{
                                inputJobSequence.get(0).getItem().getId(),
                                outputSlot.getId(),
                                -1
                        });
                        outputJobSequence.remove(0);
                    } else {
                        gantry.addJobTodo(new int[]{
                                inputJobSequence.get(0).getItem().getId(),
                                -1,
                                -1
                        });
                    }
                    inputJobSequence.remove(0);
                    break;
                }
            }
        }
    }

    /**
     * Method to simulate the time flow, will call actions on gantries.
     */
    private void tick()
            throws SlotAlreadyHasItemException, NoSlotAvailableException, SlotUnreachableException {
        if (gantries.size() == 1) {
            if (gantries.get(0).getNextSlot() == null) return;
            gantries.get(0).moveAndDrop();
            performAction(gantries.get(0));
        }
        else {
            if (gantries.get(0).getNextSlot() == null && gantries.get(1).getNextSlot() == null) {
                load();
                return;
            }

            if (gantries.get(1).getNextSlot() == null) moveOneWaitOther(gantries.get(0), gantries.get(1));
            else if (gantries.get(0).getNextSlot() == null) moveOneWaitOther(gantries.get(1), gantries.get(0));
            else {
                int first = gantries.get(0).actionTime() > gantries.get(1).actionTime() ? 1 : 0;
                if (gantries.get(1).getNextSlot().getCenterX() - gantries.get(0).getNextSlot().getCenterX() >
                        safetyDistance){
                    move(gantries.get(first), gantries.get(first == 1 ? 0 : 1));
                /*
                if (todo.getLast().getCenterX() - todo.getFirst().getCenterX() <= safetyDistance) {
                    avoid(first, first == 0 ? 1 : 0, todo);
                } else {
                    move(first, first == 0 ? 1 : 0, todo);
                }
                */
                } else {
                    gantries.get(first).print();
                    gantries.get(first == 1 ? 0: 1).print();
                    while (!singleTick(gantries.get(first), gantries.get(first == 1 ? 0 : 1)));
                    performAction(gantries.get(first));
                }
            }
        }
    }

    private boolean singleTick(Gantry first, Gantry second) throws SlotAlreadyHasItemException {
        if ((first.getId() > second.getId() && first.next() > second.next() + safetyDistance) ||
                (first.getId() < second.getId() && first.next() < second.next() - safetyDistance)) {
            second.tick();
            return first.tick();
        } else {
            second.waitTick(first.next() + (first.getId() > second.getId() ? - safetyDistance : safetyDistance));
            return first.tick();
        }
    }

    private void performAction(Gantry gantry)
            throws SlotAlreadyHasItemException, NoSlotAvailableException {
        gantry.getTodo().removeFirst();
        load();
        toIgnore.remove(gantry.getNextSlot());
        getNextSlot(gantry);
    }

    private void moveOneWaitOther(Gantry first, Gantry second)
            throws SlotAlreadyHasItemException, NoSlotAvailableException {
        double timeLeft = first.moveAndDrop();
        if ((first.getId() > second.getId() &&
                    first.getNextSlot().getCenterX() < second.getCurrentX() + safetyDistance) ||
            (first.getId() < second.getId() &&
                    first.getNextSlot().getCenterX() > second.getCurrentX() - safetyDistance)) {
            timeLeft -= second.moveTo(
                    first.getNextSlot().getCenterX() +
                            (first.getId() > second.getId() ? -safetyDistance : safetyDistance),
                    second.getCurrentY(),
                    timeLeft
            );
        }
        second.waitForOther(timeLeft);
        performAction(first);
        getNextSlot(second);
    }
/*
    private void avoid(int first, int second)
            throws SlotAlreadyHasItemException, NoSlotAvailableException, SlotUnreachableException {
        double timeLeft = gantries.get(first).moveAndDrop(todo.get(first));
        time += timeLeft;
        timeLeft -= gantries.get(second).moveTo(
                todo.get(first).getCenterX() + (first > second ? -safetyDistance : safetyDistance),
                gantries.get(second).getCurrentY(),
                timeLeft
        );
        gantries.get(second).waitForOther(timeLeft);
        performAction(first, todo);
    }
*/
    private void move(Gantry first, Gantry second)
            throws SlotAlreadyHasItemException, NoSlotAvailableException {
        double timeLeft = first.moveAndDrop();
        performAction(first);
        timeLeft -= second.moveTo(second.getNextSlot(), timeLeft);
        if (timeLeft > 0) {
            timeLeft -= second.pickupDrop();
            performAction(second);
            // TODO improve ********************************************************************************************
            if (timeLeft > 0) second.waitForOther(timeLeft);
            else if (timeLeft < 0) first.waitForOther(Math.abs(timeLeft));
        }
    }

/*
    private double catchUp(int first, int second, LinkedList<Slot> todo, double timeLeft)
            throws SlotAlreadyHasItemException, SlotUnreachableException, NoSlotAvailableException {

        if (todo.getFirst() == null && todo.getLast() == null) {
            gantries.get(timeLeft < 0 ? first : second).waitForOther(Math.abs(timeLeft));
            return 0;
        }

        if (todo.get(second) == null) return moveOneWaitOther(first, second, todo, timeLeft);
        else if (todo.get(first) == null) return moveOneWaitOther(second, first, todo, -timeLeft);
        else if ((first < second && todo.get(second).getCenterX() - todo.get(first).getCenterX() > safetyDistance) ||
                (first > second) && todo.get(first).getCenterX() - todo.get(second).getCenterX() > safetyDistance) {
            return move(first, second, todo, timeLeft);
        } else return avoid(first, second, todo, timeLeft);
    }
    private double avoid(int first, int second, LinkedList<Slot> todo, double timeLeft)
            throws SlotAlreadyHasItemException, NoSlotAvailableException, SlotUnreachableException {
        int moving = timeLeft < 0 ? first : second;
        int waiting = timeLeft < 0 ? second : first;
        timeLeft = Math.abs(timeLeft) - gantries.get(moving).moveTo(
                moving < waiting ?
                        Math.min(todo.get(moving).getCenterX(), gantries.get(waiting).getCurrentX() - safetyDistance) :
                        Math.max(todo.get(moving).getCenterX(), gantries.get(waiting).getCurrentX() + safetyDistance),
                todo.get(moving).getCenterY(),
                Math.abs(timeLeft)
        );
        if (gantries.get(moving).getCurrentX() == todo.get(moving).getCenterX() &&
                gantries.get(moving).getCurrentY() == todo.get(moving).getCenterY()) {
            timeLeft -= gantries.get(moving).pickupDrop(todo.get(moving));
            performAction(moving);
        }
        if (timeLeft > 0) {
            gantries.get(moving).waitForOther(timeLeft);
            timeLeft = 0;
        }
        return moving == first ? -timeLeft : timeLeft;
    }

    private double move(int first, int second, LinkedList<Slot> todo, double timeLeft)
            throws SlotAlreadyHasItemException, SlotUnreachableException, NoSlotAvailableException {
        double firstTime = gantries.get(first).moveAndDrop(todo.get(first));
        performAction(first, todo);
        double secondTime = gantries.get(second).moveAndDrop(todo.get(second));
        performAction(second, todo);
        return timeLeft + firstTime - secondTime;
    }

    private double moveOneWaitOther(int first, int second, LinkedList<Slot> todo, double timeLeft)
            throws SlotAlreadyHasItemException, NoSlotAvailableException, SlotUnreachableException {
        double newTimeLeft;
        if ((gantries.get(first).getCurrentX() < todo.get(first).getCenterX() &&
                gantries.get(second).getCurrentX() < todo.get(first).getCenterX()) ||
            (gantries.get(first).getCurrentX() > todo.get(first).getCenterX() &&
                gantries.get(second).getCurrentX() > todo.get(first).getCenterX())) {
            newTimeLeft = timeLeft + gantries.get(first).moveTo(
                    gantries.get(second).getCurrentX() + (first > second ? safetyDistance : -safetyDistance),
                    todo.get(first).getCenterY(),
                    Double.MAX_VALUE
            );
            int moving = newTimeLeft > 0 ? second : first;
            gantries.get(moving).moveFor(
                    gantries.get(moving).getCurrentX(),
                    moving == first ? todo.get(first).getCenterY() : gantries.get(moving).getCurrentY(),
                    Math.abs(newTimeLeft)
            );
            newTimeLeft = gantries.get(first).moveAndDrop(todo.get(first));
            gantries.get(second).moveTo(
                    todo.get(first).getCenterX() + (first < second ? safetyDistance : -safetyDistance),
                    gantries.get(second).getCurrentY(),
                    newTimeLeft
            );
            performAction(first, todo);
            todo.set(second, getNextSlot(gantries.get(second)));
            return newTimeLeft;
        } else {
            newTimeLeft = gantries.get(first).moveAndDrop(todo.get(first)) + timeLeft;
            performAction(first, todo);
            todo.set(second, getNextSlot(gantries.get(second)));
            if (newTimeLeft > 0) {
                gantries.get(second).waitForOther(newTimeLeft);
                return 0;
            } else return newTimeLeft;
        }
    }
*/
    /**
     * Move items from slots above the slot containing the needed item to empty closest slots. Will add jobs to the
     * gantry selected to un-bury the item.
     *
     * @param slot      bottom slot to un-bury
     * @param gantry    gantry to un-bury item
     */
    private void unBury(Slot slot, Gantry gantry) {

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
        gantry.addPriorityTodo(sequence);
    }

    private Slot findEmpty(double centerX, double centerY, Item toPlace, int minX, int maxX)
            throws NoSlotAvailableException{

        // set comparator settings and sort ----------------------------------------------------------------------------
        comparator.setCenterX(centerX);
        comparator.setCenterY(centerY);
        slots.sort(comparator);
        Slot best = null;

        // start looking for slot --------------------------------------------------------------------------------------
        for (Slot slot: slots) {
            if (shouldIgnore(slot)) continue;
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
                if(best.getPriority() > toPlace.getPriority() ||
                        (toPlace.getPriority() == Integer.MAX_VALUE && best.getPriority() == Integer.MAX_VALUE)) break;
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

    private boolean shouldIgnore(Slot slot) {
        for (Slot ignore: toIgnore) if (ignore == slot || slot.hasChild(ignore) || ignore.hasChild(slot)) return true;
        return false;
    }

    private void getNextSlot(Gantry gantry)
            throws SlotAlreadyHasItemException, NoSlotAvailableException {
        if (gantry.getTodo().isEmpty()) gantry.setNextSlot(null);
        else if (items.get(gantry.getTodo().getFirst()[0]).getSlot() != null &&
                !gantry.canReachSlot(items.get(gantry.getTodo().getFirst()[0]).getSlot())) {
            inputJobSequence.add(0, new Job(
                    -1,
                    items.get(gantry.getTodo().getFirst()[0]),
                    items.get(gantry.getTodo().getFirst()[0]).getSlot(),
                    null
            ));
            gantry.setNextSlot(null);
        }
        else if (gantry.getTodo().getFirst()[1] == inputSlot.getId()) {
            gantry.setNextSlot(inputSlot);
            items.get(gantry.getTodo().getFirst()[0]).setSlot(gantry.getNextSlot());
        } else if (gantry.getTodo().getFirst()[1] == outputSlot.getId()) gantry.setNextSlot(outputSlot);
        else if (gantry.getTodo().getFirst()[1] == -1) {
            gantry.setNextSlot(findEmpty(
                    gantry.getCurrentX(),
                    gantry.getCurrentY(),
                    gantry.getItem(),
                    gantry.getItem().getPriority() == Integer.MAX_VALUE ? gantry.getXMin() : sharedMinX,
                    gantry.getItem().getPriority() == Integer.MAX_VALUE ? gantry.getXMax() : sharedMaxX
            ));
        } else {
            gantry.setNextSlot(items.get(gantry.getTodo().getFirst()[0]).getSlot());
            if (gantry.getNextSlot().isBuried()) {
                unBury(gantry.getNextSlot(), gantry);
                gantry.setNextSlot(items.get(gantry.getTodo().getFirst()[0]).getSlot());
            }
        }
    }
}