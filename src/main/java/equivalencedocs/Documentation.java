package equivalencedocs;

import java.io.*;
import java.util.*;

public class Documentation {

    static final int CONNECTION_SLOT_PADDING = 5;

    static final int MODULE_INNER_HEIGHT = 13;

    static final int OWN_MODULE_WIDTH = 57;

    private static final int FOREIGN_MODULE_WIDTH = 120;

    private static final String MODULE_OUTER_HEIGHT = "1.5";

    private static final int MODULE_RIGHT_SEP_BIG = 14;

    private static final int MODULE_RIGHT_SEP_SMALL = 10;

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

    private final List<String> comments;

    private final Map<Integer, Integer> covered;

    private final List<Module> foreignModules;

    private final String major;

    private final List<Match> matches;

    private final List<Module> ownModules;

    private final String qualification;

    private final Map<Integer, Integer> taken;

    public Documentation(
        final String qualification,
        final String major,
        final List<String> comments,
        final List<Module> ownModules,
        final List<Module> foreignModules,
        final List<Match> matches
    ) {
        this.qualification = qualification;
        this.major = major;
        this.matches = matches;
        this.ownModules = ownModules;
        this.foreignModules = foreignModules;
        this.comments = comments;
        this.covered = new LinkedHashMap<Integer, Integer>();
        this.taken = new LinkedHashMap<Integer, Integer>();
        for (final Match match : matches) {
            this.covered.merge(match.ownID(), match.hours(), Integer::sum);
            this.taken.merge(match.otherID(), match.hours(), Integer::sum);
        }
    }

    public void write(final BufferedWriter writer) throws IOException {
        this.writePreamble(writer);
        this.writeComments(writer);
        this.writeOverview(writer);
        for (final Module ownModule : this.ownModules) {
            this.writeModuleDetails(
                ownModule,
                this.covered.getOrDefault(ownModule.id(), 0),
                writer
            );
        }
        this.writeDecision(writer);
        writer.write("\\pagebreak\n\n");
        writer.write("\\printbibliography[heading=bibintoc,title={Quellenverzeichnis}]\n\n");
        writer.write("\\end{document}\n");
    }

    private int computeUnusedHours() {
        int result = 0;
        for (final Module module : this.foreignModules) {
            result += module.hours() - this.taken.getOrDefault(module.id(), 0);
        }
        return result;
    }

    private void writeComments(final BufferedWriter writer) throws IOException {
        writer.write("\\section{Vorbemerkungen}\n\n");
        writer.write("Dieses Dokument beschreibt die Äquivalenzprüfung der \\textit{");
        writer.write(this.qualification);
        writer.write("} und dem ersten Studienjahr unseres Studiengangs \\textit{");
        writer.write(this.major);
        writer.write("}. Dazu werden die Module aus dem ersten Jahr unseres Studiengangs den Lehreinheiten ");
        writer.write("aus der Ausbildung so zugeordnet, dass eine möglichst gute inhaltliche und umfängliche ");
        writer.write("Abdeckung unserer Module gewährleistet ist. Allgemein wird eine Abdeckung von 80\\% als ");
        writer.write("vollkommen ausreichend für eine Äquivalenzprüfung betrachtet. Bei einer Abdeckung zwischen ");
        writer.write("60\\% und 80\\% wird es allgemein als eine Ermessensentscheidung angesehen, inwiefern die ");
        writer.write("Äquivalenzprüfung als erfolgreich betrachtet wird. In diesen Fällen können auch zusätzliche ");
        writer.write("Auflagen an die Anrechnung der zugehörigen Module geknüpft werden. Bei einer Abdeckung unter ");
        writer.write("60\\% wird allgemein nicht von einer Äquivalenz ausgegangen.\n\n");
        writer.write("Diese Angaben sind jedoch reine Erfahrungswerte von Hochschulen. Rechtlich obliegt es jeder ");
        writer.write("Hochschule, ihre Entscheidung für oder gegen eine Äquivalenzprüfung frei zu treffen, ohne ");
        writer.write("spezifische Abdeckungsmengen einhalten zu müssen. Im Gegenteil besteht bei individueller ");
        writer.write("Anrechnung sogar das Gebot, bis zu 50\\% der eigenen Lehrinhalte anzurechnen, falls ");
        writer.write("entsprechende Kompetenzen nachgewiesen werden (vgl.~\\cite{hrkmodus}). Dabei ist die ");
        writer.write("Hochschule selbst für die Einhaltung ihrer eigenen Qualitätsstandards verantwortlich. Deshalb ");
        writer.write("wurden die vorliegenden Abdeckungseinschätzungen von den jeweils angegebenen ");
        writer.write("modulverantwortlichen Personen inhaltlich geprüft. Das Datum dieser Prüfungen ist bei den ");
        writer.write("jeweiligen Modulen vermerkt.\n\n");
        writer.write("Zunächst erfolgt eine grafische Übersicht über die Abdeckungsbeziehungen. Anschließend wird ");
        writer.write("für jedes Modul detailliert aufgelistet, welche Lehreinheiten zu welchem Umfang für die ");
        writer.write("Abdeckung herangezogen wurden. Inhaltlich werden dafür die in den Modulen bzw.\\ Lehreinheiten ");
        writer.write("zu vermittelnden Kompetenzen betrachtet. Dies entspricht dem allgemein empfohlenen ");
        writer.write("Vorgehen \\cite{hrkmodus}, da die genauen Inhalte aufgrund der unterschiedlichen Natur von ");
        writer.write("Lehreinheiten in beispielsweise praktischen Ausbildungen und unseren Modulen häufig nicht ");
        writer.write("direkt miteinander vergleichbar sind. Die Angaben über die jeweiligen Kompetenzen sind ");
        writer.write("wörtliche Auszüge aus unseren Modulhandbüchern bzw.\\ den jeweils angegebenen Quellen.\n\n");
        for (final String line : this.comments) {
            writer.write(line);
            writer.write("\n");
        }
        writer.write("\n");
    }

    private void writeDecision(final BufferedWriter writer) throws IOException {
        writer.write("\\pagebreak\n\n");
        writer.write("\\section{Entscheidung des Pr\\\"ufungsausschusses}\n\n");
        writer.write("Der erfolgreiche Abschluss der ");
        writer.write(this.qualification);
        writer.write(" f\\\"uhrt zur pauschalen Anrechnung der Module\n\n");
        writer.write("\\begin{center}\n");
        writer.write("\\renewcommand{\\arraystretch}{1.5}\n");
        writer.write("\\begin{tabular}{|l|c|c|}\n");
        writer.write("\\hline\n");
        writer.write("\\textbf{Modul} & \\phantom{x}\\textbf{Ja}\\phantom{x} & \\textbf{Nein}\\\\\\hline\n");
        for (final Module module : this.ownModules) {
            writer.write(module.name());
            writer.write(" & & \\\\\\hline\n");
        }
        writer.write("\\end{tabular}\n");
        writer.write("\\renewcommand{\\arraystretch}{1}\n");
        writer.write("\\end{center}\n\n");
        writer.write("\\noindent aus unseren Studiengang ");
        writer.write(this.major);
        writer.write(" unter den folgenden Auflagen:\n\n");
        writer.write("\\vfill\n\n");
        writer.write("\\begin{tikzpicture}\n");
        writer.write("\\node (place) {Ort, Datum};\n");
        writer.write("\\node (sig) [right=7 of place] {Unterschrift};\n");
        writer.write("\\draw[thick] ($(place.north west)+(-2,0.1)$) -- ($(place.north east)+(2,0.1)$);\n");
        writer.write("\\draw[thick] ($(sig.north west)+(-2,0.1)$) -- ($(sig.north east)+(2,0.1)$);\n");
        writer.write("\\end{tikzpicture}\n\n");
    }

    private void writeModule(
        final int node,
        final Module module,
        final String right,
        final boolean own,
        final int width,
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
                writer.write(Documentation.MODULE_OUTER_HEIGHT);
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
                CoverDegree.forCoverPercentage(this.covered.getOrDefault(module.id(), 0) * 100 / module.hours()).color
            );
        } else {
            writer.write("\\draw[black");
        }
        writer.write("] (n");
        writer.write(String.valueOf(node));
        writer.write(") rectangle ++(");
        writer.write(String.valueOf(width));
        writer.write("mm, -");
        writer.write(String.valueOf(Documentation.MODULE_INNER_HEIGHT));
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
            writer.write(String.valueOf(this.taken.getOrDefault(module.id(), 0)));
            writer.write(" / ");
        }
        writer.write(String.valueOf(module.hours()));
        writer.write(" Stunden}\\end{minipage}};\n");
    }

    private void writeModuleDetails(
        final Module ownModule,
        final int covered,
        final BufferedWriter writer
    ) throws IOException {
        final boolean isChecked = ownModule.checked() != null && !ownModule.checked().isBlank();
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
        writer.write("\\%)\\\\\n");
        if (isChecked) {
            writer.write("Geprüft durch ");
            writer.write(ownModule.responsible());
            writer.write(" (modulverantwortlich) am ");
            writer.write(ownModule.checked());
            writer.write(".");
        } else {
            writer.write("modulverantwortlich: ");
            writer.write(ownModule.responsible());
        }
        writer.write("\n\n");
        writer.write("\\subsection*{Kompetenzen}\n\n");
        for (final String competency : ownModule.competencies()) {
            writer.write(competency);
            writer.write("\n");
        }
        writer.write("\n\\subsection*{Abdeckung}\n\n");
        for (final Match match : this.matches) {
            if (match.ownID() != ownModule.id()) {
                continue;
            }
            final Module foreignModule =
                this.foreignModules.stream().filter(module -> module.id() == match.otherID()).findAny().get();
            writer.write("\\subsubsection*{");
            writer.write(foreignModule.name());
            writer.write("}\n\n");
            writer.write("\\noindent Umfang: ");
            writer.write(String.valueOf(match.hours()));
            writer.write(" von ");
            writer.write(String.valueOf(foreignModule.hours()));
            writer.write(" Stunden\\\\\n");
            writer.write("Quelle: \\cite{");
            writer.write(foreignModule.responsible());
            writer.write("}\\\\\n\n");
            writer.write("\\noindent \\textit{Kompetenzen:}\\\\\n");
            for (final String competency : foreignModule.competencies()) {
                writer.write(competency);
                writer.write("\n");
            }
            writer.write("\n");
        }
    }

    private void writeOverview(final BufferedWriter writer) throws IOException {
        final Map<Integer, Integer> idToNode = new LinkedHashMap<Integer, Integer>();
        final Map<Integer, Integer> maxNodeConnections = new LinkedHashMap<Integer, Integer>();
        final List<Interval> verticalConnections = new ArrayList<Interval>();
        final int numOfOwnModules = this.ownModules.size();
        final boolean big = numOfOwnModules < this.foreignModules.size();
        writer.write("\\section{\\\"Ubersicht}\n\n");
        writer.write("\\begin{center}\n");
        if (big) {
            writer.write("\\resizebox{!}{0.82\\paperheight}{%\n");
        }
        writer.write("\\begin{tikzpicture}\n");
        int node = 1;
        for (final Module module : this.ownModules) {
            this.writeModule(node, module, null, true, Documentation.OWN_MODULE_WIDTH, writer);
            idToNode.put(module.id(), node);
            maxNodeConnections.put(node, 1);
            node++;
        }
        boolean first = true;
        final int width = big ? Documentation.FOREIGN_MODULE_WIDTH : Documentation.OWN_MODULE_WIDTH;
        final int right = big ? Documentation.MODULE_RIGHT_SEP_BIG : Documentation.MODULE_RIGHT_SEP_SMALL;
        for (final Module module : this.foreignModules) {
            this.writeModule(node, module, first ? String.valueOf(right) : null, false, width, writer);
            if (first) {
                first = false;
            }
            idToNode.put(module.id(), node);
            maxNodeConnections.put(node, 1);
            node++;
        }
        for (final Match match : this.matches) {
            final int ownNode = idToNode.get(match.ownID());
            final int otherNode = idToNode.get(match.otherID());
            maxNodeConnections.merge(ownNode, 1, Integer::sum);
            maxNodeConnections.merge(otherNode - numOfOwnModules, 1, Integer::sum);
            verticalConnections.add(new Interval(ownNode, otherNode - numOfOwnModules));
        }
        final int slotWidth = right * 10 - 2 * Documentation.CONNECTION_SLOT_PADDING - Documentation.OWN_MODULE_WIDTH;
        final ConnectionSlots slots =
            new ConnectionSlots(numOfOwnModules, slotWidth, maxNodeConnections, verticalConnections);
        slots.drawConnections(writer);
        final int lowestNode = numOfOwnModules > node - 1 - numOfOwnModules ? numOfOwnModules : node - 1;
        final int unused = this.computeUnusedHours();
        Documentation.writeLegend(lowestNode, unused, big, writer);
        writer.write("\\end{tikzpicture}\n");
        if (big) {
            writer.write("}\n");
        }
        writer.write("\\end{center}\n\n");
    }

    private void writePreamble(final BufferedWriter writer) throws IOException {
        writer.write("\\documentclass{article}\n\n");
        writer.write("\\usepackage[ngerman]{babel}\n");
        writer.write("\\usepackage[T1]{fontenc}\n");
        writer.write("\\usepackage[a4paper,margin=2cm]{geometry}\n");
        writer.write("\\usepackage{xcolor}\n");
        writer.write("\\usepackage{biblatex}\n");
        writer.write("\\addbibresource{references.bib}\n");
        writer.write("\\usepackage{tikz}\n");
        writer.write("\\usetikzlibrary{calc,positioning}\n\n");
        writer.write("\\colorlet{fhdwdarkgreen}{green!80!black}\n");
        writer.write("\\colorlet{fhdwlightgreen}{green!50!white}\n");
        writer.write("\\colorlet{fhdwyellow}{yellow}\n");
        writer.write("\\colorlet{fhdworange}{orange}\n");
        writer.write("\\colorlet{fhdwred}{red}\n\n");
        writer.write("\\title{\\\"Aquivalenzpr\\\"ufung ");
        writer.write(this.qualification);
        writer.write(" und ");
        writer.write(this.major);
        writer.write("}\n\n");
        writer.write("\\begin{document}\n\n");
        writer.write("\\maketitle\n\n");
        writer.write("\\tableofcontents\n\n");
        writer.write("\\pagebreak\n\n");
    }

}
