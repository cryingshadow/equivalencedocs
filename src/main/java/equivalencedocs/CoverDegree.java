package equivalencedocs;

import java.util.*;

public enum CoverDegree {

    ALMOST_FULL(70, "fhdwlightgreen"),
    BARELY(50, "fhdworange"),
    FULL(80, "fhdwdarkgreen"),
    MOSTLY(60, "fhdwyellow"),
    NOT(0, "fhdwred");

    public static CoverDegree forCoverPercentage(final int percentage) {
        for (final CoverDegree degree : CoverDegree.getSortedDegrees()) {
            if (degree.threshold <= percentage) {
                return degree;
            }
        }
        return NOT;
    }

    public static List<CoverDegree> getSortedDegrees() {
        final List<CoverDegree> degrees = new ArrayList<CoverDegree>(Arrays.stream(CoverDegree.values()).toList());
        Collections.sort(
            degrees,
            new Comparator<CoverDegree>() {

                @Override
                public int compare(final CoverDegree o1, final CoverDegree o2) {
                    return Integer.compare(o2.threshold, o1.threshold);
                }

            }
        );
        return degrees;
    }

    public final String color;

    public final int threshold;

    private CoverDegree(final int threshold, final String color) {
        this.threshold = threshold;
        this.color = color;
    }

}
