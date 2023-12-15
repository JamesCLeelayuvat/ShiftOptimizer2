package View.XMLControllers;

import Controller.CSVController.CSVAvailabilityImporter;
import Controller.ShiftAssignment.ShiftAssigner;
import Controller.File.JacksonEditor;
import Controller.File.JacksonGetter;
import Controller.UserAuth.Session;
import Controller.UserAuth.SessionAuth;
import Model.Schedules.OptimizedSchedule.Hour;
import Model.Schedules.OptimizedSchedule.OptimizedDaySchedule;
import Model.Schedules.OptimizedSchedule.OptimizedSchedule;
import Model.Schedules.WorkerSchedule.DayWorkerSchedule;
import Model.Schedules.WorkerSchedule.TimeUnavailable;
import Model.Schedules.WorkerSchedule.WorkerSchedule;
import Model.Staff.Worker;
import Controller.Time.Week;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

import static View.XMLControllers.HelperMethods.showAlert;

public class WorkerInputTimesController {

    @FXML
    private ComboBox<String> dayComboBox;

    @FXML
    private ComboBox<Integer> startHourComboBox;

    @FXML
    private ComboBox<Integer> startMinuteComboBox;

    @FXML
    private ComboBox<Integer> endHourComboBox;

    @FXML
    private ComboBox<Integer> endMinuteComboBox;
    @FXML
    private ListView<String> sundayListView;

    @FXML
    private ListView<String> mondayListView;

    @FXML
    private ListView<String> tuesdayListView;

    @FXML
    private ListView<String> wednesdayListView;

    @FXML
    private ListView<String> thursdayListView;

    @FXML
    private ListView<String> fridayListView;

    @FXML
    private ListView<String> saturdayListView;

    @FXML
    private TextField csvFilePathField;

    @FXML TabPane dayTabPane;

    @FXML
    private void initialize(){
        Worker worker = JacksonGetter.getWorkerByUsername(SessionAuth.authenticateToken(Session.getToken()));
        WorkerSchedule workerSchedule = worker.getSchedule();
        for(DayWorkerSchedule dayWorkerSchedule :workerSchedule.dayWorkerSchedules){
            for(TimeUnavailable timeUnavailable: dayWorkerSchedule.getTimesUnavailable()){
                updateListView(dayWorkerSchedule.name, timeUnavailable.getStartTime(), timeUnavailable.getEndTime());
            }
        }

    }
    private void updateListView(Week.DayNames dayName, LocalTime startTime, LocalTime endTime) {
        String entry = String.format("%02d:%02d - %02d:%02d",
                startTime.getHour(), startTime.getMinute(),
                endTime.getHour(), endTime.getMinute());

        // Determine the appropriate ListView based on the day
        ListView<String> dayListView = getDayListView(dayName);
        if (dayListView != null) {
            dayListView.getItems().add(entry);
            clearInputFields();
        }
    }

    private ListView<String> getDayListView(Week.DayNames dayName) {
        switch (dayName) {
            case Sunday:
                return sundayListView;
            case Monday:
                return mondayListView;
            case Tuesday:
                return tuesdayListView;
            case Wednesday:
                return wednesdayListView;
            case Thursday:
                return thursdayListView;
            case Friday:
                return fridayListView;
            case Saturday:
                return saturdayListView;
            default:
                return null;
        }
    }
    private void clearInputFields() {
        // Clear input fields after submission
        dayComboBox.getSelectionModel().clearSelection();
        startHourComboBox.getSelectionModel().clearSelection();
        startMinuteComboBox.getSelectionModel().clearSelection();
        endHourComboBox.getSelectionModel().clearSelection();
        endMinuteComboBox.getSelectionModel().clearSelection();
    }




    public void submit(ActionEvent event) {


        if (hasNullInputs()) {
            showAlert("Invalid Input", "Must fill out all fields of the input");

        } else if(invalidInput()){
            showAlert("Invalid Input", "End of shift must be in the future of the start of shift");

        }else if((timesOverlap())){
            showAlert("Invalid Input", "Unavailable times cannot overlap");

        }else{
            TimeUnavailable timeUnavailable = new TimeUnavailable(Week.DayNames.valueOf(dayComboBox.getValue()),
                    LocalTime.of(startHourComboBox.getValue(), startMinuteComboBox.getValue()),
                    LocalTime.of(endHourComboBox.getValue(), endMinuteComboBox.getValue()));
            JacksonEditor.addTimeUnavailable(SessionAuth.authenticateToken(Session.getToken()),
                    Week.DayNames.valueOf(dayComboBox.getValue()), timeUnavailable);

            updateListView(Week.DayNames.valueOf(dayComboBox.getValue()),LocalTime.of(startHourComboBox.getValue(),
                    startMinuteComboBox.getValue()),LocalTime.of(endHourComboBox.getValue(), endMinuteComboBox.getValue()));
        }
    }

    private boolean hasNullInputs(){
        boolean day = dayComboBox.getValue() == null;
        boolean startHour = startHourComboBox.getValue() == null;
        boolean startMinute = startMinuteComboBox.getValue() == null;
        boolean endHour = endHourComboBox.getValue() == null;
        boolean endMinute = endMinuteComboBox.getValue() == null;

        return day||startHour||startMinute||endHour||endMinute;
    }

    private boolean invalidInput(){
       return LocalTime.of(startHourComboBox.getValue(), startMinuteComboBox.getValue()).isAfter(LocalTime.of(endHourComboBox.getValue(), endMinuteComboBox.getValue()));
    }

    public void removeSelected(ActionEvent event) throws IOException {

        Tab selectedTab = dayTabPane.getSelectionModel().getSelectedItem();



        // Remove the selected item from the currently selected day's ListView
        Week.DayNames selectedDay = Week.DayNames.valueOf(selectedTab.getText());
        ListView<String> selectedDayListView = getDayListView(selectedDay);
        String selectedItem = selectedDayListView.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            String[] timeComponents = selectedItem.split(" - ");
            if (timeComponents.length == 2) {
                String startTimeStr = timeComponents[0];
                String endTimeStr = timeComponents[1];

                // Parse start and end times
                LocalTime startTime = LocalTime.parse(startTimeStr);
                LocalTime endTime = LocalTime.parse(endTimeStr);

                // Now you have startTime and endTime as LocalTime objects
                // You can use them as needed

                // Remove the selected item from the ListView
                selectedDayListView.getItems().remove(selectedItem);
                JacksonEditor.removeTimeUnavailable(SessionAuth.authenticateToken(Session.getToken()),selectedDay, new TimeUnavailable(selectedDay, startTime,endTime));
            }        }

    }

    private boolean timesOverlap() {
        Worker worker = JacksonGetter.getWorkerByUsername(SessionAuth.authenticateToken(Session.getToken()));
        WorkerSchedule workerSchedule = worker.getSchedule();
        DayWorkerSchedule dayWorkerSchedule = workerSchedule.getDaySchedule(Week.DayNames.valueOf(dayComboBox.getValue()));
        int startHour = startHourComboBox.getValue();
        int startMinute = startMinuteComboBox.getValue();
        int endHour = endHourComboBox.getValue();
        int endMinute = endMinuteComboBox.getValue();

        LocalTime startTime = LocalTime.of(startHour, startMinute);
        LocalTime endTime = LocalTime.of(endHour, endMinute);

        for (TimeUnavailable timeUnavailable: dayWorkerSchedule.getTimesUnavailable()) {

            boolean timeFirst = startTime.isAfter(timeUnavailable.getStartTime()) && startTime.isBefore(timeUnavailable.getEndTime());
            boolean addedTimeFirst = timeUnavailable.getStartTime().isBefore(endTime) && timeUnavailable.getStartTime().isAfter(startTime);
            boolean timeInAddedTime = timeUnavailable.getStartTime().isAfter(startTime) && timeUnavailable.getEndTime().isBefore(endTime);
            boolean addedTimeInTime = startTime.isAfter(timeUnavailable.getStartTime()) && endTime.isBefore(timeUnavailable.getEndTime());

            if (timeFirst || addedTimeFirst || timeInAddedTime || addedTimeInTime) {
                return true;
            }
        }
        return false;
    }


    public void handleImportFromCSV(ActionEvent event) {
        String csvFilePath = csvFilePathField.getText();
        Worker currentWorkerUsername = JacksonGetter.getWorkerByUsername(SessionAuth.authenticateToken(Session.getToken()));

        if (!csvFilePath.isEmpty()) {
            try {
                // Call your method to import the schedule from CSV
                CSVAvailabilityImporter.importAvailability(csvFilePath , String.valueOf(currentWorkerUsername));
                importScheduleFromCSV(csvFilePath);
            } catch (IOException e) {
                // Handle exceptions (e.g., file not found, format issues)
                e.printStackTrace();
            }
        }
    }

    private void importScheduleFromCSV(String filePath) throws IOException {
        Worker currentWorker = JacksonGetter.getWorkerByUsername(SessionAuth.authenticateToken(Session.getToken()));

        try {
            // Import availability from CSV
            CSVAvailabilityImporter.importAvailability(filePath, currentWorker.getUserName());

            // Trigger the optimization process
            ShiftAssigner.optimizeWeek();

            // Update the worker's schedule in the GUI
            updateWorkerScheduleInView(currentWorker);

            showAlert("Import Successful", "Schedule imported and optimized successfully.");
        } catch (Exception e) {
            showAlert("Import Failed", "Failed to import and optimize schedule: " + e.getMessage());
            throw e;
        }
    }


    private void updateWorkerScheduleInView(Worker worker) {
        clearListViews();
        List<String> shifts = OptimizedSchedule.getShiftsForWorker(worker.getUserName());
        for (String shift : shifts) {
            String[] parts = shift.split(" ");
            Week.DayNames dayName = Week.DayNames.valueOf(parts[0]);
            String time = parts[1];
            ListView<String> dayListView = getDayListView(dayName);
            if (dayListView != null) {
                dayListView.getItems().add(time);
            }
        }
    }


    private void clearListViews() {
        sundayListView.getItems().clear();
        mondayListView.getItems().clear();
        tuesdayListView.getItems().clear();
        wednesdayListView.getItems().clear();
        thursdayListView.getItems().clear();
        fridayListView.getItems().clear();
        saturdayListView.getItems().clear();
        // ... clear other ListViews
    }



    public void handleBack(ActionEvent event) throws IOException {
        Parent signUpScreenRoot = FXMLLoader.load(getClass().getResource("/views/worker/worker-main-view.fxml"));
        Stage stage = HelperMethods.getStageFromEvent(event);
        stage.setScene(new Scene(signUpScreenRoot));
        stage.show();
    }
}
