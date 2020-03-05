/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protonmail.sarahszabo.mindwavemobiledataserver.core.ui.mindwaveviewer;

import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.MindWavePacket;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.MindWaveServer;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

/**
 * An application class that is a visualizer for {@link MindWavePacket}s.
 *
 * @author Sarah Szabo <SarahSzabo@Protonmail.com>
 */
public class MindwaveViewer extends Application {

    private static final ExecutorService MINDWAVE_VIEWER_EXECUTOR = Executors.newSingleThreadExecutor(new BasicThreadFactory.Builder()
            .daemon(true).namingPattern("Mindwave Viewer UI Controller Thread").build());

    @FXML
    private static BorderPane baseGridPane;

    @FXML
    private static AreaChart<Double, Double> eSenseChart;

    @FXML
    private static AreaChart<Double, Double> brainwaveChart;

    /**
     * Starts the UI in it's own controller thread.
     */
    public static void initializeUI() {
        MindwaveViewer.launch();
    }

    /**
     * Shuts down the UI thread executor. UI will no longer be responsive to
     * data.
     */
    public static void shutdownUI() {
        MINDWAVE_VIEWER_EXECUTOR.shutdown();
    }

    @FXML
    void initialize() {
        assert baseGridPane != null : "fx:id=\"baseGridPane\" was not injected: check your FXML file 'MindWaveViewerFXML.fxml'.";
        assert eSenseChart != null : "fx:id=\"eSenseChart\" was not injected: check your FXML file 'MindWaveViewerFXML.fxml'.";
        assert brainwaveChart != null : "fx:id=\"brainwaveChart\" was not injected: check your FXML file 'MindWaveViewerFXML.fxml'.";

        MINDWAVE_VIEWER_EXECUTOR.submit(() -> {
            while (true) {
                /*
                SERIES FORMAT:
                    ESENSE: ATTENTION, MINDFULNESS, MENTALEFFORT, FAMILIARITY

                    BRAINWAVE: DELTA, THETA, LOW_ALPHA, HIGH_ALPHA, LOW_BETA, HIGH_BETA, LOW_GAMMA, HIGH_GAMMA
                 */
                var packet = MindWaveServer.MINDWAVESERVER.OUTPUT_QUEUE.take();
                if (!packet.isBlinkPacketOnly()) {
                    //eSense Data Series
                    var attention = new XYChart.Series<Double, Double>();
                    var meditation = new XYChart.Series<Double, Double>();
                    var mentalEffort = new XYChart.Series<Double, Double>();
                    var familiarity = new XYChart.Series<Double, Double>();
                    //Brainwave Data Series
                    var delta = new XYChart.Series<Double, Double>();
                    var theta = new XYChart.Series<Double, Double>();
                    var lowAlpha = new XYChart.Series<Double, Double>();
                    var highAlpha = new XYChart.Series<Double, Double>();
                    var lowBeta = new XYChart.Series<Double, Double>();
                    var highBeta = new XYChart.Series<Double, Double>();
                    var lowGamma = new XYChart.Series<Double, Double>();
                    var highGamma = new XYChart.Series<Double, Double>();

                    var eSenseData = eSenseChart.getData();
                    var brainwaveData = brainwaveChart.getData();
                    //If there are no series, add empty ones
                    if (eSenseData.isEmpty()) {
                        eSenseData.add(attention);
                        eSenseData.add(meditation);
                        //data.add(mentalEffort);
                        //data.add(familiarity);
                        brainwaveData.add(delta);
                        brainwaveData.add(theta);
                        brainwaveData.add(lowAlpha);
                        brainwaveData.add(highAlpha);
                        brainwaveData.add(lowBeta);
                        brainwaveData.add(highBeta);
                        brainwaveData.add(lowGamma);
                        brainwaveData.add(highGamma);
                    }
                    //Get Old Data, take most recent 20 entries and graph them
                    var acceptableEntries = 20;
                    //If the number of entries is less than acceptableEntries,
                    //just add at end, else take top (entries - 1) and and new to end
                    //They're always the same size, so just grab eSense
                    if (eSenseData.get(0).getData().size() < acceptableEntries) {
                        eSenseData.get(0).getData().add(new Data<>((double) eSenseData.get(0).getData().size(), packet.getAttention()));
                        eSenseData.get(1).getData().add(new Data<>((double) eSenseData.get(1).getData().size(), packet.getMeditation()));
                        //eSenseData.get(2).getData().add(new Data<>((double) eSenseData.get(2).getData().size(), packet.getMentalEffort()));
                        //eSenseData.get(3).getData().add(new Data<>((double) eSenseData.get(3).getData().size(), packet.getFamiliarity()));

                        //Now Brainwaves
                        brainwaveData.get(0).getData().add(new Data<>((double) brainwaveData.get(0).getData().size(), packet.getDelta()));
                        brainwaveData.get(1).getData().add(new Data<>((double) brainwaveData.get(1).getData().size(), packet.getTheta()));
                        brainwaveData.get(2).getData().add(new Data<>((double) brainwaveData.get(2).getData().size(), packet.getLowAlpha()));
                        brainwaveData.get(3).getData().add(new Data<>((double) brainwaveData.get(3).getData().size(), packet.getHighAlpha()));
                        brainwaveData.get(4).getData().add(new Data<>((double) brainwaveData.get(4).getData().size(), packet.getLowBeta()));
                        brainwaveData.get(5).getData().add(new Data<>((double) brainwaveData.get(5).getData().size(), packet.getHighBeta()));
                        brainwaveData.get(6).getData().add(new Data<>((double) brainwaveData.get(6).getData().size(), packet.getLowGamma()));
                        brainwaveData.get(7).getData().add(new Data<>((double) brainwaveData.get(7).getData().size(), packet.getHighGamma()));
                    } else {
                        setNew(eSenseData.get(0), new Data<>((double) eSenseData.get(0).getData().size(), packet.getAttention()));
                        setNew(eSenseData.get(1), new Data<>((double) eSenseData.get(1).getData().size(), packet.getMeditation()));
                        //setNew(eSenseData.get(2), new Data<>((double) eSenseData.get(2).getData().size(), packet.getMentalEffort()));
                        //setNew(eSenseData.get(3), new Data<>((double) eSenseData.get(3).getData().size(), packet.getFamiliarity()));

                        //Brainwaves
                        setNew(brainwaveData.get(0), new Data<>((double) eSenseData.get(0).getData().size(), packet.getDelta()));
                        setNew(brainwaveData.get(1), new Data<>((double) eSenseData.get(1).getData().size(), packet.getTheta()));
                        setNew(brainwaveData.get(2), new Data<>((double) eSenseData.get(2).getData().size(), packet.getLowAlpha()));
                        setNew(brainwaveData.get(3), new Data<>((double) eSenseData.get(3).getData().size(), packet.getHighAlpha()));
                        setNew(brainwaveData.get(4), new Data<>((double) eSenseData.get(4).getData().size(), packet.getLowBeta()));
                        setNew(brainwaveData.get(5), new Data<>((double) eSenseData.get(5).getData().size(), packet.getHighBeta()));
                        setNew(brainwaveData.get(6), new Data<>((double) eSenseData.get(6).getData().size(), packet.getLowGamma()));
                        setNew(brainwaveData.get(7), new Data<>((double) eSenseData.get(7).getData().size(), packet.getHighGamma()));
                    }
                }
            }
        });
    }

    /**
     * Removes the first entry and adds the new one.
     *
     * @param series The series to use
     * @param value The value to add
     */
    private void setNew(XYChart.Series<Double, Double> series, Data<Double, Double> value) {
        series.getData().remove(0);
        series.getData().add(value);
    }

    @Override
    public void start(Stage stage) {
        try {
            stage.setAlwaysOnTop(true);
            stage.setResizable(true);
            stage.setTitle("Mindwave Mobile 2 Data Viewer");
            //Load FXML
            var fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/MindWaveViewer.fxml"));
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(MindwaveViewer.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("FXML Couldn't Load! " + ex);
        }
    }
}
