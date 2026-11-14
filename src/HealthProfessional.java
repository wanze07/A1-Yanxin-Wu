import java.util.ArrayList;

/**
 * The generic health professional: everything that is true for every type of
 * professional in the clinic, and nothing that is true only for a few of them.
 *
 * <p>This class is {@code abstract} because "a health professional with no
 * specialty" is not a real thing in this system. Making it abstract means the
 * compiler refuses {@code new HealthProfessional(...)}, so every object that
 * reaches an appointment is guaranteed to be a real, specialised professional.
 * That is the design answer to Part 2's question about a "plain/generic"
 * professional: the base type describes behaviour, the sub-types supply it.</p>
 *
 * <p>All fields are {@code private} and {@code final}. External code can read
 * the data through getters, but nothing outside this class can move an object
 * into an invalid state - the only way in is through a constructor that
 * validates its arguments.</p>
 *
 * @author Student Name
 * @version 1.0
 */
public abstract class HealthProfessional {

    /** Numeric identifier, unique inside the clinic. */
    private final int professionalId;

    /** Full name of the professional, for example "Dr Alice Smith". */
    private final String name;

    /** The days this professional works; a subset of {@link Weekday}. */
    private final ArrayList<Weekday> workingDays;

    /**
     * Creates a professional with an explicit set of working days.
     *
     * @param professionalId numeric identifier, must be positive
     * @param name           the professional's name, must not be blank
     * @param workingDays    one or more days of the week
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public HealthProfessional(int professionalId, String name, Weekday... workingDays) {
        if (professionalId <= 0) {
            throw new IllegalArgumentException("Professional ID must be a positive number.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Professional name cannot be empty.");
        }
        if (workingDays == null || workingDays.length == 0) {
            throw new IllegalArgumentException("A professional must work on at least one day.");
        }

        this.professionalId = professionalId;
        this.name = name.trim();
        this.workingDays = new ArrayList<>();

        // Copy the arguments into our own list and drop duplicates, so the
        // object owns its data and cannot be changed from the outside.
        for (int i = 0; i < workingDays.length; i++) {
            Weekday day = workingDays[i];
            if (day == null) {
                throw new IllegalArgumentException("A working day cannot be null.");
            }
            if (!this.workingDays.contains(day)) {
                this.workingDays.add(day);
            }
        }
    }

    /**
     * Convenience constructor for the common case: Monday to Friday.
     *
     * <p>Part 1 asks whether a second constructor is worthwhile. It is, because
     * a full-time professional works the standard week far more often than any
     * other pattern, and this saves every caller from repeating five arguments
     * (and from forgetting one of them).</p>
     *
     * @param professionalId numeric identifier, must be positive
     * @param name           the professional's name, must not be blank
     */
    public HealthProfessional(int professionalId, String name) {
        this(professionalId, name,
                Weekday.MONDAY, Weekday.TUESDAY, Weekday.WEDNESDAY,
                Weekday.THURSDAY, Weekday.FRIDAY);
    }

    /**
     * Returns the numeric identifier of this professional.
     *
     * @return the identifier, always positive
     */
    public int getProfessionalId() {
        return professionalId;
    }

    /**
     * Returns the name of this professional.
     *
     * @return the name, never blank
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a copy of the working days, so a caller cannot modify the
     * professional's roster.
     *
     * @return a new list containing the working days
     */
    public ArrayList<Weekday> getWorkingDays() {
        return new ArrayList<>(workingDays);
    }

    /**
     * Reports whether this professional works on a given day.
     *
     * @param day the day to test
     * @return {@code true} if the professional works that day
     */
    public boolean worksOn(Weekday day) {
        return workingDays.contains(day);
    }

    /**
     * Supplies the name of the professional's role, for example
     * "General Practitioner".
     *
     * <p>Abstract, so every sub-type must state what it is; that is how the
     * system can tell the types apart when they are printed.</p>
     *
     * @return the role name
     */
    public abstract String getRole();

    /**
     * Supplies the details that are unique to this sub-type, for example the
     * clinic a general practitioner belongs to.
     *
     * @return a short description of the sub-type's own data
     */
    public abstract String getSpecialistDetails();

    /**
     * Builds the part of the description that every professional shares.
     *
     * <p>Protected, because it is a building block for the sub-types rather
     * than part of the public API.</p>
     *
     * @return a formatted description of the shared data
     */
    protected String getSharedDetails() {
        StringBuilder description = new StringBuilder();
        description.append("ID: ").append(professionalId);
        description.append(", Name: ").append(name);
        description.append(", Works: ");
        for (int i = 0; i < workingDays.size(); i++) {
            if (i > 0) {
                description.append("/");
            }
            description.append(workingDays.get(i));
        }
        return description.toString();
    }

    /**
     * Returns a complete, readable description of this professional.
     *
     * <p>Implemented once here, so {@code System.out.println(professional)}
     * shows the shared data plus whatever each sub-type adds.</p>
     *
     * @return a one-line description of the professional
     */
    @Override
    public String toString() {
        return "[" + getRole() + "] " + getSharedDetails() + " | " + getSpecialistDetails();
    }

    /**
     * Two professionals are the same person when their identifiers match.
     *
     * <p>This is the rule Part 5 needs: an appointment clashes with another
     * appointment when they belong to the same professional and start at the
     * same time. Comparing IDs is safer than comparing object references, which
     * would treat two objects built from the same row of data as different
     * people.</p>
     *
     * @param other the object to compare with
     * @return {@code true} if both describe the same professional
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HealthProfessional)) {
            return false;
        }
        HealthProfessional otherProfessional = (HealthProfessional) other;
        return professionalId == otherProfessional.professionalId;
    }

    /**
     * Keeps {@code equals} and {@code hashCode} consistent.
     *
     * @return a hash code derived from the identifier
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(professionalId);
    }
}
