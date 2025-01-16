package com.mycompany.peminjamanonline_sister_kelompok4.KafkaProducer;

import com.mycompany.peminjamanonline_sister_kelompok4.RiwayatPinjaman;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class KafkaPembayaranConsumer implements Runnable {

    private final KafkaConsumer<String, String> consumer;

    public KafkaPembayaranConsumer() {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "payment-group");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList("notifikasi-pembayaran"));
    }

    @Override
    public void run() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                processMessage(record.value());
            }
        }
    }

    private void processMessage(String message) {
        try {
            // Parsing pesan yang dikirim dari producer
            String[] parts = message.split(", ");
            int iduser = Integer.parseInt(parts[0].split("=")[1]);
            String tanggalPembayaran = parts[2].split("=")[1];

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/loan_app", "root", "")) {
                conn.setAutoCommit(false);

                // Update tanggal pembayaran pada tabel tagihan
                String updateTagihan = "UPDATE tagihan SET tanggal_pembayaran = ? WHERE pinjaman_id = (SELECT pinjaman_id FROM pinjaman WHERE iduser = ?) LIMIT 1";
                try (PreparedStatement stmtUpdateTagihan = conn.prepareStatement(updateTagihan)) {
                    stmtUpdateTagihan.setDate(1, Date.valueOf(tanggalPembayaran));
                    stmtUpdateTagihan.setInt(2, iduser);
                    stmtUpdateTagihan.executeUpdate();
                }

                // Menghitung total jumlah bayar yang sudah dilakukan
                String totalBayarQuery = "SELECT SUM(jumlah_bayar) FROM tagihan WHERE pinjaman_id = (SELECT pinjaman_id FROM pinjaman WHERE iduser = ?)";
                double totalBayar = 0;
                try (PreparedStatement stmtTotalBayar = conn.prepareStatement(totalBayarQuery)) {
                    stmtTotalBayar.setInt(1, iduser);
                    ResultSet rs = stmtTotalBayar.executeQuery();
                    if (rs.next()) {
                        totalBayar = rs.getDouble(1);
                    }
                }

                // Update sisa tagihan pada tabel pinjaman
                String updatePinjaman = "UPDATE pinjaman SET sisa_tagihan = (total_tagihan - ?) WHERE iduser = ?";
                try (PreparedStatement stmtUpdatePinjaman = conn.prepareStatement(updatePinjaman)) {
                    stmtUpdatePinjaman.setDouble(1, totalBayar);
                    stmtUpdatePinjaman.setInt(2, iduser);
                    stmtUpdatePinjaman.executeUpdate();
                }

                // Cek apakah sisa tagihan sudah 0 dan ubah status menjadi lunas
                String checkAndUpdateStatus = "UPDATE pinjaman SET status = 'lunas' WHERE sisa_tagihan = 0 AND iduser = ?";
                try (PreparedStatement stmtUpdateStatus = conn.prepareStatement(checkAndUpdateStatus)) {
                    stmtUpdateStatus.setInt(1, iduser);
                    stmtUpdateStatus.executeUpdate();
                }

                conn.commit();

                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, "Pembayaran berhasil!", "Info", JOptionPane.INFORMATION_MESSAGE);
                    // Kembali ke form Riwayat Pinjaman
                    RiwayatPinjaman riwayatPinjaman = new RiwayatPinjaman(iduser);
                    riwayatPinjaman.setVisible(true);
                });

 } catch (SQLException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, "Gagal melakukan pembayaran: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, "Format pesan tidak valid: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            });
        }
    }
}