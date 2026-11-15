/**
 * A general practitioner: the one health professional type every clinic in the
 * brief must have.
 *
 * <p>Everything shared with the other professionals (identifier, name, working
 * days) comes from {@link HealthProfessional}; the only thing this class adds is
 * the clinic the doctor practises at.</p>
 *
 * @author Yanxin Wu
 * @version 1.0
 */
public class GeneralPractitioner extends HealthProfessional {

    /** The clinic this doctor is based at. */
    private final String clinicName;

    /**
     * Creates a general practitioner with an explicit roster.
     *
     * @param professionalId numeric identifier, must be positive
     * @param name           the doctor's name, must not be blank
     * @param clinicName     the clinic the doctor works at, must not be blank
     * @param workingDays    one or more days of the week
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public GeneralPractitioner(int professionalId, String name, String clinicName, Weekday... workingDays) {
        super(professionalId, name, workingDays);
        if (clinicName == null || clinicName.trim().isEmpty()) {
            throw new IllegalArgumentException("Clinic name cannot be empty.");
        }
        this.clinicName = clinicName.trim();
    }

    /**
     * Creates a full-time general practitioner (Monday to Friday).
     *
     * @param professionalId numeric identifier, must be positive
     * @param name           the doctor's name, must not be blank
     * @param clinicName     the clinic the doctor works at, must not be blank
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public GeneralPractitioner(int professionalId, String name, String clinicName) {
        this(professionalId, name, clinicName,
                Weekday.MONDAY, Weekday.TUESDAY, Weekday.WEDNESDAY,
                Weekday.THURSDAY, Weekday.FRIDAY);
    }

    /**
     * Returns the clinic this doctor is based at.
     *
     * @return the clinic name, never blank
     */
    public String getClinicName() {
        return clinicName;
    }

    /**
     * {@inheritDoc}
     *
     * @return always "General Practitioner"
     */
    @Override
    public String getRole() {
        return "General Practitioner";
    }

    /**
     * {@inheritDoc}
     *
     * @return the clinic name in a readable form
     */
    @Override
    public String getSpecialistDetails() {
        return "Clinic: " + clinicName;
    }
}
