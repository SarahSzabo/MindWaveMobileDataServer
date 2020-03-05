/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.json.JSONException;

/**
 * An enum that represents a MindWave server.
 *
 * @author Sarah Szabo <SarahSzabo@Protonmail.com>
 */
public enum MindWaveServer {
    /**
     * The MindWave server instance.
     */
    MINDWAVESERVER;

    private static final ExecutorService mindwaveServerExecutor = Executors.newSingleThreadExecutor(new BasicThreadFactory.Builder()
            .daemon(true).namingPattern("MindWave Live Data Server").build());

    private static final String LOCAL_HOST = "127.0.0.1";
    private static final int THINKGEAR_PORT = 13854, MINDWAVE_SERVER_PORT = 45_000;
    private static final String TO_JSON_COMMAND = "{\"enableRawOutput\": false, \"format\": \"Json\"}\n";
    private static final String AUTHORIZE_APP_COMMAND = "{\"appName\": \"Mindwave Mobile 2 Data Server\", \"appKey\":"
            + "\"55cf229b95b3fafa976b385af1b5670a817208d2\"}";

    /**
     * A public queue in which elements are published in addition to the UDP
     * output.
     */
    public final NoBlockAddBlockingQueue<MindWavePacket> OUTPUT_QUEUE = new NoBlockAddBlockingQueue<>(20);

    /**
     * Starts the MindWave server and initiates the connection to the ThinkGear
     * Connector. This occurs in another (daemon) thread.
     */
    public void start() {
        mindwaveServerExecutor.submit(() -> {
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
                try (var udpOutputStream = new DatagramSocket(new InetSocketAddress(LOCAL_HOST, MINDWAVE_SERVER_PORT))) {
                    //Start 60Hz Loop
                    while (true) {
                        if (mindwaveReader.ready()) {
                            String sourceJson = mindwaveReader.readLine();
                            System.out.println("JSON TEXT RAW: \n\n" + sourceJson);
                            try {
                                var packet = new MindWavePacket(sourceJson);
                                System.out.println(packet);
                                var text = packet.toByteString().getBytes();
                                var datagramPacket = new DatagramPacket(text, text.length,
                                        InetAddress.getByName("localhost"), MINDWAVE_SERVER_PORT);
                                udpOutputStream.send(datagramPacket);
                                OUTPUT_QUEUE.add(packet);
                            } catch (JSONException je) {
                                System.err.println("JSON Exception Caught, Continuing Loop");
                            }
                        }
                        //.03333333333333 ms = 1/30 s = 30Hz
                        Thread.sleep(33);
                    }
                }
            } catch (InterruptedException | IOException ex) {
                //Logger.getLogger(MindWaveServer.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Exception Caught: " + ex);
            }
        });

    }

    /**
     * Shuts down the MindWave server and the connection to the ThinkGear
     * Connector.
     */
    public void shutdown() {
        mindwaveServerExecutor.shutdown();
    }
}
