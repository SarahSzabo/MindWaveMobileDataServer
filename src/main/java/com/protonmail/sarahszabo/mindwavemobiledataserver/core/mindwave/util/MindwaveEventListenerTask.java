/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util;

import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.MindWavePacket;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.MindWaveServer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that waits for a mindwave event or else waits 33 milliseconds.
 *
 * @author Sarah Szabo <SarahSzabo@Protonmail.com>
 */
public class MindwaveEventListenerTask implements MindwaveEventListener, Runnable {

    /**
     * Launches a new event handler daemon thread with the specified name and
     * function to implement.
     *
     * @param threadName The name of the thread
     * @param eventHandler What to do when the
     * {@link MindwaveEventListenerTask#mindwaveUpdate(com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.MindWavePacket)}
     * method is called.
     */
    public static void launchMindwaveListenerThread(String threadName, Consumer<MindWavePacket> eventHandler) {
        var thread = new Thread(new MindwaveEventListenerTask(eventHandler), threadName);
        thread.setDaemon(true);
        thread.start();
    }

    private final Consumer<MindWavePacket> eventHandler;
    private final BlockingQueue<MindWavePacket> queue = new ArrayBlockingQueue<>(20);

    /**
     * Sets up the event handler which will be run ever time there is a new
     * update. Auto-registers with the
     * {@link MindWaveServer#registerMindwaveEventListener(com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util.MindwaveEventListener)}
     * method.
     *
     * @param eventHandler What is run in the thread when we get the mindwave
     * event
     */
    public MindwaveEventListenerTask(Consumer<MindWavePacket> eventHandler) {
        this.eventHandler = eventHandler;
        MindWaveServer.registerMindwaveEventListener(this);
    }

    @Override
    public void mindwaveUpdate(MindWavePacket packet) {
        this.queue.offer(packet);
    }

    @Override
    public void run() {
        try {
            while (true) {
                this.eventHandler.accept(this.queue.take());
            }
        } catch (InterruptedException ex) {
            throw new IllegalStateException("Mindwave Event Listener Thread Interrupted while waiting for a new mindwave event", ex);
        }
    }
}
