/**
 * A nurse: a health professional who can take blood samples and who is
 * assigned to a ward.
 *
 * <p>Reuses the shared data of {@link HealthProfessional} and adds the one
 * piece of information that makes a nurse different from a doctor.</p>
 *
 * @author Yanxin Wu
 * @version 1.0
 */
public class Nurse extends HealthProfessional {

    /** The ward this nurse is assigned to. */
    private final String ward;

    /**
     * Creates a nurse with an explicit roster.
     *
     * @param professionalId numeric identifier, must be positive
     * @param name           the nurse's name, must not be blank
     * @param ward           the ward the nurse works on, must not be blank
     * @param workingDays    one or more days of the week
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public Nurse(int professionalId, String name, String ward, Weekday... workingDays) {
        super(professionalId, name, workingDays);
        if (ward == null || ward.trim().isEmpty()) {
            throw new IllegalArgumentException("Ward cannot be empty.");
        }
        this.ward = ward.trim();
    }

    /**
     * Creates a full-time nurse (Monday to Friday).
     *
     * @param professionalId numeric identifier, must be positive
     * @param name           the nurse's name, must not be blank
     * @param ward           the ward the nurse works on, must not be blank
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public Nurse(int professionalId, String name, String ward) {
        this(professionalId, name, ward,
                Weekday.MONDAY, Weekday.TUESDAY, Weekday.WEDNESDAY,
                Weekday.THURSDAY, Weekday.FRIDAY);
    }

    /**
     * Returns the ward this nurse is assigned to.
     *
     * @return the ward name, never blank
     */
    public String getWard() {
        return ward;
    }

    /**
     * {@inheritDoc}
     *
     * @return always "Nurse"
     */
    @Override
    public String getRole() {
        return "Nurse";
    }

    /**
     * {@inheritDoc}
     *
     * @return the ward in a readable form
     */
    @Override
    public String getSpecialistDetails() {
        return "Ward: " + ward;
    }
}
