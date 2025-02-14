package equivalencedocs;

@FunctionalInterface
public interface CheckedFunction<D, C, E extends Exception> {

    C apply(D input) throws E;

}
