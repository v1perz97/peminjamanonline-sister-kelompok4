package com.mycompany.peminjamanonline_sister_kelompok4.KafkaProducer;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class KafkaEditConsumer extends javax.swing.JFrame {

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
    private JTextField txtNik;
    private JTextField txtKontak;
    private JTextField txtAlamat;
    private JTextField txtPekerjaan;
    private JDateChooser txtTanggalLahir;
    private JRadioButton rbLakiLaki;
    private JRadioButton rbPerempuan;
    private JComboBox<String> jComboBox1;

    public KafkaEditConsumer() {
        initializeGUI();
        startKafkaConsumer();
    }

    private void initializeGUI() {
        setTitle("Edit Profil");
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
        messageList.setBorder(BorderFactory.createTitledBorder("User  Edit"));

        scrollPane = new JScrollPane(messageList);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Detail panel
        JPanel detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("Detail Edit"));
        detailPanel.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addDetailField(detailPanel, gbc, "Nama:", txtNama = new JTextField(20), 0);
        addDetailField(detailPanel, gbc, "Email:", txtEmail = new JTextField(20), 1);
        addDetailField(detailPanel, gbc, "Password:", txtPassword = new JTextField(20), 2);
        addDetailField(detailPanel, gbc, "NIK:", txtNik = new JTextField(20), 3);
        addDetailField(detailPanel, gbc, "Kontak:", txtKontak = new JTextField(20), 4);
        addDetailField(detailPanel, gbc, "Alamat:", txtAlamat = new JTextField(20), 5);
        addDetailField(detailPanel, gbc, "Pekerjaan:", txtPekerjaan = new JTextField(20), 6);
        txtTanggalLahir = new JDateChooser();
        gbc.gridx = 0;
        gbc.gridy = 7;
        detailPanel .add(new JLabel("Tanggal Lahir:"), gbc);
        gbc.gridx = 1;
        detailPanel.add(txtTanggalLahir, gbc);

        rbLakiLaki = new JRadioButton("Laki-Laki");
        rbPerempuan = new JRadioButton("Perempuan");
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(rbLakiLaki);
        genderGroup.add(rbPerempuan);
        gbc.gridx = 0;
        gbc.gridy = 8;
        detailPanel.add(rbLakiLaki, gbc);
        gbc.gridx = 1;
        detailPanel.add(rbPerempuan, gbc);

        jComboBox1 = new JComboBox<>(new String[]{"< 1 Juta", "2 Juta - 5 Juta", "5 Juta - 10 Juta", "10 Juta - 15 Juta", "> 15 Juta"});
        gbc.gridx = 0;
        gbc.gridy = 9;
        detailPanel.add(new JLabel("Gaji Pokok:"), gbc);
        gbc.gridx = 1;
        detailPanel.add(jComboBox1, gbc);

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
        if (validateData(data)) {
            updateFields(data);
        } else {
            JOptionPane.showMessageDialog(this, "Data tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
        }
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
                consumer.subscribe(Collections.singletonList("edit"));

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
            if (data != null && validateData(data)) {
                String formattedMessage = String.format(
                        "[%tF %<tT] "
                        + "Username: %s | "
                        + "Email: %s | "
                        + "NIK: %s | "
                        + "Kontak: %s | "
                        + "Tanggal Lahir: %s | "
                        + "Alamat: %s | "
                        + "Jenis Kelamin: %s | "
                        + "Pekerjaan: %s | "
                        + "Gaji Pokok: %s",
                        System.currentTimeMillis(),
                        data.getOrDefault("username", "N/A"),
 data.getOrDefault("email", "N/A"),
                        data.getOrDefault("nik", "N/A"),
                        data.getOrDefault("kontak", "N/A"),
                        data.getOrDefault("tanggal_lahir", "N/A"),
                        data.getOrDefault("alamat", "N/A"),
                        data.getOrDefault("jenis_kelamin", "N/A"),
                        data.getOrDefault("pekerjaan", "N/A"),
                        data.getOrDefault("gaji_pokok", "N/A")
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
            JOptionPane.showMessageDialog(this, "Data tidak valid, tidak dapat disimpan!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "UPDATE users SET " +
                "username = ?, email = ?, password = ?, nik = ?, " +
                "kontak = ?, tanggal_lahir = ?, alamat = ?, " +
                "jenis_kelamin = ?, pekerjaan = ?, gaji_pokok = ? " +
                "WHERE nik = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, data.get("username"));
                pstmt.setString(2, data.get("email"));
                pstmt.setString(3, data.get("password"));
                pstmt.setString(4, data.get("nik"));
                pstmt.setString(5, data.get("kontak"));
                pstmt.setDate(6, java.sql.Date.valueOf(data.get("tanggal_lahir")));
                pstmt.setString(7, data.get("alamat"));
                pstmt.setString(8, data.get("jenis_kelamin"));
                pstmt.setString(9, data.get("pekerjaan"));
                pstmt.setString(10, data.get("gaji_pokok"));
                pstmt.setString(11, data.get("nik"));

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Data berhasil diperbarui untuk NIK: " + data.get("nik"));
                    SwingUtilities.invokeLater(() -> {
                        listModel.addElement("Data berhasil diperbarui: " + data.get("username"));
                        messageList.ensureIndexIsVisible(listModel.getSize() - 1);
                    });
                }
            }
        } catch (SQLException e) {
            SwingUtilities.invokeLater(() -> {
                listModel.addElement("Gagal memperbarui database: " + e.getMessage());
                messageList.ensureIndexIsVisible(listModel.getSize() - 1);
            });
            e.printStackTrace();
        }
    }

    private boolean validateData(Map<String, String> data) {
        String[] requiredFields = {
            "username", "email", "nik", "kontak", 
            "tanggal_lahir", "alamat", "jenis_kelamin", 
            "pekerjaan", "gaji_pokok"
        };

        for (String field : requiredFields) {
            if (!data.containsKey(field) || data.get(field) == null || data.get(field).trim().isEmpty()) {
                System.out.println("Field yang hilang atau kosong: " + field);
                return false;
            }
        }

        if (!data.get("email").matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            System.out.println("Format email tidak valid");
            return false;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.parse(data.get("tanggal_lahir"));
        } catch (ParseException e) {
            System.out.println("Format tanggal lahir tidak valid");
            return false;
        }

        if (!data.get("kontak").matches("\\d+")) {
            System.out.println("Nomor kontak harus berupa angka");
            return false;
        }

        return true;
    }

    private Map<String, String> parseMessage(String message) {
        Map<String, String> data = new HashMap<>();
        try {
            message = message.replace("{", "").replace("}", "");
            String[] pairs = message.split(",");

            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue [0].trim();
                    String value = keyValue[1].trim();
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
        txtNama.setText(data.getOrDefault("username", ""));
        txtEmail.setText(data.getOrDefault("email", ""));
        txtPassword.setText(data.getOrDefault("password", ""));
        txtNik.setText(data.getOrDefault("nik", ""));
        txtKontak.setText(data.getOrDefault("kontak", ""));
        txtTanggalLahir.setDate(parseDate(data.getOrDefault("tanggal_lahir", "")));
        txtAlamat.setText(data.getOrDefault("alamat", ""));
        
        String jenisKelamin = data.getOrDefault("jenis_kelamin", "");
        rbLakiLaki.setSelected("Laki-Laki".equalsIgnoreCase(jenisKelamin));
        rbPerempuan.setSelected("Perempuan".equalsIgnoreCase(jenisKelamin));

        txtPekerjaan.setText(data.getOrDefault("pekerjaan", ""));
        jComboBox1.setSelectedItem(data.getOrDefault("gaji_pokok", ""));
    }

    private Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(dateString);
        } catch (ParseException e) {
            System.err.println("Error parsing date: " + dateString);
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KafkaEditConsumer().setVisible(true));
    }
}