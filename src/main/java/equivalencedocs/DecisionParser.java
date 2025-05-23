package equivalencedocs;

import java.io.*;

import com.google.gson.*;

public class DecisionParser implements CheckedFunction<File, DecisionList, IOException> {

    @Override
    public DecisionList apply(final File input) throws IOException {
        try (FileReader reader = new FileReader(input)) {
            return new Gson().fromJson(reader, DecisionList.class);
        }
    }

}
