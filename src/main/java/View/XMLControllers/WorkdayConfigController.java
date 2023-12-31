package View.XMLControllers;

import Controller.Time.WorkdayConfig;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalTime;

public class WorkdayConfigController {
    @FXML
    private ComboBox<Integer> startHourComboBox;

    @FXML
    private ComboBox<Integer> startMinuteComboBox;

    @FXML
    private ComboBox<Integer> endHourComboBox;

    @FXML
    private ComboBox<Integer> endMinuteComboBox;

    @FXML
    private void initialize(){
        LocalTime startTime = WorkdayConfig.getWorkdayStart();
        LocalTime endTime = WorkdayConfig.getWorkdayEnd();

        startHourComboBox.setValue(startTime.getHour());
        startMinuteComboBox.setValue(startTime.getMinute());
        endHourComboBox.setValue(endTime.getHour());
        endMinuteComboBox.setValue(endTime.getMinute());

    }
    public void submitChanges(ActionEvent event) throws IOException {
        if (hasNullInputs()) {
            HelperMethods.showAlert("Invalid Input", "Must fill out all fields of the input");

        } else if (invalidInput()) {
            HelperMethods.showAlert("Invalid Input", "End of shift must be in the future of the start of shift");
        }else{
            WorkdayConfig.setWorkdayStart(startHourComboBox.getValue(), startMinuteComboBox.getValue());
            WorkdayConfig.setWorkdayEnd(endHourComboBox.getValue(), endMinuteComboBox.getValue());
        }
    }

    private boolean invalidInput() {
        return LocalTime.of(startHourComboBox.getValue(), startMinuteComboBox.getValue()).isAfter(LocalTime.of(endHourComboBox.getValue(), endMinuteComboBox.getValue()));
    }

    private boolean hasNullInputs() {
        boolean startHour = startHourComboBox.getValue() == null;
        boolean startMinute = startMinuteComboBox.getValue() == null;
        boolean endHour = endHourComboBox.getValue() == null;
        boolean endMinute = endMinuteComboBox.getValue() == null;

        return startHour || startMinute || endHour || endMinute;
    }

    public void handleBack(ActionEvent event) throws IOException {
        Parent signUpScreenRoot = FXMLLoader.load(getClass().getResource("/views/manager/manager-main-view.fxml"));
        Stage stage = HelperMethods.getStageFromEvent(event);
        stage.setScene(new Scene(signUpScreenRoot));
        stage.show();
    }
}
