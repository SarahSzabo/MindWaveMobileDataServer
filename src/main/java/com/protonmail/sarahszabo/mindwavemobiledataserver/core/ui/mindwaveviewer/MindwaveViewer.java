/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protonmail.sarahszabo.mindwavemobiledataserver.core.ui.mindwaveviewer;

import com.protonmail.sarahszabo.mindwavemobiledataserver.core.Init;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.MindWavePacket;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.MindWaveServer;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util.MindwaveEventListenerTask;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util.MindwaveServerMode;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util.MindwaveServerStatusListener;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util.MindwaveStatus;
import com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave.util.ThinkGearServerConnectionQuality;
import java.io.IOException;
import java.util.Random;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * An application class that is a visualizer for {@link MindWavePacket}s.
 *
 * @author Sarah Szabo <SarahSzabo@Protonmail.com>
 */
public class MindwaveViewer extends Application implements MindwaveServerStatusListener {

    /**
     * Starts the UI in it's own controller thread.
     */
    public static void initializeUI() {
        if (!MindWaveServer.isInitialized()) {
            throw new IllegalStateException("The Mindwave Server is not running. It must be run first before calling the UI");
        } else {
            MindwaveViewer.launch();
        }
    }

    /**
     * Removes the first entry and adds the new one.
     *
     * @param series The series to use
     * @param value The value to add
     */
    private static <X, Y> void setNew(Series<Number, Y> series, Data<Number, Y> value) {
        series.getData().remove(0);
        series.getData().stream().forEach(data -> {
            data.setXValue(((int) data.getXValue() - 1));
        });
        series.getData().add(value);
    }

    @FXML
    private BorderPane baseBorderPane;

    @FXML
    private ImageView iconImageView;

    @FXML
    private GridPane rightGridPane;

    @FXML
    private Label labelDataSource;

    @FXML
    private Label labelEEGConnectionStatus;

    @FXML
    private AreaChart<Number, Number> eSenseChart;

    @FXML
    private AreaChart<Number, Number> brainwaveChart;

    @FXML
    void initialize() {
        assert baseBorderPane != null : "fx:id=\"baseGridPane\" was not injected: check your FXML file 'MindWaveViewerFXML.fxml'.";
        assert eSenseChart != null : "fx:id=\"eSenseChart\" was not injected: check your FXML file 'MindWaveViewerFXML.fxml'.";
        assert brainwaveChart != null : "fx:id=\"brainwaveChart\" was not injected: check your FXML file 'MindWaveViewerFXML.fxml'.";
    }

    /**
     * Changes the stage's icon view to an appropriate response to the current
     * {@link MindwaveStatus}. If default is used, this resets to the normal
     * picture. After changing the image to the desired one and waiting the wait
     * period, changes the picture back to default.
     *
     * @param status The status the mindwaves are currently in
     * @param wait The number of milliseconds to wait in between the changed
     * image and the default image change. Need not be called from the Platform
     * thread.
     */
    private void imageToAndWaitToDefault(MindwaveStatus status, int wait) {
        try {
            imageTo(status);
            Thread.sleep(wait);
            imageTo(MindwaveStatus.DEFAULT);
        } catch (InterruptedException ex) {
            throw new IllegalStateException("Image changer thread interrupted while sleeping");
        }
    }

    /**
     * Changes the stage's icon view to an appropriate response to the current
     * {@link MindwaveStatus}. If default is used, this resets to the normal
     * picture. Need not be called from the Platform thread.
     *
     * @param status The status the mindwaves are currently in
     */
    private void imageTo(MindwaveStatus status) {
        if (status == MindwaveStatus.ATTENTION_DOMINANT) {
            Platform.runLater(() -> this.iconImageView.setImage(new Image(getClass().getClassLoader()
                    .getResource(getRandomImageFromPackage(MindwaveStatus.ATTENTION_DOMINANT)).toString())));
        } else if (status == MindwaveStatus.MEDITATION_DOMINANT) {
            Platform.runLater(() -> this.iconImageView.setImage(new Image(getClass().getClassLoader()
                    .getResource(getRandomImageFromPackage(MindwaveStatus.MEDITATION_DOMINANT)).toString())));
        } else if (status == MindwaveStatus.WINK) {
            Platform.runLater(() -> this.iconImageView.setImage(new Image(getClass().getClassLoader()
                    .getResource(getRandomImageFromPackage(MindwaveStatus.WINK)).toString())));
        } else {
            Platform.runLater(() -> this.iconImageView.setImage(new Image(getClass().getClassLoader()
                    .getResourceAsStream("pictures/KHFinal.png"))));
        }
    }

    /**
     * Gets a picture from the classpath pictures folder.
     *
     * @param packageName The string path (in package format) to the classpath
     * folder
     * @return The random picture
     */
    private String getRandomImageFromPackage(MindwaveStatus status) {
        var list = status.getPicturesFilenames();
        return list.get(new Random().nextInt(list.size()));
    }

    @Override
    public void start(Stage stage) {
        try {
            stage.setAlwaysOnTop(true);
            stage.setResizable(true);
            stage.setTitle(Init.FULL_PROGRAM_NAME);
            stage.setMaximized(true);
            //Load FXML
            var fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/MindWaveViewer.fxml"));
            fxmlLoader.setController(this);
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("pictures/KaguyaHime.png")));

            //Launch Helper Threads
            startImageChangerThread();
            //eSenseChart.setAnimated(false);
            //brainwaveChart.setAnimated(false);
            startChartDataListenerThread();
            iconImageView.setImage(new Image(getClass().getClassLoader().getResourceAsStream("pictures/KHFinal.png")));

            stage.show();

            //Set lables
            this.labelDataSource.setText(MindWaveServer.currentMode().toString());
            //Register with mindwave server for changes
            MindWaveServer.registerServerStatusListener(this);
        } catch (IOException ex) {
            Logger.getLogger(MindwaveViewer.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("FXML Couldn't Load! " + ex);
        }

        eSenseChart = new AreaChart<>(new NumberAxis(1, 20, 1), new NumberAxis(0, 20, 1));
        brainwaveChart = new AreaChart<>(new NumberAxis(1, 20, 1), new NumberAxis(0, 20, 1));

        //Set Y-Axes to auto-ranging & Disable Animation
        eSenseChart.getYAxis().setAutoRanging(true);
        eSenseChart.setAnimated(false);
        brainwaveChart.getYAxis().setAutoRanging(true);
        brainwaveChart.setAnimated(false);

        baseBorderPane.setCenter(eSenseChart);
        baseBorderPane.setBottom(brainwaveChart);
    }

    /**
     * Handles the blink behaviour of the image changer thread. Call this
     * whenever a probabilistic blink needs to occur; will handle the
     * probability and make the determination and execute the blink.
     *
     * @param random The source of randomness
     * @param packet The packet to use
     */
    private void handleBlinkImageBehavious(Random random, MindWavePacket packet) {
        //Do a random blink probabilistically
        int number = random.nextInt(1000), gate = 70;
        //Do a different probability if blinking continuously
        if (packet.getConnectionQuality() == ThinkGearServerConnectionQuality.DISCONNECTED) {
            gate = 30;
        }
        if (number <= gate) {
            imageToAndWaitToDefault(MindwaveStatus.WINK, 1500);
        }
    }

    /**
     * Launches the thread which looks for and changes the image based on the
     * status of the mindwaves.
     */
    private void startImageChangerThread() {
        //Has to be annonymous inner class to keep track of state
        MindwaveEventListenerTask.launchMindwaveListenerThread("Mindwave Viewer Image Changer Thread", new Consumer<MindWavePacket>() {
            private int numberOfHighAttentionsInRow = 0, numberOfHighMeditationsInRow = 0, ignoreQueue = 0;
            private boolean displayedImage = false;
            private Random random = new Random();

            @Override
            public void accept(MindWavePacket packet) {
                //Ignore every 5 images if we just displayed one, else reset queue and fall through
                if (this.displayedImage) {
                    if (this.ignoreQueue < 5) {
                        this.ignoreQueue++;
                        this.numberOfHighAttentionsInRow = 0;
                        this.numberOfHighMeditationsInRow = 0;
                        return;
                    } else {
                        this.ignoreQueue = 0;
                        this.displayedImage = false;
                    }
                }
                if (!packet.isBlinkOnly()) {
                    //For both, either increment or set to 0 every time
                    int gateLevel = 80, timesInRowForTrigger = 2;
                    if (packet.getAttention() >= gateLevel) {
                        this.numberOfHighAttentionsInRow++;
                    } else {
                        this.numberOfHighAttentionsInRow = 0;
                    }
                    if (packet.getMeditation() >= gateLevel) {
                        this.numberOfHighMeditationsInRow++;
                    } else {
                        this.numberOfHighMeditationsInRow = 0;
                    }
                    //Checks for changing image conditions
                    if (this.numberOfHighAttentionsInRow >= timesInRowForTrigger) {
                        this.displayedImage = true;
                        imageToAndWaitToDefault(MindwaveStatus.ATTENTION_DOMINANT, 5000);
                    } else if (this.numberOfHighMeditationsInRow >= timesInRowForTrigger) {
                        this.displayedImage = true;
                        imageToAndWaitToDefault(MindwaveStatus.MEDITATION_DOMINANT, 5000);
                    } else if (packet.getBlinkStrength() > 0) {
                        handleBlinkImageBehavious(this.random, packet);
                    }
                } else {
                    handleBlinkImageBehavious(this.random, packet);
                }
            }
        });
    }

    /**
     * Initializes the data reader thread.
     */
    private void startChartDataListenerThread() {
        MindwaveEventListenerTask.launchMindwaveListenerThread("Mindwave Viewer Chart Data Listener Thread", (MindWavePacket packet) -> {
            //Assume Server Initialized Already
            /*
                SERIES FORMAT:
                    ESENSE: ATTENTION, MINDFULNESS, MENTALEFFORT, FAMILIARITY

                    BRAINWAVE: DELTA, THETA, LOW_ALPHA, HIGH_ALPHA, LOW_BETA, HIGH_BETA, LOW_GAMMA, HIGH_GAMMA
             */
            if (!packet.isBlinkOnly()) {
                Platform.runLater(() -> {//eSense Data Series
                    var attention = new XYChart.Series<Number, Number>();
                    attention.setName("Attention");
                    var meditation = new XYChart.Series<Number, Number>();
                    meditation.setName("Meditation");
                    var mentalEffort = new XYChart.Series<Number, Number>();
                    mentalEffort.setName("Mental Effort");
                    var familiarity = new XYChart.Series<Number, Number>();
                    familiarity.setName("Familiarity");
                    //Brainwave Data Series
                    var delta = new XYChart.Series<Number, Number>();
                    delta.setName("Delta");
                    var theta = new XYChart.Series<Number, Number>();
                    theta.setName("Theta");
                    var lowAlpha = new XYChart.Series<Number, Number>();
                    lowAlpha.setName("Low Alpha");
                    var highAlpha = new XYChart.Series<Number, Number>();
                    highAlpha.setName("High Alpha");
                    var lowBeta = new XYChart.Series<Number, Number>();
                    lowBeta.setName("Low Beta");
                    var highBeta = new XYChart.Series<Number, Number>();
                    highBeta.setName("High Beta");
                    var lowGamma = new XYChart.Series<Number, Number>();
                    lowGamma.setName("Low Gamma");
                    var highGamma = new XYChart.Series<Number, Number>();
                    highGamma.setName("High Gamma");

                    var eSenseData = eSenseChart.getData();
                    var brainwaveData = brainwaveChart.getData();
                    //If there are no series, add empty ones
                    if (eSenseData.isEmpty()) {
                        eSenseData.add(attention);
                        eSenseData.add(meditation);
                        //eSenseData.add(mentalEffort);
                        //eSenseData.add(familiarity);

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
                        var x = packet.getAttention();
                        eSenseData.get(0).getData().add(new Data<>(eSenseData.get(0).getData().size(), packet.getAttention()));
                        eSenseData.get(1).getData().add(new Data<>(eSenseData.get(1).getData().size(), packet.getMeditation()));
                        //eSenseData.get(2).getData().add(new Data<>(eSenseData.get(2).getData().size(), packet.getMentalEffort()));
                        //eSenseData.get(3).getData().add(new Data<>(eSenseData.get(3).getData().size(), packet.getFamiliarity()));

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
                        //setNew(eSenseData.get(2), new Data<>(eSenseData.get(2).getData().size(), packet.getMentalEffort()));
                        //setNew(eSenseData.get(3), new Data<>(eSenseData.get(3).getData().size(), packet.getFamiliarity()));

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

                    //Do other listener stuff
                    this.labelEEGConnectionStatus.setText(packet.getConnectionQuality().toString());
                });
            }
        });
    }

    @Override
    public void serverModeUpdate(MindwaveServerMode mode) {
        Platform.runLater(() -> this.labelDataSource.setText(mode.toString()));
    }
}
