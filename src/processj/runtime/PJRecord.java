package processj.runtime;

/**
 * @author Ben
 */
public interface PJRecord<T> {
    default String getPJRecordName() {
        return this.getClass().getSimpleName();
    }
}
