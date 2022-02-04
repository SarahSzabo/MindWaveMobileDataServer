package com.protonmail.sarahszabo.mindwavemobiledataserver.core;

import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.MindWaveServer;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.ui.mindwaveviewer.MindwaveViewer;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 * The initialization class for the Neurosky Mindwave Mobile.
 *
 * @author Sarah Szabo <SarahSzabo@Protonmail.com>
 */
public class Init {

    /**
     * The name of the program.
     */
    public static final String PROGRAM_NAME = "Mindwave Mobile 2 EEG Data Server (DS)";
    /**
     * The version number of the program.
     */
    public static final String PROGRAM_VERSION = "2.0";
    /**
     * The full name + version number of the program.
     */
    public static final String FULL_PROGRAM_NAME = PROGRAM_NAME + " " + PROGRAM_VERSION;

    /**
     * The main method where the server starts.
     *
     * @param args The command line arguments
     * @throws java.net.URISyntaxException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws URISyntaxException, IOException {
        //TODO: Test Dummy Value Server
        if (args.length == 0) {
            printHelpDocAndExit();
        } else if (args.length == 1) {
            //Just generate random packets
            if (args[0].equalsIgnoreCase("TestData")) {
                MindWaveServer.startEmulatorThread();
                while (true) {
                    try {
                        Thread.sleep(10_000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Init.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                //Use Live TGC data
            } else if (args[0].equalsIgnoreCase("TGC")) {
                MindWaveServer.start();
            } else {
                printHelpDocAndExit();
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("Viewer")) {
                if (args[1].equalsIgnoreCase("TGC-Emulator")) {
                    MindWaveServer.startEmulatorThread();
                    //Use Live TGC data
                } else if (args[1].equalsIgnoreCase("TGC-Emulator-Squarewave")) {
                    MindWaveServer.startEmulatorThread("TGC-Emulator-Squarewave");
                    //Use Live TGC data
                } else if (args[1].equalsIgnoreCase("TGC")) {
                    MindWaveServer.start();
                    MindwaveViewer.initializeUI();
                } else {
                    printHelpDocAndExit();
                }
                //Initialize UI in either case
                MindwaveViewer.initializeUI();
            }
        } else {
            printHelpDocAndExit();
        }
        //MindwaveViewer.initializeUI();
    }

    private static void printHelpDocAndExit() throws URISyntaxException, IOException {
        var helpDoc = IOUtils.toString(Init.class.getResourceAsStream("/Help Text.dat"), Charset.defaultCharset());
        var prefix = FULL_PROGRAM_NAME + " Help Documentation:\n\n";
        messageThenExit(prefix + "\n\n" + helpDoc);
    }

    public static void messageThenExit(String message) {
        System.out.println(message);
        System.exit(0);
    }
}
