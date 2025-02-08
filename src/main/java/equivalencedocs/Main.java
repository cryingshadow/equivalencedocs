package equivalencedocs;

import java.io.*;
import java.util.*;

import com.google.gson.*;

public class Main {

    private static final String MODULE_WIDTH = "5.5cm";

    public static void main(final String[] args) throws IOException {
        final List<Module> ownModules = Main.parseModules(new File(args[0]));
        final List<Module> otherModules = Main.parseModules(new File(args[1]));
        final List<Match> matches = Main.parseMatches(new File(args[2]));
        Main.writeDocumentation(ownModules, otherModules, matches, new File(args[3]));
    }

    private static String color(final CoverDegree cover) {
        return cover == null ? "white" : cover.color;
    }

    private static void drawConnection(final Interval connection, final int slot, final int numOfOwnModules, final int maxSlot) {
        // TODO Auto-generated method stub

    }

    private static void fillSlots(final List<Interval> verticalConnections, final Map<Integer, List<Interval>> slots) {
        // TODO Auto-generated method stub

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
        final Map<Integer, Integer> nodeConnections = new LinkedHashMap<Integer, Integer>();
        final List<Interval> verticalConnections = new ArrayList<Interval>();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            Main.writePreamble(writer);
            writer.write("\\begin{tikzpicture}\n");
            writer.write("\\coordinate (n0) at (0,0);\n");
            int node = 1;
            for (final Module module : ownModules) {
                Main.writeModule(node, module, false, idToNode, writer);
                nodeConnections.put(node, 2);
                node++;
            }
            boolean first = true;
            for (final Module module : otherModules) {
                Main.writeModule(node, module, first, idToNode, writer);
                if (first) {
                    first = false;
                }
                nodeConnections.put(node, 2);
                node++;
            }
            final int numOfOwnModules = ownModules.size();
            for (final Match match : matches) {
                final int ownNode = idToNode.get(match.ownID());
                final int otherNode = idToNode.get(match.otherID());
                nodeConnections.merge(ownNode, 1, Integer::sum);
                nodeConnections.merge(otherNode, 1, Integer::sum);
                verticalConnections.add(new Interval(ownNode, otherNode - numOfOwnModules));
            }
            final Map<Integer, List<Interval>> slots = new LinkedHashMap<Integer, List<Interval>>();
            Main.fillSlots(verticalConnections, slots);
            final int maxSlot = slots.keySet().stream().max(Integer::compare).get();
            for (final Map.Entry<Integer, List<Interval>> entry : slots.entrySet()) {
                final int slot = entry.getKey();
                for (final Interval connection : entry.getValue()) {
                    Main.drawConnection(connection, slot, numOfOwnModules, maxSlot);
                }
            }
            writer.write("\\end{tikzpicture}\n");
            writer.write("\\end{document}\n");
        }
    }

    private static void writeModule(
        final int node,
        final Module module,
        final boolean topRight,
        final Map<Integer, Integer> idToNode,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\node[draw=black,fill=");
        writer.write(Main.color(module.covered()));
        writer.write("] (n");
        writer.write(String.valueOf(node));
        writer.write(") [");
        if (topRight) {
            writer.write("yshift=-1mm,right=10 of n0");
        } else {
            writer.write("below=0.1 of n");
            writer.write(String.valueOf(node - 1));
        }
        idToNode.put(module.id(), node);
        writer.write(".south west,anchor=north west] {\\begin{minipage}{");
        writer.write(Main.MODULE_WIDTH);
        writer.write("}");
        writer.write(module.name());
        writer.write("\\\\{\\scriptsize ");
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
        writer.write("\\usetikzlibrary{positioning}\n\n");
        writer.write("\\colorlet{fhdwdarkgreen}{green!80!black}\n");
        writer.write("\\colorlet{fhdwlightgreen}{green!50!white}\n");
        writer.write("\\colorlet{fhdwyellow}{yellow}\n");
        writer.write("\\colorlet{fhdworange}{orange}\n");
        writer.write("\\colorlet{fhdwred}{red}\n\n");
        writer.write("\\pagestyle{empty}\n\n");
        writer.write("\\begin{document}\n");
    }

}
