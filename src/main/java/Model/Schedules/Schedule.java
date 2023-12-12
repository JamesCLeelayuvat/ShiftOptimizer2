package Model.Schedules;import Model.Time.TimeUnavailable;import Model.Time.Week;public class Schedule {    public DaySchedule[] daySchedules = new DaySchedule[7];    public Schedule() {//        for (int i = 0; i < 7; i++) {//            days[i] = new Day(Week.DAY_NAMES[i]);//        }    }    // Method to check for shift conflicts    public String printSchedule() {        StringBuilder scheduleString = new StringBuilder();        for (DaySchedule daySchedule : daySchedules) {            scheduleString.append(daySchedule.name).append(":\n");            for(TimeUnavailable timeUnavailable: daySchedule.getTimesUnavailable())            scheduleString.append("Shift: ")                    .append(timeUnavailable.getStartTime())                    .append(" to ")                    .append(timeUnavailable.getEndTime())                    .append("\n");        }        return scheduleString.toString();    }    public DaySchedule getDay(Week.DayNames day){        for(int i = 0;i< 7;i++){            if(daySchedules[i].name.equals(day)){                return daySchedules[i];            }        }        throw new IllegalArgumentException();    }    public void setDay(DaySchedule daySchedule, int i){        daySchedules[i] = daySchedule;    }    // Additional methods as needed}