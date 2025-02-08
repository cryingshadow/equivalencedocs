package equivalencedocs;

public enum CoverDegree {

    ALMOST_FULL("fhdwlightgreen"), BARELY("fhdworange"), FULL("fhdwdarkgreen"), MOSTLY("fhdwyellow"), NOT("fhdwred");

    public final String color;

    private CoverDegree(final String color) {
        this.color = color;
    }

}
