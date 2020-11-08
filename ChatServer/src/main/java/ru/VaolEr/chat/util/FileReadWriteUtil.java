package ru.VaolEr.chat.util;

import ru.VaolEr.ServerApp;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;

public class FileReadWriteUtil {

    private static final Logger logger = Logger.getLogger(FileReadWriteUtil.class.getName());

    public static void main(String[] args) {
        String username = "StriX";
        String filePath = "history_" + username + ".txt";

        for (int i = 0; i < 17; i++) {
            //addMessageToLocalHistoryFile(filePath, username, "String " + i);
            addMessageToServerHistoryFile(filePath, username, "String " + i);
        }
    }

    /**
     * <p>Add message to user's local history file.</p>
     *
     * @param filePath name and directory of file;
     * @param message string, which we need to write.
     *
     */
    public static void addMessageToLocalHistoryFile(String filePath, String username, String message){
        String text = message + "\n";
        try {
            writeStringToFile(filePath, text);
            logger.info("Add message to history.");
        }
        catch (IOException e) {
            //System.out.println(e);
            logger.severe(e.toString());
        }
    }

    /**
     * <p>Write string to file method.</p>
     *
     * @param filePath name and directory of file;
     * @param text string, which we need to write.
     *
     */
    private static void writeStringToFile(String filePath, String text) throws IOException {
        BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, true), StandardCharsets.UTF_8));
        bufferWriter.write(text);
        bufferWriter.close();
    }

    /**
     * <p>Add message to server history file.</p>
     *
     * @param filePath name and directory of file;
     * @param message string, which we need to write.
     *
     */
    public static void addMessageToServerHistoryFile(String filePath, String username, String message){
        String text = message + "\n";
        int messagesInFile;
        try {
            File file = new File(filePath);
            if(file.exists()) {
                messagesInFile = countLinesInFile(filePath);
                if (messagesInFile >= 100) {
                    shiftStringsInFile(filePath, 100);
                }
            }

            writeStringToFile(filePath, text);
        }
        catch (IOException e) {
            //System.out.println(e);
            logger.severe(e.toString());
        }
    }

    /**
     * <p>Shift lines in file to top.</p>
     *
     * <p>Explanation: when we write new line, more than lineLimit count
     * first line will be removed from file. New line wil add in the end of file.
     * Looks like FIFO. S0, S1, S2 in file. Limit = 3. Add S3. In file we will see
     * S1, S2, S3<p/>
     * @param filePath name and directory of file;
     * @param linesLimit limit of lines in file, from which we will rewrite data.
     */
    private static void shiftStringsInFile(String filePath, int linesLimit) {
        ArrayList<String> stringArrayList = new ArrayList<>();
        try {
            Scanner input = new Scanner(new File(filePath));
            int counter = 0;
            while (input.hasNextLine() && counter < linesLimit) {
                stringArrayList.add(input.nextLine());
                counter++;
            }
            input.close();

            clearFile(filePath);

            BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, true), StandardCharsets.UTF_8));
            for (int i = 1; i < stringArrayList.size(); i++) {
                //System.out.println(stringArrayList.get(i));
                bufferWriter.write(stringArrayList.get(i)+"\n");
            }
            //System.out.println("//**********************************//");
            bufferWriter.close();
        } catch(Exception e){
            //e.printStackTrace();
            logger.severe(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * <p>Return count of strings (lines) in file.</p>
     *
     * @param filePath name and directory of file
     *
     */
    public static int countLinesInFile(String filePath) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(filePath))) {
            byte[] c = new byte[1024];

            int readChars = is.read(c);
            if (readChars == -1) {
                // bail out if nothing to read
                return 0;
            }

            // make it easy for the optimizer to tune this loop
            int count = 0;
            while (readChars == 1024) {
                for (int i = 0; i < 1024; ) {
                    if (c[i++] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            // count remaining characters
            while (readChars != -1) {
                //System.out.println("In function value readChars: " + readChars);
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            return count == 0 ? 1 : count;
        }
    }

    /**
     * <p>Clear the file in filePath.</p>
     *
     * @param filePath name and directory of file
     *
     */
    public static void clearFile(String filePath){
        try {
            PrintWriter writer = new PrintWriter(filePath);
            writer.print("");
            writer.close();
        }catch (Exception e){
            //e.printStackTrace();
            logger.severe(Arrays.toString(e.getStackTrace()));
        }
    }


}