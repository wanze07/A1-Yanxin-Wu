import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * An interactive front desk for the appointment system: the user types a
 * patient's name, mobile number, symptom, chosen professional and time slot,
 * and the program books, lists or cancels the appointment.
 *
 * <p>This class is an <em>extra</em> to the assessment, not part of it. The
 * graded demonstration required by the brief stays in {@link AssignmentOne},
 * which runs the whole system automatically. This console exists so the same
 * classes can be driven by a real person, which is the quickest way to see how
 * the objects behave together.</p>
 *
 * <p>The professional objects are fixed, exactly as requested: the clinic's
 * staff does not change, only the bookings do. Everything the user types is
 * validated by the existing classes - a bad mobile number is rejected by
 * {@link Patient}, a bad time by {@link Appointment}, and a double booking by
 * {@link AppointmentManager} - so the console contains no business rules of its
 * own and cannot disagree with the system it is driving.</p>
 *
 * @author Yanxin Wu
 * @version 1.0
 */
public class AppointmentConsole {

    /**
     * Single reader for the whole session.
     *
     * <p>Created in {@link #main(String[])} inside a try-with-resources block, so
     * the reader is closed even when the loop ends through an exception.</p>
     */
    private static Scanner input;

    /** Width of the separator lines used in the console output. */
    private static final String LINE =
            "--------------------------------------------------------------------------";

    /**
     * Where the bookings are kept between runs.
     *
     * <p>Created next to the project, so the desk still has its appointments
     * after the window is closed. The file is plain text on purpose: it is easy
     * to read, and it never decides anything - every restored booking goes back
     * through the same validation as a typed one.</p>
     */
    private static final String DATA_FILE = "appointments.txt";

    /** The clinic staff, created once when the program starts. */
    private static ArrayList<HealthProfessional> professionals;

    /** The appointment collection for the day. */
    private static AppointmentManager manager;

    /**
     * Runs the interactive front desk until the user chooses to exit.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        manager = new AppointmentManager();
        professionals = createProfessionals();
        int restored = loadSavedBookings();

        System.out.println(LINE);
        System.out.println(" HEALTH SERVICE - APPOINTMENT DESK");
        System.out.println(" Clinic day: " + manager.getSchedule().getSlotCount()
                + " slots between 08:00 and 16:00, every 30 minutes");
        System.out.println(" Type your answer after each prompt, then press Enter.");
        System.out.println(" Press Enter on an empty line at any prompt to cancel that step.");
        if (restored > 0) {
            System.out.println(" Restored " + restored + " appointment(s) from " + DATA_FILE + ".");
        } else {
            System.out.println(" No saved appointments yet; everything you book is kept in "
                    + DATA_FILE + ".");
        }
        System.out.println(LINE);

        boolean running = true;
        try (Scanner reader = new Scanner(System.in)) {
            input = reader;
            while (running) {
                printMenu();
                int choice = readChoice();
                switch (choice) {
                    case 0 -> running = false;
                    case 1 -> bookAppointment();
                    case 2 -> showAllAppointments();
                    case 3 -> showAppointmentsForProfessional();
                    case 4 -> showAppointmentsForPatient();
                    case 5 -> searchBySymptom();
                    case 6 -> cancelOneAppointment();
                    case 7 -> cancelPatientAppointments();
                    case 8 -> showReference();
                    case 9 -> showSampleBookings();
                    default -> System.out.println("  Please type a number from 0 to 9.");
                }
            }
        } catch (NoSuchElementException endOfInput) {
            // The window was closed or the input was redirected to an empty file.
            // Ending quietly is friendlier than a stack trace.
            System.out.println();
            System.out.println("No more input - the desk is closing.");
        }

        saveBookings();
        System.out.println();
        System.out.println("Desk closed. " + manager.getAppointmentCount()
                + " appointment(s) still booked for today.");
    }

    /**
     * Reads the bookings left behind by an earlier run.
     *
     * <p>Each line holds five fields separated by {@code |}: name, mobile,
     * professional ID, time slot and symptom. A line that no longer parses is
     * skipped rather than trusted, and every accepted booking is created through
     * the normal constructor and manager, so a saved file can never put the desk
     * into a state a typed booking could not reach.</p>
     *
     * @return how many appointments were restored
     */
    private static int loadSavedBookings() {
        Path file = Paths.get(DATA_FILE);
        if (!Files.exists(file)) {
            return 0;
        }
        int restored = 0;
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length != 5) {
                    continue;
                }
                HealthProfessional professional = findProfessionalById(parseId(parts[2]));
                if (professional == null) {
                    continue;
                }
                try {
                    Patient patient = new Patient(parts[0], parts[1]);
                    String reply = manager.addAppointment(
                            patient, professional, parts[3], parts[4]);
                    if (reply.startsWith("SUCCESS")) {
                        restored++;
                    }
                } catch (IllegalArgumentException damagedLine) {
                    // One damaged line must not stop the desk from opening.
                }
            }
        } catch (IOException error) {
            System.out.println("  (could not read " + DATA_FILE + ": " + error.getMessage() + ")");
            return 0;
        }
        return restored;
    }

    /**
     * Writes every current booking back to {@link #DATA_FILE}.
     *
     * <p>Called after anything that changes the collection, so the file always
     * mirrors what the desk is holding; a cancelled appointment disappears from
     * it at the same moment it disappears from memory.</p>
     */
    private static void saveBookings() {
        ArrayList<Appointment> appointments = manager.getAllAppointments();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < appointments.size(); i++) {
            Appointment appointment = appointments.get(i);
            text.append(clean(appointment.getPatient().getName())).append('|')
                    .append(clean(appointment.getPatient().getMobile())).append('|')
                    .append(appointment.getHealthProfessional().getProfessionalId()).append('|')
                    .append(appointment.getTimeSlot()).append('|')
                    .append(clean(appointment.getSymptom()))
                    .append(System.lineSeparator());
        }
        try {
            Files.write(Paths.get(DATA_FILE), text.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException error) {
            System.out.println("  (could not save " + DATA_FILE + ": " + error.getMessage() + ")");
        }
    }

    /**
     * Makes a value safe to store in one field of the save file.
     *
     * @param value the text typed by the user
     * @return the same text with separators and line breaks replaced
     */
    private static String clean(String value) {
        return value.replace('|', '/').replace('\n', ' ').replace('\r', ' ');
    }

    /**
     * Reads an identifier from the save file without throwing.
     *
     * @param text the stored field
     * @return the number, or -1 when the field is not a number
     */
    private static int parseId(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException error) {
            return -1;
        }
    }

    /**
     * Creates the clinic's professionals.
     *
     * <p>Fixed data: the staff roster is not something the front desk edits.</p>
     *
     * @return the professionals available for booking
     */
    private static ArrayList<HealthProfessional> createProfessionals() {
        ArrayList<HealthProfessional> staff = new ArrayList<>();
        staff.add(new GeneralPractitioner(101, "Dr Alice Smith", "City Medical Clinic",
                Weekday.MONDAY, Weekday.WEDNESDAY, Weekday.FRIDAY));
        staff.add(new GeneralPractitioner(102, "Dr Bob Lee", "Suburb Family Clinic",
                Weekday.TUESDAY, Weekday.THURSDAY));
        staff.add(new GeneralPractitioner(103, "Dr Carol Ng", "Riverside Family Clinic"));
        staff.add(new Nurse(201, "Clara Brown", "Emergency Ward",
                Weekday.MONDAY, Weekday.TUESDAY));
        staff.add(new Nurse(202, "Ben Carter", "Outpatient Ward",
                Weekday.TUESDAY, Weekday.WEDNESDAY, Weekday.THURSDAY));
        staff.add(new Surgeon(301, "Dr David Wong", "Cardiac Surgery",
                Weekday.MONDAY, Weekday.TUESDAY, Weekday.WEDNESDAY));
        staff.add(new Dietitian(401, "Ms Eva Taylor", "Weight management",
                Weekday.THURSDAY, Weekday.FRIDAY));
        return staff;
    }

    /**
     * Prints the menu of every operation available at the desk.
     */
    private static void printMenu() {
        System.out.println();
        System.out.println("========== MENU ==========");
        System.out.println(" 1. Book an appointment (name, mobile, symptom, professional, time)");
        System.out.println(" 2. Show all appointments (earliest first)");
        System.out.println(" 3. Show appointments for one professional (by ID)");
        System.out.println(" 4. Show appointments for one patient (by mobile)");
        System.out.println(" 5. Search appointments by symptom");
        System.out.println(" 6. Cancel one appointment (professional ID + time)");
        System.out.println(" 7. Cancel all appointments of one patient (by mobile)");
        System.out.println(" 8. Show the professionals and the clinic timetable");
        System.out.println(" 9. Preview the demonstration's example bookings (not added to your list)");
        System.out.println(" 0. Exit");
    }

    /**
     * Reads the menu choice.
     *
     * @return the chosen number, or -1 when the input is not a number
     */
    private static int readChoice() {
        System.out.print("Your choice: ");
        String text = input.nextLine().trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException error) {
            return -1;
        }
    }

    /**
     * Books an appointment with data typed by the user.
     *
     * <p>Each prompt can be answered with an empty line to abandon the booking,
     * so a mistake never traps the user inside a question.</p>
     */
    private static void bookAppointment() {
        System.out.println();
        System.out.println("--- Book an appointment (press Enter on any question to cancel) ---");

        Patient patient = readPatient();
        if (patient == null) {
            return;
        }

        // Asked before the staff list, because what the patient describes often
        // decides which kind of professional they need to see.
        String symptom = readText("Symptom or reason for the visit (optional): ");

        printProfessionals();
        HealthProfessional professional = readProfessional();
        if (professional == null) {
            return;
        }

        printAvailability(professional);
        String timeSlot = readTimeSlot(professional);
        if (timeSlot == null) {
            return;
        }

        String reply = manager.addAppointment(patient, professional, timeSlot, symptom);
        System.out.println(reply);
        if (reply.startsWith("SUCCESS")) {
            saveBookings();
        }
    }

    /**
     * Reads a patient's details, re-asking until the appointment class accepts
     * them.
     *
     * @return a valid patient, or {@code null} if the user cancelled
     */
    private static Patient readPatient() {
        String name = readText("Patient name: ");
        if (name.isEmpty()) {
            System.out.println("  Booking cancelled.");
            return null;
        }

        while (true) {
            String mobile = readText("Patient mobile (digits only): ");
            if (mobile.isEmpty()) {
                System.out.println("  Booking cancelled.");
                return null;
            }
            try {
                return new Patient(name, mobile);
            } catch (IllegalArgumentException error) {
                System.out.println("  " + error.getMessage() + " Please try again.");
            }
        }
    }

    /**
     * Prints the clinic's professionals so the user can choose one.
     */
    private static void printProfessionals() {
        System.out.println("  Professionals:");
        for (int i = 0; i < professionals.size(); i++) {
            System.out.println("    " + professionals.get(i));
        }
    }

    /**
     * Reads a professional ID until it matches a real member of staff.
     *
     * @return the chosen professional, or {@code null} if the user cancelled
     */
    private static HealthProfessional readProfessional() {
        while (true) {
            String text = readText("Professional ID: ");
            if (text.isEmpty()) {
                System.out.println("  Booking cancelled.");
                return null;
            }
            int identifier;
            try {
                identifier = Integer.parseInt(text);
            } catch (NumberFormatException error) {
                System.out.println("  Please type the professional's number, for example 101.");
                continue;
            }
            for (int i = 0; i < professionals.size(); i++) {
                HealthProfessional candidate = professionals.get(i);
                if (candidate.getProfessionalId() == identifier) {
                    return candidate;
                }
            }
            System.out.println("  There is no professional with ID " + identifier + ".");
        }
    }

    /**
     * Shows the six example bookings used by the graded demonstration.
     *
     * <p>This is a <em>preview</em>: the examples are booked into a throw-away
     * manager, printed, and then discarded. They never enter the desk's own
     * collection and are never written to {@link #DATA_FILE}, so a user who only
     * wants to see what the demonstration books does not end up with six
     * strangers in their appointment list.</p>
     */
    private static void showSampleBookings() {
        AppointmentManager exampleDesk = new AppointmentManager();
        addExample(exampleDesk, "Tom Wilson", "0400 111 222", 101, "09:00", "headache and fever");
        addExample(exampleDesk, "Lisa Hall", "0400 777 888", 102, "09:00", "sore throat");
        addExample(exampleDesk, "Sarah Moore", "0400 333 444", 301, "14:00", "knee pain");
        addExample(exampleDesk, "James Clark", "0400 555 666", 201, "11:00", "wound dressing change");
        addExample(exampleDesk, "Eva Brown", "0400 999 000", 103, "08:30", "blood pressure review");
        // Lisa books a second appointment with a different professional, exactly
        // as the graded demonstration does.
        addExample(exampleDesk, "Lisa Hall", "0400 777 888", 401, "12:00", "dietary advice");
        exampleDesk.sortAllAppointments();

        System.out.println();
        System.out.println("--- Example bookings from the graded demonstration (preview only) ---");
        printAppointments("These are shown for reference and are NOT in your list",
                exampleDesk.getAllAppointments());
        System.out.println("  Your own list still holds " + manager.getAppointmentCount()
                + " appointment(s).");
    }

    /**
     * Books one preview appointment into the throw-away manager.
     *
     * <p>Even a preview goes through the normal constructor and the normal
     * manager, so the printed examples obey the same rules as a real booking.</p>
     *
     * @param desk           the temporary manager holding the preview
     * @param name           the patient's name
     * @param mobile         the patient's mobile number
     * @param professionalId the identifier of the professional being seen
     * @param timeSlot       the time of the appointment, "HH:mm"
     * @param symptom        what the patient reports
     */
    private static void addExample(AppointmentManager desk, String name, String mobile,
                                   int professionalId, String timeSlot, String symptom) {
        HealthProfessional professional = findProfessionalById(professionalId);
        if (professional == null) {
            return;
        }
        desk.addAppointment(new Patient(name, mobile), professional, timeSlot, symptom);
    }

    /**
     * Looks a professional up by identifier.
     *
     * @param professionalId the identifier to search for
     * @return the matching professional, or {@code null} when the ID is unknown
     */
    private static HealthProfessional findProfessionalById(int professionalId) {
        for (int i = 0; i < professionals.size(); i++) {
            HealthProfessional candidate = professionals.get(i);
            if (candidate.getProfessionalId() == professionalId) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Shows which times are still free for a professional.
     *
     * @param professional the professional being booked
     */
    private static void printAvailability(HealthProfessional professional) {
        ArrayList<String> slots = manager.getSchedule().getTimeSlots();
        StringBuilder free = new StringBuilder();
        StringBuilder booked = new StringBuilder();
        for (int i = 0; i < slots.size(); i++) {
            String slot = slots.get(i);
            if (isTaken(professional, slot)) {
                append(booked, slot);
            } else {
                append(free, slot);
            }
        }
        System.out.println("  " + professional.getName() + " [ID "
                + professional.getProfessionalId() + "] today:");
        System.out.println("    free  : " + (free.length() == 0 ? "(none)" : free.toString()));
        System.out.println("    booked: " + (booked.length() == 0 ? "(none)" : booked.toString()));
    }

    /**
     * Reads a time slot until it is one the clinic uses and the professional is
     * free at that moment.
     *
     * @param professional the professional being booked
     * @return the chosen time, or {@code null} if the user cancelled
     */
    private static String readTimeSlot(HealthProfessional professional) {
        while (true) {
            String time = readText("Time slot HH:mm: ");
            if (time.isEmpty()) {
                System.out.println("  Booking cancelled.");
                return null;
            }
            if (!manager.getSchedule().isAvailableSlot(time)) {
                System.out.println("  " + time + " is not a clinic slot. Available times: "
                        + manager.getSchedule().getTimeSlotsText() + ".");
                continue;
            }
            if (isTaken(professional, time)) {
                System.out.println("  " + professional.getName() + " is already booked at "
                        + time + ". Please choose another time.");
                continue;
            }
            return time;
        }
    }

    /**
     * Reports whether a professional already has a booking at a given time.
     *
     * @param professional the professional to check
     * @param timeSlot     the time to check, format "HH:mm"
     * @return {@code true} if that professional is busy at that time
     */
    private static boolean isTaken(HealthProfessional professional, String timeSlot) {
        ArrayList<Appointment> all = manager.getAllAppointments();
        for (int i = 0; i < all.size(); i++) {
            Appointment appointment = all.get(i);
            if (appointment.getHealthProfessional().getProfessionalId() == professional.getProfessionalId()
                    && appointment.getTimeSlot().equals(timeSlot)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prints every appointment of the day in time order.
     */
    private static void showAllAppointments() {
        manager.sortAllAppointments();
        printAppointments("All appointments, earliest first", manager.getAllAppointments());
    }

    /**
     * Prints the appointments of one professional, asked for by ID.
     */
    private static void showAppointmentsForProfessional() {
        String text = readText("Professional ID: ");
        if (text.isEmpty()) {
            return;
        }
        int identifier;
        try {
            identifier = Integer.parseInt(text);
        } catch (NumberFormatException error) {
            System.out.println("  Please type a number, for example 101.");
            return;
        }
        ArrayList<Appointment> matches = manager.filterByProfessionalId(identifier);
        manager.sortAppointments(matches);
        printAppointments("Appointments for professional ID " + identifier, matches);
    }

    /**
     * Prints the appointments of one patient, asked for by mobile number.
     */
    private static void showAppointmentsForPatient() {
        String mobile = readText("Patient mobile: ");
        if (mobile.isEmpty()) {
            return;
        }
        ArrayList<Appointment> matches = manager.filterByPatientMobile(mobile);
        manager.sortAppointments(matches);
        printAppointments("Appointments for patient " + mobile, matches);
    }

    /**
     * Prints every appointment whose symptom contains a keyword.
     *
     * <p>The search is done here rather than in the manager on purpose: it is a
     * reading convenience of this front desk, not a booking rule, so it does not
     * belong in the class the assessment is graded on.</p>
     */
    private static void searchBySymptom() {
        String keyword = readText("Keyword to look for in the symptom: ").toLowerCase();
        if (keyword.isEmpty()) {
            System.out.println("  No keyword given.");
            return;
        }
        ArrayList<Appointment> matches = new ArrayList<>();
        ArrayList<Appointment> all = manager.getAllAppointments();
        for (int i = 0; i < all.size(); i++) {
            Appointment appointment = all.get(i);
            if (appointment.getSymptom().toLowerCase().contains(keyword)) {
                matches.add(appointment);
            }
        }
        manager.sortAppointments(matches);
        printAppointments("Appointments whose reason contains \"" + keyword + "\"", matches);
    }

    /**
     * Cancels one appointment, asked for by professional ID and time.
     */
    private static void cancelOneAppointment() {
        String text = readText("Professional ID: ");
        if (text.isEmpty()) {
            return;
        }
        int identifier;
        try {
            identifier = Integer.parseInt(text);
        } catch (NumberFormatException error) {
            System.out.println("  Please type a number, for example 101.");
            return;
        }
        String time = readText("Time slot HH:mm: ");
        if (time.isEmpty()) {
            return;
        }
        System.out.println(manager.cancelAppointment(identifier, time));
        saveBookings();
    }

    /**
     * Cancels every appointment of one patient, asked for by mobile number.
     */
    private static void cancelPatientAppointments() {
        String mobile = readText("Patient mobile: ");
        if (mobile.isEmpty()) {
            return;
        }
        System.out.println(manager.cancelAppointmentsForPatient(mobile));
        saveBookings();
    }

    /**
     * Prints the staff roster and the bookable times, so the user does not have
     * to remember them.
     */
    private static void showReference() {
        System.out.println();
        System.out.println("  Professionals:");
        for (int i = 0; i < professionals.size(); i++) {
            System.out.println("    " + professionals.get(i));
        }
        System.out.println();
        System.out.println("  Clinic timetable (" + manager.getSchedule().getSlotCount() + " slots):");
        System.out.println("    " + manager.getSchedule().getTimeSlotsText());
    }

    /**
     * Prints a titled list of appointments, or a short message when it is empty.
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
            System.out.println("  " + (i + 1) + ". " + appointments.get(i));
        }
        System.out.println("  Total: " + appointments.size() + " appointment(s)");
    }

    /**
     * Prints a prompt and reads one line of text.
     *
     * <p>Every value is read as a line, never as a token, so a name containing a
     * space such as "Tom Wilson" is read in one piece.</p>
     *
     * @param prompt the question shown to the user
     * @return the trimmed answer, possibly empty
     */
    private static String readText(String prompt) {
        System.out.print(prompt);
        return input.nextLine().trim();
    }

    /**
     * Adds a value to a comma-separated list.
     *
     * @param list  the list being built
     * @param value the value to append
     */
    private static void append(StringBuilder list, String value) {
        if (list.length() > 0) {
            list.append(", ");
        }
        list.append(value);
    }
}
