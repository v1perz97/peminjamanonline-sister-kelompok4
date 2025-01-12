/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.peminjamanonline_sister_kelompok4.KafkaProducer;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 *
 * @author ACER
 */
public class KafkaKonfirmasiProducer {
    public static void PesanKonfirmasi(String nik, String status) {

    Properties props = new Properties();
    props.put("bootstrap.servers", "localhost:9092");
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

    KafkaProducer<String, String> producer = new KafkaProducer<>(props);

    String message = "Pengajuan dengan NIK " + nik + " telah " + status + ".";

    producer.send(new ProducerRecord<>("konfirmasi", nik, message), (metadata, exception) -> {
        if (exception != null) {
            System.err.println("Gagal mengirim pesan konfirmasi ke Kafka: " + exception.getMessage());
        } else {
            System.out.println("Pesan konfirmasi berhasil dikirim ke Kafka. Offset: " + metadata.offset());
        }
    });

    producer.close();
}

}


