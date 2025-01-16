package com.mycompany.peminjamanonline_sister_kelompok4.KafkaProducer;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KafkaPengajuanConsumer extends javax.swing.JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/loan_app";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private DefaultListModel<String> listModel;
    private JList<String> messageList;
    private JScrollPane scrollPane;
    private volatile boolean isRunning = true;

    private JTextField txtJumlah;
    private JTextField txtTenor;
    private JTextField txtBunga;
    private JTextField txtCicilan;

    public KafkaPengajuanConsumer() {
        TanpilanGUI();
        startKafkaConsumer();
    }

    private void TanpilanGUI() {
        setTitle("Data Pengajuan Pinjaman");
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
        messageList.setBorder(BorderFactory.createTitledBorder("Daftar Pinjaman"));

        scrollPane = new JScrollPane(messageList);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("Detail Pinjaman"));
        detailPanel.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addDetailField(detailPanel, gbc, "Jumlah:", txtJumlah = new JTextField(20), 0);
        addDetailField(detailPanel, gbc, "Tenor:", txtTenor = new JTextField(20), 1);
        addDetailField(detailPanel, gbc, "Bunga:", txtBunga = new JTextField(20), 2);
        addDetailField(detailPanel, gbc, "Cicilan:", txtCicilan = new JTextField(20), 3);

        mainPanel.add(detailPanel, BorderLayout.SOUTH);

        // Tombol Tutup
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
        mainPanel .add(buttonPanel, BorderLayout.NORTH);

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
        props.put("group.id", "pengajuan_consumer_group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("max.poll.records", "100");
        props.put("max.poll.interval.ms", "300000");

        Thread consumerThread = new Thread(() -> {
            try (org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer
                    = new org.apache.kafka.clients.consumer.KafkaConsumer<>(props)) {

                consumer.subscribe(Collections.singletonList("pengajuan"));

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
                String formattedMessage = String.format(
                    "[%tF %<tT] Jumlah: %s | Tenor: %s | Suku Bunga: %s | Angsuran Bulanan: %s | Tanggal Cair: %s | Total Cair: %s | Sisa Tagihan: %s | Status: %s ",
                    System.currentTimeMillis(),
                    data.get("jumlah"),
                    data.get("tenor"),
                    data.get("suku_bunga"),
                    data.get("angsuran_bulanan"),
                    data.get("tanggal_cair"),
                    data.get("total_cair"),
                    data.get("sisa_tagihan"),
                    data.get("status"),
                    data.get("statusPengajuan")
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
    if (!validateData(data)) {
        System.err.println("Data tidak valid, tidak akan disimpan ke database.");
        return;
    }

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
        String queryPinjaman = "INSERT INTO pinjaman (iduser, jumlah, tenor, suku_bunga, angsuran_bulanan, tanggal_cair, total_cair, sisa_tagihan, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmtPinjaman = conn.prepareStatement(queryPinjaman, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmtPinjaman.setString(1, data.get("iduser"));
            stmtPinjaman.setDouble(2, Double.parseDouble(data.get("jumlah")));
            stmtPinjaman.setInt(3, Integer.parseInt(data.get("tenor")));
            stmtPinjaman.setDouble(4, Double.parseDouble(data.get("suku_bunga")));
            stmtPinjaman.setDouble(5, Double.parseDouble(data.get("angsuran_bulanan")));
            stmtPinjaman.setDate(6, java.sql.Date.valueOf(data.get("tanggal_cair")));
            stmtPinjaman.setDouble(7, Double.parseDouble(data.get("total_cair")));
            stmtPinjaman.setDouble(8, Double.parseDouble(data.get("sisa_tagihan")));
            stmtPinjaman.setString(9, data.get("status").trim()); // Pastikan status valid dan tidak ada spasi tambahan

            int rowsAffected = stmtPinjaman.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data pinjaman berhasil disimpan ke database.");
                // Ambil ID pinjaman yang baru saja disimpan
                try (ResultSet generatedKeys = stmtPinjaman.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long pinjamanId = generatedKeys.getLong(1);
                        // Simpan data ke tabel pengajuan_pinjaman
                        String queryPengajuan = "INSERT INTO pengajuan_pinjaman (iduser, pinjaman_id, tanggal_pengajuan, status_pengajuan) "
                                + "VALUES (?, ?, ?, ?)";
                        try (PreparedStatement stmtPengajuan = conn.prepareStatement(queryPengajuan)) {
                            stmtPengajuan.setString(1, data.get("iduser"));
                            stmtPengajuan.setLong(2, pinjamanId);
                            stmtPengajuan.setDate(3, java.sql.Date.valueOf(data.get("tanggal_pengajuan")));
                            stmtPengajuan.setString(4, data.get("status_pengajuan").trim()); // Pastikan status valid

                            stmtPengajuan.executeUpdate();
                            System.out.println("Data pengajuan berhasil disimpan ke database.");
                        }

                        // Simpan data ke tabel tagihan
                        String queryTagihan = "INSERT INTO tagihan (pinjaman_id, jumlah_bayar, jatuh_tempo) "
                                + "VALUES (?, ?, ?)";
                        try (PreparedStatement stmtTagihan = conn.prepareStatement(queryTagihan)) {
                            stmtTagihan.setLong(1, pinjamanId);
                            stmtTagihan.setDouble(2, Double.parseDouble(data.get("jumlah_bayar")));
                            stmtTagihan.setDate(3, java.sql.Date.valueOf(data.get("jatuh_tempo")));

                            stmtTagihan.executeUpdate();
                            System.out.println("Data tagihan berhasil disimpan ke database.");
                        }
                    }
                }
            } else {
                System.out.println("Gagal menyimpan data pinjaman ke database");
            }
        }
    } catch (SQLException e) {
        System.err.println("Error saat menyimpan data: " + e.getMessage());
        e.printStackTrace();
    } catch (NumberFormatException e) {
        System.err.println("Error parsing numeric values: " + e.getMessage());
        e.printStackTrace();
    } catch (IllegalArgumentException e) {
        System.err.println("Error parsing date: " + e.getMessage());
        e.printStackTrace();
    }
}
private boolean validateData(Map<String, String> data) {
    String[] requiredFields = {
        "iduser", "jumlah", "tenor", "suku_bunga",
        "angsuran_bulanan", "tanggal_cair", "total_cair",
        "sisa_tagihan", "status", "status_pengajuan"
    };

    for (String field : requiredFields) {
        if (!data.containsKey(field) || data.get(field) == null || data.get(field).trim().isEmpty()) {
            System.out.println("Field yang hilang atau kosong: " + field);
            return false;
        }
    }

    // Validasi status untuk tabel pinjaman
    String status = data.get("status").trim();
    if (!status.equals("lunas") && !status.equals("belum lunas")) {
        System.out.println("Status pinjaman tidak valid");
        return false;
    }

    // Validasi status untuk tabel pengajuan_pinjaman
    String pengajuanStatus = data.get("status_pengajuan").trim();
    if (!pengajuanStatus.equals("menunggu") && !pengajuanStatus.equals("disetujui") && !pengajuanStatus.equals("ditolak")) {
        System.out.println("Status pengajuan tidak valid");
        return false;
    }

    return true;
}

    private Map<String, String> parseMessage(String message) {
        Map<String, String> data = new HashMap<>();
        try {
            message = message.replace("{", "").replace("}", "").trim();
            String[] pairs = message.split(",");

            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    data.put(key, value);
                }
            }

            System.out.println("Parsed message data: " + data);
        } catch (Exception e) {
            System.err.println("Error parsing message: " + message);
            e.printStackTrace();
        }
        return data;
    }

    private void updateFields(Map<String, String> data) {
        txtJumlah.setText(data.getOrDefault("jumlah", ""));
        txtTenor.setText(data.getOrDefault("tenor", ""));
        txtBunga.setText(data.getOrDefault("suku_bunga", ""));
        txtCicilan.setText(data.getOrDefault("angsuran_bulanan", ""));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KafkaPengajuanConsumer().setVisible(true));
    }
}