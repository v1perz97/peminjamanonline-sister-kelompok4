package com.mycompany.peminjamanonline_sister_kelompok4.KafkaProducer;

import com.mycompany.peminjamanonline_sister_kelompok4.DatabaseConnection;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class KafkaEditConsumer extends javax.swing.JFrame {
    private final String groupId = "group_pengajuan";
    private JTextArea logArea;
    private JLabel statusLabel;
    private volatile boolean isRunning = true;

    public KafkaEditConsumer() {
        setTitle("Kafka Consumer GUI");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(30, 144, 255));
        headerPanel.setPreferredSize(new Dimension(700, 60));
        headerPanel.setLayout(new BorderLayout());

        JLabel headerLabel = new JLabel("Topik Edit", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        logArea.setBackground(new Color(240, 248, 255));
        logArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(logArea);

        statusLabel = new JLabel("Status: Menunggu pesan...", SwingConstants.CENTER);
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
        startConsumer();
    }

    private void startConsumer() {
        new Thread(() -> {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
                consumer.subscribe(Collections.singletonList("konfirmasi"));

                log("Menunggu pesan muncul...");
                statusLabel.setText("Status: Menunggu pesan...");

                while (isRunning) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                    for (ConsumerRecord<String, String> record : records) {
                        String[] data = record.value().split(",");
                        if (data.length != 2) {
                            log("Format pesan tidak valid: " + record.value());
                            continue;
                        }
                        String nik = data[0].trim();
                        String status = data[1].trim();

                        try (Connection conn = DatabaseConnection.getConnection()) {
                            String sql = "UPDATE users SET username = ?, email = ?, password = ?, nik = ?, kontak = ?, tanggal_lahir = ?, alamat = ?, jenis_kelamin = ? WHERE iduser = ?";
                            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                                stmt.setString(1, status);
                                stmt.setString(2, nik);
                                int rowsUpdated = stmt.executeUpdate();
                                if (rowsUpdated > 0) {
                                    log("Berhsil Edit");
                                } else {
                                    log("Tidak Berhasil Edit");
                                }
                            }
                        } catch (Exception e) {
                            log("Kesalahan saat memperbarui database: " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log("Kesalahan Kafka Consumer: " + e.getMessage());
            } finally {
                statusLabel.setText("Status: Consumer dihentikan.");
                log("Consumer dihentikan.");
            }
        }).start();
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            KafkaEditConsumer gui = new KafkaEditConsumer();
            gui.setVisible(true);
        });
    }
}
