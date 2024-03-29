/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave;

import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util.MindwaveServerMode;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util.ThinkGearServerConnectionQuality;
import java.time.LocalTime;
import java.util.Random;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * A class representing a packet from the Neurosky Mindwave Mobile hardware in
 * JSON format.
 *
 * @author Sarah Szabo <PhysicistSarah@Gmail.com>
 */
public class MindWavePacket {

    private static final Logger LOG = Logger.getLogger(MindWavePacket.class.getName());

    /**
     * A static factory for getting a log-based parameterization of the values.
     * All inputs are first run through the log algorithm (with the exception of
     * poorSignalLevel.
     *
     * @param attention This packet parameter
     * @param meditation This packet parameter
     * @param mentalEffort This packet parameter
     * @param familiarity This packet parameter
     * @param delta This packet parameter
     * @param theta This packet parameter
     * @param lowAlpha This packet parameter
     * @param highAlpha This packet parameter
     * @param lowBeta This packet parameter
     * @param highBeta This packet parameter
     * @param lowGamma This packet parameter
     * @param highGamma This packet parameter
     * @param blinkStrength This packet parameter
     * @param poorSignalLevel This packet parameter
     * @return The easily graphable mindwave packet
     */
    public static MindWavePacket generateEasyGraphPacket(int attention, int meditation, int mentalEffort, int familiarity, int delta,
            int theta, int lowAlpha, int highAlpha, int lowBeta, int highBeta, int lowGamma,
            int highGamma, int blinkStrength, int poorSignalLevel) {
        //
        return new MindWavePacket(attention, meditation, mentalEffort, familiarity, (int) Math.log(delta), (int) Math.log(theta),
                (int) Math.log(lowAlpha), (int) Math.log(highAlpha), (int) Math.log(lowBeta), (int) Math.log(highBeta),
                (int) Math.log(lowGamma), (int) Math.log(highGamma), blinkStrength, poorSignalLevel);

    }

    /**
     * Generates a random brainwave packet with the default scaling.
     *
     * @return The randomly generated packet
     */
    public static MindWavePacket generateRandomPacket() {
        return generateRandomPacket(MindWaveServer.BRAINWAVE_MAX_VALUE);
    }

    /**
     * Generates a random brainwave packet.
     *
     * @param BRAINWAVE_MAX_VALUE The scale maximum value for the brainwaves
     * @return The randomly generated packet
     */
    public static MindWavePacket generateRandomPacket(int BRAINWAVE_MAX_VALUE) {
        var rand = new Random();
        return new MindWavePacket(rand.nextInt(101), rand.nextInt(101), rand.nextInt(101),
                rand.nextInt(101), rand.nextInt(BRAINWAVE_MAX_VALUE), rand.nextInt(BRAINWAVE_MAX_VALUE), rand.nextInt(BRAINWAVE_MAX_VALUE),
                rand.nextInt(BRAINWAVE_MAX_VALUE), rand.nextInt(BRAINWAVE_MAX_VALUE), rand.nextInt(BRAINWAVE_MAX_VALUE),
                rand.nextInt(BRAINWAVE_MAX_VALUE), rand.nextInt(BRAINWAVE_MAX_VALUE), rand.nextInt(101), 200);
    }

    private final int attention, meditation, mentalEffort, familiarity, delta, theta,
            lowAlpha, highAlpha, lowBeta, highBeta, lowGamma, highGamma, blinkStrength, poorSignalLevel;
    private final LocalTime creationTime;

    private final boolean isBlinkOnly, hasESense, isScanningPacket;
    private final ThinkGearServerConnectionQuality connectionQuality;

    /**
     * The default value for all fields of the "double" type.
     */
    public final int MINDWAVE_DEFAULT_NULL_VALUE = 0;

    /**
     * The constructor that allows the caller to fully construct the packet.
     *
     * @param attention This eSense attribute
     * @param meditation This eSense attribute
     * @param mentalEffort This eSense attribute
     * @param familiarity This eSense attribute
     * @param delta This brainwave
     * @param theta This brainwave
     * @param lowAlpha This brainwave
     * @param highAlpha This brainwave
     * @param lowBeta This brainwave
     * @param highBeta This brainwave
     * @param lowGamma This brainwave
     * @param highGamma This brainwave
     * @param blinkStrength The strength of the blink
     * @param poorSignalLevel The signal level quality
     */
    public MindWavePacket(int attention, int meditation, int mentalEffort, int familiarity,
            int delta, int theta, int lowAlpha, int highAlpha, int lowBeta, int highBeta,
            int lowGamma, int highGamma, int blinkStrength, int poorSignalLevel) {
        this.attention = attention;
        this.meditation = meditation;
        this.mentalEffort = mentalEffort;
        this.familiarity = familiarity;
        this.delta = delta;
        this.theta = theta;
        this.lowAlpha = lowAlpha;
        this.highAlpha = highAlpha;
        this.lowBeta = lowBeta;
        this.highBeta = highBeta;
        this.lowGamma = lowGamma;
        this.highGamma = highGamma;
        this.blinkStrength = blinkStrength;
        this.poorSignalLevel = poorSignalLevel;

        this.creationTime = LocalTime.now();

        //TODO: Fix how these values are defined, and instead define them based on whether or not the JSON has these fields
        /*
        We consider this a "Blink Only" packet if basically everything is 0, set values according to TGC Spec
        Values are 0-200
        0: Good
        200: Disconnected
         */
        this.isBlinkOnly = (this.highAlpha == 0) && (this.attention == 0) && (this.meditation == 0);
        this.hasESense = (this.attention != 0) && (this.meditation != 0);

        //If TGC Data available, copy scanning status and connection status
        var tgcData = MindwaveServerMode.TGC.getData();
        if (tgcData.isPresent()) {
            this.connectionQuality = tgcData.get().getConnectionQuality();
            this.isScanningPacket = tgcData.get().isScanningPacket();
        } else {
            this.isScanningPacket = false;
            //Poor signal level definitions
            if (this.poorSignalLevel == 0) {
                this.connectionQuality = ThinkGearServerConnectionQuality.OPTIMAL;
            } else if (this.poorSignalLevel > 0 && this.poorSignalLevel <= 50) {
                this.connectionQuality = ThinkGearServerConnectionQuality.SUB_OPTIMAL;
            } else if (this.poorSignalLevel > 50 && this.poorSignalLevel < 200) {
                this.connectionQuality = ThinkGearServerConnectionQuality.POOR;
            } else {
                this.connectionQuality = ThinkGearServerConnectionQuality.DISCONNECTED;
            }
        }
    }

    /**
     * The constructor that accepts raw JSON text from the ThinkGear Connector.
     *
     * @param originText The raw JSON text from the ThinkGear Connector
     */
    public MindWavePacket(String originText) {
        /*
         Sample JSON Data:
         {"eSense":{"attention":91,"meditation":41},
        "eegPower":{"delta":1105014,"theta":211310,"lowAlpha":7730,"highAlpha":68568,"lowBeta":12949,"highBeta":47455,
        "lowGamma":55770,"highGamma":28247},"poorSignalLevel":0}
         */
        this.creationTime = LocalTime.now();
        JSONObject packet = new JSONObject(originText);
        if (packet.has("eSense")) {
            JSONObject eSense = packet.getJSONObject("eSense");
            this.attention = eSense.getInt("attention");
            this.meditation = eSense.getInt("meditation");
        } else {
            this.attention = MINDWAVE_DEFAULT_NULL_VALUE;
            this.meditation = MINDWAVE_DEFAULT_NULL_VALUE;
        }
        if (packet.has("eegPower")) {
            JSONObject eegPower = packet.getJSONObject("eegPower");
            this.delta = eegPower.getInt("delta");
            this.theta = eegPower.getInt("theta");
            this.lowAlpha = eegPower.getInt("lowAlpha");
            this.highAlpha = eegPower.getInt("highAlpha");
            this.lowBeta = eegPower.getInt("lowBeta");
            this.highBeta = eegPower.getInt("highBeta");
            this.lowGamma = eegPower.getInt("lowGamma");
            this.highGamma = eegPower.getInt("highGamma");
        } else {
            this.delta = MINDWAVE_DEFAULT_NULL_VALUE;
            this.theta = MINDWAVE_DEFAULT_NULL_VALUE;
            this.lowAlpha = MINDWAVE_DEFAULT_NULL_VALUE;
            this.highAlpha = MINDWAVE_DEFAULT_NULL_VALUE;
            this.lowBeta = MINDWAVE_DEFAULT_NULL_VALUE;
            this.highBeta = MINDWAVE_DEFAULT_NULL_VALUE;
            this.lowGamma = MINDWAVE_DEFAULT_NULL_VALUE;
            this.highGamma = MINDWAVE_DEFAULT_NULL_VALUE;
        }
        if (packet.has("poorSignalLevel")) {
            this.poorSignalLevel = packet.getInt("poorSignalLevel");
        } else {
            this.poorSignalLevel = MINDWAVE_DEFAULT_NULL_VALUE;
        }
        if (packet.has("blinkStrength")) {
            this.blinkStrength = packet.getInt("blinkStrength");
        } else {
            this.blinkStrength = MINDWAVE_DEFAULT_NULL_VALUE;
        }
        if (packet.has("mentalEffort")) {
            this.mentalEffort = packet.getInt("mentalEffort");
        } else {
            this.mentalEffort = MINDWAVE_DEFAULT_NULL_VALUE;
        }
        if (packet.has("familiarity")) {
            this.familiarity = packet.getInt("familiarity");
        } else {
            this.familiarity = MINDWAVE_DEFAULT_NULL_VALUE;
        }

        //Check if this is a scanning packet
        if (packet.has("status")) {
            var status = packet.getString("status");
            //TODO Add detailed scnaning information
            if (status.equalsIgnoreCase("scanning")) {
            }
            this.isScanningPacket = true;
            this.connectionQuality = ThinkGearServerConnectionQuality.ATTEMPTING_CONNECTION_SCANNING;
        } else {
            this.isScanningPacket = false;
            //Since this isn't a scanning packet, follow typical workflow
            if (this.poorSignalLevel == 0) {
                this.connectionQuality = ThinkGearServerConnectionQuality.OPTIMAL;
            } else if (this.poorSignalLevel > 0 && this.poorSignalLevel <= 50) {
                this.connectionQuality = ThinkGearServerConnectionQuality.SUB_OPTIMAL;
            } else if (this.poorSignalLevel > 50 && this.poorSignalLevel < 200) {
                this.connectionQuality = ThinkGearServerConnectionQuality.POOR;
            } else {
                this.connectionQuality = ThinkGearServerConnectionQuality.DISCONNECTED;
            }
        }
        /*
        We consider this a "Blink Only" packet if basically everything is 0, set values according to TGC Spec
        Values are 0-200
        0: Good
        200: Disconnected
         */
        this.isBlinkOnly = (this.highAlpha == 0) && (this.attention == 0) && (this.meditation == 0);
        this.hasESense = (this.attention != 0) && (this.meditation != 0);
    }

    /**
     * Gets the {@link MindWaveEasyGraphPacket} version of this packet.
     *
     * @return The version of this packet that is easily graph-able
     */
    public MindWavePacket toEasyGraphPacket() {
        return generateEasyGraphPacket(this.attention, this.meditation, this.mentalEffort, this.familiarity,
                this.delta, this.theta, this.lowAlpha, this.highAlpha, this.lowBeta, this.highBeta,
                this.lowGamma, this.highGamma, this.blinkStrength, this.poorSignalLevel);
    }

    /**
     * Returns this {@link MindWavePacket} in the form of: ATTENTION MEDITATION
     * DELTA THETA LOWALPHA HIGHALPHA LOWBETA HIGHBETA LOWGAMMA HIGHGAMMA
     * POORSIGNALLEVEL BLINKSTRENGTH for transmission to MAXMSP in the form of a
     * OSC UDP packet.
     *
     * @return
     */
    public String toByteString() {
        return this.attention + " " + this.meditation + " " + this.mentalEffort + " " + this.familiarity + " " + this.delta + " "
                + this.theta + " " + this.lowAlpha + " " + this.highAlpha + " " + this.lowBeta + " " + this.highBeta + " " + this.lowGamma
                + " " + this.highGamma + " " + this.poorSignalLevel + " " + this.blinkStrength;
    }

    @Override
    public String toString() {
        return "Mind Wave Mobile 2 Data Packet: " + this.creationTime + "\n\neSense:\n\nAttention: " + attention + "\nMeditation: " + meditation
                + "\nMental Effort: " + this.mentalEffort + "\nFamiliarity: " + this.familiarity
                + "\n\nEEG Power:\n\nDelta: " + delta + "\nTheta: " + theta + "\nLow Alpha: " + lowAlpha + "\nHigh Alpha: " + highAlpha
                + "\nLow Beta: " + lowBeta + "\nHigh Beta: " + highBeta + "\nLow Gamma: " + lowGamma + "\nHigh Gamma: " + highGamma
                + "\n\nPoor Signal Level: " + this.poorSignalLevel + "\n\nBlink Strength: " + this.blinkStrength;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public int getAttention() {
        return this.attention;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public int getMeditation() {
        return this.meditation;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public int getMentalEffort() {
        return this.mentalEffort;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public int getFamiliarity() {
        return this.familiarity;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public int getDelta() {
        return this.delta;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public int getTheta() {
        return this.theta;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public int getLowAlpha() {
        return this.lowAlpha;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public int getHighAlpha() {
        return this.highAlpha;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public int getLowBeta() {
        return this.lowBeta;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public int getHighBeta() {
        return this.highBeta;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public int getLowGamma() {
        return this.lowGamma;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public int getHighGamma() {
        return this.highGamma;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public int getBlinkStrength() {
        return this.blinkStrength;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public int getPoorSignalLevel() {
        return this.poorSignalLevel;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public LocalTime getCreationTime() {
        return this.creationTime;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public boolean isScanningPacket() {
        return this.isScanningPacket;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public boolean isBlinkOnly() {
        return this.isBlinkOnly;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public ThinkGearServerConnectionQuality getConnectionQuality() {
        return this.connectionQuality;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public boolean hasESense() {
        return this.hasESense;
    }

}
