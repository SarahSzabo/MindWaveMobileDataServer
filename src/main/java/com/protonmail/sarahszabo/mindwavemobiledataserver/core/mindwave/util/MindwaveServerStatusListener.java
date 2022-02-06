/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util;

/**
 * A class representing a listener to mindwave mode change events.
 *
 * @author Sarah Szabo <SarahSzabo@Protonmail.com>
 */
public interface MindwaveServerStatusListener {

    /**
     * Updates this class with the most recent mindwave mode change.
     *
     * @param mode The current mode
     */
    void serverModeUpdate(MindwaveServerMode mode);
}
