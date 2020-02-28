package com.protonmail.sarahszabo.mindwavemobiledataserver.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * The initialization class for the Neurosky Mindwave Mobile.
 *
 * @author Sarah Szabo <SarahSzabo@Protonmail.com>
 */
public class Init {

    private static final String LOCAL_HOST = "127.0.0.1";
    private static final int THINKGEAR_PORT = 13854;
    private static final String TO_JSON_COMMAND = "{\"enableRawOutput\": false, \"format\": \"Json\"}\n";

    /**
     * The main method where the server starts.
     *
     * @param args The command line arguments
     * @throws IOException If something happened
     */
    /*
    TODO: Output to Ports
    TODO: Import Java Program into MAX
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Connecting to host = " + LOCAL_HOST + ", port = " + THINKGEAR_PORT);
        try (Socket clientSocket = new Socket(LOCAL_HOST, THINKGEAR_PORT)) {
            //Define variables
            var input = clientSocket.getInputStream();
            var reader = new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8")));
            var output = clientSocket.getOutputStream();
            //Write to output
            System.out.println("Sending command: " + TO_JSON_COMMAND);
            output.write(TO_JSON_COMMAND.getBytes());
            output.flush();
            //Declare Output Stream to Max
            var toMax = new DatagramSocket(new InetSocketAddress(LOCAL_HOST, 45_000));
            var address = InetAddress.getByName("localhost");
            //Start 60Hz Loop
            while (true) {
                if (reader.ready()) {
                    String sourceJson = reader.readLine();
                    var packet = new MindWavePacket(sourceJson);
                    System.out.println("JSON TEXT RAW: \n\n" + sourceJson);
                    var text = packet.toByteString().getBytes();
                    var datagramPacket = new DatagramPacket(text, text.length, address, 45_000);
                }
                //16.6666666666 ms = 1/60 ms = 60Hz
                Thread.sleep(17);
            }
        }
    }
}
