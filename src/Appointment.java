/**
 * One booking: a patient, the health professional they will see, the starting
 * time of the appointment, and optionally the reason for the visit.
 *
 * <p>An appointment is only meaningful when its required parts are present, so
 * the constructor refuses to build a half-filled object. That is what the brief
 * means by "an appointment can never be created in an incomplete or
 * invalid state": the rule is enforced once, in the only place where an
 * appointment can come into existence, instead of being checked again at
 * every later step.</p>
 *
 * <p>The class implements {@link Comparable} so a whole list of appointments
 * can be sorted from the earliest to the latest with a single library call.
 * Because times use the fixed-width {@code "HH:mm"} format, comparing the
 * strings as text gives exactly the right order - no parsing is needed.</p>
 *
 * @author Student Name
 * @version 1.0
 */
public class Appointment implements Comparable<Appointment> {

    /** Text used when the patient did not describe a symptom. */
    public static final String NO_SYMPTOM = "Not recorded";

    /** The patient who booked the appointment. */
    private final Patient patient;

    /** The professional the patient will see. */
    private final HealthProfessional healthProfessional;

    /** Starting time of the appointment, format "HH:mm". */
    private final String timeSlot;

    /** What the patient is coming in for, for example "sore throat". */
    private final String symptom;

    /**
     * Creates an appointment without a recorded symptom.
     *
     * @param patient            the patient, must not be {@code null}
     * @param healthProfessional the professional, must not be {@code null}
     * @param timeSlot           the starting time in the 24-hour format
     *                           "HH:mm", must not be blank
     * @throws IllegalArgumentException if any part is missing or malformed
     */
    public Appointment(Patient patient, HealthProfessional healthProfessional, String timeSlot) {
        this(patient, healthProfessional, timeSlot, NO_SYMPTOM);
    }

    /**
     * Creates an appointment with the reason for the visit.
     *
     * <p>An appointment is only meaningful when all of its required parts are
     * present, so the constructor refuses to build a half-filled object. The
     * symptom is the one optional detail: a patient may not know what to call
     * their problem, and a booking should not be refused for that.</p>
     *
     * @param patient            the patient, must not be {@code null}
     * @param healthProfessional the professional, must not be {@code null}
     * @param timeSlot           the starting time in the 24-hour format
     *                           "HH:mm", must not be blank
     * @param symptom            what the patient is coming in for; may be
     *                           {@code null} or blank when not supplied
     * @throws IllegalArgumentException if any required part is missing or
     *                                  malformed
     */
    public Appointment(Patient patient, HealthProfessional healthProfessional,
                       String timeSlot, String symptom) {
        if (patient == null) {
            throw new IllegalArgumentException("An appointment must have a patient.");
        }
        if (healthProfessional == null) {
            throw new IllegalArgumentException("An appointment must have a health professional.");
        }
        if (!isValidTime(timeSlot)) {
            throw new IllegalArgumentException("Time slot must use the 24-hour format HH:mm, for example 09:30.");
        }

        this.patient = patient;
        this.healthProfessional = healthProfessional;
        this.timeSlot = timeSlot;
        this.symptom = normalizeSymptom(symptom);
    }

    /**
     * Returns what the patient is coming in for.
     *
     * @return the symptom text, or {@link #NO_SYMPTOM} when none was given
     */
    public String getSymptom() {
        return symptom;
    }

    /**
     * Turns a missing symptom into the standard placeholder and trims anything
     * the user typed.
     *
     * @param symptom the raw text, possibly {@code null} or blank
     * @return a usable symptom description
     */
    private static String normalizeSymptom(String symptom) {
        if (symptom == null || symptom.trim().isEmpty()) {
            return NO_SYMPTOM;
        }
        return symptom.trim();
    }


    /**
     * Returns the patient who booked the appointment.
     *
     * @return the patient
     */
    public Patient getPatient() {
        return patient;
    }

    /**
     * Returns the professional the patient will see.
     *
     * @return the health professional
     */
    public HealthProfessional getHealthProfessional() {
        return healthProfessional;
    }

    /**
     * Returns the starting time of the appointment.
     *
     * @return the time in the format "HH:mm"
     */
    public String getTimeSlot() {
        return timeSlot;
    }

    /**
     * Orders appointments by time, earliest first.
     *
     * <p>Two appointments at the same time are ordered by professional ID so
     * that the same collection always prints in the same order. The brief
     * allows either order, but a stable order makes the demonstration easier
     * to read.</p>
     *
     * @param other the appointment to compare with
     * @return a negative number if this appointment is earlier, zero if both
     *         start at the same time, a positive number if this one is later
     */
    @Override
    public int compareTo(Appointment other) {
        int timeComparison = this.timeSlot.compareTo(other.timeSlot);
        if (timeComparison != 0) {
            return timeComparison;
        }
        return Integer.compare(this.healthProfessional.getProfessionalId(),
                other.healthProfessional.getProfessionalId());
    }

    /**
     * Returns a one-line summary of the appointment, used when a whole list
     * of appointments is printed.
     *
     * <p>The symptom is only shown when the patient actually described one, so
     * a list of bookings does not become unreadable just because a few patients
     * did not say what was wrong.</p>
     *
     * @return the time, the patient, the reason and the professional on one line
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(timeSlot);
        summary.append(" | ").append(patient.getName());
        summary.append(" (").append(patient.getMobile()).append(")");
        summary.append(" -> ").append(healthProfessional.getRole());
        summary.append(" ").append(healthProfessional.getName());
        summary.append(" [ID ").append(healthProfessional.getProfessionalId()).append("]");
        if (!NO_SYMPTOM.equals(symptom)) {
            summary.append(" | Reason: ").append(symptom);
        }
        return summary.toString();
    }

    /**
     * Returns the full details of the appointment, including the details of
     * the chosen professional.
     *
     * <p>Delegating to the professional's own {@code toString()} keeps this
     * class free of any knowledge about the individual professional types.</p>
     *
     * @return a multi-line description of the appointment
     */
    public String getDetails() {
        StringBuilder details = new StringBuilder();
        details.append("Appointment at ").append(timeSlot).append("\n");
        details.append("    Patient : ").append(patient.getName());
        details.append(" (mobile ").append(patient.getMobile()).append(")\n");
        details.append("    Reason  : ").append(symptom).append("\n");
        details.append("    With    : ").append(healthProfessional);
        return details.toString();
    }

    /**
     * Returns the appointment as a readable single line.
     *
     * @return the summary of the appointment
     */
    @Override
    public String toString() {
        return getSummary();
    }

    /**
     * Checks that a string is a real 24-hour time.
     *
     * @param time the text to test
     * @return {@code true} if the text is "HH:mm" between 00:00 and 23:59
     */
    private static boolean isValidTime(String time) {
        if (time == null || time.length() != 5 || time.charAt(2) != ':') {
            return false;
        }
        for (int i = 0; i < time.length(); i++) {
            if (i != 2 && !Character.isDigit(time.charAt(i))) {
                return false;
            }
        }
        int hour = Integer.parseInt(time.substring(0, 2));
        int minute = Integer.parseInt(time.substring(3, 5));
        return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
    }
}