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
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    /**
     * Removes the first entry and adds the new one.
     *
     * @param series The series to use
     * @param value The value to add
     */
    private static <X, Y> void setNew(Series<Integer, Y> series, Data<Integer, Y> value) {
        series.getData().remove(0);
        series.getData().stream().forEach(data -> {
            data.setXValue(data.getXValue() - 1);
        });
        series.getData().add(value);
    }

    @FXML
    private BorderPane baseGridPane;

    @FXML
    private StackedAreaChart<Integer, Double> eSenseChart;

    @FXML
    private StackedAreaChart<Integer, Double> brainwaveChart;

    @FXML
    private ImageView iconImageView;

    @FXML
    void initialize() {
        assert baseGridPane != null : "fx:id=\"baseGridPane\" was not injected: check your FXML file 'MindWaveViewerFXML.fxml'.";
        assert eSenseChart != null : "fx:id=\"eSenseChart\" was not injected: check your FXML file 'MindWaveViewerFXML.fxml'.";
        assert brainwaveChart != null : "fx:id=\"brainwaveChart\" was not injected: check your FXML file 'MindWaveViewerFXML.fxml'.";
    }

    @Override
    public void start(Stage stage) {
        try {
            stage.setAlwaysOnTop(true);
            stage.setResizable(true);
            stage.setTitle("Mindwave Mobile 2 Data Viewer");
            //Load FXML
            var fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/MindWaveViewer.fxml"));
            fxmlLoader.setController(this);
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("pictures/KaguyaHime.jpg")));

            //Setup Axes & Charts
            eSenseChart.getXAxis().setLabel("Time");
            eSenseChart.getYAxis().setLabel("Intensity");
            brainwaveChart.getXAxis().setLabel("Time");
            brainwaveChart.getYAxis().setLabel("Intensity");

            initDataReader();
            iconImageView.setImage(new Image(getClass().getClassLoader().getResourceAsStream("pictures/KaguyaHime.jpg")));

            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(MindwaveViewer.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("FXML Couldn't Load! " + ex);
        }
    }

    /**
     * Initializes the data reader executor.
     */
    private void initDataReader() {
        MINDWAVE_VIEWER_EXECUTOR.submit(() -> {
            //Initialize Server
            MindWaveServer.start();
            while (true) {
                /*
                SERIES FORMAT:
                    ESENSE: ATTENTION, MINDFULNESS, MENTALEFFORT, FAMILIARITY

                    BRAINWAVE: DELTA, THETA, LOW_ALPHA, HIGH_ALPHA, LOW_BETA, HIGH_BETA, LOW_GAMMA, HIGH_GAMMA
                 */
                var packet = MindWaveServer.MINDWAVESERVER.OUTPUT_QUEUE.take();
                if (!packet.isBlinkOnly()) {
                    Platform.runLater(() -> {//eSense Data Series
                        var attention = new XYChart.Series<Integer, Double>();
                        attention.setName("Attention");
                        var meditation = new XYChart.Series<Integer, Double>();
                        meditation.setName("Meditation");
                        var mentalEffort = new XYChart.Series<Integer, Double>();
                        mentalEffort.setName("Mental Effort");
                        var familiarity = new XYChart.Series<Integer, Double>();
                        familiarity.setName("Familiarity");
                        //Brainwave Data Series
                        var delta = new XYChart.Series<Integer, Double>();
                        delta.setName("Delta");
                        var theta = new XYChart.Series<Integer, Double>();
                        theta.setName("Theta");
                        var lowAlpha = new XYChart.Series<Integer, Double>();
                        lowAlpha.setName("Low Alpha");
                        var highAlpha = new XYChart.Series<Integer, Double>();
                        highAlpha.setName("High Alpha");
                        var lowBeta = new XYChart.Series<Integer, Double>();
                        lowBeta.setName("Low Beta");
                        var highBeta = new XYChart.Series<Integer, Double>();
                        highBeta.setName("High Beta");
                        var lowGamma = new XYChart.Series<Integer, Double>();
                        lowGamma.setName("Low Gamma");
                        var highGamma = new XYChart.Series<Integer, Double>();
                        highGamma.setName("High Gamma");

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
                        //Take most recent 20 entries and graph them
                        var acceptableEntries = 20;
                        //If the number of entries is less than acceptableEntries,
                        //just add at end, else take top (entries - 1) and and new to end
                        //They're all always the same size, so just grab eSense
                        if (eSenseData.get(0).getData().size() < acceptableEntries) {
                            eSenseData.get(0).getData().add(new Data<>(eSenseData.get(0).getData().size(), packet.getAttention()));
                            eSenseData.get(1).getData().add(new Data<>(eSenseData.get(1).getData().size(), packet.getMeditation()));
                            //eSenseData.get(2).getData().add(new Data<>((double) eSenseData.get(2).getData().size(), packet.getMentalEffort()));
                            //eSenseData.get(3).getData().add(new Data<>((double) eSenseData.get(3).getData().size(), packet.getFamiliarity()));

                            //Now Brainwaves
                            brainwaveData.get(0).getData().add(new Data<>(brainwaveData.get(0).getData().size(), packet.getDelta()));
                            brainwaveData.get(1).getData().add(new Data<>(brainwaveData.get(1).getData().size(), packet.getTheta()));
                            brainwaveData.get(2).getData().add(new Data<>(brainwaveData.get(2).getData().size(), packet.getLowAlpha()));
                            brainwaveData.get(3).getData().add(new Data<>(brainwaveData.get(3).getData().size(), packet.getHighAlpha()));
                            brainwaveData.get(4).getData().add(new Data<>(brainwaveData.get(4).getData().size(), packet.getLowBeta()));
                            brainwaveData.get(5).getData().add(new Data<>(brainwaveData.get(5).getData().size(), packet.getHighBeta()));
                            brainwaveData.get(6).getData().add(new Data<>(brainwaveData.get(6).getData().size(), packet.getLowGamma()));
                            brainwaveData.get(7).getData().add(new Data<>(brainwaveData.get(7).getData().size(), packet.getHighGamma()));
                        } else {
                            setNew(eSenseData.get(0), new Data<>(20, packet.getAttention()));
                            setNew(eSenseData.get(1), new Data<>(20, packet.getMeditation()));
                            //setNew(eSenseData.get(2), new Data<>((double) eSenseData.get(2).getData().size(), packet.getMentalEffort()));
                            //setNew(eSenseData.get(3), new Data<>((double) eSenseData.get(3).getData().size(), packet.getFamiliarity()));

                            //Brainwaves
                            setNew(brainwaveData.get(0), new Data<>(20, packet.getDelta()));
                            setNew(brainwaveData.get(1), new Data<>(20, packet.getTheta()));
                            setNew(brainwaveData.get(2), new Data<>(20, packet.getLowAlpha()));
                            setNew(brainwaveData.get(3), new Data<>(20, packet.getHighAlpha()));
                            setNew(brainwaveData.get(4), new Data<>(20, packet.getLowBeta()));
                            setNew(brainwaveData.get(5), new Data<>(20, packet.getHighBeta()));
                            setNew(brainwaveData.get(6), new Data<>(20, packet.getLowGamma()));
                            setNew(brainwaveData.get(7), new Data<>(20, packet.getHighGamma()));
                        }
                    });
                }
            }
        });
    }
}
