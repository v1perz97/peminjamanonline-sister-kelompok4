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
public class KafkaRegisterProducer {
    public static void KirimDataRegister(String username, String email, String password, String nik, String kontak, String tanggalLahirStr, String alamat, String jenisKelamin, String pekerjaan, String gaji_pokok, String fotoKTP) {

    Properties props = new Properties();
    props.put("bootstrap.servers", "192.168.2.82:9092,192.168.2.112:9093,192.168.2.154:9094");
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

    KafkaProducer<String, String> producer = new KafkaProducer<>(props);

    String message = "Username " + username + " Dengan Email " + email + " Berhasil Melakukan Registrasi";

    // Kirim pesan tanpa key (key = null)
    producer.send(new ProducerRecord<>("register", username, message));

    producer.close();
}


}
