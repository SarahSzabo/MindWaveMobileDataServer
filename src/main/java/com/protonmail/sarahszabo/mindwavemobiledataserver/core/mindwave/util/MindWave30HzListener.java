/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util;

import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.MindWavePacket;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.MindWaveServer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that waits for a mindwave event or else waits 33 milliseconds.
 *
 * @author Sarah Szabo <SarahSzabo@Protonmail.com>
 */
public class MindWave30HzListener implements MindwaveEventListener, Runnable {

    protected final Consumer<MindWavePacket> eventHandler;
    protected MindWavePacket packet;

    /**
     * Sets up the event handler which will be run ever time there is a new
     * update. Auto-registers with the
     * {@link MindWaveServer#registerMindwaveEventListener(com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util.MindwaveEventListener)}
     * method.
     *
     * @param eventHandler What is run in the thread when we get the mindwave
     * event
     */
    public MindWave30HzListener(Consumer<MindWavePacket> eventHandler) {
        this.eventHandler = eventHandler;
        MindWaveServer.registerMindwaveEventListener(this);
    }

    @Override
    public void mindwaveUpdate(MindWavePacket packet) {
        this.packet = packet;
    }

    @Override
    public void run() {
        try {
            if (this.packet != null) {
                this.eventHandler.accept(packet);
                this.packet = null;
            }
            Thread.sleep(33);
        } catch (InterruptedException ex) {
            throw new IllegalStateException("30Hz listener Interrupted while sleeping", ex);
        }
    }
}
