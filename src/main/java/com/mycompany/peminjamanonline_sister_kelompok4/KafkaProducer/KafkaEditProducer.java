package com.mycompany.peminjamanonline_sister_kelompok4.KafkaProducer;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 *
 * @author ACER
 */
public class KafkaEditProducer {
    public static void EditProfil(String nama, String email, String nik, String kontak, String tanggalLahir, String alamat, String jenis_kelamin) {

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // Membuat pesan dalam format JSON
        String message = String.format(
            "{\"nama\": \"%s\", \"email\": \"%s\", \"nik\": \"%s\", \"kontak\": \"%s\", \"tanggal_lahir\": \"%s\", \"alamat\": \"%s\", \"jenis_kelamin\": \"%s\", \"action\": \"Melakukan Edit Data Profil\"}",
            nama, email, nik, kontak, tanggalLahir, alamat, jenis_kelamin
        );

        // Mengirim pesan ke Kafka
        producer.send(new ProducerRecord<>("pengajuan", nama, message), (metadata, exception) -> {
            if (exception != null) {
                System.err.println("Gagal mengirim data ke Kafka: " + exception.getMessage());
            } else {
                System.out.println("Data berhasil dikirim ke Kafka. Offset: " + metadata.offset());
            }
        });

        // Menutup Kafka producer
        producer.close();
    }
}
