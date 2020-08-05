/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.transport.udp.OSCPortOut;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.Init;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    MINDWAVESERVER();

    private static final String LOCAL_HOST = "127.0.0.1";
    private static final int THINKGEAR_PORT = 13854, MINDWAVE_SERVER_PORT = 45_000, MINDWAVE_SERVER_PORT_DESTINATION = MINDWAVE_SERVER_PORT + 1;
    private static final String TO_JSON_COMMAND = "{\"enableRawOutput\": false, \"format\": \"Json\"}\n";
    private static final String AUTHORIZE_APP_COMMAND = "{\"appName\": \"Mindwave Mobile 2 Data Server\", \"appKey\":"
            + "\"55cf229b95b3fafa976b385af1b5670a817208d2\"}";
    private static final ServerSocket SERVER_SOCKET;
    private static final int BRAINWAVE_MAX_VALUE = 4_000_000;

    private static boolean initialied = false;

    static {
        try {
            SERVER_SOCKET = new ServerSocket(THINKGEAR_PORT);
        } catch (IOException ex) {
            Logger.getLogger(MindWaveServer.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Unable to initialize Mindwave Server Socket", ex);
        }
    }

    /**
     * A public queue in which elements are published in addition to the UDP
     * output.
     */
    public static final NoBlockAddBlockingQueue<MindWavePacket> OUTPUT_QUEUE = new NoBlockAddBlockingQueue<>(20);

    /**
     * Checks the initialization state. THrows an exception if we're already
     * initialized.
     */
    private static void checkInitialization() {
        if (initialied) {
            throw new IllegalStateException("We've already started either a Test Data Thread, or a ThinkGearConnector Thread Already!");
        }
    }

    private static MindWavePacket generateRandomPacket() {
        var rand = new Random();
        return new MindWavePacket(rand.nextInt(101), rand.nextInt(101), rand.nextInt(101),
                rand.nextInt(101), rand.nextInt(BRAINWAVE_MAX_VALUE), rand.nextInt(BRAINWAVE_MAX_VALUE), rand.nextInt(BRAINWAVE_MAX_VALUE),
                rand.nextInt(BRAINWAVE_MAX_VALUE), rand.nextInt(BRAINWAVE_MAX_VALUE), rand.nextInt(BRAINWAVE_MAX_VALUE),
                rand.nextInt(BRAINWAVE_MAX_VALUE), rand.nextInt(BRAINWAVE_MAX_VALUE), rand.nextInt(101), 0);
    }

    /**
     * Starts the packet creator thread. Makes random packets for testing
     * purposes.
     */
    public static void startEmulatorThread() {
        startEmulatorThread("");
    }

    /**
     * Starts the packet creator thread. Makes random packets for testing
     * purposes.
     */
    public static void startEmulatorThread(String type) {
        checkInitialization();
        initialied = true;
        Runnable emulatorType;
        //Use the Hold-Value emulator type
        if (type.equalsIgnoreCase("TGC-Emulator-Squarewave")) {
            emulatorType = () -> {
                while (true) {
                    var mindwavePacket = generateRandomPacket();
                    System.out.println("Squarewave TGC-Emulator Thread Initial Packet:" + mindwavePacket);
                    final int MAX = 7;
                    for (int i = 0; i < MAX; i++) {
                        System.out.println("Squarewave TGC-Emulator Thread Index:" + i + "/" + MAX);
                        outputMindWavePacket(mindwavePacket);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(MindWaveServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }
            };
        } //Use the random emulator type
        else {
            emulatorType = () -> {
                System.out.println("Starting TGC Emulator Thread");
                while (true) {
                    try {
                        var randomPacket = generateRandomPacket();
                        System.out.println("Emulated Packet Created:\n\n" + randomPacket);
                        outputMindWavePacket(randomPacket);
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MindWaveServer.class.getName()).log(Level.SEVERE, null, ex);
                        throw new IllegalStateException("The Mindwave Server test data creator thread was interrupted", ex);
                    }

                }
            };
        }
        var thread = new Thread(emulatorType, Init.PROGRAM_NAME + " ThingGearConnector (TGC) Emulator Thread");

        thread.setDaemon(
                true);
        thread.start();
    }

    /**
     * Starts the MindWave server and initiates the connection to the ThinkGear
     * Connector. This occurs in another (daemon) thread.
     */
    public static void start() {
        checkInitialization();
        initialied = true;
        var thread = new Thread(() -> {
            while (true) {
                System.out.println("Connecting to host = " + LOCAL_HOST + ", port = " + THINKGEAR_PORT);
                try (var mindwaveSocket = SERVER_SOCKET.accept()) {
                    //Define variables
                    var mindwaveInput = mindwaveSocket.getInputStream();
                    var mindwaveReader = new BufferedReader(new InputStreamReader(mindwaveInput, Charset.forName("UTF-8")));
                    var mindwaveOutput = mindwaveSocket.getOutputStream();
                    //Write to output
                    System.out.println("Requesting Authorization: " + AUTHORIZE_APP_COMMAND);
                    mindwaveOutput.write(TO_JSON_COMMAND.getBytes());
                    mindwaveOutput.flush();
                    //Wait for authorization
                    System.out.println("Waiting for Authorization (3 Seconds)...");
                    Thread.sleep(3000);
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
                                    outputMindWavePacket(new MindWavePacket(sourceJson));
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
                System.out.println("Connection to ThinkGear Connector Dropped \nAttempting Reconnection");
            }
        }, Init.PROGRAM_NAME + " Live Data Decode/Export Thread");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Gets the boolean value for whether or not this server is running.
     *
     * @return The boolean
     */
    public static boolean isInitialized() {
        return initialied;
    }

    /**
     * Sends the packet created over the UDP connection given. Currently puts
     * the output on the output queue and sends it over UDP.
     *
     * @param packet The packet to send
     * @param udpOutputStream The datagram socket output stream
     * @throws UnknownHostException
     * @throws IOException
     */
    private static void outputMindWavePacket(MindWavePacket packet) {
        try {
            var oscOut = new OSCPortOut(InetAddress.getLocalHost(), MINDWAVE_SERVER_PORT_DESTINATION);
            var bundle = toOSCBundle(packet);
            //Only send this packet out if it has eSense values in it
            if (packet.hasESense()) {
                //Send out packets
                for (OSCPacket oscPacket : bundle.getPackets()) {
                    oscOut.send(oscPacket);
                }
                //Add to internal queue so that other classes (Viewer) can display data
                OUTPUT_QUEUE.add(packet);
            }
        } catch (OSCSerializeException | IOException ex) {
            Logger.getLogger(MindWaveServer.class
                    .getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Something messed up when sending an OSC Packet", ex);
        }
    }

    /**
     * Gets the OSC formatted version of a {@link MindWavePacket}.
     *
     * @param packet The packet to use as a data source
     * @return The OSCBundle of this packet
     */
    private static OSCBundle toOSCBundle(MindWavePacket packet) {
        return new OSCBundle(Arrays.asList(
                toMessage("/Attention", packet.getAttention()),
                toMessage("/Meditation", packet.getMeditation()),
                toMessage("/Delta", packet.getDelta()),
                toMessage("/Theta", packet.getTheta()),
                toMessage("/LowAlpha", packet.getLowAlpha()),
                toMessage("/HighAlpha", packet.getHighAlpha()),
                toMessage("/LowBeta", packet.getLowBeta()),
                toMessage("/HighBeta", packet.getHighBeta()),
                toMessage("/LowGamma", packet.getLowGamma()),
                toMessage("/HighGamma", packet.getHighGamma()),
                toMessage("/PoorSignalLevel", packet.getPoorSignalLevel()),
                toMessage("/BlinkStrength", packet.getBlinkStrength())
        ));
    }

    private static <T> OSCMessage toMessage(String text, T arg) {
        return new OSCMessage(text, Arrays.asList(arg));
    }
}
