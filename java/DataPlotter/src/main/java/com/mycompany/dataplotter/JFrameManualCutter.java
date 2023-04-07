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
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.io.FileWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileFilter;

public class JFrameManualCutter extends javax.swing.JFrame {

    private long startTime;
    private long endTime;
    private ValueMarker startMarker;
    private ValueMarker endMarker;
    private String filename;

    private int windowSize = 160;
    private double overlap = 0.5;
    private boolean motionFlag = false;
    private int stepCount = 0;
    List<DataPoint> dataPoints;
    private static String result = "";
    private static boolean flag = false;
    private static int index = 0;

    public JFrameManualCutter() {
        initComponents();

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if (keyCode == KeyEvent.VK_I) {
                        int selectedRow = jTable1.getSelectedRow();
                        jTable1.setValueAt("iddle", selectedRow, 1);
                        export("iddle");
                        return true; // Consume the event to prevent other components from processing it
                    } else if (keyCode == KeyEvent.VK_M) {
                        int selectedRow = jTable1.getSelectedRow();
                        jTable1.setValueAt("motion", selectedRow, 1);
                        export("motion");
                        return true; // Consume the event to prevent other components from processing it
                    } else if (keyCode == KeyEvent.VK_DOWN) {
                        // Do something when "m" is pressed globally
                        int selectedRow = jTable1.getSelectedRow();
                        selectedRow++;
                        jTable1.setRowSelectionInterval(selectedRow, selectedRow);
                        return true; // Consume the event to prevent other components from processing it
                    }
                }
                return false;
            }
        });

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose a JSON file");
            fileChooser.setCurrentDirectory(new File("C:\\Users\\Antony Garcia\\Desktop\\wpi\\sensors\\python\\final_scripts\\manually_cut_data\\testing_samples"));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files", "json");
            fileChooser.setFileFilter(filter);

            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                filename = selectedFile.getAbsolutePath();

                dataPoints = readJsonFile(filename);
                startTime = dataPoints.get(0).getTimestampDate().getTime();
                endTime = dataPoints.get(dataPoints.size() - 1).getTimestampDate().getTime();

                int intervals = (int) (dataPoints.size() * Math.pow(windowSize, -1));
                DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                int i = 0;
                int windowJump = (int) (windowSize * overlap);
                int end = 0;
                while (true) {
                    int start = i * windowJump;
                    end = start + windowSize;
                    if (end >= dataPoints.size()) {
                        break;
                    } else {
                        model.addRow(new Object[]{"sample_" + (i + 1)});
                        i++;
                    }
                }

                createChart(dataPoints, startTime, endTime);
            } else {
                System.exit(0);
            }
        } catch (Exception ex) {
            Logger.getLogger(JFrameManualCutter.class.getName()).log(Level.SEVERE, null, ex);
        }

        ListSelectionModel selectionModel = jTable1.getSelectionModel();
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                // Get the selected rows
                int[] selectedRows = jTable1.getSelectedRows();

                // Do something with the selected rows
                // For example, print the selected row indices to the console
                for (int i = 0; i < selectedRows.length; i++) {
                    jSlider1.setValue((int) (jTable1.getSelectedRow() * windowSize * overlap));
                }
            }
        });

    }

    public void export(String label) {
        try {
            endTime = (long) endMarker.getValue();
            startTime = (long) startMarker.getValue();
            String prefix = "";
            if (label.equals("motion")) {
                prefix = "motion";
            } else if (label.equals("iddle")) {
                prefix = "iddle";
            }
            String outputDir = "C:\\Users\\Antony Garcia\\Desktop\\wpi\\sensors\\python\\final_scripts\\manually_cut_data\\added_steps";
            File dir = new File(outputDir);
            String str = prefix;
            File[] files = dir.listFiles((dir1, name) -> name.startsWith(str));
            int fileCount = files == null ? 0 : files.length;
            String outputFileName = prefix + "_" + (fileCount + 1) + ".json";
            String outputFile = outputDir + "\\" + outputFileName;
            createJsonSubset(startTime, endTime, filename, outputFile);
        } catch (Exception ex) {
            Logger.getLogger(JFrameManualCutter.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                "Accelerometer and Gyroscope Readings",
                "Time",
                "Acc & Gyro",
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
        startMarker.setPaint(Color.RED);
        startMarker.setStroke(new BasicStroke(2.0f));
        plot.addDomainMarker(startMarker);

        endMarker = new ValueMarker(endTime);
        endMarker.setPaint(Color.RED);
        endMarker.setStroke(new BasicStroke(2.0f));
        plot.addDomainMarker(endMarker);

        // Create JSliders for the start and end markers
        JSlider startSlider = new JSlider(JSlider.HORIZONTAL, 0, dataPoints.size() - 1, 0);
        JSlider endSlider = new JSlider(JSlider.HORIZONTAL, 0, dataPoints.size() - 1, dataPoints.size() - 1);

        jSlider1.setMinimum(0);
        jSlider1.setMaximum(dataPoints.size() - 1 - windowSize);

        // Add listeners to the JSliders to update the position of the markers
        jSlider1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int sliderValue = ((JSlider) e.getSource()).getValue();
                long newStartTime = dataPoints.get(sliderValue).getTimestampDate().getTime();
                startMarker.setValue(newStartTime);
                endMarker.setValue(newStartTime + (windowSize * 12.5));
            }
        });
        startMarker.setValue(startTime);
        endMarker.setValue(startTime + (windowSize * 12.5));

        ChartPanel frame = new ChartPanel(chart);
        frame.setVisible(true);
        frame.setBounds(0, 0, jPanel1.getWidth(), jPanel1.getHeight());
        jPanel1.add(frame);

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
        jSlider1 = new javax.swing.JSlider();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        jRadioButtonMenuItem1.setSelected(true);
        jRadioButtonMenuItem1.setText("jRadioButtonMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 796, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jSlider1.setValue(0);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSlider1, javax.swing.GroupLayout.DEFAULT_SIZE, 682, Short.MAX_VALUE)
                .addGap(108, 108, 108))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Interval", "Label"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
            jTable1.getColumnModel().getColumn(1).setResizable(false);
        }

        jButton3.setText("Motion");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton1.setText("Iddle");
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 504, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        int selectedRow = jTable1.getSelectedRow();
        jTable1.setValueAt("iddle", selectedRow, 1);
        export("iddle");

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        int selectedRow = jTable1.getSelectedRow();
        jTable1.setValueAt("motion", selectedRow, 1);
        export("motion");
    }//GEN-LAST:event_jButton3ActionPerformed

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
            java.util.logging.Logger.getLogger(JFrameManualCutter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JFrameManualCutter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JFrameManualCutter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JFrameManualCutter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrameManualCutter().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
