package tez.experiment.debug;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * Created by suat on 08-May-17.
 */
public class Reporter {
    private PrintStream printStream;
    private ReportMode reportMode;

    public Reporter (String filePath, ReportMode reportMode) {
        this.reportMode = reportMode;
        try {
            this.printStream = new PrintStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void report(String string) {
        if(reportMode == ReportMode.CONSOLE) {
            System.out.println(string);
        } else {
            writeToFile(string);
        }
    }

    private void writeToFile(String string) {
        printStream.println(string);
    }

    public enum ReportMode {
        CONSOLE, FILE
    }
}
