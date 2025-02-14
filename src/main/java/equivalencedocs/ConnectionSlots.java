package equivalencedocs;

import java.io.*;
import java.util.*;

public class ConnectionSlots extends LinkedHashMap<Integer, List<Interval>> {

    private static final long serialVersionUID = 1L;

    private static boolean hasConflict(final Interval connection, final Interval existingConnection) {
        return connection.min() <= existingConnection.max() && connection.max() >= existingConnection.min();
    }

    private static boolean hasConflict(final Interval connection, final List<Interval> connections) {
        if (connections == null) {
            return false;
        }
        for (final Interval existingConnection : connections) {
            if (ConnectionSlots.hasConflict(connection, existingConnection)) {
                return true;
            }
        }
        return false;
    }

    private final Map<Integer, Integer> maxNodeConnections;

    private final int maxSlot;

    private final int numOfOwnModules;

    private final int slotWidth;

    public ConnectionSlots(
        final int numOfOwnModules,
        final int slotWidth,
        final Map<Integer, Integer> maxNodeConnections,
        final List<Interval> verticalConnections
    ) {
        this.numOfOwnModules = numOfOwnModules;
        this.slotWidth = slotWidth;
        this.maxNodeConnections = maxNodeConnections;
        for (final Interval connection : verticalConnections) {
            int currentSlot = 1;
            while (ConnectionSlots.hasConflict(connection, this.get(currentSlot))) {
                currentSlot++;
            }
            if (!this.containsKey(currentSlot)) {
                this.put(currentSlot, new ArrayList<Interval>());
            }
            this.get(currentSlot).add(connection);
        }
        this.maxSlot = this.keySet().stream().max(Integer::compare).get();
    }

    public void drawConnections(final BufferedWriter writer) throws IOException {
        final Map<Integer, Integer> currentNodeConnections = new LinkedHashMap<Integer, Integer>();
        for (final Map.Entry<Integer, List<Interval>> entry : this.entrySet()) {
            final int slot = entry.getKey();
            for (final Interval connection : entry.getValue()) {
                this.drawConnection(
                    connection,
                    slot,
                    currentNodeConnections,
                    writer
                );
            }
        }
    }

    private void drawConnection(
        final Interval connection,
        final int slot,
        final Map<Integer, Integer> currentNodeConnections,
        final BufferedWriter writer
    ) throws IOException {
        final int leftNode = connection.start();
        final int rightNode = connection.end() + this.numOfOwnModules;
        final int width = Math.max(this.slotWidth / this.maxSlot, 1) * slot + Documentation.CONNECTION_SLOT_PADDING;
        currentNodeConnections.merge(leftNode, 1, Integer::sum);
        currentNodeConnections.merge(connection.end(), 1, Integer::sum);
        final int leftConnectionCount = currentNodeConnections.get(leftNode);
        final int rightConnectionCount = currentNodeConnections.get(connection.end());
        final int leftMaxCount = this.maxNodeConnections.get(leftNode);
        final int rightMaxCount = this.maxNodeConnections.get(connection.end());
        final int leftVerticalPadding =
            -Math.max(1, Documentation.MODULE_INNER_HEIGHT / leftMaxCount) * leftConnectionCount;
        final int rightVerticalPadding =
            -Math.max(1, Documentation.MODULE_INNER_HEIGHT / rightMaxCount) * rightConnectionCount;
        writer.write("\\draw[->,thick] ($(n");
        writer.write(String.valueOf(leftNode));
        writer.write(")+(");
        writer.write(String.valueOf(Documentation.OWN_MODULE_WIDTH));
        writer.write("mm,");
        writer.write(String.valueOf(leftVerticalPadding));
        writer.write("mm)$) -- ++(");
        writer.write(String.valueOf(width));
        writer.write("mm,0mm) |- ($(n");
        writer.write(String.valueOf(rightNode));
        writer.write(")+(0mm,");
        writer.write(String.valueOf(rightVerticalPadding));
        writer.write("mm)$);\n");
    }

}
