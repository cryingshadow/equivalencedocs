package equivalencedocs;

import java.util.*;

public record Match(int ownID, int otherID, int hours, Map<Integer, Integer> competencyMatch) {}
