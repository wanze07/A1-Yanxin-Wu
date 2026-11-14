/**
 * A surgeon: a specialist who performs operations and therefore records a
 * surgical specialty.
 *
 * <p>Reuses the shared data of {@link HealthProfessional} and adds the one
 * piece of information that identifies a surgeon.</p>
 *
 * @author Student Name
 * @version 1.0
 */
public class Surgeon extends HealthProfessional {

    /** The surgical specialty, for example "Cardiac Surgery". */
    private final String specialty;

    /**
     * Creates a surgeon with an explicit roster.
     *
     * @param professionalId numeric identifier, must be positive
     * @param name           the surgeon's name, must not be blank
     * @param specialty      the surgical specialty, must not be blank
     * @param workingDays    one or more days of the week
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public Surgeon(int professionalId, String name, String specialty, Weekday... workingDays) {
        super(professionalId, name, workingDays);
        if (specialty == null || specialty.trim().isEmpty()) {
            throw new IllegalArgumentException("Surgical specialty cannot be empty.");
        }
        this.specialty = specialty.trim();
    }

    /**
     * Creates a full-time surgeon (Monday to Friday).
     *
     * @param professionalId numeric identifier, must be positive
     * @param name           the surgeon's name, must not be blank
     * @param specialty      the surgical specialty, must not be blank
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public Surgeon(int professionalId, String name, String specialty) {
        this(professionalId, name, specialty,
                Weekday.MONDAY, Weekday.TUESDAY, Weekday.WEDNESDAY,
                Weekday.THURSDAY, Weekday.FRIDAY);
    }

    /**
     * Returns the surgical specialty of this surgeon.
     *
     * @return the specialty, never blank
     */
    public String getSpecialty() {
        return specialty;
    }

    /**
     * {@inheritDoc}
     *
     * @return always "Surgeon"
     */
    @Override
    public String getRole() {
        return "Surgeon";
    }

    /**
     * {@inheritDoc}
     *
     * @return the specialty in a readable form
     */
    @Override
    public String getSpecialistDetails() {
        return "Specialty: " + specialty;
    }
}
