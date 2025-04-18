package equivalencedocs;

public record Source(String source, String page) {

    @Override
    public String toString() {
        final String result = String.format("\\cite{%s}", this.source());
        if (this.page() != null && !this.page().isBlank()) {
            return String.format("%s, %s", result, this.page());
        }
        return result;
    }

}
