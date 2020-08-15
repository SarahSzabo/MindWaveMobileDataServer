/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave;

import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.MindWavePacket;

/**
 * A {@link MindWavePacket} that has raw brainwave values that are easy to view.
 *
 * @author Sarah Szabo <SarahSzabo@Protonmail.com>
 */
public class MindWaveEasyGraphPacket extends MindWavePacket {

    public MindWaveEasyGraphPacket(int attention, int meditation, int mentalEffort, int familiarity, int delta, int theta, int lowAlpha, int highAlpha, int lowBeta, int highBeta, int lowGamma, int highGamma, int blinkStrength, int poorSignalLevel) {
        super(attention, meditation, mentalEffort, familiarity, (int) Math.log(delta), (int) Math.log(theta),
                (int) Math.log(lowAlpha), (int) Math.log(highAlpha), (int) Math.log(lowBeta), (int) Math.log(highBeta),
                (int) Math.log(lowGamma), (int) Math.log(highGamma), blinkStrength, poorSignalLevel);
    }

}
