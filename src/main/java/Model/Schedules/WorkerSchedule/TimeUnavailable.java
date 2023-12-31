package Model.Schedules.WorkerSchedule;

import Controller.File.Jackson;
import Controller.Time.Week;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.time.LocalTime;
import java.time.Duration;


public class TimeUnavailable {
    private Week.DayNames day;
    private LocalTime startTime;
    private LocalTime endTime;

    public TimeUnavailable(Week.DayNames day, LocalTime startTime, LocalTime endTime) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ArrayNode getTimesArrayAsJsonNode(){
        ObjectMapper objectMapper = Jackson.getObjectMapper();
        ArrayNode timesArray = objectMapper.createArrayNode();
        timesArray.add(startTime.getHour());
        timesArray.add(startTime.getMinute());
        timesArray.add(endTime.getHour());
        timesArray.add(endTime.getMinute());
        return timesArray;
    }

    // Getters and Setters

    public int getDurationInMinutes() {
        return (int) Duration.between(startTime, endTime).toMinutes();
    }

    public Week.DayNames getDay() {
        return day;
    }

    public void setDay(Week.DayNames day) {
        this.day = day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    // Additional methods for overlap checks, etc., can be added here
}
