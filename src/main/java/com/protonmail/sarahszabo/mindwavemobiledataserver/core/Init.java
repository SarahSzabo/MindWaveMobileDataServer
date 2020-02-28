package com.protonmail.sarahszabo.mindwavemobiledataserver.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The initialization class for the Neurosky Mindwave Mobile Much of the source
 * code is from sarikayamehmet:
 * https://github.com/sarikayamehmet/MindwaveJsonCollector/blob/master/src/JsonReader.java
 *
 * @author Sarah Szabo <Sarah.Szabo@Protonmail.com>
 */
public class Init {

    private static final String THINKGEAR_HOST = "127.0.0.1";
    private static final int THINKGEAR_PORT = 13854;
    private static final String TO_JSON_COMMAND = "{\"enableRawOutput\": false, \"format\": \"Json\"}\n";
    private static InputStream input;
    private static OutputStream output;
    private static BufferedReader reader;
    private static String timeStamp;
    private static String poorSignalLevel;
    private static String attention;
    private static String meditation;
    private static String delta;
    private static String theta;
    private static String lowAlpha;
    private static String highAlpha;
    private static String lowBeta;
    private static String highBeta;
    private static String lowGamma;
    private static String highGamma;
    private static String blinkStrength;

    public static void main(String[] args) throws IOException, JSONException {
        System.out.println("Connecting to host = " + THINKGEAR_HOST + ", port = " + THINKGEAR_PORT);
        try (Socket clientSocket = new Socket(THINKGEAR_HOST, THINKGEAR_PORT)) {
            input = clientSocket.getInputStream();
            output = clientSocket.getOutputStream();
            System.out.println("Sending command: " + TO_JSON_COMMAND);
            write(TO_JSON_COMMAND);
            reader = new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8")));
            //DosyayaEkle("timeStamp;poorSignalLevel;attention;meditation;delta;theta;lowAlpha;highAlpha;
            //lowBeta;highBeta;lowGamma;highGamma;blinkStrength", "mindStream.csv");
            while (true) {
                clientEvent();
            }
        }
    }

    /**
     * Writes to the output stream.
     *
     * @param data The data to write
     */
    public static void write(String data) {
        try {
            output.write(data.getBytes());
            output.flush();
        } catch (Exception e) { // null pointer or serial port dead
            e.printStackTrace();
        }
    }

    public static void clientEvent() throws NumberFormatException, IOException {
        // Sample JSON data:
        /*
         {"eSense":{"attention":91,"meditation":41},
        "eegPower":{"delta":1105014,"theta":211310,"lowAlpha":7730,"highAlpha":68568,"lowBeta":12949,"highBeta":47455,
        "lowGamma":55770,"highGamma":28247},"poorSignalLevel":0}
         */
        if (reader.ready()) {
            String jsonText = reader.readLine();
            System.out.println("JSON TEXT RAW: " + jsonText);
            java.util.Date date = new java.util.Date();
            timeStamp = "" + new Timestamp(date.getTime());
            try {
                String uniText = "";
                JSONObject json = new JSONObject(jsonText);
                if (json.has("blinkStrength")) {
                    blinkStrength = json.getString("blinkStrength");
                    uniText = timeStamp + ";NA;NA;NA;NA;NA;NA;NA;NA;NA;NA;NA;" + blinkStrength;
                } else {
                    poorSignalLevel = json.getString("poorSignalLevel");

                    JSONObject eSense = json.getJSONObject("eSense");
                    if (eSense != null) {
                        attention = eSense.getString("attention");
                        meditation = eSense.getString("meditation");
                    }

                    JSONObject eegPower = json.getJSONObject("eegPower");
                    if (eegPower != null) {
                        delta = eegPower.getString("delta");
                        theta = eegPower.getString("theta");
                        lowAlpha = eegPower.getString("lowAlpha");
                        highAlpha = eegPower.getString("highAlpha");
                        lowBeta = eegPower.getString("lowBeta");
                        highBeta = eegPower.getString("highBeta");
                        lowGamma = eegPower.getString("lowGamma");
                        highGamma = eegPower.getString("highGamma");
                    }
                    uniText = timeStamp + ";" + poorSignalLevel + ";" + attention + ";" + meditation + ";" + delta + ";" + theta + ";" + lowAlpha + ";" + highAlpha + ";" + lowBeta + ";" + highBeta + ";" + lowGamma + ";" + highGamma + ";NA";
                }
                System.out.println(uniText + "\n");
                //DosyayaEkle(uniText, "rtMindOut.csv");
            } catch (JSONException e) {
                System.out.println("There was an error parsing the JSONObject." + e);
            };
        }
    }

    public static void DosyayaEkle(String metin, String filename) {
        try {
            File dosya = new File("C:\\Users\\XmasX\\Desktop\\Real-time\\" + filename);
            FileWriter yazici = new FileWriter(dosya, true);
            BufferedWriter yaz = new BufferedWriter(yazici);
            yaz.write(metin + "\n");
            yaz.close();
        } catch (Exception hata) {
            hata.printStackTrace();
        }
    }
}
