package com.spamdetector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spamdetector.domain.TestFile;
import com.spamdetector.util.SpamDetector;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import jakarta.ws.rs.core.Response;

@Path("/spam")
public class SpamResource {

    // SpamDetector Class is responsible for all the SpamDetecting logic
    SpamDetector detector = new SpamDetector();
    List<TestFile> result;

    // Load resources, train and test to improve performance on the endpoint calls
    SpamResource() throws URISyntaxException, IOException {
        this.result = this.trainAndTest();
    }

    // Reads the contents from a file provided folder
    private String readFileContents(String filename) {
        if (filename.charAt(0) != '/')
            filename = '/' + filename;

        try {
            java.nio.file.Path file = java.nio.file.Path.of(
                    SpamResource.class.getResource(filename)
                            .toString()
                            .substring(6));
            return Files.readString(file);
        } catch (IOException e) {
            return "Did you forget to create the file?\n" +
                    "Is the file in the right location?\n" +
                    e;
        }
    }

    // Get table results
    @GET
    @Produces("application/json")
    public Response getSpamResults() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String to_return = mapper.writeValueAsString(result);
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Content-Type", "application/json")
                .entity(to_return)
                .build();
    }


    // Return the accuracy of the detector, return in a Response object
    @GET
    @Path("/accuracy")
    @Produces("application/json")
    public Response getAccuracy() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String to_return =mapper.writeValueAsString(this.detector.getAccuracy());
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Content-Type", "application/json")
                .entity(to_return)
                .build();
    }

    // Return the precision of the detector, return in a Response object
    @GET
    @Path("/precision")
    @Produces("application/json")
    public Response getPrecision() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String to_return =mapper.writeValueAsString(this.detector.getPrecision());
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Content-Type", "application/json")
                .entity(to_return)
                .build();
    }

    private List<TestFile> trainAndTest() throws URISyntaxException, IOException {
        if (this.detector==null){
            this.detector = new SpamDetector();
        }

        // Load the main directory "data" here from the Resources folder
        File mainDirectory = new File(Objects.requireNonNull(getClass().getResource("/data")).toURI());;
        return this.detector.trainAndTest(mainDirectory);
    }
}