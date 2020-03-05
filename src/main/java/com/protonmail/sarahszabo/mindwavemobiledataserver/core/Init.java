package com.protonmail.sarahszabo.mindwavemobiledataserver.core;

import com.protonmail.sarahszabo.mindwavemobiledataserver.core.ui.mindwaveviewer.MindwaveViewer;

/**
 * The initialization class for the Neurosky Mindwave Mobile.
 *
 * @author Sarah Szabo <SarahSzabo@Protonmail.com>
 */
public class Init {

    /**
     * The main method where the server starts.
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        MindwaveViewer.initializeUI();
    }
}
