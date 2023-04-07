package com.mycompany.dataplotter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import com.panamahitek.ArduinoException;
import com.panamahitek.PanamaHitek_Arduino;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class JFrameSerialVisualizer extends javax.swing.JFrame {

    private long startTime;
    private long endTime;
    private ValueMarker startMarker;
    private ValueMarker endMarker;
    private String filename;

    private int windowSize = 160;
    private double overlap = 0.25;
    private boolean motionFlag = false;
    private int stepCount = 0;
    List<DataPoint> dataPoints;
    private static String result = "";
    private static boolean flag = false;
    private static int index = 0;

    PanamaHitek_Arduino ino = new PanamaHitek_Arduino();
    //Se crea un eventListener para el puerto serie
    SerialPortEventListener listener = new SerialPortEventListener() {
        @Override
        //Si se recibe algun dato en el puerto serie, se ejecuta el siguiente metodo
        public void serialEvent(SerialPortEvent serialPortEvent) {
            try {
                if (ino.isMessageAvailable()) {
                    String msg = ino.printMessage();
                    System.out.println(msg);

                    if (msg.contains("Prediction")) {
                        result = msg.replace("Prediction: ", "");
                        if (result.contains("0")) {
                            result = "iddle";
                        } else if (result.contains("1")) {
                            result = "motion";
                        } else if (result.contains("2")) {
                            result = "step";
                        }
                        if (!result.contains("step")) {
                            motionFlag = true;
                        } else {
                            if (motionFlag) {
                                stepCount++;
                                motionFlag = false;
                                jLabel1.setText("Step count: " + stepCount);
                            }
                        }
                        jTable1.setValueAt(result, index, 1);
                    }

                }
            } catch (SerialPortException ex) {
                Logger.getLogger(JFrameSerialVisualizer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ArduinoException ex) {
                Logger.getLogger(JFrameSerialVisualizer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    public JFrameSerialVisualizer() {
        initComponents();

        try {
            ino.arduinoRXTX("COM5", 115200, listener);
        } catch (ArduinoException ex) {
            Logger.getLogger(JFrameSerialVisualizer.class.getName()).log(Level.SEVERE, null, ex);
        }

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
            Logger.getLogger(JFrameSerialVisualizer.class.getName()).log(Level.SEVERE, null, ex);
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

    public void sendFeaturesArray(List<DataPoint> inputDataset) {

        try {

            for (int i = 0; i < inputDataset.size(); i++) {
                ino.sendData(inputDataset.get(i).getAcc_x() + ",");
                Thread.sleep(1);
            }
            for (int i = 0; i < inputDataset.size(); i++) {
                ino.sendData(inputDataset.get(i).getAcc_y() + ",");
                Thread.sleep(1);
            }
            for (int i = 0; i < inputDataset.size(); i++) {
                ino.sendData(inputDataset.get(i).getAcc_z() + ",");
                Thread.sleep(1);
            }
            for (int i = 0; i < inputDataset.size(); i++) {
                ino.sendData(inputDataset.get(i).getGy_x() + ",");
                Thread.sleep(1);
            }
            for (int i = 0; i < inputDataset.size(); i++) {
                ino.sendData(inputDataset.get(i).getGy_y() + ",");
                Thread.sleep(1);
            }
            for (int i = 0; i < inputDataset.size(); i++) {
                ino.sendData(inputDataset.get(i).getGy_z() + ",");
                Thread.sleep(1);
            }
            Thread.sleep(100);
        } catch (ArduinoException ex) {
            Logger.getLogger(JFrameSerialVisualizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SerialPortException ex) {
            Logger.getLogger(JFrameSerialVisualizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(JFrameSerialVisualizer.class.getName()).log(Level.SEVERE, null, ex);
        }

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
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

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
                .addComponent(jSlider1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
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

        jButton2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jButton2.setText("ML Classification");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Step count: 0");

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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            int numRows = jTable1.getRowCount();
            ListSelectionModel selectionModel = jTable1.getSelectionModel();
            for (int i = 0; i < numRows; i++) {
                index = i;
                try {
                    selectionModel.setSelectionInterval(i, i);

                    int start = (int) (i * (windowSize * overlap));
                    int end = start + windowSize;

                    List<DataPoint> selectedSample = dataPoints.subList(start, end);

                    sendFeaturesArray(selectedSample);

                    //double[] data = new Model().score(features);

                    /* String result;
                    if (data[0] > data[1] && data[0] > data[2]) {
                        result = "iddle";
                    } else if (data[1] > data[2]) {
                        result = "motion";
                    } else {
                        result = "step";
                    }

                    if (!result.contains("step")) {
                        motionFlag = true;
                    } else {
                        if (motionFlag) {
                            stepCount++;
                            motionFlag = false;
                            jLabel1.setText("Step count: " + stepCount);
                        }
                    }

                    jTable1.setValueAt(result, i, 1);
                     */
                } catch (Exception ex) {
                    Logger.getLogger(JFrameSerialVisualizer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });


    }//GEN-LAST:event_jButton2ActionPerformed

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
            java.util.logging.Logger.getLogger(JFrameSerialVisualizer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JFrameSerialVisualizer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JFrameSerialVisualizer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JFrameSerialVisualizer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
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
                new JFrameSerialVisualizer().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
