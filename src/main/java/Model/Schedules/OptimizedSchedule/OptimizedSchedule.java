package Model.Schedules.OptimizedSchedule;

import Controller.Time.Week;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OptimizedSchedule {
    public static OptimizedDaySchedule[] optimizedDaySchedules = new OptimizedDaySchedule[7];
    private static Map<String, List<String>> workerShifts = new HashMap<>();

    public static void initialize() {
        for (int i = 0; i < 7; i++) {
            optimizedDaySchedules[i] = new OptimizedDaySchedule(Week.DAY_NAMES[i]);
        }
        workerShifts.clear();
    }

    public static void setNameToShift(String username, Week.DayNames day, LocalTime time) {
        int halfHourSegment = time.getMinute() == 30 ? 1 : 0;
        optimizedDaySchedules[Week.getIndexFromDay(day)].hours[time.getHour()].halfHours[halfHourSegment] = username;
        updateWorkerShifts(username, day, time);
    }

    private static void updateWorkerShifts(String username, Week.DayNames day, LocalTime time) {
        String shiftKey = day.toString() + " " + String.format("%02d:%02d", time.getHour(), time.getMinute());
        workerShifts.computeIfAbsent(username, k -> new java.util.ArrayList<>()).add(shiftKey);
    }

    public static List<String> getShiftsForWorker(String username) {
        return workerShifts.getOrDefault(username, java.util.Collections.emptyList());
    }
}
