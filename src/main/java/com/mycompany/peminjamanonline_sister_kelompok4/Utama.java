/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.peminjamanonline_sister_kelompok4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 *
 * @author ACER
 */
public class Utama extends Thread{
    private KafkaConsumer<String, String> kafkaConsumer;
    private Connection connection;
    private Connection connection2;
    private Connection connection3;
    
    public Utama() {
        
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.38.245:9092, 192.168.35.245:9093, 192.168.32.190:9094");
        props.put("group.id", "Konfirmasi");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        kafkaConsumer = new KafkaConsumer<>(props);
        kafkaConsumer.subscribe(Arrays.asList("register", "pengajuan", "konfirmasi"));

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/loan_app", "root", "");
            connection2 = DriverManager.getConnection("jdbc:mysql://192.168.35.245:3306/loan_app", "root", "");
            connection3 = DriverManager.getConnection("jdbc:mysql://192.168.32.190:3306/loan_app", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
    while (true) {
        ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));
        for (ConsumerRecord<String, String> record : records) {
            String topic = record.topic();
            String message = record.value();
            System.out.printf("offset = %d, key = %s, value = %s%n, Partition = %d, Topik = %s%n",
                    record.offset(), record.key(), record.value(), record.partition(), record.topic());

            try {
                String[] fields = message.split(",");
                switch (topic) {
                    case "register":
                        if (fields.length >= 9) {
                            SimpanDataUser(fields);
                        } else {
                            System.out.println("Format pesan tidak valid untuk 'register': " + message);
                        }
                        break;
                    case "pengajuan":
                        if (fields.length >= 8) {
                            SimpanDataPengajuan(fields);
                        } else {
                            System.out.println("Format pesan tidak valid untuk 'pengajuan': " + message);
                        }
                        break;
                    case "pembayaran":
                        if (fields.length >= 4) {
                            SimpanDataPembayaran(fields);
                        } else {
                            System.out.println("Format pesan tidak valid untuk 'pembayaran': " + message);
                        }
                        break;
                    case "konfirmasi":
                        if (fields.length >= 4) {
                            Konfirmasi(fields);
                        } else {
                            System.out.println("Format pesan tidak valid untuk 'konfirmasi': " + message);
                        }
                        break;
                    case "login":
                        if (fields.length >= 4) {
                            Login(fields);
                        } else {
                            System.out.println("Format pesan tidak valid untuk 'login': " + message);
                        }
                        break;
                }
                kafkaConsumer.commitSync();
            } catch (SQLException e) {
                System.err.println("Kesalahan saat memasukkan ke dalam basis data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
    private void SimpanDataUser(String[] fields) throws SQLException {
    String sql = "INSERT INTO users (username, email, password, nik, kontak, tanggal_lahir, alamat, jenis_kelamin, foto_ktp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        System.out.println("Inserting into users: " + Arrays.toString(fields));
        for (int i = 0; i < fields.length; i++) {
            stmt.setString(i + 1, fields[i]);
        }
        int rowsAffected = stmt.executeUpdate();
        System.out.println("Rows affected in 'users': " + rowsAffected);
    }
}

    private void SimpanDataPengajuan(String[] fields) throws SQLException {
    
    String queryPinjaman = "INSERT INTO pinjaman (iduser, jumlah, tenor, suku_bunga, angsuran_bulanan, tanggal_cair, total_cair, sisa_angsuran) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    String queryPengajuan = "INSERT INTO pengajuan_pinjaman (iduser, pinjaman_id, tanggal_pengajuan, status) VALUES (?, ?, ?, ?)";
    String queryTagihan = "INSERT INTO tagihan (pinjaman_id, tanggal_pembayaran, jumlah_bayar, jatuh_tempo) VALUES (?, ?, ?, ?)";

    try {
        connection.setAutoCommit(false);

        try (PreparedStatement stmtPinjaman = connection.prepareStatement(queryPinjaman)) {
            for (int i = 0; i < fields.length; i++) {
                stmtPinjaman.setString(i + 1, fields[i]);
            }
            stmtPinjaman.executeUpdate();
        }

        try (PreparedStatement stmtPengajuan = connection.prepareStatement(queryPengajuan)) {
            for (int i = 0; i < fields.length; i++) {
                stmtPengajuan.setString(i + 1, fields[i]);
            }
            stmtPengajuan.executeUpdate();
        }

        try (PreparedStatement stmtTagihan = connection.prepareStatement(queryTagihan)) {
            for (int i = 0; i < fields.length; i++) {
                stmtTagihan.setString(i + 1, fields[i]);
            }
            stmtTagihan.executeUpdate();
        }

        connection.commit();
        System.out.println("Data berhasil disimpan ke ketiga tabel.");
    } catch (SQLException e) {
        connection.rollback();
        System.err.println("Terjadi kesalahan, transaksi dibatalkan: " + e.getMessage());
        e.printStackTrace();
    } finally {
        connection.setAutoCommit(true);
    }
}

    private void SimpanDataPembayaran(String[] fields) throws SQLException {
        String sql = "INSERT INTO notifikasi (iduser, tagihan_id, total_pembayaran, sisa_angsurans) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            System.out.println("Inserting into payments: " + Arrays.toString(fields));
            for (int i = 0; i < fields.length; i++) {
                stmt.setString(i + 1, fields[i]);
            }
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Rows affected in 'payments': " + rowsAffected);
        }
    }
    
    private void Konfirmasi(String[] fields) throws SQLException {
    String updateQuery = "UPDATE pengajuan_pinjaman pp "
            + "JOIN users u ON pp.iduser = u.iduser "
            + "SET pp.status = ? "
            + "WHERE u.nik = ?";

    try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
        System.out.println("Updating status for user: " + Arrays.toString(fields));

        stmt.setString(1, fields[0]);
        stmt.setString(2, fields[1]);

        int rowsAffected = stmt.executeUpdate();
        
    } catch (SQLException e) {
        System.err.println("Error during confirmation update: " + e.getMessage());
        e.printStackTrace();
    }
}

    
    private void Login(String[] fields) throws SQLException {
        
    }

    public static void main(String[] args) {
        new Utama().start(); 
    }
}

