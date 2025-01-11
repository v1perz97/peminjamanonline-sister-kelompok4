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
            connection = DriverManager.getConnection("jdbc:mysql://192.168.38.245:3306/loan_app", "root", "");
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
                System.out.println("Received message from topic '" + topic + "': " + message);
                
                try {
                    String[] fields = message.split(",");
                    switch (topic) {
                        case "register":
                            if (fields.length >= 9) {
                                insertIntoUsers(fields);
                            } else {
                                System.out.println("Invalid message format for 'register': " + message);
                            }
                            break;
//                        case "pengajuan":
//                            if (fields.length >= 5) {
//                                insertIntoLoans(fields);
//                            } else {
//                                System.out.println("Invalid message format for 'loan_request': " + message);
//                            }
//                            break;
//                        case "payment":
//                            if (fields.length >= 4) {
//                                insertIntoPayments(fields);
//                            } else {
//                                System.out.println("Invalid message format for 'payment': " + message);
//                            }
//                            break;
                    }
                    kafkaConsumer.commitSync(); 
                    } catch (SQLException e) {
                    System.err.println("Error inserting into database: " + e.getMessage());
                    e.printStackTrace();
                }
        }
    }

    

    }
    private void insertIntoUsers(String[] fields) throws SQLException {
    String sql = "INSERT INTO users (username, email, password, nik, kontak, tanggal_lahir, alamat, jenis_kelamin, foto_ktp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        System.out.println("Inserting into users: " + Arrays.toString(fields));
        for (int i = 0; i < fields.length; i++) {
            stmt.setString(i + 1, fields[i]); // Mengisi nilai parameter dari array fields
        }
        int rowsAffected = stmt.executeUpdate();
        System.out.println("Rows affected in 'users': " + rowsAffected);
    }
}

//private void insertIntoLoans(String[] fields) throws SQLException {
//    String sql = "INSERT INTO loans (user_id, loan_amount, interest_rate, loan_term, status) VALUES (?, ?, ?, ?, ?)";
//    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//        System.out.println("Inserting into loans: " + Arrays.toString(fields));
//        for (int i = 0; i < fields.length; i++) {
//            stmt.setString(i + 1, fields[i]);
//        }
//        int rowsAffected = stmt.executeUpdate();
//        System.out.println("Rows affected in 'loans': " + rowsAffected);
//    }
//}
//
//private void insertIntoPayments(String[] fields) throws SQLException {
//    String sql = "INSERT INTO payments (loan_id, payment_date, amount, payment_method) VALUES (?, ?, ?, ?)";
//    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//        System.out.println("Inserting into payments: " + Arrays.toString(fields));
//        for (int i = 0; i < fields.length; i++) {
//            stmt.setString(i + 1, fields[i]);
//        }
//        int rowsAffected = stmt.executeUpdate();
//        System.out.println("Rows affected in 'payments': " + rowsAffected);
//    }
//}

    public static void main(String[] args) {
        new Utama().start(); 
    }
}

