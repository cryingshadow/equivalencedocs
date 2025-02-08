package equivalencedocs;

import java.io.*;
import java.util.*;

import com.google.gson.*;

public class Main {

    private static final int CONNECTION_SLOT_PADDING = 5;

    private static final int CONNECTION_SLOT_WIDTH = 35;

    private static final int CONNECTION_VERTICAL_PADDING = 2;

    private static final String MODULE_INNER_WIDTH = "5.5cm";

    private static final String MODULE_OUTER_WIDTH = "5.7cm";

    public static void main(final String[] args) throws IOException {
        final List<Module> ownModules = Main.parseModules(new File(args[0]));
        final List<Module> otherModules = Main.parseModules(new File(args[1]));
        final List<Match> matches = Main.parseMatches(new File(args[2]));
        Main.writeDocumentation(ownModules, otherModules, matches, new File(args[3]));
    }

    private static void drawConnection(
        final Interval connection,
        final int slot,
        final int numOfOwnModules,
        final int maxSlot,
        final Map<Integer, Integer> currentNodeConnections,
        final Map<Integer, Integer> maxNodeConnections,
        final BufferedWriter writer
    ) throws IOException {
        final int leftNode = connection.start();
        final int rightNode = connection.end() + numOfOwnModules;
        final int width = Math.max(Main.CONNECTION_SLOT_WIDTH / maxSlot, 1) * slot + Main.CONNECTION_SLOT_PADDING;
        currentNodeConnections.merge(leftNode, 1, Integer::sum);
        currentNodeConnections.merge(connection.end(), 1, Integer::sum);
        final int leftConnectionCount = currentNodeConnections.get(leftNode);
        final int rightConnectionCount = currentNodeConnections.get(connection.end());
        final int leftVerticalPadding = -leftConnectionCount * Main.CONNECTION_VERTICAL_PADDING;
        final int rightVerticalPadding = -rightConnectionCount * Main.CONNECTION_VERTICAL_PADDING;
        writer.write("\\draw[->,thick] ($(n");
        writer.write(String.valueOf(leftNode));
        writer.write(")+(");
        writer.write(Main.MODULE_OUTER_WIDTH);
        writer.write(",");
        writer.write(String.valueOf(leftVerticalPadding));
        writer.write("mm)$) -- ++(");
        writer.write(String.valueOf(width));
        writer.write("mm,0mm) |- ($(n");
        writer.write(String.valueOf(rightNode));
        writer.write(")+(0mm,");
        writer.write(String.valueOf(rightVerticalPadding));
        writer.write("mm)$);\n");
    }

    private static void fillSlots(final List<Interval> verticalConnections, final Map<Integer, List<Interval>> slots) {
        for (final Interval connection : verticalConnections) {
            int currentSlot = 1;
            while (Main.hasConflict(connection, slots.get(currentSlot))) {
                currentSlot++;
            }
            if (!slots.containsKey(currentSlot)) {
                slots.put(currentSlot, new ArrayList<Interval>());
            }
            slots.get(currentSlot).add(connection);
        }
    }

    private static boolean hasConflict(final Interval connection, final Interval existingConnection) {
        return connection.min() <= existingConnection.max() && connection.max() >= existingConnection.min();
    }

    private static boolean hasConflict(final Interval connection, final List<Interval> connections) {
        if (connections == null) {
            return false;
        }
        for (final Interval existingConnection : connections) {
            if (Main.hasConflict(connection, existingConnection)) {
                return true;
            }
        }
        return false;
    }

    private static List<Match> parseMatches(final File matchesFile) throws IOException {
        try (FileReader reader = new FileReader(matchesFile)) {
            return new Gson().fromJson(reader, MatchList.class);
        }
    }

    private static List<Module> parseModules(final File modulesFile) throws IOException {
        try (FileReader reader = new FileReader(modulesFile)) {
            return new Gson().fromJson(reader, ModuleList.class);
        }
    }

    private static void writeDocumentation(
        final List<Module> ownModules,
        final List<Module> otherModules,
        final List<Match> matches,
        final File output
    ) throws IOException {
        final Map<Integer, Integer> idToNode = new LinkedHashMap<Integer, Integer>();
        final Map<Integer, Integer> maxNodeConnections = new LinkedHashMap<Integer, Integer>();
        final Map<Integer, Integer> currentNodeConnections = new LinkedHashMap<Integer, Integer>();
        final List<Interval> verticalConnections = new ArrayList<Interval>();
        final Map<Integer, Integer> covered = new LinkedHashMap<Integer, Integer>();
        final Map<Integer, Integer> taken = new LinkedHashMap<Integer, Integer>();
        for (final Match match : matches) {
            covered.merge(match.ownID(), match.hours(), Integer::sum);
            taken.merge(match.otherID(), match.hours(), Integer::sum);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            Main.writePreamble(writer);
            writer.write("\\section{\\\"Ubersicht}\n\n");
            writer.write("\\begin{center}\n");
            writer.write("\\begin{tikzpicture}\n");
            writer.write("\\coordinate (n0) at (0,0);\n");
            int node = 1;
            for (final Module module : ownModules) {
                Main.writeModule(node, module, false, true, covered, taken, writer);
                idToNode.put(module.id(), node);
                maxNodeConnections.put(node, 2);
                node++;
            }
            boolean first = true;
            for (final Module module : otherModules) {
                Main.writeModule(node, module, first, false, covered, taken, writer);
                if (first) {
                    first = false;
                }
                idToNode.put(module.id(), node);
                maxNodeConnections.put(node, 2);
                node++;
            }
            final int numOfOwnModules = ownModules.size();
            for (final Match match : matches) {
                final int ownNode = idToNode.get(match.ownID());
                final int otherNode = idToNode.get(match.otherID());
                maxNodeConnections.merge(ownNode, 1, Integer::sum);
                maxNodeConnections.merge(otherNode, 1, Integer::sum);
                verticalConnections.add(new Interval(ownNode, otherNode - numOfOwnModules));
            }
            final Map<Integer, List<Interval>> slots = new LinkedHashMap<Integer, List<Interval>>();
            Main.fillSlots(verticalConnections, slots);
            final int maxSlot = slots.keySet().stream().max(Integer::compare).get();
            for (final Map.Entry<Integer, List<Interval>> entry : slots.entrySet()) {
                final int slot = entry.getKey();
                for (final Interval connection : entry.getValue()) {
                    Main.drawConnection(
                        connection,
                        slot,
                        numOfOwnModules,
                        maxSlot,
                        currentNodeConnections,
                        maxNodeConnections,
                        writer
                    );
                }
            }
            writer.write("\\end{tikzpicture}\n");
            writer.write("\\end{center}\n\n");
            writer.write("\\end{document}\n");
        }
    }

    private static void writeModule(
        final int node,
        final Module module,
        final boolean topRight,
        final boolean own,
        final Map<Integer, Integer> covered,
        final Map<Integer, Integer> taken,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\coordinate ");
        if (node == 1) {
            writer.write("(n1) at (0,0);\n");
        } else {
            if (topRight) {
                writer.write("[right=10 of n1");
            } else {
                writer.write("[below=1.5 of n");
                writer.write(String.valueOf(node - 1));
            }
            writer.write("] (n");
            writer.write(String.valueOf(node));
            writer.write(");\n");
        }
        if (own) {
            writer.write("\\filldraw[draw=black,fill=");
            writer.write(
                CoverDegree.forCoverPercentage(covered.getOrDefault(module.id(), 0) * 100 / module.hours()).color
            );
        } else {
            writer.write("\\draw[black");
        }
        writer.write("] (n");
        writer.write(String.valueOf(node));
        writer.write(") rectangle ++(");
        writer.write(Main.MODULE_OUTER_WIDTH);
        writer.write(", -1.3cm);\n");
        writer.write("\\node (t");
        writer.write(String.valueOf(node));
        writer.write(") [anchor=north west] at (n");
        writer.write(String.valueOf(node));
        writer.write(") {\\begin{minipage}{");
        writer.write(Main.MODULE_INNER_WIDTH);
        writer.write("}");
        writer.write(module.name());
        writer.write("\\\\{\\scriptsize ");
        if (!own) {
            writer.write(String.valueOf(taken.getOrDefault(module.id(), 0)));
            writer.write(" / ");
        }
        writer.write(String.valueOf(module.hours()));
        writer.write(" Stunden}\\end{minipage}};\n");
    }

    private static void writePreamble(final BufferedWriter writer) throws IOException {
        writer.write("\\documentclass{article}\n\n");
        writer.write("\\usepackage[ngerman]{babel}\n");
        writer.write("\\usepackage[T1]{fontenc}\n");
        writer.write("\\usepackage[a4paper,margin=2cm]{geometry}\n");
        writer.write("\\usepackage{xcolor}\n");
        writer.write("\\usepackage{tikz}\n");
        writer.write("\\usetikzlibrary{calc,positioning}\n\n");
        writer.write("\\colorlet{fhdwdarkgreen}{green!80!black}\n");
        writer.write("\\colorlet{fhdwlightgreen}{green!50!white}\n");
        writer.write("\\colorlet{fhdwyellow}{yellow}\n");
        writer.write("\\colorlet{fhdworange}{orange}\n");
        writer.write("\\colorlet{fhdwred}{red}\n\n");
        writer.write("\\pagestyle{empty}\n\n");
        writer.write("\\begin{document}\n\n");
    }

}
