package com.spamdetector.util;

import com.spamdetector.domain.TestFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SpamDetector {
    private double accuracy;
    private double precision;
    private final double laplaceK = 1e-8; //LaPlace smoothing constant

    // Get accuracy value
    public double getAccuracy() {
        return this.accuracy*100;
    }

    // Get precision value
    public double getPrecision() {
        return this.precision*100;
    }

    // Train the model and test it, then set precision and accuracy values
    public List<TestFile> trainAndTest(File mainDirectory) {

        Map<String, Double> probabilities = trainModel(mainDirectory);
        List<TestFile> testResults = test(mainDirectory, probabilities);

        double falsePositive = 0;
        double truePositives = 0;
        double trueNegative = 0;

        for (TestFile obj : testResults) {
            if (obj.getActualClass().equals("ham")) {
                if (obj.getSpamProbability() < 0.6) {
                    trueNegative++;
                } else {
                    falsePositive++;
                }
            } else {
                if (obj.getSpamProbability() >= 0.6) {
                    truePositives++;
                }
            }
        }

        this.precision = truePositives / (truePositives + falsePositive + laplaceK);
        this.accuracy = (truePositives + trueNegative) / (testResults.size() + laplaceK);
        return testResults;
    }

    // Test the trained model on emails in the test directory
    private List<TestFile> test(File mainDirectory, Map<String, Double> probabilities) {
        List<TestFile> testResults = new ArrayList<>();

        File hamDirectory = new File(mainDirectory, "test/ham");
        File spamDirectory = new File(mainDirectory, "test/spam");

        testResults.addAll(processEmails(hamDirectory, probabilities, "ham"));
        testResults.addAll(processEmails(spamDirectory, probabilities, "spam"));

        return testResults;
    }

    // Processes Emails Data
    private List<TestFile> processEmails(File directory, Map<String, Double> probabilities, String actualClass) {
        List<TestFile> results = new ArrayList<>();
        File[] emails = directory.listFiles();
        if (emails != null) {
            for (File email : emails) {
                double n = calculateNSum(email, probabilities);
                double spamProbability = 1 / (1 + Math.exp(n));
                results.add(new TestFile(email.getName(), spamProbability, actualClass));
            }
        }
        return results;
    }

    // Calculate the sum of probabilities of words in a file
    private double calculateNSum(File email, Map<String, Double> probabilities) {
        double n = 0.0;
        try (Scanner scanner = new Scanner(new FileInputStream(email), StandardCharsets.UTF_8)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] words = line.toLowerCase().split("\\W+");
                for (String word : words) {
                    double spamProb = probabilities.getOrDefault(word, laplaceK);
                    n += Math.log(1.0 - spamProb + laplaceK) - Math.log(spamProb + laplaceK);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return n;
    }

    // Train the model on data and calculate probability of each word being in spam
    private Map<String, Double> trainModel(File mainDirectory) {
        Map<String, Double> probabilities = new HashMap<>();

        File spamDir = new File(mainDirectory, "train/spam");
        File hamDir = new File(mainDirectory, "train/ham");
        File ham2Dir = new File(mainDirectory, "train/ham2");

        int spamCount = getEmailCount(spamDir);
        int hamCount = getEmailCount(hamDir) + getEmailCount(ham2Dir);

        Map<String, Integer> spamWordCounts = processFilesInDirectory(spamDir);
        Map<String, Integer> hamWordCounts = processFilesInDirectory(hamDir);
        Map<String, Integer> ham2WordCounts = processFilesInDirectory(ham2Dir);
        hamWordCounts.putAll(ham2WordCounts);


        Set<String> allWords = new HashSet<>(spamWordCounts.keySet());
        allWords.addAll(hamWordCounts.keySet());
        int allWordSize = allWords.size();

        for (String word : allWords) {
            int numSpamFiles = spamWordCounts.getOrDefault(word, 0);
            int numHamFiles = hamWordCounts.getOrDefault(word, 0);

            double probOfSpam = (numSpamFiles + laplaceK) / (spamCount + laplaceK * allWordSize);
            double probOfHam = (numHamFiles + laplaceK) / (hamCount + laplaceK * allWordSize);
            double finalProb = probOfSpam / (probOfSpam + probOfHam);
            probabilities.put(word, finalProb);
        }

        return probabilities;
    }

    // Count of files in the directory
    private int getEmailCount(File directory) {
        int count = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    count++;
                }
            }
        }
        return count;
    }

    // Insert every file into a map
    private Map<String, Integer> processFilesInDirectory(File directory) {
        Map<String, Integer> wordCounts = new HashMap<>();
        File[] files = directory.listFiles(File::isFile);
        if (files != null) {
            for (File file : files) {
                processFile(file, wordCounts);
            }
        }
        return wordCounts;
    }

    // Process words and place into a map
    private void processFile(File file, Map<String, Integer> wordCounts) {
        Set<String> uniqueWords = new HashSet<>();

        try (Scanner scanner = new Scanner(new FileInputStream(file), StandardCharsets.UTF_8)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                String[] words = line.split("\\W+");
                for (String word : words) {
                    if (!word.trim().isEmpty()) {
                        uniqueWords.add(word.toLowerCase());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String word : uniqueWords) {
            wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
        }
    }
}
