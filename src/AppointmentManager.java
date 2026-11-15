import java.util.ArrayList;
import java.util.Collections;

/**
 * Owns the single collection of appointments and every operation the clinic
 * performs on it: adding, viewing, filtering, sorting and cancelling.
 *
 * <p>Keeping the collection private inside one manager means no other class can
 * insert an appointment without passing the double-booking check. Every
 * operation that can fail returns a warning message instead of throwing, because
 * the brief asks for feedback the clinic staff can read, and because "the
 * professional is already booked" is an ordinary, expected outcome rather than
 * a programming error.</p>
 *
 * <p>One manager represents one clinic day; the brief limits bookings to the
 * current day, so no date is stored.</p>
 *
 * @author Yanxin Wu
 * @version 1.0
 */
public class AppointmentManager {

    /** The appointments of the day, kept in the order they were booked. */
    private final ArrayList<Appointment> appointments;

    /** The timetable that defines which times can be booked. */
    private final ClinicSchedule schedule;

    /**
     * Creates a manager for a given timetable.
     *
     * @param schedule the clinic's timetable, must not be {@code null}
     * @throws IllegalArgumentException if the timetable is missing
     */
    public AppointmentManager(ClinicSchedule schedule) {
        if (schedule == null) {
            throw new IllegalArgumentException("An appointment manager needs a clinic schedule.");
        }
        this.schedule = schedule;
        this.appointments = new ArrayList<>();
    }

    /**
     * Creates a manager for the standard timetable described in the brief
     * (08:00 to 16:00, every 30 minutes).
     */
    public AppointmentManager() {
        this(ClinicSchedule.createDefaultSchedule());
    }

    /**
     * Returns the timetable this manager works with.
     *
     * @return the clinic timetable
     */
    public ClinicSchedule getSchedule() {
        return schedule;
    }

    /**
     * Books an appointment without a recorded symptom.
     *
     * <p>Delegates to the four-argument version, so the checks exist in exactly
     * one place and this convenience method cannot drift out of step with it.</p>
     *
     * @param patient      the patient, may be {@code null}
     * @param professional the health professional, may be {@code null}
     * @param timeSlot     the requested starting time, format "HH:mm"
     * @return a message describing the result: "SUCCESS" or "WARNING"
     */
    public String addAppointment(Patient patient, HealthProfessional professional, String timeSlot) {
        return addAppointment(patient, professional, timeSlot, Appointment.NO_SYMPTOM);
    }

    /**
     * Books an appointment, or explains why it could not be booked.
     *
     * <p>The checks run in the order a receptionist would make them: is there a
     * patient, is there a professional, is the time one the clinic uses, is that
     * professional free at that time, and only then is the appointment object
     * created. The double-booking check compares both the professional and the
     * time slot, so two different professionals can both be booked at 09:00.</p>
     *
     * @param patient      the patient, may be {@code null}
     * @param professional the health professional, may be {@code null}
     * @param timeSlot     the requested starting time, format "HH:mm"
     * @param symptom      what the patient is coming in for; may be blank
     * @return a message describing the result: "SUCCESS" or "WARNING"
     */
    public String addAppointment(Patient patient, HealthProfessional professional,
                                 String timeSlot, String symptom) {
        if (patient == null) {
            return "WARNING: Cannot book an appointment without a patient.";
        }
        if (professional == null) {
            return "WARNING: Cannot book an appointment without a health professional.";
        }
        if (!schedule.isAvailableSlot(timeSlot)) {
            return "WARNING: " + timeSlot + " is not a clinic time slot. Available times: "
                    + schedule.getTimeSlotsText() + ".";
        }

        Appointment existing = findAppointment(professional.getProfessionalId(), timeSlot);
        if (existing != null) {
            return "WARNING: Double booking rejected. " + professional.getName()
                    + " [ID " + professional.getProfessionalId() + "] already has "
                    + existing.getPatient().getName() + " booked at " + timeSlot + ".";
        }

        try {
            Appointment appointment = new Appointment(patient, professional, timeSlot, symptom);
            appointments.add(appointment);
            return "SUCCESS: Appointment booked. " + appointment.getSummary();
        } catch (IllegalArgumentException error) {
            return "WARNING: Appointment could not be created - " + error.getMessage();
        }
    }

    /**
     * Returns every appointment of the day.
     *
     * <p>A copy is returned so a caller cannot add or remove appointments
     * behind the manager's back.</p>
     *
     * @return a new list of all appointments in booking order
     */
    public ArrayList<Appointment> getAllAppointments() {
        return new ArrayList<>(appointments);
    }

    /**
     * Returns the number of appointments currently booked.
     *
     * @return the size of the collection
     */
    public int getAppointmentCount() {
        return appointments.size();
    }

    /**
     * Returns every appointment for one professional.
     *
     * @param professionalId the professional's numeric identifier
     * @return a new list of matching appointments, possibly empty
     */
    public ArrayList<Appointment> filterByProfessionalId(int professionalId) {
        ArrayList<Appointment> matches = new ArrayList<>();
        for (Appointment appointment : appointments) {
            if (appointment.getHealthProfessional().getProfessionalId() == professionalId) {
                matches.add(appointment);
            }
        }
        return matches;
    }

    /**
     * Returns every appointment for one patient, identified by mobile number.
     *
     * @param mobile the patient's mobile number, digits only
     * @return a new list of matching appointments, possibly empty
     */
    public ArrayList<Appointment> filterByPatientMobile(String mobile) {
        ArrayList<Appointment> matches = new ArrayList<>();
        if (mobile == null || mobile.trim().isEmpty()) {
            return matches;
        }
        String digitsOnly = mobile.trim().replace(" ", "");
        for (Appointment appointment : appointments) {
            if (appointment.getPatient().getMobile().equals(digitsOnly)) {
                matches.add(appointment);
            }
        }
        return matches;
    }

    /**
     * Sorts one collection of appointments from earliest to latest.
     *
     * <p>This is the version used for a filtered subset: the caller passes the
     * list it wants ordered, and the manager sorts that list in place.</p>
     *
     * @param appointmentsToSort the list to sort, may be {@code null}
     */
    public void sortAppointments(ArrayList<Appointment> appointmentsToSort) {
        if (appointmentsToSort == null) {
            return;
        }
        Collections.sort(appointmentsToSort);
    }

    /**
     * Sorts the manager's own collection from earliest to latest.
     *
     * <p>Sorting the stored collection, rather than a copy, means the
     * "view all appointments" operation keeps returning a day in the order the
     * clinic actually works through it.</p>
     */
    public void sortAllAppointments() {
        Collections.sort(appointments);
    }

    /**
     * Cancels the appointment of one professional at one time.
     *
     * @param professionalId the professional's numeric identifier
     * @param timeSlot       the starting time, format "HH:mm"
     * @return a message describing the result: "SUCCESS" or "WARNING"
     */
    public String cancelAppointment(int professionalId, String timeSlot) {
        Appointment existing = findAppointment(professionalId, timeSlot);
        if (existing == null) {
            return "WARNING: No appointment found for professional ID " + professionalId
                    + " at " + timeSlot + ". Nothing was cancelled.";
        }
        appointments.remove(existing);
        return "SUCCESS: Cancelled " + existing.getPatient().getName() + "'s appointment with "
                + existing.getHealthProfessional().getName() + " at " + timeSlot + ".";
    }

    /**
     * Cancels every appointment belonging to one patient.
     *
     * <p>The list is walked backwards so that removing an element never shifts
     * the items that have not been inspected yet.</p>
     *
     * @param mobile the patient's mobile number, digits only
     * @return a message describing the result: "SUCCESS" or "WARNING"
     */
    public String cancelAppointmentsForPatient(String mobile) {
        ArrayList<Appointment> matches = filterByPatientMobile(mobile);
        if (matches.isEmpty()) {
            return "WARNING: No appointments found for patient " + mobile + ". Nothing was cancelled.";
        }
        for (int i = appointments.size() - 1; i >= 0; i--) {
            if (matches.contains(appointments.get(i))) {
                appointments.remove(i);
            }
        }
        return "SUCCESS: Cancelled " + matches.size() + " appointment(s) for patient " + mobile + ".";
    }

    /**
     * Finds one booking by professional and time.
     *
     * @param professionalId the professional's numeric identifier
     * @param timeSlot       the starting time, format "HH:mm"
     * @return the matching appointment, or {@code null} if the professional is
     *         free at that time
     */
    private Appointment findAppointment(int professionalId, String timeSlot) {
        for (Appointment appointment : appointments) {
            if (appointment.getHealthProfessional().getProfessionalId() == professionalId
                    && appointment.getTimeSlot().equals(timeSlot)) {
                return appointment;
            }
        }
        return null;
    }
}
