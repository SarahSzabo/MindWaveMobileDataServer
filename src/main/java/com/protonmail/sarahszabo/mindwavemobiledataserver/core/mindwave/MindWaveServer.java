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
import com.illposed.osc.transport.OSCPortOut;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.Init;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util.MindwaveEventListener;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util.MindwaveServerMode;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util.MindwaveServerStatusListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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

    /**
     * String for local host.
     */
    private static final String LOCAL_HOST = "127.0.0.1";
    /**
     * The thinkgear port.
     */
    private static final int THINKGEAR_PORT = 13854,
            /**
             * The mindwave server port that we have reserved.
             */
            MINDWAVE_SERVER_PORT = 45_000,
            /**
             * The port we use for OSC broadcasting.
             */
            MINDWAVE_SERVER_OSC_BROADCAST = MINDWAVE_SERVER_PORT + 1,
            /**
             * The port we use for raw OSC broadcasting.
             */
            MINDWAVE_SERVER_RAW_BROADCAST = MINDWAVE_SERVER_OSC_BROADCAST + 1;
    /**
     * The command string we use to enable JSON output from TGC.
     */
    private static final String TO_JSON_COMMAND = "{\"enableRawOutput\": false, \"format\": \"Json\"}\n";
    /**
     * Our authorization to TGC command string.
     */
    private static final String AUTHORIZE_APP_COMMAND = "{\"appName\": \"Mindwave Mobile 2 Data Server\", \"appKey\":"
            + "\"55cf229b95b3fafa976b385af1b5670a817208d2\"}";
    /**
     * The maximum range for brainwaves.
     */
    public static final int BRAINWAVE_MAX_VALUE = 4_000_000;

    /**
     * The command string to start the squarewave emulator.
     */
    private static final String SQUAREWAVE_EMULATOR_COMMAND = "TGC-Emulator-Squarewave";

    /**
     * The connection status of the EEG. Used for monitoring which mode we are
     * in, and which thread gets to output data to the GUI / port. By default,
     * this is set to TGC mode.
     */
    private static final AtomicReference<MindwaveServerMode> SERVER_MODE = new AtomicReference<>(MindwaveServerMode.TGC);

    /**
     * A sentinel which ensures that the server has not already been
     * initialized.
     */
    private static boolean initialied = false;

    /**
     * The mindwave event listener list
     */
    private static final List<MindwaveEventListener> EVENT_LISTENERS = new ArrayList<>(20);

    /**
     * The mindwave event listener list
     */
    private static final List<MindwaveServerStatusListener> SERVER_STATUS_LISTENERS = new ArrayList<>(2);

    /**
     * Gets the current server mode.
     *
     * @return The current mode the server is in
     */
    public static MindwaveServerMode currentMode() {
        return SERVER_MODE.get();
    }

    /**
     * Registers an event listener for updates to the mindwave EEG packets.
     *
     * @param listener The listener to register
     */
    public static void registerServerStatusListener(MindwaveServerStatusListener listener) {
        SERVER_STATUS_LISTENERS.add(listener);
    }

    /**
     * Registers an event listener for updates to the mindwave EEG packets.
     *
     * @param listener The listener to register
     */
    public static void registerMindwaveEventListener(MindwaveEventListener listener) {
        EVENT_LISTENERS.add(listener);
    }

    /**
     * Checks the initialization state. THrows an exception if we're already
     * initialized.
     */
    private static void checkInitialization() {
        if (initialied) {
            throw new IllegalStateException("We've already started either a Test Data Thread, or a ThinkGearConnector Thread Already!");
        }
    }

    /**
     * Starts the packet creator thread. Makes random packets for testing
     * purposes.
     *
     * @deprecated Currently, we have no use for this thread, but there is a
     * possibility it may be used in the future for more compelx emulation
     * requirements.
     */
    @Deprecated
    public static void startEmulatorThread() {
        startEmulatorThread("");
    }

    /**
     * Starts the packet creator thread.Makes random packets for testing
     * purposes.
     *
     * @param type The type of emulator to start
     * @deprecated Currently, we have no use for this thread, but there is a
     * possibility it may be used in the future for more compelx emulation
     * requirements.
     */
    @Deprecated
    public static void startEmulatorThread(String type) {
        Runnable emulatorType;
        //Use the Hold-Value emulator type
        if (type.equalsIgnoreCase("TGC-Emulator-Squarewave")) {
            emulatorType = () -> {
                while (true) {
                    try {
                        var mindwavePacket = MindWavePacket.generateRandomPacket(BRAINWAVE_MAX_VALUE);
                        System.out.println("Squarewave TGC-Emulator Thread Initial Packet:" + mindwavePacket);
                        MindwaveServerMode.SQUAREWAVE_EMULATED.setData(mindwavePacket);
                        Thread.sleep(33);
                        break;
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException("TGC Squarewave Emulator thread interrupted while sleeping", ex);
                    }
                }
            };
        } //Not recognized
        else {
            throw new IllegalStateException(type + " emulator not recognized. Misspelling of argument?");
        }
        var thread = new Thread(emulatorType, "ThingGearConnector (TGC) Emulator Thread");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Starts the MindWave server and initiates the connection to the ThinkGear
     * Connector. This occurs in another (daemon) thread.
     */
    public static void start() {
        checkInitialization();
        initialied = true;
        //Launch authorization thread
        var thread = new Thread(() -> {
            while (true) {
                try {
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
                        System.out.println("Waiting for Authorization (3 Seconds)...");
                        Thread.sleep(3000);
                        if (mindwaveReader.ready()) {
                            System.out.println("Authorization: " + mindwaveReader.readLine());
                        }
                        //Start 30Hz Loop
                        while (true) {
                            if (mindwaveReader.ready()) {
                                String sourceJson = mindwaveReader.readLine();
                                System.out.println("JSON TEXT RAW: \n\n" + sourceJson);
                                try {
                                    var newPacket = new MindWavePacket(sourceJson);
                                    MindwaveServerMode.TGC.setData(newPacket);
                                    //TODO Atomic Reference
                                } catch (JSONException je) {
                                    System.err.println("JSON Exception Caught, Continuing Loop");
                                }
                            }
                            //.03333333333333 ms = 1/30 s = 30Hz
                            Thread.sleep(33);
                        }
                    } catch (InterruptedException | IOException ex) {
                        //Logger.getLogger(MindWaveServer.class.getName()).log(Level.SEVERE, null, ex);
                        System.err.println("Exception Caught: " + ex);
                    }
                    System.out.println("Connection to ThinkGear Connector Dropped \nAttempting Reconnection");
                    Thread.sleep(33);
                } catch (InterruptedException ex) {
                    throw new IllegalStateException("TGC authorization thrad interrupted while sleeping", ex);
                }
            }
        }, "TGC Live Data Connect/Decode/Export Thread");
        //This makes sense only with the GUI Viewer
        thread.setDaemon(true);
        thread.start();

        //Always start the squarewave emulator with TGC in case of dropping
        //startEmulatorThread(SQUAREWAVE_EMULATOR_COMMAND);
        //Always start the output thread which chooses which data source to pull from.
        startOutputThread();
    }

    /**
     * Changes the server mode and notifies all listeners.
     *
     * @param mode The mode to change to
     */
    private static void changeMode(MindwaveServerMode mode) {
        SERVER_MODE.set(mode);
        SERVER_STATUS_LISTENERS.parallelStream().forEach(listener -> listener.serverModeUpdate(mode));
    }

    /**
     * Starts the thread which calls for output once a second.
     */
    private static void startOutputThread() {
        //If TGC, check for data, if none, set to Emulator. If in emulator, check for TGC.
        var thread = new Thread(() -> {
            while (true) {
                if (SERVER_MODE.get() == MindwaveServerMode.TGC) {
                    //Try and get something from TGC
                    boolean canReachTGC = false;
                    //Do for 4 seconds
                    for (int i = 0; i < 12; i++) {
                        if (MindwaveServerMode.TGC.isReady()) {
                            canReachTGC = true;
                            break;
                        } else {
                            try {
                                Thread.sleep(333);
                            } catch (InterruptedException ex) {
                                throw new IllegalStateException("Output thread selector interrupted while sleeping", ex);
                            }
                        }
                    }
                    if (canReachTGC) {
                        outputMindWavePacket(MindwaveServerMode.TGC.getData());
                    } else {
                        changeMode(MindwaveServerMode.SQUAREWAVE_EMULATED);
                        continue;
                    }
                } else {
                    if (MindwaveServerMode.TGC.isReady()) {
                        changeMode(MindwaveServerMode.TGC);
                    } else {
                        try {
                            outputMindWavePacket(MindwaveServerMode.SQUAREWAVE_EMULATED.getData());
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            throw new IllegalStateException("Output thread selector interrupted while sleeping", ex);
                        }
                    }
                }
            }
        }, Init.PROGRAM_NAME + " Output Decider Thread");
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
            //Notify listeners of event change
            EVENT_LISTENERS.parallelStream().forEach(listener -> listener.mindwaveUpdate(packet));

            //Broadcast OSC & RAW UDP Integers
            var oscOut = new OSCPortOut(InetAddress.getLocalHost(), MINDWAVE_SERVER_OSC_BROADCAST);
            var bundle = toOSCBundle(packet);
            //Only send this packet out if it has eSense values in it
            if (packet.hasESense()) {
                //Send out OSC Bundle
                for (OSCPacket oscPacket : bundle.getPackets()) {
                    oscOut.send(oscPacket);
                }
                //Broadcast RAW Integers over UDP
                var socket = new DatagramSocket();
                var bytes = packet.toByteString().getBytes();
                socket.send(new DatagramPacket(bytes, bytes.length, InetAddress.getLocalHost(), MINDWAVE_SERVER_RAW_BROADCAST));

                //Close Sockets
                socket.close();
                oscOut.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(MindWaveServer.class
                    .getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Something messed up when sending an OSC Packet", ex);
        } catch (OSCSerializeException ex) {
            Logger.getLogger(MindWaveServer.class.getName()).log(Level.SEVERE, null, ex);
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
