package com.mycompany.peminjamanonline_sister_kelompok4.KafkaProducer;

import com.mycompany.peminjamanonline_sister_kelompok4.DatabaseConnection;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import static java.rmi.server.LogStream.log;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import javax.swing.border.EmptyBorder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class KafkaPengajuanConsumer extends JFrame {
    private final String groupId = "group pengajuan";
    private JTextArea logArea;
    private JButton startButton;
    private JLabel statusLabel;
    
    public KafkaPengajuanConsumer() {
        setTitle("Kafka Consumer GUI");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(30, 144, 255));
        headerPanel.setPreferredSize(new Dimension(700, 60));
        headerPanel.setLayout(new BorderLayout());

        JLabel headerLabel = new JLabel("Topik Pengajuan", SwingConstants.CENTER);
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

            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singletonList("pengajuan"));

            log("Menunggu Pesan Muncul");
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    String[] data = record.value().split(",");

                    if (data.length != 8) {
                        log("Pesan : " + record.value());
                        continue;
                    }

                    String iduser = data[0];
                    String jumlah = data[1];
                    String tenor = data[2];
                    String suku_bunga = data[3];
                    String angsuran_bulanan = data[4];
                    String tanggal_cair = data[5];
                    String total_cair = data[6];
                    String sisa_angsuran = data[7];

                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String queryPinjaman = "INSERT INTO pinjaman (iduser, jumlah, tenor, suku_bunga, angsuran_bulanan, tanggal_cair, total_cair, sisa_angsuran) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement stmt = conn.prepareStatement(queryPinjaman)) {
                            stmt.setString(1, iduser);
                            stmt.setString(2, jumlah);
                            stmt.setString(3, tenor);
                            stmt.setString(4, suku_bunga);
                            stmt.setString(5, angsuran_bulanan);
                            stmt.setString(6, tanggal_cair);
                            stmt.setString(7, total_cair);
                            stmt.setString(8, sisa_angsuran);
                            stmt.executeUpdate();
                            log("Data saved to 'pinjaman' table: " + iduser);
                        }

                        String queryPengajuan = "INSERT INTO pengajuan_pinjaman (iduser, pinjaman_id, tanggal_pengajuan, status) "
                                + "VALUES (?, LAST_INSERT_ID(), ?, ?)";
                        try (PreparedStatement stmt = conn.prepareStatement(queryPengajuan)) {
                            stmt.setString(1, iduser);
                            stmt.setString(2, tanggal_cair);
                            stmt.setString(3, "Pending");
                            stmt.executeUpdate();
                            log("Data saved to 'pengajuan_pinjaman' table: " + iduser);
                        }

                        String queryTagihan = "INSERT INTO tagihan (pinjaman_id, tanggal_pembayaran, jumlah_bayar, jatuh_tempo) "
                                + "VALUES (LAST_INSERT_ID(), ?, ?, ?)";
                        try (PreparedStatement stmt = conn.prepareStatement(queryTagihan)) {
                            stmt.setString(1, "2025-01-01");
                            stmt.setString(2, "0");
                            stmt.setString(3, "2025-02-01");
                            stmt.executeUpdate();
                            log("Data saved to 'tagihan' table: " + iduser);
                        }
                    } catch (Exception e) {
                        log("Error saving data to database: " + e.getMessage());
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
            new KafkaPengajuanConsumer().setVisible(true);
        });
    }
}
