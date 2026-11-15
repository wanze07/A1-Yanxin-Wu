/**
 * A dietitian: a health professional who advises on nutrition and therefore
 * records a focus area.
 *
 * <p>Reuses the shared data of {@link HealthProfessional} and adds the one
 * piece of information that identifies a dietitian.</p>
 *
 * @author Yanxin Wu
 * @version 1.0
 */
public class Dietitian extends HealthProfessional {

    /** The nutrition focus area, for example "Weight management". */
    private final String focusArea;

    /**
     * Creates a dietitian with an explicit roster.
     *
     * @param professionalId numeric identifier, must be positive
     * @param name           the dietitian's name, must not be blank
     * @param focusArea      the nutrition focus area, must not be blank
     * @param workingDays    one or more days of the week
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public Dietitian(int professionalId, String name, String focusArea, Weekday... workingDays) {
        super(professionalId, name, workingDays);
        if (focusArea == null || focusArea.trim().isEmpty()) {
            throw new IllegalArgumentException("Nutrition focus area cannot be empty.");
        }
        this.focusArea = focusArea.trim();
    }

    /**
     * Creates a full-time dietitian (Monday to Friday).
     *
     * @param professionalId numeric identifier, must be positive
     * @param name           the dietitian's name, must not be blank
     * @param focusArea      the nutrition focus area, must not be blank
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public Dietitian(int professionalId, String name, String focusArea) {
        this(professionalId, name, focusArea,
                Weekday.MONDAY, Weekday.TUESDAY, Weekday.WEDNESDAY,
                Weekday.THURSDAY, Weekday.FRIDAY);
    }

    /**
     * Returns the nutrition focus area of this dietitian.
     *
     * @return the focus area, never blank
     */
    public String getFocusArea() {
        return focusArea;
    }

    /**
     * {@inheritDoc}
     *
     * @return always "Dietitian"
     */
    @Override
    public String getRole() {
        return "Dietitian";
    }

    /**
     * {@inheritDoc}
     *
     * @return the focus area in a readable form
     */
    @Override
    public String getSpecialistDetails() {
        return "Focus area: " + focusArea;
    }
}
