package com.mycompany.peminjamanonline_sister_kelompok4.KafkaProducer;

import com.mycompany.peminjamanonline_sister_kelompok4.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Duration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Properties;
import java.util.Collections;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import javax.swing.border.EmptyBorder;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public class KafkaKonfirmasiConsumer extends JFrame {
    private final String groupId = "group konfirmasi";
    private JTextArea logArea;
    private JButton startButton;
    private JLabel statusLabel;

    public KafkaKonfirmasiConsumer() {
        setTitle("Kafka Consumer GUI");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(30, 144, 255));
        headerPanel.setPreferredSize(new Dimension(700, 60));
        headerPanel.setLayout(new BorderLayout());  // Menggunakan BorderLayout untuk menempatkan label di tengah

        JLabel headerLabel = new JLabel("Kafka Consumer Konfirmasi", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.CENTER);  // Menambahkan label di tengah

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        logArea.setBackground(new Color(240, 248, 255));
        logArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        logArea.setCaretPosition(logArea.getDocument().getLength());  // Menempatkan posisi caret di akhir teks
        JScrollPane scrollPane = new JScrollPane(logArea);

        statusLabel = new JLabel("Status: Menunggu pesan...", SwingConstants.CENTER);  // Menambahkan centering di sini
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        statusLabel.setForeground(Color.GRAY);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 245, 245));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(statusLabel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);

    }

    private void ConsumerKonfirmasi() {
        
        new Thread(() -> {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singletonList("konfirmasi"));

            log("Menunggu Pesan Muncul");

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    String[] data = record.value().split(",");
                    if (data.length != 3) {
                        log("Pesan : " + record.value());
                        continue;
                    }
                    String nik = data[0];
                    String status = data[1];
                    
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String updateQuery = "UPDATE pengajuan_pinjaman pp "
                                + "JOIN users u ON pp.iduser = u.iduser "
                                + "SET pp.status = ? "
                                + "WHERE u.nik = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                            stmt.setString(1, nik);           
                            stmt.setString(2, status);             
                            stmt.executeUpdate();
                            log("Data saved to database: " + nik);
                        }
                    } catch (Exception e) {
                        log("Error saving data to database: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            KafkaKonfirmasiConsumer gui = new KafkaKonfirmasiConsumer();
            gui.setVisible(true);
        });
    }
}
