package cmu.csdetector.refactor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SaveRecommendationIntoFile {

    public void save(String recommendations){
        try {
            // Create a FileWriter object with the filename
            FileWriter fw = new FileWriter("recommendation.txt", true);

            // Create a BufferedWriter object to write the comments
            BufferedWriter bw = new BufferedWriter(fw);

            // Write the comment to the file
            bw.write(recommendations);
            bw.newLine();

            // Close the BufferedWriter
            bw.close();

            // Print a message to confirm that the comment has been saved
            //System.out.println("Comment saved successfully!");
        }
        catch (IOException e) {
            System.out.println("An error occurred while saving the comment.");
            e.printStackTrace();
        }
    }

    public void clear() {
        try {
            // Create a FileWriter object with the filename
            FileWriter fw = new FileWriter("recommendation.txt", false);

            // Create a BufferedWriter object to write the comments
            BufferedWriter bw = new BufferedWriter(fw);

            // Write the comment to the file
            bw.write("");

            // Close the BufferedWriter
            bw.close();
        }
        catch (IOException e) {
            System.out.println("An error occurred while saving the comment.");
            e.printStackTrace();
        }
    }
}
