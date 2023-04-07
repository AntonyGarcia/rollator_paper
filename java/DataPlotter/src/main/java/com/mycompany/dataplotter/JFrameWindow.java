package com.mycompany.dataplotter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class JFrameWindow extends javax.swing.JFrame {

    private long startTime;
    private long endTime;
    private ValueMarker startMarker;
    private ValueMarker endMarker;
    private String filename;

    public JFrameWindow() {
        initComponents();
        List<DataPoint> dataPoints;
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose a JSON file");
            fileChooser.setCurrentDirectory(new File("C:\\Users\\Antony Garcia\\Desktop\\wpi\\sensors\\python\\final_scripts\\resampled_recorded_data"));

            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                filename = selectedFile.getAbsolutePath();

                dataPoints = readJsonFile(filename);
                startTime = dataPoints.get(0).getTimestampDate().getTime();
                endTime = dataPoints.get(dataPoints.size() - 1).getTimestampDate().getTime();
                createChart(dataPoints, startTime, endTime);
            } else {
                System.exit(0);
            }
        } catch (Exception ex) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void createChart(List<DataPoint> dataPoints, long startTime, long endTime) {
        // Create a dataset of XYSeries objects for the accelerometer readings
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries seriesX = new XYSeries("acc_x");
        XYSeries seriesY = new XYSeries("acc_y");
        XYSeries seriesZ = new XYSeries("acc_z");
        XYSeries seriesGX = new XYSeries("gy_x");
        XYSeries seriesGY = new XYSeries("gy_y");
        XYSeries seriesGZ = new XYSeries("gy_z");
        for (DataPoint dataPoint : dataPoints) {
            seriesX.add(dataPoint.getTimestampDate().getTime(), dataPoint.getAcc_x());
            seriesY.add(dataPoint.getTimestampDate().getTime(), dataPoint.getAcc_y());
            seriesZ.add(dataPoint.getTimestampDate().getTime(), dataPoint.getAcc_z());
            seriesGX.add(dataPoint.getTimestampDate().getTime(), dataPoint.getGy_x());
            seriesGY.add(dataPoint.getTimestampDate().getTime(), dataPoint.getGy_y());
            seriesGZ.add(dataPoint.getTimestampDate().getTime(), dataPoint.getGy_z());
        }
        dataset.addSeries(seriesX);
        dataset.addSeries(seriesY);
        dataset.addSeries(seriesZ);
        dataset.addSeries(seriesGX);
        dataset.addSeries(seriesGY);
        dataset.addSeries(seriesGZ);

        // Create a JFreeChart object for the accelerometer readings
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Accelerometer Readings",
                "Time",
                "Acceleration",
                dataset
        );

        // Set the plot color for each XYSeries
        XYPlot plot = chart.getXYPlot();
        plot.getRenderer().setSeriesPaint(0, Color.RED);
        plot.getRenderer().setSeriesPaint(1, Color.GREEN);
        plot.getRenderer().setSeriesPaint(2, Color.BLUE);
        plot.getRenderer().setSeriesPaint(3, Color.black);
        plot.getRenderer().setSeriesPaint(4, Color.gray);
        plot.getRenderer().setSeriesPaint(5, Color.orange);

        // Add vertical lines to the chart to indicate the range of interest
        startMarker = new ValueMarker(startTime);
        startMarker.setPaint(Color.BLACK);
        startMarker.setStroke(new BasicStroke(2.0f));
        plot.addDomainMarker(startMarker);

        endMarker = new ValueMarker(endTime);
        endMarker.setPaint(Color.BLACK);
        endMarker.setStroke(new BasicStroke(2.0f));
        plot.addDomainMarker(endMarker);

        // Create JSliders for the start and end markers
        JSlider startSlider = new JSlider(JSlider.HORIZONTAL, 0, dataPoints.size() - 1, 0);
        JSlider endSlider = new JSlider(JSlider.HORIZONTAL, 0, dataPoints.size() - 1, dataPoints.size() - 1);

        jSlider1.setMinimum(0);
        jSlider2.setMinimum(0);
        jSlider1.setMaximum(dataPoints.size() - 1);
        jSlider2.setMaximum(dataPoints.size() - 1);
        jSlider2.setValue(dataPoints.size() - 1);

        // Add listeners to the JSliders to update the position of the markers
        jSlider1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int sliderValue = ((JSlider) e.getSource()).getValue();
                long newStartTime = dataPoints.get(sliderValue).getTimestampDate().getTime();
                startMarker.setValue(newStartTime);
            }
        });

        jSlider2.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int sliderValue = ((JSlider) e.getSource()).getValue();
                long newEndTime = dataPoints.get(sliderValue).getTimestampDate().getTime();
                endMarker.setValue(newEndTime);
            }
        });

        ChartPanel frame = new ChartPanel(chart);
        frame.setVisible(true);
        frame.setBounds(0, 0, jPanel1.getWidth(), jPanel1.getHeight());
        jPanel1.add(frame);

    }

    public void createJsonSubset(long startTimeMillis, long endTimeMillis, String inputFilename, String outputFilename) throws Exception {
        // Convert the start and end time arguments to Date objects
        Date startTime = new Date(startTimeMillis);
        Date endTime = new Date(endTimeMillis);

        // Read the JSON data from the input file
        List<DataPoint> dataPoints = readJsonFile(inputFilename);

        // Create a new list to hold the data points within the specified time range
        List<DataPoint> subset = new ArrayList<>();

        // Iterate through the data points and add those within the specified time range to the subset
        for (DataPoint dataPoint : dataPoints) {
            Date timestamp = dataPoint.getTimestampDate();
            if (timestamp.compareTo(startTime) >= 0 && timestamp.compareTo(endTime) <= 0) {
                subset.add(dataPoint);
            }
        }

        // Create a Gson object with pretty printing enabled
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        // Serialize the subset of data points to JSON objects, excluding the timestampDate field
        List<JsonObject> jsonObjects = new ArrayList<>();
        for (DataPoint dataPoint : subset) {
            JsonObject jsonObject = (JsonObject) gson.toJsonTree(dataPoint);
            jsonObject.remove("timestampDate");
            JsonElement obj = jsonObject.get("timestamp");
            String str = obj.toString().replace("\"", "");
            obj = new JsonPrimitive(str);
            jsonObject.remove("timestamp");
            jsonObject.add("timestamp", obj);
            jsonObjects.add(jsonObject);
        }

        // Write the subset of data points to a new JSON file as an array of JSON objects
        JsonArray jsonArray = new JsonArray();
        for (JsonObject jsonObject : jsonObjects) {
            jsonArray.add(jsonObject);
        }

        FileWriter writer = new FileWriter(outputFilename);

        gson.toJson(jsonArray, writer);
        writer.close();
    }

    public static List<DataPoint> readJsonFile(String filename) throws Exception {
        // Read the JSON data from the file
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String json = "";
        String line = reader.readLine();
        while (line != null) {
            json += line;
            line = reader.readLine();
        }
        json = json.replace("Timestamp", "timestamp");
        reader.close();

        // Parse the JSON data into an array of JSON objects
        JsonParser parser = new JsonParser();

        JsonArray jsonArray = parser.parse(json).getAsJsonArray();

        // Create a list of DataPoint objects
        List<DataPoint> dataPoints = new ArrayList<>();

        // Iterate over the JSON objects and create a DataPoint object for each one
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            double acc_x = jsonObject.get("acc_x").getAsDouble();
            double acc_y = jsonObject.get("acc_y").getAsDouble();
            double acc_z = jsonObject.get("acc_z").getAsDouble();
            double gy_x = jsonObject.get("gy_x").getAsDouble();
            double gy_y = jsonObject.get("gy_y").getAsDouble();
            double gy_z = jsonObject.get("gy_z").getAsDouble();
            String timestampString = jsonObject.get("timestamp").getAsString();
            DataPoint dataPoint = new DataPoint(acc_x, acc_y, acc_z, gy_x, gy_y, gy_z, timestampString);
            dataPoints.add(dataPoint);

        }

        return dataPoints;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jSlider2 = new javax.swing.JSlider();
        jSlider1 = new javax.swing.JSlider();
        jButton1 = new javax.swing.JButton();

        jRadioButtonMenuItem1.setSelected(true);
        jRadioButtonMenuItem1.setText("jRadioButtonMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(800, 700));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jSlider2.setValue(100);

        jSlider1.setValue(0);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSlider1, javax.swing.GroupLayout.DEFAULT_SIZE, 773, Short.MAX_VALUE)
                    .addComponent(jSlider2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
        );

        jButton1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jButton1.setText("Export JSON");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        endTime = (long) endMarker.getValue();
        startTime = (long) startMarker.getValue();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save As");
            fileChooser.setCurrentDirectory(new File("C:\\Users\\Antony Garcia\\Desktop\\wpi\\sensors\\python\\final_scripts\\manually_cut_data"));
            // Add a file filter to only show files with a .json extension
            FileFilter filter = new FileNameExtensionFilter("JSON files", "json");
            fileChooser.setFileFilter(filter);
            int result = fileChooser.showSaveDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String outputFile = selectedFile.getAbsolutePath();
                createJsonSubset(startTime, endTime, filename, outputFile);
            }
        } catch (Exception ex) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_jButton1ActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(JFrameWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JFrameWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JFrameWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JFrameWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrameWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JSlider jSlider2;
    // End of variables declaration//GEN-END:variables
}
