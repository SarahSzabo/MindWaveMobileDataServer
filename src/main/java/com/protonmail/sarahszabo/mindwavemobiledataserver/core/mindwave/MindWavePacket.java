/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave;

import java.time.LocalTime;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * A class representing a packet from the Neurosky Mindwave Mobile hardware is
 * JSON format.
 *
 * @author Sarah Szabo <PhysicistSarah@Gmail.com>
 */
public class MindWavePacket {

    private static final Logger LOG = Logger.getLogger(MindWavePacket.class.getName());

    private final double attention, meditation, mentalEffort, familiarity, delta, theta,
            lowAlpha, highAlpha, lowBeta, highBeta, lowGamma, highGamma, blinkStrength, poorSignalLevel;
    private final LocalTime creationTime;

    private final boolean isBlinkPacketOnly;
    private final ThinkGearServerConnectionQuality isPoorConnectionQuality;

    /**
     * The default value for all fields of the "double" type.
     */
    public final double MINDWAVE_DEFAULT_NULL_VALUE = 0;

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
            this.attention = eSense.getDouble("attention");
            this.meditation = eSense.getDouble("meditation");
        } else {
            this.attention = MINDWAVE_DEFAULT_NULL_VALUE;
            this.meditation = MINDWAVE_DEFAULT_NULL_VALUE;
        }
        if (packet.has("eegPower")) {
            JSONObject eegPower = packet.getJSONObject("eegPower");
            this.delta = eegPower.getDouble("delta");
            this.theta = eegPower.getDouble("theta");
            this.lowAlpha = eegPower.getDouble("lowAlpha");
            this.highAlpha = eegPower.getDouble("highAlpha");
            this.lowBeta = eegPower.getDouble("lowBeta");
            this.highBeta = eegPower.getDouble("highBeta");
            this.lowGamma = eegPower.getDouble("lowGamma");
            this.highGamma = eegPower.getDouble("highGamma");
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

        /*
        We consider this a "Blink Only" packet if basically everything is 0, set values according to TGC Spec
        Values are 0-200
        0: Good
        200: Disconnected
         */
        this.isBlinkPacketOnly = (this.highAlpha == 0) && (this.attention == 0) && (this.meditation == 0);
        if (this.poorSignalLevel == 0) {
            this.isPoorConnectionQuality = ThinkGearServerConnectionQuality.OPTIMAL;
        } else if (this.poorSignalLevel > 0 && this.poorSignalLevel < 100) {
            this.isPoorConnectionQuality = ThinkGearServerConnectionQuality.SUB_OPTIMAL;
        } else if (this.poorSignalLevel > 100 && this.poorSignalLevel < 200) {
            this.isPoorConnectionQuality = ThinkGearServerConnectionQuality.POOR;
        } else {
            this.isPoorConnectionQuality = ThinkGearServerConnectionQuality.DISCONNECTED;
        }
    }

    /**
     * Returns this {@link MindWavePacket} in the form of:
     * ATTENTION|MEDITATION|DELTA|THETA|LOWALPHA|HIGHALPHA|LOWBETA|HIGHBETA|LOWGAMMA|HIGHGAMMA|
     * POORSIGNALLEVEL|BLINKSTRENGTH for transmission to MAXMSP in the form of a
     * UDP packet.
     *
     * @return
     */
    public String toByteString() {
        return this.attention + "|" + this.meditation + "|" + this.mentalEffort + "|" + this.familiarity + "|" + this.delta + "|" + this.theta + "|"
                + this.lowAlpha + "|" + this.highAlpha + "|" + this.lowBeta + "|" + this.highBeta + "|" + this.lowGamma
                + "|" + this.highGamma + "|" + this.poorSignalLevel + "|" + this.blinkStrength;
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
    public double getAttention() {
        return this.attention;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public double getMeditation() {
        return this.meditation;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public double getMentalEffort() {
        return this.mentalEffort;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public double getFamiliarity() {
        return this.familiarity;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public double getDelta() {
        return this.delta;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public double getTheta() {
        return this.theta;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public double getLowAlpha() {
        return this.lowAlpha;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public double getHighAlpha() {
        return this.highAlpha;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public double getLowBeta() {
        return this.lowBeta;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public double getHighBeta() {
        return this.highBeta;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public double getLowGamma() {
        return this.lowGamma;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public double getHighGamma() {
        return this.highGamma;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public double getBlinkStrength() {
        return this.blinkStrength;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public double getPoorSignalLevel() {
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
    public boolean isBlinkPacketOnly() {
        return this.isBlinkPacketOnly;
    }

    /**
     * Gets this field
     *
     * @return This field
     */
    public ThinkGearServerConnectionQuality isPoorConnectionQuality() {
        return this.isPoorConnectionQuality;
    }

}
