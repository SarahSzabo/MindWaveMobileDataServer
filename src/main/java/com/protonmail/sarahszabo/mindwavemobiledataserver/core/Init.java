package com.protonmail.sarahszabo.mindwavemobiledataserver.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import org.json.JSONException;

/**
 * The initialization class for the Neurosky Mindwave Mobile.
 *
 * @author Sarah Szabo <Sarah.Szabo@Protonmail.com>
 */
public class Init {

    private static final String THINKGEAR_HOST = "127.0.0.1";
    private static final int THINKGEAR_PORT = 13854;
    private static final String TO_JSON_COMMAND = "{\"enableRawOutput\": false, \"format\": \"Json\"}\n";

    /**
     * The main method where the server starts.
     *
     * @param args The command line arguments
     * @throws IOException If something happened
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Connecting to host = " + THINKGEAR_HOST + ", port = " + THINKGEAR_PORT);
        try (Socket clientSocket = new Socket(THINKGEAR_HOST, THINKGEAR_PORT)) {
            //Define variables
            var input = clientSocket.getInputStream();
            var reader = new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8")));
            var output = clientSocket.getOutputStream();
            //Write to output
            System.out.println("Sending command: " + TO_JSON_COMMAND);
            output.write(TO_JSON_COMMAND.getBytes());
            output.flush();
            //Start 60Hz Loop
            while (true) {
                if (reader.ready()) {
                    String sourceJson = reader.readLine();
                    var packet = new MindWavePacket(sourceJson);
                    System.out.println("JSON TEXT RAW: \n\n" + sourceJson);
                }
                //16.6666666666 ms = 1/60 ms = 60Hz
                Thread.sleep(17);
            }
        }
    }
}
