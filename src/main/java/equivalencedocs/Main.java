package equivalencedocs;

import java.io.*;
import java.util.*;

import com.google.gson.*;

public class Main {

    public static void main(final String[] args) throws IOException {
        final File ownModulesFile = new File(args[0]);
        final File otherModulesFile = new File(args[1]);
        final File matchesFile = new File(args[2]);
        final File output = new File(args[3]);
        try (
            FileReader ownReader = new FileReader(ownModulesFile);
            FileReader otherReader = new FileReader(otherModulesFile);
            FileReader matchesReader = new FileReader(matchesFile);
            BufferedWriter writer = new BufferedWriter(new FileWriter(output))
        ) {
            final List<Module> ownModules = new Gson().fromJson(ownReader, ModuleList.class);
            writer.write("\\documentclass{article}\n\n");
            writer.write("\\usepackage[ngerman]{babel}\n");
            writer.write("\\usepackage[T1]{fontenc}\n");
            writer.write("\\usepackage[a4paper,margin=2cm]{geometry}\n");
            writer.write("\\usepackage{tikz}\n");
            writer.write("\\usetikzlibrary{positioning}\n\n");
            writer.write("\\pagestyle{empty}\n\n");
            writer.write("\\begin{document}\n");
            writer.write("\\begin{tikzpicture}\n");
            writer.write("\\coordinate (n0) at (0,0);\n");
            int node = 1;
            for (final Module module : ownModules) {
                writer.write("\\node[draw=black] (n");
                writer.write(String.valueOf(node));
                writer.write(") [below=0.1 of n");
                writer.write(String.valueOf(node - 1));
                node++;
                writer.write(".south west,anchor=north west] {");
                writer.write(module.name());
                writer.write("};\n");
            }
            writer.write("\\end{tikzpicture}\n");
            writer.write("\\end{document}\n");
        }
    }

}
