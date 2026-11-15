/**
 * The fixed set of days a health professional can work.
 *
 * <p>The clinic only operates on these seven values, so an enumeration is a
 * better fit than a free-text {@code String}: the compiler stops any caller
 * from passing "Fridy" or "sunday" and every professional is described in
 * exactly the same vocabulary.</p>
 *
 * @author Yanxin Wu
 * @version 1.0
 */
public enum Weekday {

    /** Monday. */
    MONDAY("Monday"),
    /** Tuesday. */
    TUESDAY("Tuesday"),
    /** Wednesday. */
    WEDNESDAY("Wednesday"),
    /** Thursday. */
    THURSDAY("Thursday"),
    /** Friday. */
    FRIDAY("Friday"),
    /** Saturday. */
    SATURDAY("Saturday"),
    /** Sunday. */
    SUNDAY("Sunday");

    /** The name that is shown to the user, for example "Monday". */
    private final String displayName;

    /**
     * Creates one of the seven day constants.
     *
     * @param displayName the readable name of the day
     */
    Weekday(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the readable name of the day.
     *
     * @return the name shown to the user, for example "Monday"
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the readable name of the day, so that printing a weekday never
     * shows the internal constant name in capitals.
     *
     * @return the readable name of the day
     */
    @Override
    public String toString() {
        return displayName;
    }
}
