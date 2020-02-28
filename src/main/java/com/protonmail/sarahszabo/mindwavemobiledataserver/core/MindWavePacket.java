/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protonmail.sarahszabo.mindwavemobiledataserver.core;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class representing a packet from the Neurosky Mindwave Mobile hardware is
 * JSON format.
 *
 * @author Sarah Szabo <PhysicistSarah@Gmail.com>
 */
public class MindWavePacket {

    private static final Logger LOG = Logger.getLogger(MindWavePacket.class.getName());

    private final double attention, meditation, mentalEffort, familiarity, delta, theta, lowAlpha, highAlpha, lowBeta, highBeta, lowGamma, highGamma,
            blinkStrength, poorSignalLevel;
    private final ZonedDateTime creationTime;

    public MindWavePacket(String originText) {
        /*
         Sample JSON Data:
         {"eSense":{"attention":91,"meditation":41},
        "eegPower":{"delta":1105014,"theta":211310,"lowAlpha":7730,"highAlpha":68568,"lowBeta":12949,"highBeta":47455,
        "lowGamma":55770,"highGamma":28247},"poorSignalLevel":0}
         */
        this.creationTime = ZonedDateTime.now();
        try {
            JSONObject packet = new JSONObject(originText);
            if (packet.has("eSense")) {
                JSONObject eSense = packet.getJSONObject("eSense");
                this.attention = eSense.getDouble("attention");
                this.meditation = eSense.getDouble("meditation");
            } else {
                this.attention = -1;
                this.meditation = -1;
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
                this.delta = -1;
                this.theta = -1;
                this.lowAlpha = -1;
                this.highAlpha = -1;
                this.lowBeta = -1;
                this.highBeta = -1;
                this.lowGamma = -1;
                this.highGamma = -1;
            }
            if (packet.has("poorSignalLevel")) {
                this.poorSignalLevel = packet.getInt("poorSignalLevel");
            } else {
                this.poorSignalLevel = -1;
            }
            if (packet.has("blinkStrength")) {
                this.blinkStrength = packet.getInt("blinkStrength");
            } else {
                this.blinkStrength = -1;
            }
            if (packet.has("mentalEffort")) {
                this.mentalEffort = packet.getInt("mentalEffort");
            } else {
                this.mentalEffort = -1;
            }
            if (packet.has("familiarity")) {
                this.familiarity = packet.getInt("familiarity");
            } else {
                this.familiarity = -1;
            }
        } catch (JSONException js) {
            throw new IllegalStateException("Unanticipated exception from JSON reading", js);
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
        return "Mind Wave Mobile Packet: " + LocalTime.now() + "\n\neSense:\n\nAttention: " + attention + "\nMeditation: " + meditation
                + "\nMental Effort: " + this.mentalEffort + "\n Familiarity: " + this.familiarity
                + "\n\nEEG Power:\n\nDelta: " + delta + "\nTheta: " + theta + "\nLow Alpha: " + lowAlpha + "\nHigh Alpha: " + highAlpha
                + "\nLow Beta: " + lowBeta + "\nHigh Beta: " + highBeta + "\nLow Gamma: " + lowGamma + "\nHigh Gamma: " + highGamma
                + "\n\nPoor Signal Level: " + this.poorSignalLevel + "\n\nBlink Strength: " + this.blinkStrength;
    }

}
