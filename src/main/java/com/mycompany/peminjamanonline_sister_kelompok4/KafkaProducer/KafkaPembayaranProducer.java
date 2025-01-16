/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.peminjamanonline_sister_kelompok4.KafkaProducer;

import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 *
 * @author Zion Exeon Ch
 */
public class KafkaPembayaranProducer {

    private final KafkaProducer<String, String> producer;

    public KafkaPembayaranProducer() {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:9092");
        props.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.producer = new KafkaProducer<>(props);
    }

    public void sendMessage(String topic, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            "Error sending data to Kafka: " + exception.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                });
                exception.printStackTrace();
            } else {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            "Message sent successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                });
            }
        });
    }

    public void close() {
        producer.close();
    }
}
