/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An enum for mindwave states
 *
 * @author Sarah Szabo <SarahSzabo@Protonmail.com>
 */
public enum MindwaveStatus {
    /**
     * The flag for when attention is the dominant waveform.
     */
    ATTENTION_DOMINANT {
        @Override
        public List<String> getPicturesFilenames() {
            var list = new ArrayList<String>(30);
            for (int i = 0; i < 11; i++) {
                list.add("pictures/mindwave/active/SS" + i + ".gif");
            }
            return list;
        }
    },
    /**
     * The flag for when meditation is the dominant waveform.
     */
    MEDITATION_DOMINANT {
        @Override
        public List<String> getPicturesFilenames() {
            var list = new ArrayList<String>(30);
            for (int i = 0; i < 20; i++) {
                list.add("pictures/mindwave/passive/MM" + i + ".gif");
            }
            return list;
        }
    },/**
     * The flag for when meditation is the dominant waveform.
     */
    WINK {
        @Override
        public List<String> getPicturesFilenames() {
            var list = new ArrayList<String>(20);
            for (int i = 0; i < 5; i++) {
                list.add("pictures/mindwave/wink/WW" + i + ".gif");
            }
            return list;
        }
    },
    /**
     * The flag for when to reset the program to default status.
     */
    DEFAULT {
        @Override
        public List<String> getPicturesFilenames() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };

    /**
     * Gets the picture resource folder filenames.
     *
     * @return The filenames list
     */
    public abstract List<String> getPicturesFilenames();
}
