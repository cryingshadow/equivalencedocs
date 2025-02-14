package equivalencedocs;

import java.io.*;
import java.nio.file.*;

public class Main {

    public static void main(final String[] args) throws IOException {
        if (args == null || args.length != 7) {
            System.out.println(
                "Expected input: qualification, major, comments, ownModules, foreignModules, matches, output"
            );
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(args[6])))) {
            new Documentation(
                args[0],
                args[1],
                Files.readAllLines(new File(args[2]).toPath()),
                new ModuleParser().apply(new File(args[3])),
                new ModuleParser().apply(new File(args[4])),
                new MatchesParser().apply(new File(args[5]))
            ).write(writer);
        }
    }

}
