import java.util.ArrayList;

/**
 * The entry point required by the brief: a scripted simulation that exercises
 * the whole appointment system and prints its results to the console.
 *
 * <p>The main method is deliberately split into labelled sections matching
 * Parts 3 to 6 of the assessment, so a marker - or the video audience - can
 * follow the demonstration from start to finish. Helper methods keep the main
 * method short and avoid repeating the same printing loop at every step.</p>
 *
 * @author Yanxin Wu
 * @version 1.0
 */
public class AssignmentOne {

    /** Width of the separator lines used in the console output. */
    private static final String LINE =
            "--------------------------------------------------------------------------";

    /**
     * Runs the demonstration.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        System.out.println(LINE);
        System.out.println(" PROG2004 Assessment 1 - Health Service Appointment System");
        System.out.println(LINE);

        // ------------------------------------------------------------------
        // Part 3: build the professionals and display every one of them.
        // ------------------------------------------------------------------
        ArrayList<HealthProfessional> professionals = createProfessionals();
        printProfessionals("PART 3 - Health professionals working in the clinic", professionals);

        // ------------------------------------------------------------------
        // Part 4: a single appointment, shown with its full details.
        // ------------------------------------------------------------------
        System.out.println();
        System.out.println("PART 4 - One appointment, in full");
        Patient tom = new Patient("Tom Wilson", "0400 111 222");
        Appointment exampleAppointment = new Appointment(tom, professionals.get(0), "09:00",
                "headache and fever");
        System.out.println(exampleAppointment.getDetails());

        System.out.println();
        System.out.println("PART 4 - The same appointment class holding a different type of professional");
        Appointment exampleWithSurgeon = new Appointment(
                new Patient("Sarah Moore", "0400 333 444"), findProfessional(professionals, 301), "14:00",
                "knee pain");
        System.out.println(exampleWithSurgeon.getDetails());

        // An appointment refuses to exist if a required part is missing or
        // malformed: this is the "never in an invalid state" rule in action.
        demonstrateValidation(professionals);

        // ------------------------------------------------------------------
        // Parts 5 and 6: manage the collection, then show every operation.
        // ------------------------------------------------------------------
        AppointmentManager manager = new AppointmentManager();
        System.out.println();
        System.out.println("PART 5/6 - Clinic timetable: " + manager.getSchedule().getSlotCount()
                + " bookable slots, " + manager.getSchedule().getTimeSlotsText());

        System.out.println();
        System.out.println("PART 6 - Booking appointments (normal cases)");
        bookStandardAppointments(manager, professionals);

        System.out.println();
        System.out.println("PART 6 - Bookings that must be rejected (error cases)");
        System.out.println(manager.addAppointment(
                new Patient("Sarah Moore", "0400 333 444"), professionals.get(0), "09:00"));
        System.out.println(manager.addAppointment(
                new Patient("Amy Turner", "0400 666 777"), professionals.get(1), "10:15"));
        System.out.println(manager.addAppointment(
                null, professionals.get(2), "11:30"));

        // The collection is in booking order here, which is not time order.
        printAppointments("PART 5 - All appointments, as booked (not yet sorted)",
                manager.getAllAppointments());

        manager.sortAllAppointments();
        printAppointments("PART 5 - All appointments, sorted earliest first",
                manager.getAllAppointments());

        // Filtering by professional, then sorting only that subset.
        ArrayList<Appointment> gpOneAppointments = manager.filterByProfessionalId(101);
        manager.sortAppointments(gpOneAppointments);
        printAppointments("PART 5 - Appointments for professional ID 101 (Dr Alice Smith)",
                gpOneAppointments);

        printAppointments("PART 5 - Appointments for patient mobile 0400 777 888 (a subset, sorted)",
                sortSubset(manager, manager.filterByPatientMobile("0400 777 888")));

        ArrayList<Appointment> nurseAppointments = manager.filterByProfessionalId(201);
        System.out.println();
        System.out.println("PART 5 - Appointments for professional ID 201 (Nurse Clara Brown): "
                + nurseAppointments.size());

        // Cancelling one appointment by professional and time.
        System.out.println();
        System.out.println("PART 6 - Cancelling one appointment (normal case)");
        System.out.println(manager.cancelAppointment(301, "14:00"));

        // Cancelling something that does not exist must produce a warning.
        System.out.println();
        System.out.println("PART 6 - Cancelling an appointment that does not exist (error case)");
        System.out.println(manager.cancelAppointment(999, "10:00"));

        // Cancelling every appointment of one patient.
        System.out.println();
        System.out.println("PART 6 - Cancelling all appointments for one patient (by mobile)");
        System.out.println(manager.cancelAppointmentsForPatient("0400 777 888"));

        // The same request again finds nothing left to cancel.
        System.out.println();
        System.out.println("PART 6 - Cancelling again for the same patient (error case)");
        System.out.println(manager.cancelAppointmentsForPatient("0400 777 888"));

        printAppointments("PART 6 - The collection after the cancellations",
                manager.getAllAppointments());

        System.out.println();
        System.out.println("Summary");
        System.out.println("  Health professionals: " + professionals.size());
        System.out.println("  Appointments still booked: " + manager.getAppointmentCount());
        System.out.println(LINE);
        System.out.println(" End of demonstration");
        System.out.println(LINE);
    }

    /**
     * Creates the professionals used throughout the demonstration.
     *
     * <p>Three general practitioners plus one of each other type, which is the
     * mix Part 3 asks for.</p>
     *
     * @return a list containing one object of every professional type
     */
    private static ArrayList<HealthProfessional> createProfessionals() {
        // Weekday is an enum, so an invalid day name cannot be passed in.
        ArrayList<HealthProfessional> professionals = new ArrayList<>();
        professionals.add(new GeneralPractitioner(101, "Dr Alice Smith", "City Medical Clinic",
                Weekday.MONDAY, Weekday.WEDNESDAY, Weekday.FRIDAY));
        professionals.add(new GeneralPractitioner(102, "Dr Bob Lee", "Suburb Family Clinic",
                Weekday.TUESDAY, Weekday.THURSDAY));
        // Third general practitioner uses the convenience constructor:
        // Mondays to Fridays, without having to list all five days again.
        professionals.add(new GeneralPractitioner(103, "Dr Carol Ng", "Riverside Family Clinic"));
        professionals.add(new Nurse(201, "Clara Brown", "Emergency Ward",
                Weekday.MONDAY, Weekday.TUESDAY));
        professionals.add(new Nurse(202, "Ben Carter", "Outpatient Ward",
                Weekday.TUESDAY, Weekday.WEDNESDAY, Weekday.THURSDAY));
        professionals.add(new Surgeon(301, "Dr David Wong", "Cardiac Surgery",
                Weekday.MONDAY, Weekday.TUESDAY, Weekday.WEDNESDAY));
        professionals.add(new Dietitian(401, "Ms Eva Taylor", "Weight management",
                Weekday.THURSDAY, Weekday.FRIDAY));
        return professionals;
    }

    /**
     * Books the appointments used as the "everything went well" scenario.
     *
     * @param manager       the manager that stores the appointments
     * @param professionals the professionals created in Part 3
     */
    private static void bookStandardAppointments(AppointmentManager manager,
                                                 ArrayList<HealthProfessional> professionals) {
        System.out.println(manager.addAppointment(
                new Patient("Tom Wilson", "0400 111 222"), findProfessional(professionals, 101), "09:00",
                "headache and fever"));
        System.out.println(manager.addAppointment(
                new Patient("Lisa Hall", "0400 777 888"), findProfessional(professionals, 102), "09:00",
                "sore throat"));
        System.out.println(manager.addAppointment(
                new Patient("Sarah Moore", "0400 333 444"), findProfessional(professionals, 301), "14:00",
                "knee pain"));
        System.out.println(manager.addAppointment(
                new Patient("James Clark", "0400 555 666"), findProfessional(professionals, 201), "11:00",
                "wound dressing change"));
        System.out.println(manager.addAppointment(
                new Patient("Eva Brown", "0400 999 000"), findProfessional(professionals, 103), "08:30",
                "blood pressure review"));
        // Lisa books a second appointment, with a different professional, so the
        // "cancel every appointment of one patient" operation has something to do.
        System.out.println(manager.addAppointment(
                new Patient("Lisa Hall", "0400 777 888"), findProfessional(professionals, 401), "12:00",
                "dietary advice"));
    }

    /**
     * Finds a professional by identifier.
     *
     * <p>Only used to keep the demonstration readable: the manager still
     * receives objects, exactly as it would from a real user interface.</p>
     *
     * @param professionals  the list to search
     * @param professionalId the identifier to look for
     * @return the matching professional, or {@code null} if the ID is unknown
     */
    private static HealthProfessional findProfessional(ArrayList<HealthProfessional> professionals,
                                                       int professionalId) {
        HealthProfessional match = null;
        for (HealthProfessional professional : professionals) {
            if (professional.getProfessionalId() == professionalId) {
                match = professional;
            }
        }
        return match;
    }

    /**
     * Shows that invalid data is rejected at the moment an object is created.
     *
     * @param professionals the professionals to build the failed appointments
     *                      with
     */
    private static void demonstrateValidation(ArrayList<HealthProfessional> professionals) {
        System.out.println();
        System.out.println("PART 4 - Invalid objects are refused (error cases)");

        try {
            Appointment created = new Appointment(new Patient("Amy Turner", "0400 666 777"),
                    findProfessional(professionals, 401), "25:00");
            System.out.println("  Unexpected: 25:00 was accepted as " + created.getSummary());
        } catch (IllegalArgumentException error) {
            System.out.println("  Appointment with time 25:00 rejected: " + error.getMessage());
        }

        try {
            Appointment created = new Appointment(null, professionals.get(0), "10:00");
            System.out.println("  Unexpected: a missing patient was accepted as "
                    + created.getSummary());
        } catch (IllegalArgumentException error) {
            System.out.println("  Appointment without a patient rejected: " + error.getMessage());
        }

        try {
            Patient created = new Patient("Amy Turner", "0400");
            System.out.println("  Unexpected: mobile 0400 was accepted for " + created.getName());
        } catch (IllegalArgumentException error) {
            System.out.println("  Patient with mobile 0400 rejected: " + error.getMessage());
        }

        try {
            Patient created = new Patient("", "0400 666 777");
            System.out.println("  Unexpected: an empty name was accepted for " + created.getMobile());
        } catch (IllegalArgumentException error) {
            System.out.println("  Patient with no name rejected: " + error.getMessage());
        }
    }

    /**
     * Sorts a filtered list of appointments and returns it unchanged in content.
     *
     * <p>Part 5 requires sorting to work on a subset as well as on the whole
     * collection; this keeps that demonstration on one line in the main method.</p>
     *
     * @param manager the manager that performs the sort
     * @param subset  the filtered appointments to order
     * @return the same list, now in time order
     */
    private static ArrayList<Appointment> sortSubset(AppointmentManager manager,
                                                     ArrayList<Appointment> subset) {
        manager.sortAppointments(subset);
        return subset;
    }

    /**
     * Prints every professional, using one loop through the common base type.
     *
     * <p>This is polymorphism in practice: the loop never asks what type an
     * object is, yet each one describes itself in its own way.</p>
     *
     * @param title         heading printed above the list
     * @param professionals the professionals to print
     */
    private static void printProfessionals(String title, ArrayList<HealthProfessional> professionals) {
        System.out.println();
        System.out.println(title);
        for (int i = 0; i < professionals.size(); i++) {
            HealthProfessional professional = professionals.get(i);
            System.out.println("  " + (i + 1) + ". " + professional);
        }
        System.out.println("  Total: " + professionals.size() + " professionals");
    }

    /**
     * Prints a titled list of appointments, with a readable message when the
     * list is empty.
     *
     * @param title        heading printed above the list
     * @param appointments the appointments to print
     */
    private static void printAppointments(String title, ArrayList<Appointment> appointments) {
        System.out.println();
        System.out.println(title);
        if (appointments.isEmpty()) {
            System.out.println("  (no appointments)");
            return;
        }
        for (int i = 0; i < appointments.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + appointments.get(i).getSummary());
        }
        System.out.println("  Total: " + appointments.size() + " appointment(s)");
    }
}
