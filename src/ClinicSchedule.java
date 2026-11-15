import java.util.ArrayList;

/**
 * The clinic's fixed daily timetable: a start time, a closing time and the
 * interval between appointments.
 *
 * <p>The brief warns that the timetable must be easy to change without
 * breaking the rest of the system. That is why the timetable lives in one small
 * object instead of being hard-coded inside the appointment manager: change the
 * start time, the closing time or the interval here and every existing
 * appointment, filter, cancellation and validation keeps working unchanged.</p>
 *
 * <p>Times are kept as {@code "HH:mm"} strings. The format is fixed-width, so
 * alphabetical order is also chronological order; that is what lets
 * {@link Appointment} sort itself without parsing anything.</p>
 *
 * @author Yanxin Wu
 * @version 1.0
 */
public class ClinicSchedule {

    /** Start time of the default clinic day. */
    private static final String DEFAULT_START = "08:00";

    /** Closing time of the default clinic day. */
    private static final String DEFAULT_END = "16:00";

    /** Length of one appointment in the default clinic. */
    private static final int DEFAULT_INTERVAL_MINUTES = 30;

    /** Every bookable start time, in order. */
    private final ArrayList<String> timeSlots;

    /**
     * Builds a timetable.
     *
     * <p>The closing time is the moment the clinic stops accepting work, so it
     * is not itself a bookable start time: an appointment that begins at 16:00
     * would still be running when the clinic closes. The last slot therefore
     * starts one interval before the closing time.</p>
     *
     * @param startTime      first appointment of the day, format "HH:mm"
     * @param endTime        closing time, format "HH:mm"
     * @param intervalMinutes minutes between two appointments, must be positive
     * @throws IllegalArgumentException if the times are malformed or the
     *                                  interval cannot fill the day
     */
    public ClinicSchedule(String startTime, String endTime, int intervalMinutes) {
        if (!isValidTimeFormat(startTime)) {
            throw new IllegalArgumentException("Start time must use the 24-hour format HH:mm.");
        }
        if (!isValidTimeFormat(endTime)) {
            throw new IllegalArgumentException("Closing time must use the 24-hour format HH:mm.");
        }
        if (intervalMinutes <= 0) {
            throw new IllegalArgumentException("The interval between appointments must be positive.");
        }

        int startMinutes = toMinutes(startTime);
        int endMinutes = toMinutes(endTime);
        if (endMinutes <= startMinutes) {
            throw new IllegalArgumentException("The closing time must be after the start time.");
        }

        this.timeSlots = new ArrayList<>();
        for (int minute = startMinutes; minute < endMinutes; minute = minute + intervalMinutes) {
            this.timeSlots.add(toTimeText(minute));
        }
        if (this.timeSlots.isEmpty()) {
            throw new IllegalArgumentException("The interval is too long to fit a single appointment.");
        }
    }

    /**
     * Creates the timetable described in the brief: 08:00 to 16:00 in
     * 30-minute intervals.
     *
     * @return the standard clinic timetable
     */
    public static ClinicSchedule createDefaultSchedule() {
        return new ClinicSchedule(DEFAULT_START, DEFAULT_END, DEFAULT_INTERVAL_MINUTES);
    }

    /**
     * Returns a copy of every bookable start time.
     *
     * @return a new list of times in chronological order
     */
    public ArrayList<String> getTimeSlots() {
        return new ArrayList<>(this.timeSlots);
    }

    /**
     * Reports whether a time can be booked.
     *
     * @param timeSlot the time to test, format "HH:mm"
     * @return {@code true} if the time is one of the clinic's slots
     */
    public boolean isAvailableSlot(String timeSlot) {
        return timeSlot != null && this.timeSlots.contains(timeSlot);
    }

    /**
     * Returns the number of appointments the clinic can take in one day.
     *
     * @return the number of bookable slots
     */
    public int getSlotCount() {
        return this.timeSlots.size();
    }

    /**
     * Returns a readable description of the timetable, used in warning
     * messages so the user can see which times are actually available.
     *
     * @return the slots separated by commas, from the first to the last
     */
    public String getTimeSlotsText() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < this.timeSlots.size(); i++) {
            if (i > 0) {
                text.append(", ");
            }
            text.append(this.timeSlots.get(i));
        }
        return text.toString();
    }

    /**
     * Checks that a string looks like a 24-hour time.
     *
     * @param time the text to test
     * @return {@code true} if the text is "HH:mm" between 00:00 and 23:59
     */
    private static boolean isValidTimeFormat(String time) {
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

    /**
     * Converts "HH:mm" into minutes after midnight.
     *
     * @param time a time in the format "HH:mm"
     * @return the number of minutes after midnight
     */
    private static int toMinutes(String time) {
        int hour = Integer.parseInt(time.substring(0, 2));
        int minute = Integer.parseInt(time.substring(3, 5));
        return hour * 60 + minute;
    }

    /**
     * Converts minutes after midnight back into "HH:mm".
     *
     * @param minutes minutes after midnight
     * @return the matching time in the format "HH:mm"
     */
    private static String toTimeText(int minutes) {
        int hour = minutes / 60;
        int minute = minutes % 60;
        return twoDigits(hour) + ":" + twoDigits(minute);
    }

    /**
     * Pads a number below ten with a leading zero.
     *
     * @param value the number to format
     * @return the number as two characters
     */
    private static String twoDigits(int value) {
        if (value < 10) {
            return "0" + value;
        }
        return String.valueOf(value);
    }
}
