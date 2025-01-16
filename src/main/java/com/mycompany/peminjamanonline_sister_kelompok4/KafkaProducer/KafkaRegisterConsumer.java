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

public class KafkaRegisterConsumer extends javax.swing.JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/loan_app";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private DefaultListModel<String> listModel;
    private JList<String> messageList;
    private JScrollPane scrollPane;
    private volatile boolean isRunning = true;

    private JTextField txtNama;
    private JTextField txtEmail;
    private JTextField txtPassword;
    private JTextField txtNIK;
    private JTextField txtKontak;
    private JTextField txtTanggalLahir;
    private JTextField txtAlamat;
    private JTextField txtJenisKelamin;
    private JTextField txtPekerjaan;
    private JTextField txtGajiPokok;
    private JTextField txtFotoKTP;
    private JTextField txtRole;

    public KafkaRegisterConsumer() {
        TanpilanGUI();
        startKafkaConsumer();
    }

    private void TanpilanGUI() {
        setTitle("Data Registrasi User");
        setIconImage(new ImageIcon("admin_icon.png").getImage()); // Tambahkan ikon pada window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(null); // Menempatkan window di tengah layar

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // List panel
        listModel = new DefaultListModel<>();
        messageList = new JList<>(listModel);
        messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messageList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageList.setBackground(new Color(240, 248, 255)); // Alice Blue
        messageList.setBorder(BorderFactory.createTitledBorder("Daftar User"));

        scrollPane = new JScrollPane(messageList);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Detail panel
        JPanel detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("Detail User"));
        detailPanel.setBackground(new Color(245, 245, 245)); // Light Gray

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addDetailField(detailPanel, gbc, "Nama:", txtNama = new JTextField(20), 0);
        addDetailField(detailPanel, gbc, "Email:", txtEmail = new JTextField(20), 1);
        addDetailField(detailPanel, gbc, "Password:", txtPassword = new JTextField(20), 2);
        addDetailField(detailPanel, gbc, "Role:", txtRole = new JTextField(20), 3);

        mainPanel.add(detailPanel, BorderLayout.SOUTH);

        // Tombol Tutup
        JButton closeButton = new JButton("Tutup");
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        closeButton.setBackground(new Color(220, 20, 60)); // Crimson
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
        props.put("bootstrap.servers", "192.168.18.200:9092, 192.168.18.134:9093");
        props.put("group.id", "admin_group_" + UUID.randomUUID().toString());
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        props.put("partition.assignment.strategy",
                "org.apache.kafka.clients.consumer.RoundRobinAssignor");

        Thread consumerThread = new Thread(() -> {
            try (org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer
                    = new org.apache.kafka.clients.consumer.KafkaConsumer<>(props)) {

                consumer.subscribe(Collections.singletonList("register"));

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
                        "[%tF %<tT] Nama: %s | Email: %s | Password: %s | NIK: %s | Kontak: %s | Tanggal Lahir: %s | Alamat: %s | Jenis Kelamin: %s | Pekerjaan: %s | Gaji Pokok: %s | Foto KTP: %s | Role: %s",
                        System.currentTimeMillis(),
                        data.get("nama"), // username
                        data.get("email"), // email
                        data.get("password"), // password
                        data.get("nik"), // nik
                        data.get("kontak"), // kontak
                        data.get("tanggal_lahir"), // tanggal_lahir
                        data.get("alamat"), // alamat
                        data.get("jenis_kelamin"), // jenis_kelamin
                        data.get("pekerjaan"), // pekerjaan
                        data.get("gaji_pokok"), // gaji_pokok
                        data.get("foto_ktp"), // foto_ktp
                        data.getOrDefault("role", "user") // role (default: user)
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
        String query = "INSERT INTO users (username, email, password, nik, kontak, tanggal_lahir, alamat, jenis_kelamin, pekerjaan, gaji_pokok, foto_ktp, role) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Validasi data sebelum menyimpan
            if (!validateData(data)) {
                System.out.println("Data tidak valid, gagal menyimpan.");
                return;
            }

            pstmt.setString(1, data.get("nama"));                 // username
            pstmt.setString(2, data.get("email"));                // email
            pstmt.setString(3, data.get("password"));             // password
            pstmt.setString(4, data.get("nik"));                  // nik
            pstmt.setString(5, data.get("kontak"));               // kontak
            pstmt.setString(6, data.get("tanggal_lahir"));        // tanggal_lahir
            pstmt.setString(7, data.get("alamat"));               // alamat
            pstmt.setString(8, data.get("jenis_kelamin"));        // jenis_kelamin
            pstmt.setString(9, data.get("pekerjaan"));            // pekerjaan

            // Validasi dan parsing gaji pokok
            double gajiPokok = 0;
            try {
                gajiPokok = Double.parseDouble(data.get("gaji_pokok"));
            } catch (NumberFormatException e) {
                System.out.println("Gagal parsing gaji pokok: " + data.get("gaji_pokok"));
            }
            pstmt.setDouble(10, gajiPokok);

            // Penanganan foto KTP
            String fotoKtpPath = data.get("foto_ktp");
            try {
                // Jika ingin menyimpan path file
                pstmt.setString(11, fotoKtpPath);
            } catch (SQLException e) {
                System.out.println("Gagal menyimpan path foto KTP: " + e.getMessage());
                pstmt.setString(11, ""); // Simpan string kosong jika gagal
            }

            // Role default
            pstmt.setString(12, data.getOrDefault("role", "user"));

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data berhasil disimpan ke database");
            } else {
                System.out.println("Gagal menyimpan data ke database");
            }

        } catch (SQLException e) {
            System.err.println("Error saat menyimpan data: " + e.getMessage());
            e.printStackTrace();
        }
    }

// Metode validasi data
    private boolean validateData(Map<String, String> data) {
        // Cek apakah data yang diperlukan ada
        String[] requiredFields = {
            "nama", "email", "password", "nik",
            "kontak", "tanggal_lahir", "alamat",
            "jenis_kelamin", "pekerjaan", "gaji_pokok"
        };

        for (String field : requiredFields) {
            if (!data.containsKey(field) || data.get(field) == null || data.get(field).trim().isEmpty()) {
                System.out.println("Field yang hilang atau kosong: " + field);
                return false;
            }
        }

        // Validasi email
        if (!data.get("email").matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            System.out.println("Format email tidak valid");
            return false;
        }

        


        return true;
    }

// Metode parsing pesan yang lebih robust
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

// Metode updateFields yang lebih aman
    private void updateFields(Map<String, String> data) {
        // Gunakan getOrDefault untuk menghindari NullPointerException
        txtNama.setText(data.getOrDefault("nama", ""));
        txtEmail.setText(data.getOrDefault("email", ""));
        txtPassword.setText(data.getOrDefault("password", ""));
        txtNIK.setText(data.getOrDefault("nik", ""));
        txtKontak.setText(data.getOrDefault("kontak", ""));
        txtTanggalLahir.setText(data.getOrDefault("tanggal_lahir", ""));
        txtAlamat.setText(data.getOrDefault("alamat", ""));
        txtJenisKelamin.setText(data.getOrDefault("jenis_kelamin", ""));
        txtPekerjaan.setText(data.getOrDefault("pekerjaan", ""));
        txtGajiPokok.setText(data.getOrDefault("gaji_pokok", ""));
        txtFotoKTP.setText(data.getOrDefault("foto_ktp", ""));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KafkaRegisterConsumer().setVisible(true));
    }
}
