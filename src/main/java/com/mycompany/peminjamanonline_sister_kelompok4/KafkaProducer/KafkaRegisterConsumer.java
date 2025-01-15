package com.mycompany.peminjamanonline_sister_kelompok4.KafkaProducer;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class KafkaRegisterConsumer extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/loan_app";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private volatile boolean isRunning = true;

    private JTextArea logArea;
    private JLabel statusLabel;

    public KafkaRegisterConsumer() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Kafka Consumer GUI");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(30, 144, 255));
        headerPanel.setPreferredSize(new Dimension(700, 60));
        headerPanel.setLayout(new BorderLayout());

        JLabel headerLabel = new JLabel("Topik Register", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        logArea.setBackground(new Color(240, 248, 255));
        JScrollPane scrollPane = new JScrollPane(logArea);

        statusLabel = new JLabel("Status: Menunggu pesan...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        statusLabel.setForeground(Color.GRAY);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
        startKafkaConsumer();
    }

    private void startKafkaConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.39.111:9092,192.168.34.79:9093");
        props.put("group.id", "Admin2");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");

        Thread consumerThread = new Thread(() -> {
            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
                consumer.subscribe(Collections.singletonList("register"));

                while (isRunning) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                    for (ConsumerRecord<String, String> record : records) {
                        String message = record.value();
                        System.out.println("Received message: " + message);
                        processMessage(message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    logArea.append("Error in consumer: " + e.getMessage() + "\n");
                    statusLabel.setText("Status: Error dalam konsumen");
                });
            }
        });

        consumerThread.start();
    }

    private void processMessage(String message) {
        try {
            System.out.println("Processing message: " + message);

            Map<String, String> data = parseMessage(message);
            if (data != null && !data.isEmpty()) {
                String formattedMessage = String.format(
                    "[%tF %<tT] Nama: %s | Email: %s",
                    System.currentTimeMillis(),
                    data.get("nama"),
                    data.get("email")
                );

                SwingUtilities.invokeLater(() -> {
                    logArea.append(formattedMessage + "\n");
                    logArea.append("Full Data: " + data + "\n");
                    statusLabel.setText("Status: Pesan diterima");
                });

                saveToDatabase(data);
            } else {
                SwingUtilities.invokeLater(() -> {
                    logArea.append("Pesan tidak valid: " + message + "\n");
                    statusLabel.setText("Status: Pesan tidak valid");
                });
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                logArea.append("Error memproses pesan: " + e.getMessage() + "\n");
                statusLabel.setText("Status: Error memproses pesan");
            });
            e.printStackTrace();
        }
    }

    private Map<String, String> parseMessage(String message) {
        Map<String, String> data = new HashMap<>();
        try {
            String[] pairs = message.replace("{", "").replace("}", "").split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    data.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
            System.out.println("Parsed data: " + data);
        } catch (Exception e) {
            System.err.println("Error parsing message: " + message);
            e.printStackTrace();
        }
        return data;
    }

    private void saveToDatabase(Map<String, String> data) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO users (username, email, password, nik, kontak, tanggal_lahir, alamat, jenis_kelamin, pekerjaan, gaji_pokok, foto_ktp, role) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, data.get("nama"));
                stmt.setString(2, data.get("email"));
                stmt.setString(3, data.get("password"));
                stmt.setString(4, data.get("nik"));
                stmt.setString(5, data.get("kontak"));
                stmt.setDate(6, java.sql.Date.valueOf(data.get("tanggal_lahir")));
                stmt.setString(7, data.get("alamat"));
                stmt.setString(8, data.get("jenis_kelamin"));
                stmt.setString(9, data.get("pekerjaan"));
                stmt.setDouble(10, Double.parseDouble(data.get("gaji_pokok")));
                stmt.setString(11, data.get("foto_ktp"));
                stmt.setString(12, "user"); // Assuming role is always "user"

                stmt.executeUpdate();
                System.out.println("Data saved to database successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                logArea.append("Error saving to database: " + e.getMessage() + "\n");
                statusLabel.setText("Status: Error menyimpan ke database");
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KafkaRegisterConsumer());
    }
}