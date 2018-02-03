package tez2.experiment.debug;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by suat on 08-May-17.
 */
public class Reporter {
    private File outputFile;
    private BufferedWriter bw = null;
    private FileWriter fw = null;
    private ReportMode reportMode;

    public Reporter() {
        this.reportMode = ReportMode.CONSOLE;
    }

    public Reporter(String filePath) {
        this.reportMode = ReportMode.FILE;
        this.outputFile = new File(filePath);
        try {
            outputFile.getParentFile().mkdirs();
            fw = new FileWriter(outputFile, true);
            bw = new BufferedWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void report(String string) {
        if (reportMode == ReportMode.CONSOLE) {
            System.out.println(string);
        } else {
            writeToFile(string);
        }
    }

    private void writeToFile(String string) {
        try {
            bw.append(string + "\n");
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void finalizeReporting() {
        try {
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public enum ReportMode {
        CONSOLE, FILE
    }
}
