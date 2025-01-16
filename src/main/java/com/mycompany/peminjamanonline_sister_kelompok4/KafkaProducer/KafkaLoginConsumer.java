package com.mycompany.peminjamanonline_sister_kelompok4.KafkaProducer;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.time.Duration;

public class KafkaLoginConsumer extends javax.swing.JFrame {

    private DefaultListModel<String> listModel;
    private JList<String> messageList;
    private JScrollPane scrollPane;
    private volatile boolean isRunning = true;

    private JTextField txtUsername;
    private JTextField txtPassword;

    public KafkaLoginConsumer() {
        TanpilanGUI();
        startKafkaConsumer();
    }

    private void TanpilanGUI() {
        setTitle("Informasi Login");
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
        messageList.setBorder(BorderFactory.createTitledBorder("Daftar User Login"));

        scrollPane = new JScrollPane(messageList);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Detail panel
        JPanel detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("Detail Login"));
        detailPanel.setBackground(new Color(245, 245, 245)); // Light Gray

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addDetailField(detailPanel, gbc, "Username:", txtUsername = new JTextField(20), 0);
        addDetailField(detailPanel, gbc, "Password:", txtPassword = new JTextField(20), 1);

        mainPanel.add(detailPanel, BorderLayout.SOUTH);

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
        if (data != null) {
            txtUsername.setText(data.getOrDefault("username", ""));
            txtPassword.setText(data.getOrDefault("password", ""));
        }
    }

    private void startKafkaConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "login_consumer_group_" + UUID.randomUUID().toString());
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");

        Thread consumerThread = new Thread(() -> {
            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
                consumer.subscribe(Collections.singletonList("login"));

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
        SwingUtilities.invokeLater(() -> {
            listModel.addElement(message);
            messageList.ensureIndexIsVisible(listModel.getSize() - 1);
        });
    }

    private Map<String, String> parseMessage(String message) {
        Map<String, String> data = new HashMap<>();
        try {
            message = message.replace("{", "").replace("}", "");
            String[] pairs = message.split(",");

            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    data.put(key, value);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing message: " + message);
            e.printStackTrace();
        }
        return data;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KafkaLoginConsumer().setVisible(true));
    }
}