/**
 * A patient of the clinic: the person who books an appointment.
 *
 * <p>Part 4 asks whether a patient should be its own class. It should. A name
 * and a mobile number always travel together (an appointment is only valid when
 * both are present), and keeping them in one object means the validation rule
 * is written once instead of being repeated in every appointment, every filter
 * and every cancellation. It also gives Part 5 a natural way to decide whether
 * two appointments belong to the same person.</p>
 *
 * @author Student Name
 * @version 1.0
 */
public class Patient {

    /** Minimum number of digits accepted for a mobile number. */
    private static final int MIN_MOBILE_DIGITS = 8;

    /** Maximum number of digits accepted for a mobile number. */
    private static final int MAX_MOBILE_DIGITS = 15;

    /** Full name of the patient. */
    private final String name;

    /** Mobile phone number, stored as digits only. */
    private final String mobile;

    /**
     * Creates a patient.
     *
     * @param name   the patient's full name, must not be blank
     * @param mobile the mobile number, digits only (spaces are removed)
     * @throws IllegalArgumentException if the name or the number is missing or
     *                                  not a plausible phone number
     */
    public Patient(String name, String mobile) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient name cannot be empty.");
        }
        if (mobile == null || mobile.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient mobile number cannot be empty.");
        }

        // Store the number in a single predictable form so that filtering by
        // "0400 111 222" finds the appointment booked with "0400111222".
        String digitsOnly = mobile.trim().replace(" ", "");
        if (digitsOnly.length() < MIN_MOBILE_DIGITS || digitsOnly.length() > MAX_MOBILE_DIGITS) {
            throw new IllegalArgumentException(
                    "Patient mobile number must contain between " + MIN_MOBILE_DIGITS
                            + " and " + MAX_MOBILE_DIGITS + " digits.");
        }
        for (int i = 0; i < digitsOnly.length(); i++) {
            if (!Character.isDigit(digitsOnly.charAt(i))) {
                throw new IllegalArgumentException("Patient mobile number must contain digits only.");
            }
        }

        this.name = name.trim();
        this.mobile = digitsOnly;
    }

    /**
     * Returns the patient's full name.
     *
     * @return the name, never blank
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the patient's mobile number as digits only.
     *
     * @return the mobile number, never blank
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * Two patients are the same person when they share a mobile number, which
     * is what the clinic uses to identify a caller.
     *
     * @param other the object to compare with
     * @return {@code true} if both patients have the same mobile number
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Patient)) {
            return false;
        }
        Patient otherPatient = (Patient) other;
        return mobile.equals(otherPatient.mobile);
    }

    /**
     * Keeps {@code equals} and {@code hashCode} consistent.
     *
     * @return a hash code derived from the mobile number
     */
    @Override
    public int hashCode() {
        return mobile.hashCode();
    }

    /**
     * Returns the patient in a readable form.
     *
     * @return the name followed by the mobile number
     */
    @Override
    public String toString() {
        return name + " (mobile " + mobile + ")";
    }
}
