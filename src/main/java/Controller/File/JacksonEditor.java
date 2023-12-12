package Controller.File;

import Model.Schedules.DaySchedule;
import Model.Time.TimeUnavailable;
import Model.Time.Week;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import Model.Schedules.Shift;

import java.util.Iterator;

import static Controller.BouncyCastleEncrypter.hashPassword;


public class JacksonEditor extends Jackson {

    public static void addWorker(String username, String password, String status) {
        //Have to check for duplicate usernames

        // I updated what you wrote here
        JsonNode rootNode = getRootNode();
        ObjectMapper objectMapper = getObjectMapper();
        ObjectWriter objectWriter = getObjectWriter();

        // Check if username already exists
        boolean usernameExists = false;
        JsonNode workersNode = rootNode.path("workers");
        for (JsonNode workerNode : workersNode) {
            if (workerNode.path("username").asText().equals(username)) {
                usernameExists = true;
                break;
            }
        }


        // Add new worker only if username is unique
        if (!usernameExists) {
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
        for (JsonNode workerNode : managersNode) {
            if (workerNode.path("username").asText().equals(username)) {
                usernameExists = true;
                break;
            }
        }

        if (!usernameExists) {
            JsonNode newManager = objectMapper.createObjectNode()
                    .put("username", username)
                    .put("password", hashPassword(password));

            ((com.fasterxml.jackson.databind.node.ArrayNode) managersNode).add(newManager);


            try {
                objectWriter.writeValue(getJsonFile(), rootNode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("Could not add manager");
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

}
