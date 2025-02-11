package equivalencedocs;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import com.google.gson.*;

public class Main {

    private static final int CONNECTION_SLOT_PADDING = 5;

    private static final int FOREIGN_MODULE_WIDTH = 120;

    private static final int MODULE_INNER_HEIGHT = 13;

    private static final String MODULE_OUTER_HEIGHT = "1.5";

    private static final int MODULE_RIGHT_SEP_BIG = 14;

    private static final int MODULE_RIGHT_SEP_SMALL = 10;

    private static final int OWN_MODULE_WIDTH = 57;

    public static void main(final String[] args) throws IOException {
        if (args == null || args.length != 6) {
            System.out.println("Expected input: title, comments, ownModules, foreignModules, matches, output");
            return;
        }
        final List<String> comments = Files.readAllLines(new File(args[1]).toPath());
        final List<Module> ownModules = Main.parseModules(new File(args[2]));
        final List<Module> otherModules = Main.parseModules(new File(args[3]));
        final List<Match> matches = Main.parseMatches(new File(args[4]));
        Main.writeDocumentation(args[0], comments, ownModules, otherModules, matches, new File(args[5]));
    }

    private static int computeUnusedHours(final List<Module> otherModules, final Map<Integer, Integer> taken) {
        int result = 0;
        for (final Module module : otherModules) {
            result += module.hours() - taken.getOrDefault(module.id(), 0);
        }
        return result;
    }

    private static void drawConnection(
        final Interval connection,
        final int slot,
        final int numOfOwnModules,
        final int maxSlot,
        final int slotWidth,
        final Map<Integer, Integer> currentNodeConnections,
        final Map<Integer, Integer> maxNodeConnections,
        final BufferedWriter writer
    ) throws IOException {
        final int leftNode = connection.start();
        final int rightNode = connection.end() + numOfOwnModules;
        final int width = Math.max(slotWidth / maxSlot, 1) * slot + Main.CONNECTION_SLOT_PADDING;
        currentNodeConnections.merge(leftNode, 1, Integer::sum);
        currentNodeConnections.merge(connection.end(), 1, Integer::sum);
        final int leftConnectionCount = currentNodeConnections.get(leftNode);
        final int rightConnectionCount = currentNodeConnections.get(connection.end());
        final int leftMaxCount = maxNodeConnections.get(leftNode);
        final int rightMaxCount = maxNodeConnections.get(connection.end());
        final int leftVerticalPadding = -Math.max(1, Main.MODULE_INNER_HEIGHT / leftMaxCount) * leftConnectionCount;
        final int rightVerticalPadding = -Math.max(1, Main.MODULE_INNER_HEIGHT / rightMaxCount) * rightConnectionCount;
        writer.write("\\draw[->,thick] ($(n");
        writer.write(String.valueOf(leftNode));
        writer.write(")+(");
        writer.write(String.valueOf(Main.OWN_MODULE_WIDTH));
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

    private static void writeComments(final List<String> comments, final BufferedWriter writer) throws IOException {
        writer.write("\\section{Vorbemerkungen}\n\n");
        for (final String line : comments) {
            writer.write(line);
            writer.write("\n");
        }
        writer.write("\n");
    }

    private static void writeDecision(final String title, final List<Module> ownModules, final BufferedWriter writer) throws IOException {
        writer.write("\\pagebreak\n\n");
        writer.write("\\section{Entscheidung des Pr\\\"ufungsausschusses}\n\n");
        writer.write("Der erfolgreiche Berufsabschluss ");
        writer.write(title);
        writer.write(" f\\\"uhrt zur pauschalen Anrechnung der Module\n\n");
        writer.write("\\begin{center}\n");
        writer.write("\\renewcommand{\\arraystretch}{1.5}\n");
        writer.write("\\begin{tabular}{|l|c|c|}\n");
        writer.write("\\hline\n");
        writer.write("\\textbf{Modul} & \\phantom{x}\\textbf{Ja}\\phantom{x} & \\textbf{Nein}\\\\\\hline\n");
        for (final Module module : ownModules) {
            writer.write(module.name());
            writer.write(" & & \\\\\\hline\n");
        }
        writer.write("\\end{tabular}\n");
        writer.write("\\renewcommand{\\arraystretch}{1}\n");
        writer.write("\\end{center}\n\n");
        writer.write("\\noindent unter den folgenden Auflagen:\n\n");
        writer.write("\\vfill\n\n");
        writer.write("\\begin{tikzpicture}\n");
        writer.write("\\node (place) {Ort, Datum};\n");
        writer.write("\\node (sig) [right=7 of place] {Unterschrift};\n");
        writer.write("\\draw[thick] ($(place.north west)+(-2,0.1)$) -- ($(place.north east)+(2,0.1)$);\n");
        writer.write("\\draw[thick] ($(sig.north west)+(-2,0.1)$) -- ($(sig.north east)+(2,0.1)$);\n");
        writer.write("\\end{tikzpicture}\n\n");
    }

    private static void writeDocumentation(
        final String title,
        final List<String> comments,
        final List<Module> ownModules,
        final List<Module> otherModules,
        final List<Match> matches,
        final File output
    ) throws IOException {
        final Map<Integer, Integer> covered = new LinkedHashMap<Integer, Integer>();
        final Map<Integer, Integer> taken = new LinkedHashMap<Integer, Integer>();
        for (final Match match : matches) {
            covered.merge(match.ownID(), match.hours(), Integer::sum);
            taken.merge(match.otherID(), match.hours(), Integer::sum);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            Main.writePreamble(title, writer);
            Main.writeComments(comments, writer);
            Main.writeOverview(ownModules, otherModules, matches, covered, taken, writer);
            for (final Module ownModule : ownModules) {
                Main.writeModuleDetails(
                    ownModule,
                    otherModules,
                    matches,
                    covered.getOrDefault(ownModule.id(), 0),
                    writer
                );
            }
            Main.writeDecision(title, ownModules, writer);
            writer.write("\\end{document}\n");
        }
    }

    private static void writeLegend(
        final int lowestNode,
        final int unused,
        final boolean big,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\coordinate (lh) at (n");
        writer.write(String.valueOf(lowestNode));
        writer.write(" -| n1);\n");
        writer.write("\\coordinate [");
        writer.write(big ? "above=3" : "below=2");
        writer.write(" of lh] (l0);\n");
        writer.write("\\node[anchor=north west] (lt0) at (l0) {Abdeckung:};\n");
        int node = 1;
        Integer oldThreshold = null;
        for (final CoverDegree degree : CoverDegree.getSortedDegrees()) {
            writer.write("\\coordinate [below=0.5 of l");
            writer.write(String.valueOf(node - 1));
            writer.write("] (l");
            writer.write(String.valueOf(node));
            writer.write(");\n");
            writer.write("\\filldraw[draw=black,fill=");
            writer.write(degree.color);
            writer.write("] (l");
            writer.write(String.valueOf(node));
            writer.write(") rectangle ++(2,-0.5);\n");
            writer.write("\\node (lt");
            writer.write(String.valueOf(node));
            writer.write(") [right=2.1 of l");
            writer.write(String.valueOf(node));
            writer.write(",anchor=north west] {$\\geq ");
            writer.write(String.valueOf(degree.threshold));
            writer.write("\\%$");
            if (oldThreshold != null) {
                writer.write(", $< ");
                writer.write(String.valueOf(oldThreshold));
                writer.write("\\%$");
            }
            writer.write("};\n");
            node++;
            oldThreshold = degree.threshold;
        }
        writer.write("\\node (u) [below=of l");
        writer.write(String.valueOf(node - 1));
        writer.write(",anchor=north west] {Ungenutzte Stunden: ");
        writer.write(String.valueOf(unused));
        writer.write("};\n");
    }

    private static void writeModule(
        final int node,
        final Module module,
        final String right,
        final boolean own,
        final int width,
        final Map<Integer, Integer> covered,
        final Map<Integer, Integer> taken,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\coordinate ");
        if (node == 1) {
            writer.write("(n1) at (0,0);\n");
        } else {
            if (right != null) {
                writer.write("[right=");
                writer.write(right);
                writer.write(" of n1");
            } else {
                writer.write("[below=");
                writer.write(Main.MODULE_OUTER_HEIGHT);
                writer.write(" of n");
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
        writer.write(String.valueOf(width));
        writer.write("mm, -");
        writer.write(String.valueOf(Main.MODULE_INNER_HEIGHT));
        writer.write("mm);\n");
        writer.write("\\node (t");
        writer.write(String.valueOf(node));
        writer.write(") [anchor=north west] at (n");
        writer.write(String.valueOf(node));
        writer.write(") {\\begin{minipage}{");
        writer.write(String.valueOf(width - 2));
        writer.write("mm}");
        writer.write(module.name());
        writer.write("\\\\{\\scriptsize ");
        if (!own) {
            writer.write(String.valueOf(taken.getOrDefault(module.id(), 0)));
            writer.write(" / ");
        }
        writer.write(String.valueOf(module.hours()));
        writer.write(" Stunden}\\end{minipage}};\n");
    }

    private static void writeModuleDetails(
        final Module ownModule,
        final List<Module> otherModules,
        final List<Match> matches,
        final int covered,
        final BufferedWriter writer
    ) throws IOException {
        writer.write("\\pagebreak\n\n");
        writer.write("\\section{");
        writer.write(ownModule.name());
        writer.write("}\n\n");
        writer.write("\\noindent Umfang: ");
        writer.write(String.valueOf(ownModule.hours()));
        writer.write(" Stunden, abgedeckt: ");
        writer.write(String.valueOf(covered));
        writer.write(" Stunden (");
        writer.write(String.valueOf(covered * 100 / ownModule.hours()));
        writer.write("\\%), modulverantwortlich: ");
        writer.write(ownModule.responsible());
        writer.write("\n\n");
        writer.write("\\subsection*{Kompetenzen}\n\n");
        for (final String competency : ownModule.competencies()) {
            writer.write(competency);
            writer.write("\n");
        }
        writer.write("\n\\subsection*{Abdeckung}\n\n");
        for (final Match match : matches) {
            if (match.ownID() != ownModule.id()) {
                continue;
            }
            final Module otherModule =
                otherModules.stream().filter(module -> module.id() == match.otherID()).findAny().get();
            writer.write("\\subsubsection*{");
            writer.write(otherModule.name());
            writer.write("}\n\n");
            writer.write("\\noindent Umfang: ");
            writer.write(String.valueOf(match.hours()));
            writer.write(" von ");
            writer.write(String.valueOf(otherModule.hours()));
            writer.write(" Stunden\\\\\n");
            writer.write("Quelle: ");
            writer.write(otherModule.responsible());
            writer.write("\\\\\n\n");
            writer.write("\\noindent \\textit{Kompetenzen:}\\\\\n");
            for (final String competency : otherModule.competencies()) {
                writer.write(competency);
                writer.write("\n");
            }
            writer.write("\n");
        }
    }

    private static void writeOverview(
        final List<Module> ownModules,
        final List<Module> otherModules,
        final List<Match> matches,
        final Map<Integer, Integer> covered,
        final Map<Integer, Integer> taken,
        final BufferedWriter writer
    ) throws IOException {
        final Map<Integer, Integer> idToNode = new LinkedHashMap<Integer, Integer>();
        final Map<Integer, Integer> maxNodeConnections = new LinkedHashMap<Integer, Integer>();
        final Map<Integer, Integer> currentNodeConnections = new LinkedHashMap<Integer, Integer>();
        final List<Interval> verticalConnections = new ArrayList<Interval>();
        final int numOfOwnModules = ownModules.size();
        final boolean big = numOfOwnModules < otherModules.size();
        writer.write("\\section{\\\"Ubersicht}\n\n");
        writer.write("\\begin{center}\n");
        if (big) {
            writer.write("\\resizebox{!}{0.82\\paperheight}{%\n");
        }
        writer.write("\\begin{tikzpicture}\n");
        int node = 1;
        for (final Module module : ownModules) {
            Main.writeModule(node, module, null, true, Main.OWN_MODULE_WIDTH, covered, taken, writer);
            idToNode.put(module.id(), node);
            maxNodeConnections.put(node, 1);
            node++;
        }
        boolean first = true;
        final int width = big ? Main.FOREIGN_MODULE_WIDTH : Main.OWN_MODULE_WIDTH;
        final int right = big ? Main.MODULE_RIGHT_SEP_BIG : Main.MODULE_RIGHT_SEP_SMALL;
        for (final Module module : otherModules) {
            Main.writeModule(node, module, first ? String.valueOf(right) : null, false, width, covered, taken, writer);
            if (first) {
                first = false;
            }
            idToNode.put(module.id(), node);
            maxNodeConnections.put(node, 1);
            node++;
        }
        for (final Match match : matches) {
            final int ownNode = idToNode.get(match.ownID());
            final int otherNode = idToNode.get(match.otherID());
            maxNodeConnections.merge(ownNode, 1, Integer::sum);
            maxNodeConnections.merge(otherNode - numOfOwnModules, 1, Integer::sum);
            verticalConnections.add(new Interval(ownNode, otherNode - numOfOwnModules));
        }
        final Map<Integer, List<Interval>> slots = new LinkedHashMap<Integer, List<Interval>>();
        Main.fillSlots(verticalConnections, slots);
        final int maxSlot = slots.keySet().stream().max(Integer::compare).get();
        final int slotWidth = right * 10 - 2 * Main.CONNECTION_SLOT_PADDING - Main.OWN_MODULE_WIDTH;
        for (final Map.Entry<Integer, List<Interval>> entry : slots.entrySet()) {
            final int slot = entry.getKey();
            for (final Interval connection : entry.getValue()) {
                Main.drawConnection(
                    connection,
                    slot,
                    numOfOwnModules,
                    maxSlot,
                    slotWidth,
                    currentNodeConnections,
                    maxNodeConnections,
                    writer
                );
            }
        }
        final int lowestNode = numOfOwnModules > node - 1 - numOfOwnModules ? numOfOwnModules : node - 1;
        final int unused = Main.computeUnusedHours(otherModules, taken);
        Main.writeLegend(lowestNode, unused, big, writer);
        writer.write("\\end{tikzpicture}\n");
        if (big) {
            writer.write("}\n");
        }
        writer.write("\\end{center}\n\n");
    }

    private static void writePreamble(final String title, final BufferedWriter writer) throws IOException {
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
        writer.write("\\title{\\\"Aquivalenzpr\\\"ufung ");
        writer.write(title);
        writer.write("}\n\n");
        writer.write("\\begin{document}\n\n");
        writer.write("\\maketitle\n\n");
        writer.write("\\tableofcontents\n\n");
        writer.write("\\pagebreak\n\n");
    }

}
