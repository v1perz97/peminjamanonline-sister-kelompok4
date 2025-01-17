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
import java.util.UUID;

public class KafkaKonfirmasiConsumer extends javax.swing.JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/loan_app";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private DefaultListModel<String> listModel;
    private JList<String> messageList;
    private JScrollPane scrollPane;
    private volatile boolean isRunning = true;

    private JTextField txtId;
    private JTextField txtNik;
    private JTextField txtStatus;

    public KafkaKonfirmasiConsumer() {
        TanpilanGUI();
        startKafkaConsumer();
    }

    private void TanpilanGUI() {
        setTitle("Data Konfirmasi User");
        setIconImage(new ImageIcon("admin_icon.png").getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // List panel
        listModel = new DefaultListModel<>();
        messageList = new JList<>(listModel);
        messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messageList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageList.setBackground(new Color(240, 248, 255));
        messageList.setBorder(BorderFactory.createTitledBorder("Daftar Konfirmasi User"));

        scrollPane = new JScrollPane(messageList);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Detail panel
        JPanel detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("Detail Konfirmasi"));
        detailPanel.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addDetailField(detailPanel, gbc, "Nama:", txtId = new JTextField(20), 0);
        addDetailField(detailPanel, gbc, "NIK:", txtNik = new JTextField(20), 1);
        addDetailField(detailPanel, gbc, "Status:", txtStatus = new JTextField(20), 2);

        mainPanel.add(detailPanel, BorderLayout.SOUTH);

        JButton closeButton = new JButton("Tutup");
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        closeButton.setBackground(new Color(220, 20, 60));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        closeButton.setToolTipText("Klik untuk menutup aplikasi");
        closeButton.addActionListener(e -> {
            isRunning = false;
            dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        add(mainPanel);

        messageList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = messageList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String selectedMessage = messageList.getSelectedValue();
                    updateDetailFields(selectedMessage);
                }
            }
        });
    }

    private void addDetailField(JPanel panel, GridBagConstraints gbc, String labelText, JTextField textField, int yPosition) {
        gbc.gridx = 0;
        gbc.gridy = yPosition;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        textField.setEditable(false);
        textField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(textField, gbc);
    }

    private void updateDetailFields(String message) {
        Map<String, String> data = parseMessage(message);
        updateFields(data);
    }

    private void startKafkaConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "admin_group_" + UUID.randomUUID().toString());
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        props.put("partition.assignment.strategy", "org.apache.kafka.clients.consumer.RoundRobinAssignor");

        Thread consumerThread = new Thread(() -> {
            try (org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(props)) {
                consumer.subscribe(Collections.singletonList("konfirmasi"));

                while (isRunning) {
                    var records = consumer.poll(Duration.ofMillis(100));
                    for (var record : records) {
                        String message = record.value();
                        processMessage(message);
                    }
                }
            }
        });

        consumerThread.start();
    }

    private void processMessage(String message) {
        try {
            Map<String, String> data = parseMessage(message);
            if (data != null) {
                // Hanya menampilkan NIK dan status_pengajuan
                String formattedMessage = String.format(
                        "[%tF %<tT] NIK: %s | Status: %s",
                        System.currentTimeMillis(),
                        data.get("nik"),
                        data.get("status_pengajuan")
                );

                SwingUtilities.invokeLater(() -> {
                    listModel.addElement(formattedMessage);
                    messageList.ensureIndexIsVisible(listModel.getSize() - 1);
                    updateFields(data);
                });

                saveToDatabase(data);
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> listModel.addElement("Error memproses pesan: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private void saveToDatabase(Map<String, String> data) {
        String updateQuery = "UPDATE pengajuan_pinjaman pp "
                + "JOIN users u ON pp.iduser = u.iduser "
                + "SET pp.status_pengajuan = ? "
                + "WHERE u.nik = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            if (!validateData(data)) {
                System.out.println("Data tidak valid, gagal menyimpan.");
                return;
            }

            pstmt.setString(1, data.get("status_pengajuan"));
            pstmt.setString(2, data.get("nik"));

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data berhasil disimpan.");
            } else {
                System.out.println("Tidak ada data yang diupdate.");
            }

        } catch (SQLException e) {
            System.err.println("Error saat menyimpan data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateData(Map<String, String> data) {
        String[] requiredFields = {
            "status_pengajuan",
            "nik"
        };

        for (String field : requiredFields) {
            if (!data.containsKey(field) || data.get(field) == null || data.get(field).trim().isEmpty()) {
                System.out.println("Field yang hilang atau kosong: " + field);
                return false;
            }
        }
        return true;
    }

    private Map<String, String> parseMessage(String message) {
        Map<String, String> data = new HashMap<>();
        try {
            // Hapus kurung kurawal jika ada
            message = message.replace("{", "").replace("}", "");

            // Split berdasarkan koma
            String[] pairs = message.split(",");

            for (String pair : pairs) {
                // Split berdasarkan tanda sama dengan
                String[] keyValue = pair.split("=");

                if (keyValue.length == 2) {
                    // Trim key dan value untuk menghapus spasi tambahan
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();

                    // Tambahkan ke map
                    data.put(key, value);
                }
            }

            System.out.println("Parsed data: " + data);
        } catch (Exception e) {
            System.err.println("Error parsing message: " + message);
            e.printStackTrace();
        }

        return data;
    }

    private void updateFields(Map<String, String> data) {
        txtId.setText(data.getOrDefault("Id User", ""));
        txtNik.setText(data.getOrDefault("Nik", ""));
        txtStatus.setText(data.getOrDefault("Status", ""));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KafkaKonfirmasiConsumer().setVisible(true));
    }
}
