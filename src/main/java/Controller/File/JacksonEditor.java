package Controller.File;

import Model.Schedules.ManagerSchedule.AvailableShift;
import Model.Schedules.WorkerSchedule.TimeUnavailable;
import Controller.Time.Week;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Iterator;

import static Controller.UserAuth.BouncyCastleEncrypter.hashPassword;


public class JacksonEditor extends Jackson {

    public static void addWorker(String username, String password, String status) {
        //Have to check for duplicate usernames

        // I updated what you wrote here
        JsonNode rootNode = getRootNode();
        ObjectMapper objectMapper = getObjectMapper();
        ObjectWriter objectWriter = getObjectWriter();

        // Check if username already exists

        JsonNode workersNode = rootNode.path("workers");

        if (!isUsernameAvailable(username)) {
            throw new IllegalArgumentException("Username already taken.");
        } else {
            String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

            ArrayNode newSchedule = objectMapper.createArrayNode();

            for (String day : daysOfWeek) {
                JsonNode newDaySchedule = objectMapper.createObjectNode().put("day-name", day)
                        .set("times-unavailable", objectMapper.createArrayNode());
                newSchedule.add(newDaySchedule);
            }


            JsonNode newWorker = objectMapper.createObjectNode()
                    .put("username", username)
                    .put("password", hashPassword(password))
                    .put("status", status)
                    .set("schedules", newSchedule);


            ((com.fasterxml.jackson.databind.node.ArrayNode) workersNode).add(newWorker);

            try {
                objectWriter.writeValue(getJsonFile(), rootNode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }




        // Add new worker only if username is unique
        /*if (!usernameExists) {
            String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

            ArrayNode newSchedule = objectMapper.createArrayNode();

            for (String day : daysOfWeek) {
                JsonNode newDaySchedule = objectMapper.createObjectNode().put("day-name", day)
                        .set("times-unavailable", objectMapper.createArrayNode());
                newSchedule.add(newDaySchedule);
            }


            JsonNode newWorker = objectMapper.createObjectNode()
                    .put("username", username)
                    .put("password", hashPassword(password))
                    .put("status", status)
                    .set("schedules", newSchedule);


            ((com.fasterxml.jackson.databind.node.ArrayNode) workersNode).add(newWorker);

            try {
                objectWriter.writeValue(getJsonFile(), rootNode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("Could not add worker");
        }

    }*/

    private static boolean isUsernameAvailable(String username) {
        JsonNode rootNode = getRootNode();
        if (rootNode != null && rootNode.has("workers")) {
            for (JsonNode workerNode : rootNode.get("workers")) {
                if (workerNode.get("username").asText().equals(username)) {
                    return false; // Username already exists
                }
            }

            for(JsonNode managerNode : rootNode.get("managers")){
                if(managerNode.get("username").asText().equals(username)){
                    return false;//Username already exists
                }
            }
        }


        return true; // Username is available
    }



    public static void removeWorker(String username) {
        JsonNode rootNode = getRootNode();
        ObjectMapper objectMapper = getObjectMapper();
        ObjectWriter objectWriter = getObjectWriter();

        for (int i = 0; i < rootNode.get("workers").size(); i++) {
            if (username.equals(rootNode.get("workers").get(i).get("username").asText())) {
                ((com.fasterxml.jackson.databind.node.ArrayNode) rootNode.get("workers")).remove(i);
                try {
                    objectWriter.writeValue(getJsonFile(), rootNode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }


    public static void clearJsonFile() {
        ObjectMapper objectMapper = getObjectMapper();
        ObjectWriter objectWriter = getObjectWriter();

        // Create a new JSON object with an empty "workers" array
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.putArray("workers");

        // Write the new empty structure back to the file
        try {
            objectWriter.writeValue(getJsonFile(), rootNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addManager(String username, String password) {
        JsonNode rootNode = getRootNode();
        ObjectMapper objectMapper = getObjectMapper();
        ObjectWriter objectWriter = getObjectWriter();

        // Check if username already exists
        boolean usernameExists = false;
        JsonNode managersNode = rootNode.path("managers");

        if (!isUsernameAvailable(username)) {
            throw new IllegalArgumentException("Username already taken.");
        } else {
            JsonNode newManager = objectMapper.createObjectNode()
                    .put("username", username)
                    .put("password", hashPassword(password));

            ((com.fasterxml.jackson.databind.node.ArrayNode) managersNode).add(newManager);


            try {
                objectWriter.writeValue(getJsonFile(), rootNode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void addTimeUnavailable(String username, Week.DayNames day, TimeUnavailable timeUnavailable) {
        JsonNode rootNode = getRootNode();
        ObjectMapper objectMapper = getObjectMapper();
        ObjectWriter objectWriter = getObjectWriter();

        JsonNode workersNode = rootNode.get("workers");

        //Iterate through all the workers
        Iterator<JsonNode> workersNodeIterator = workersNode.elements();

        while (workersNodeIterator.hasNext()) {
            JsonNode workerNode = workersNodeIterator.next();


            //Found the worker
            if (workerNode.get("username").asText().equals(username)) {
                JsonNode schedules = workerNode.get("schedules");


                //Iterate through all the days
                Iterator<JsonNode> schedulesIterator = schedules.elements();
                for (int i = 0; schedulesIterator.hasNext(); i++) {
                    JsonNode scheduleNode = schedulesIterator.next();

                    //Convert the day to enum and find the day
                    Week.DayNames dayFromJson = Week.DayNames.valueOf(scheduleNode.get("day-name").asText());
                    if (dayFromJson.equals(day)) {

                        //get the shifts array and add an array of 4 times
                        ArrayNode shiftsArray = (ArrayNode) scheduleNode.get("times-unavailable");
                        shiftsArray.add(timeUnavailable.getTimesArrayAsJsonNode());
                        try {
                            objectWriter.writeValue(getJsonFile(), rootNode);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }


    }

    public static void removeTimeUnavailable(String username, Week.DayNames day, TimeUnavailable timeUnavailable) throws IOException {
        JsonNode rootNode = getRootNode();
        ObjectMapper objectMapper = getObjectMapper();
        ObjectWriter objectWriter = getObjectWriter();

        JsonNode workersNode = rootNode.get("workers");

        //Iterate through all the workers
        Iterator<JsonNode> workersNodeIterator = workersNode.elements();

        while (workersNodeIterator.hasNext()) {
            JsonNode workerNode = workersNodeIterator.next();


            //Found the worker
            if (workerNode.get("username").asText().equals(username)) {
                JsonNode schedules = workerNode.get("schedules");


                //Iterate through all the days
                Iterator<JsonNode> schedulesIterator = schedules.elements();
                for (int i = 0; schedulesIterator.hasNext(); i++) {
                    JsonNode scheduleNode = schedulesIterator.next();

                    //Convert the day to enum and find the day
                    Week.DayNames dayFromJson = Week.DayNames.valueOf(scheduleNode.get("day-name").asText());
                    if (dayFromJson.equals(day)) {

                        JsonNode timesUnavailableNode = scheduleNode.get("times-unavailable");

                        Iterator<JsonNode> timesUnavailableIterator = timesUnavailableNode.elements();
                        for (int j = 0; timesUnavailableIterator.hasNext(); j++) {
                            JsonNode timeUnavailableNode = timesUnavailableIterator.next();

                            int startHourIn = timeUnavailable.getStartTime().getHour();
                            int startHourJson = timeUnavailableNode.get(0).asInt();
                            int startMinuteIn = timeUnavailable.getStartTime().getMinute();
                            int startMinuteJson = timeUnavailableNode.get(1).asInt();
                            int endHourIn = timeUnavailable.getEndTime().getHour();
                            int endHourJson = timeUnavailableNode.get(2).asInt();
                            int endMinuteIn = timeUnavailable.getEndTime().getMinute();
                            int endMinuteJson = timeUnavailableNode.get(3).asInt();
//
                            if (startHourIn == startHourJson && startMinuteIn == startMinuteJson && endHourIn == endHourJson && endMinuteIn == endMinuteJson) {
                                timesUnavailableIterator.remove();
                                objectWriter.writeValue(getJsonFile(), rootNode);
                            }
                        }
                    }
                }
            }
        }


    }

    public static void addAvailableShift(Week.DayNames day, AvailableShift availableShift) {
        JsonNode rootNode = getRootNode();
        ObjectMapper objectMapper = getObjectMapper();
        ObjectWriter objectWriter = getObjectWriter();

        JsonNode availableShiftsNode = rootNode.get("available-schedule");

        Iterator<JsonNode> availableScheduleIterator = availableShiftsNode.elements();
        for (int i = 0; availableScheduleIterator.hasNext(); i++) {
            JsonNode scheduleNode = availableScheduleIterator.next();

            //Convert the day to enum and find the day
            Week.DayNames dayFromJson = Week.DayNames.valueOf(scheduleNode.get("day-name").asText());
            if (dayFromJson.equals(day)) {

                //get the shifts array and add an array of 4 times
                ArrayNode shiftsArray = (ArrayNode) scheduleNode.get("shifts");
                shiftsArray.add(availableShift.getTimesArrayAsJsonNode());
                try {
                    objectWriter.writeValue(getJsonFile(), rootNode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void removeAvailableShift(Week.DayNames day, AvailableShift availableShift) throws IOException {
        JsonNode rootNode = getRootNode();
        ObjectMapper objectMapper = getObjectMapper();
        ObjectWriter objectWriter = getObjectWriter();

        JsonNode availableShiftsNode = rootNode.get("available-schedule");

        Iterator<JsonNode> availableScheduleIterator = availableShiftsNode.elements();
        for (int i = 0; availableScheduleIterator.hasNext(); i++) {
            JsonNode scheduleNode = availableScheduleIterator.next();

            //Convert the day to enum and find the day
            Week.DayNames dayFromJson = Week.DayNames.valueOf(scheduleNode.get("day-name").asText());
            if (dayFromJson.equals(day)) {

                Iterator<JsonNode> availableShiftsIterator = scheduleNode.get("shifts").elements();

                for(int j = 0;availableShiftsIterator.hasNext();j++){
                    JsonNode availableShiftNode = availableShiftsIterator.next();

                    //Checking if the times are the same
                    int startHourIn = availableShift.getStartTime().getHour();
                    int startHourJson = availableShiftNode.get(0).asInt();
                    int startMinuteIn = availableShift.getStartTime().getMinute();
                    int startMinuteJson = availableShiftNode.get(1).asInt();
                    int endHourIn = availableShift.getEndTime().getHour();
                    int endHourJson = availableShiftNode.get(2).asInt();
                    int endMinuteIn = availableShift.getEndTime().getMinute();
                    int endMinuteJson = availableShiftNode.get(3).asInt();

                    if (startHourIn == startHourJson && startMinuteIn == startMinuteJson && endHourIn == endHourJson && endMinuteIn == endMinuteJson) {
                        availableShiftsIterator.remove();
                        objectWriter.writeValue(getJsonFile(), rootNode);
                    }

                }

            }
        }
    }

}
