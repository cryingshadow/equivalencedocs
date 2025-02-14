package equivalencedocs;

import java.io.*;
import java.util.*;

import com.google.gson.*;

public class ModuleParser implements CheckedFunction<File, List<Module>, IOException> {

    @Override
    public List<Module> apply(final File input) throws IOException {
        try (FileReader reader = new FileReader(input)) {
            return new Gson().fromJson(reader, ModuleList.class);
        }
    }

}
