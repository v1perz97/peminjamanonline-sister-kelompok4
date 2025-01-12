package com.mycompany.peminjamanonline_sister_kelompok4.KafkaProducer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

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

        startConsumer();
    }

    private void startConsumer() {
        logArea.append("Consumer dimulai...\n");
        statusLabel.setText("Status: Consumer berjalan...");

        // Simulasi menerima pesan Kafka secara otomatis
        new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logArea.append("Pesan diterima: Pembayaran berhasil!\n");
                statusLabel.setText("Status: Pesan diterima...");
            }
        }).start();
    }

    public static void main(String[] args) {
        new KafkaPembayaranConsumer();
    }
}
