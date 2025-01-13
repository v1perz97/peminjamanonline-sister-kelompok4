package com.mycompany.peminjamanonline_sister_kelompok4.KafkaProducer;

import com.mycompany.peminjamanonline_sister_kelompok4.DatabaseConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class KafkaPembayaranConsumer extends javax.swing.JFrame {
    private JTextArea logArea;
    private JLabel statusLabel;

    public KafkaPembayaranConsumer() {
        setTitle("Kafka Consumer GUI");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(30, 144, 255));
        headerPanel.setPreferredSize(new Dimension(700, 60));
        headerPanel.setLayout(new BorderLayout());

        JLabel headerLabel = new JLabel("Topik Pembayaran", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        logArea.setBackground(new Color(240, 248, 255));
        logArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        logArea.setCaretPosition(logArea.getDocument().getLength()); 
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

        KirimDataPembayaran();
    }

    private void KirimDataPembayaran() {
        new Thread(() -> {
            Properties props = new Properties();
            props.put("bootstrap.servers", "localhost:9092");
            props.put("group.id", "nasabah");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singletonList("pembayaran"));

            log("Menunggu pesan dari Kafka...");

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    String[] data = record.value().split(",");
                    if (data.length != 4) {
                        log("Pesan Baru : " + record.value());
                        continue;
                    }

                    String idUserStr = data[0];
                    String amountToPayStr = data[1];
                    String paymentDateStr = data[2];
                    String jatuhTempo = data[3];

                    try (Connection conn = DatabaseConnection.getConnection()) {
                        int iduser = Integer.parseInt(idUserStr);
                        int amountToPay = Integer.parseInt(amountToPayStr);
                        java.sql.Date sqlPaymentDate = java.sql.Date.valueOf(paymentDateStr);

                        String insertPaymentQuery = "INSERT INTO tagihan (pinjaman_id, tanggal_pembayaran, jumlah_bayar, jatuh_tempo) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement insertPaymentStmt = conn.prepareStatement(insertPaymentQuery)) {
                            insertPaymentStmt.setInt(1, iduser);
                            insertPaymentStmt.setDate(2, sqlPaymentDate);
                            insertPaymentStmt.setInt(3, amountToPay);
                            insertPaymentStmt.setString(4, jatuhTempo);
                            insertPaymentStmt.executeUpdate();
                        }

                        String updatePinjamanQuery = "UPDATE pinjaman SET sisa_tagihan = sisa_tagihan - ?, status = CASE WHEN sisa_tagihan - ? <= 0 THEN 'lunas' ELSE status END WHERE iduser = ?";
                        try (PreparedStatement updatePinjamanStmt = conn.prepareStatement(updatePinjamanQuery)) {
                            updatePinjamanStmt.setInt(1, amountToPay);
                            updatePinjamanStmt.setInt(2, amountToPay);
                            updatePinjamanStmt.setInt(3, iduser);
                            int rowsUpdated = updatePinjamanStmt.executeUpdate();

                            if (rowsUpdated > 0) {
                                log("Pembayaran berhasil untuk ID User: " + iduser);
                            } else {
                                log("Tidak ada pinjaman yang diperbarui untuk ID User: " + iduser);
                            }
                        }

                    } catch (Exception e) {
                        log("Error processing payment: " + e.getMessage());
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
        new KafkaPembayaranConsumer();
    }
}
