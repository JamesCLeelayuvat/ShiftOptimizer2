package Controller.Time;

import Controller.File.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.time.LocalTime;

import static Controller.File.Jackson.getJsonFile;

public class WorkdayConfig {
    private static LocalTime workdayStart = LocalTime.of(8, 0); // Default start time
    private static LocalTime workdayEnd = LocalTime.of(23, 0); // Default end time

    public static void setWorkdayStart(int hour, int minute) throws IOException {
        ObjectWriter objectWriter = Jackson.getObjectWriter();
        JsonNode rootNode = Jackson.getRootNode();
        JsonNode configNode = rootNode.get("workday-config");
        int[] workdayStart = new int[]{hour, minute};
        ((ObjectNode) configNode).put("workday-start-hour", hour);
        ((ObjectNode) configNode).put("workday-start-minute", minute);
        objectWriter.writeValue(Jackson.getJsonFile(), rootNode);


    }

    public static void setWorkdayEnd(int hour, int minute) throws IOException {
        ObjectWriter objectWriter = Jackson.getObjectWriter();
        JsonNode rootNode = Jackson.getRootNode();
        JsonNode configNode = rootNode.get("workday-config");
        int[] workdayStart = new int[]{hour, minute};
        ((ObjectNode) configNode).put("workday-end-hour", hour);
        ((ObjectNode) configNode).put("workday-end-minute", minute);
        objectWriter.writeValue(Jackson.getJsonFile(), rootNode);

    }

    public static LocalTime getWorkdayStart() {

        JsonNode rootNode = Jackson.getRootNode();
        JsonNode configNode = rootNode.get("workday-config");
        return LocalTime.of(configNode.get("workday-start-hour").asInt(),configNode.get("workday-start-minute").asInt());
    }

    public static LocalTime getWorkdayEnd() {
        JsonNode rootNode = Jackson.getRootNode();
        JsonNode configNode = rootNode.get("workday-config");
        return LocalTime.of(configNode.get("workday-end-hour").asInt(),configNode.get("workday-end-minute").asInt());
    }

    public static boolean isWithinWorkHours(LocalTime time) {
        return !time.isBefore(workdayStart) && !time.isAfter(workdayEnd);
    }
}
