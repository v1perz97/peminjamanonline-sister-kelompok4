package com.mycompany.peminjamanonline_sister_kelompok4.KafkaProducer;

import com.mycompany.peminjamanonline_sister_kelompok4.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import javax.swing.border.EmptyBorder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class KafkaRegisterConsumer extends JFrame {
    
    private JTextArea logArea;
    private JButton startButton;
    private JLabel statusLabel;

    public KafkaRegisterConsumer() {
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

    private void ConsumerRegister() {
        
        new Thread(() -> {
            Properties props = new Properties();
            props.put("bootstrap.servers", "localhost:9092");
            props.put("group.id", "nasabah");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singletonList("register"));

            log("Menunggu Pesan Muncul");

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    String[] data = record.value().split(",");
                    if (data.length != 3) {
                        log("Invalid message format: " + record.value());
                        continue;
                    }
                    String username = data[0];
                    String email = data[1];
                    String password = data[2];
                    String nik = data[3];
                    String kontak = data[4];
                    String tanggalLahirStr = data[5];
                    String alamat = data[6];
                    String jenisKelamin = data[7];
                    String pekerjaan = data[8];
                    String gaji_pokok = data[9];
                    String fotoKTP = data[10];
                    String role = data[11];

                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String query = "INSERT INTO users (username, email, password, nik, kontak, tanggal_lahir, alamat, jenis_kelamin, pekerjaan, gaji_pokok, foto_ktp, role) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement stmt = conn.prepareStatement(query)) {
                            stmt.setString(1, username);           // Username
                            stmt.setString(2, email);              // Email
                            stmt.setString(3, password);           // Password
                            stmt.setString(4, nik);                // NIK
                            stmt.setString(5, kontak);             // Kontak
                            stmt.setDate(6, java.sql.Date.valueOf(tanggalLahirStr));  // Tanggal Lahir
                            stmt.setString(7, alamat);             // Alamat
                            stmt.setString(8, jenisKelamin);       // Jenis Kelamin
                            stmt.setString(9, pekerjaan);          // Pekerjaan
                            stmt.setDouble(10, Double.parseDouble(gaji_pokok));  // Gaji Pokok
                            stmt.setString(11, fotoKTP);           // Foto KTP (nama file)
                            stmt.setString(12, role);              // Role (misalnya 'user')

                            stmt.executeUpdate();
                            log("Data saved to database: " + username);
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
            KafkaRegisterConsumer gui = new KafkaRegisterConsumer();
            gui.setVisible(true);
        });
    }
}
