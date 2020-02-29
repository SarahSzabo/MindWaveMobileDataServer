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
import org.json.JSONException;

/**
 * The initialization class for the Neurosky Mindwave Mobile.
 *
 * @author Sarah Szabo <SarahSzabo@Protonmail.com>
 */
public class Init {

    private static final String LOCAL_HOST = "127.0.0.1";
    private static final int THINKGEAR_PORT = 13854, MINDWAVE_SERVER_PORT = 45_000;
    private static final String TO_JSON_COMMAND = "{\"enableRawOutput\": false, \"format\": \"Json\"}\n";
    private static final String AUTHORIZE_APP_COMMAND = "{\"appName\": \"MAX MSP Mindwave Server\", \"appKey\":"
            + "\"55cf229b95b3fafa976b385af1b5670a817208d2\"}";

    /**
     * The main method where the server starts.
     *
     * @param args The command line arguments
     * @throws IOException If something happened
     * @throws java.lang.InterruptedException If interrupted while sleeping
     */
    /*
    TODO: Import Java Program into MAX
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Connecting to host = " + LOCAL_HOST + ", port = " + THINKGEAR_PORT);
        try (var mindwaveSocket = new Socket(LOCAL_HOST, THINKGEAR_PORT)) {
            //Define variables
            var mindwaveInput = mindwaveSocket.getInputStream();
            var mindwaveReader = new BufferedReader(new InputStreamReader(mindwaveInput, Charset.forName("UTF-8")));
            var mindwaveOutput = mindwaveSocket.getOutputStream();
            //Write to output
            System.out.println("Requesting Authorization: " + AUTHORIZE_APP_COMMAND);
            mindwaveOutput.write(TO_JSON_COMMAND.getBytes());
            mindwaveOutput.flush();
            //Wait for authorization
            System.out.println("Waiting for Authorization (5 Seconds)...");
            Thread.sleep(5000);
            if (mindwaveReader.ready()) {
                System.out.println("Authorization: " + mindwaveReader.readLine());
            }
            //Declare Output Stream to Max
            try (var maxmspOutputStream = new DatagramSocket(new InetSocketAddress(LOCAL_HOST, MINDWAVE_SERVER_PORT))) {
                //Start 60Hz Loop
                while (true) {
                    if (mindwaveReader.ready()) {
                        String sourceJson = mindwaveReader.readLine();
                        System.out.println("JSON TEXT RAW: \n\n" + sourceJson);
                        try {
                            var packet = new MindWavePacket(sourceJson);
                            System.out.println("\n\nMind Wave Packet: " + packet);
                            var text = packet.toByteString().getBytes();
                            var datagramPacket = new DatagramPacket(text, text.length, InetAddress.getByName("localhost"), MINDWAVE_SERVER_PORT);
                            maxmspOutputStream.send(datagramPacket);
                        } catch (JSONException je) {
                            System.err.println("JSON Exception Caught, Continuing Loop");
                        }
                    }
                    //16.6666666666 ms = 1/60 ms = 60Hz
                    Thread.sleep(17);
                }
            }
        }
    }
}
