/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.peminjamanonline_sister_kelompok4;

import java.util.Properties;
import javax.swing.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class NotifikasiConsumer extends JFrame {

    private final JTextArea textArea; // Area untuk menampilkan pesan notifikasi
    private Consumer<String, String> kafkaConsumer;

    public NotifikasiConsumer() {
        // Inisialisasi JFrame
        setTitle("Notifikasi Pembayaran");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tambahkan JTextArea untuk menampilkan pesan
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea); // Supaya scrollable
        add(scrollPane);

        // Konfigurasi Kafka Consumer
        configureKafkaConsumer();

        // Jalankan Kafka Consumer dalam thread terpisah
        Thread consumerThread = new Thread(this::startKafkaConsumer);
        consumerThread.start();
    }

    /**
     * Konfigurasi Kafka Consumer.
     */
    private void configureKafkaConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "notifikasi-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        kafkaConsumer = new KafkaConsumer<>(props);
        kafkaConsumer.subscribe(java.util.Collections.singletonList("notifikasi-pembayaran")); // Topic untuk notifikasi
    }

    /**
     * Jalankan Kafka Consumer untuk mendengarkan pesan.
     */
    private void startKafkaConsumer() {
        try {
            while (true) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(java.time.Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    String message = record.value();
                    appendMessage(message);
                }
            }
        } catch (Exception e) {
            appendMessage("Error: " + e.getMessage());
        } finally {
            kafkaConsumer.close();
        }
    }

    /**
     * Tambahkan pesan ke JTextArea.
     *
     * @param message Pesan notifikasi
     */
    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> textArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NotifikasiConsumer consumerFrame = new NotifikasiConsumer();
            consumerFrame.setVisible(true);
        });
    }
}
