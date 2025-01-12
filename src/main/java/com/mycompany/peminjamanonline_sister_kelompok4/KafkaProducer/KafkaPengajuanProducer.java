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
public class KafkaPengajuanProducer {
    public static void KirimDataPengajuan(int iduser, int jumlahPinjaman, String tanggalPengajuanStr, String tanggalCairStr, double bunga, String totalCairStr, String tenor, String angsuranBulananStr, String sisaAngsuranStr) {

    Properties props = new Properties();
    props.put("bootstrap.servers", "localhost:9092");
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

    KafkaProducer<String, String> producer = new KafkaProducer<>(props);

    String message = String.format("{\"iduser\": %d Telah Melakukan Pengajuan Pinjaman Dengan, \"jumlahPinjaman\": %d}", iduser, jumlahPinjaman);

    producer.send(new ProducerRecord<>("pengajuan", String.valueOf(iduser), message), (metadata, exception) -> {
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
