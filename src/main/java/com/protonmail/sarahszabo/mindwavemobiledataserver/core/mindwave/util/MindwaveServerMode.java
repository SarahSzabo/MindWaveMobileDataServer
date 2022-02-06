/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util;

import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.MindWavePacket;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A mode enum which determines what kind of data gets outputted and from what
 * source.
 *
 * @author Sarah Szabo <SarahSzabo@Protonmail.com>
 */
public enum MindwaveServerMode {
    TGC {
        @Override
        public String toString() {
            return "LIVE: Thinkgear Connector (TGC)";
        }
    }, SQUAREWAVE_EMULATED {
        @Override
        public String toString() {
            return "EEG SQUAREWAVE EMULATOR";
        }
    };

    /**
     * The temporary holder packet for data. Reset to null when we send data to
     * someone.
     */
    protected final AtomicReference<MindWavePacket> packet = new AtomicReference<>();

    /**
     * Represents whether or not this server mode is ready to provide data or
     * not.
     *
     * @return
     */
    public boolean isReady() {
        return this.packet.get() == null;
    }

    /**
     * Gets the data if available; null otherwise.
     *
     * @return The data
     */
    public MindWavePacket getData() {
        synchronized (this) {
            var packet = this.packet.get();
            this.packet.set(null);
            return packet;
        }
    }

    ;

    /**
     * Sets the data for this server mode.
     *
     * @param packet The packet to set to
     */
    public void setData(MindWavePacket packet) {
        this.packet.set(packet);
    }

    @Override
    public abstract String toString();
;

}
