package Model.Schedules.ManagerSchedule;

import Model.Time.Week;

import java.util.ArrayList;

public class AvailableDaySchedule {
    private Week.DayNames name;
    ArrayList<AvailableShift> availableShifts = new ArrayList<>();

    public AvailableDaySchedule(Week.DayNames name){
        this.name = name;
    }

    public void addShiftToDay(AvailableShift availableShift) {
        availableShifts.add(availableShift);
    }
}