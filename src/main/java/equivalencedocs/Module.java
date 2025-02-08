package equivalencedocs;

import java.util.*;

public record Module(
    int id,
    String name,
    boolean own,
    int hours,
    CoverDegree covered,
    List<String> competencies,
    String responsible
) {}
