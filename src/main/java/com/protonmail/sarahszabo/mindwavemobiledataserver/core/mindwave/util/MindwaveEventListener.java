/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util;

import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.MindWavePacket;

/**
 * Defines a class that can subscribe for Mindwave event updates.
 *
 * @author Sarah Szabo <SarahSzabo@Protonmail.com>
 */
public interface MindwaveEventListener {

    /**
     * Updates this class with the most recent mindwave event.
     *
     * @param packet The packet to send
     */
    void mindwaveUpdate(MindWavePacket packet);
}
