/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util;

import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.MindWavePacket;
import java.util.Optional;
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
        public MindWavePacket getDataAndDestroy() {
            return MindWavePacket.generateRandomPacket();
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public String toString() {
            return "EEG WAVE EMULATOR";
        }
    };

    /**
     * The temporary holder packet for data. Reset to null when we send data to
     * someone.
     */
    protected final AtomicReference<MindWavePacket> packet = new AtomicReference<>();

    /**
     * Represents whether or not this server mode is ready to provide data or
     * not. Returns true if not null and false if is a scanning packet.
     *
     * @return Only return true if not null AND not scanning
     */
    public boolean isReady() {
        //TODO Condense this into a single expression
        var packet = this.packet.get();
        ///Only return true if not null AND not scanning
        return packet != null && !packet.isScanningPacket();
    }

    /**
     * Gets the data if available; null otherwise. Destroys the current data
     * element.
     *
     * @return The data
     */
    public MindWavePacket getDataAndDestroy() {
        synchronized (this) {
            var packet = this.packet.get();
            this.packet.set(null);
            return packet;
        }
    }

    /**
     * Gets the data if available; null otherwise.
     *
     * @return The data
     */
    public Optional<MindWavePacket> getData() {
        synchronized (this) {
            if (this.packet.get() != null) {
                return Optional.of(this.packet.get());
            } else {
                return Optional.empty();
            }

        }
    }

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
