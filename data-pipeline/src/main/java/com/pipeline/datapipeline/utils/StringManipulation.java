package com.pipeline.datapipeline.utils;

public class StringManipulation {
    public static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // Remove special characters and split into words
        String[] words = input.replaceAll("[^a-zA-Z0-9]+", " ").split(" ");

        StringBuilder camelCaseString = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                camelCaseString.append(word.substring(0, 1).toUpperCase()); // Capitalize the first letter
                camelCaseString.append(word.substring(1).toLowerCase());    // Lowercase the rest of the word
            }
        }

        return camelCaseString.toString();
    }
}
