package equivalencedocs;

import java.util.*;

public record Module(
    String id,
    String name,
    boolean own,
    int hours,
    List<String> competencies,
    String responsible,
    String checked,
    List<Source> sources
) {}
