package equivalencedocs;

public record Interval(int start, int end) {

    public int min() {
        return Math.min(this.start(), this.end());
    }

    public int max() {
        return Math.max(this.start(), this.end());
    }

}
