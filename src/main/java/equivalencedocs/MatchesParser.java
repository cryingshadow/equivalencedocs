package equivalencedocs;

import java.io.*;
import java.util.*;

import com.google.gson.*;

public class MatchesParser implements CheckedFunction<File, List<Match>, IOException> {

    @Override
    public List<Match> apply(final File input) throws IOException {
        try (FileReader reader = new FileReader(input)) {
            return new Gson().fromJson(reader, MatchList.class);
        }
    }

}
