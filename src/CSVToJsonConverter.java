import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class CSVToJsonConverter extends JFrame {
    private JTextField csvFilePathField;
    private JTextArea csvPreviewArea;
    private JTextArea jsonTextArea;

    public CSVToJsonConverter() {
        super("CSV to JSON Converter");

        // Create components
        JLabel csvLabel = new JLabel("CSV File Path:");
        csvFilePathField = new JTextField(20);
        JButton browseButton = new JButton("Browse");
        JButton convertButton = new JButton("Convert to JSON");
        csvPreviewArea = new JTextArea(10, 40);
        jsonTextArea = new JTextArea(10, 40);
        JButton saveButton = new JButton("Save JSON");



        // Set layout
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(csvLabel);
        topPanel.add(csvFilePathField);
        topPanel.add(browseButton);
        topPanel.add(convertButton);
        add(topPanel, BorderLayout.NORTH);
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.add(new JScrollPane(csvPreviewArea));
        centerPanel.add(new JScrollPane(jsonTextArea));
        add(centerPanel, BorderLayout.CENTER);
        add(saveButton, BorderLayout.SOUTH);

        // Browse button action
        browseButton.addActionListener(e -> browseCSVFile());

        // Convert button action
        convertButton.addActionListener(e -> convertToJSON());

        // Save button action
        saveButton.addActionListener(e -> saveJSONToFile());

        // Set frame properties
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void browseCSVFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            csvFilePathField.setText(filePath);
            displayCSVPreview(filePath);
        }
    }

    private void displayCSVPreview(String filePath) {
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withIgnoreSurroundingSpaces().withIgnoreEmptyLines())) {
            StringBuilder preview = new StringBuilder();
            List<CSVRecord> records = csvParser.getRecords();
            for (CSVRecord record : records) {
                for (String field : record) {
                    preview.append(field).append(",");
                }
                preview.deleteCharAt(preview.length() - 1); // Remove last comma
                preview.append("\n");
            }
            csvPreviewArea.setText(preview.toString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading CSV file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void convertToJSON() {
        String csvFilePath = csvFilePathField.getText();
        JSONArray jsonArray = new JSONArray(); // Declare jsonArray here

        int headerRow = 1; // Assuming header row is the first row
        // You can implement the logic to allow users to choose or auto-detect the header row

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            StringBuilder csvContent = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                csvContent.append(line).append("\n");
            }

            StringReader stringReader = new StringReader(csvContent.toString());

            CSVFormat csvFormat = null;
            // Detecting the delimiter automatically
            if (csvContent.toString().contains(";")) {
                csvFormat = CSVFormat.DEFAULT.withDelimiter(';');
            } else if (csvContent.toString().contains(",")) {
                csvFormat = CSVFormat.DEFAULT.withDelimiter(',');
            }

            if (csvFormat == null) {
                JOptionPane.showMessageDialog(this, "Unable to detect CSV delimiter.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (CSVParser csvParser = new CSVParser(stringReader, csvFormat.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

                List<String> headerNames = csvParser.getHeaderNames();

                List<CSVRecord> records = csvParser.getRecords();

                // Adjust starting index based on header row selection
                int startIndex = headerRow == 1 ? 0 : 1;

                for (int i = startIndex; i < records.size(); i++) {
                    CSVRecord record = records.get(i);
                    JSONObject jsonObject = new JSONObject();
                    for (int j = 0; j < record.size(); j++) {
                        String headerName = headerNames.get(j);
                        jsonObject.put(headerName, record.get(j));
                    }
                    jsonArray.put(jsonObject);
                }

                jsonTextArea.setText(jsonArray.toString(4));

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading CSV file: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading CSV file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private char detectDelimiter(String line) {
        if (line.contains(",")) {
            return ',';
        } else if (line.contains(";")) {
            return ';';
        } else {
            // Default to comma if neither comma nor semicolon is found
            return ',';
        }
    }








    private void saveJSONToFile() {
        String json = jsonTextArea.getText();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save JSON File");
        fileChooser.setSelectedFile(new File("output.json")); // Default filename
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                writer.write(json);
                JOptionPane.showMessageDialog(this, "JSON file saved successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving JSON file: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CSVToJsonConverter::new);
    }
}
