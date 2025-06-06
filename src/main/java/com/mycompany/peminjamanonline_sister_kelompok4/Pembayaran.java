package com.mycompany.peminjamanonline_sister_kelompok4;

//import java.sql.Connection;
import com.mycompany.peminjamanonline_sister_kelompok4.KafkaProducer.KafkaPembayaranConsumer;
import com.mycompany.peminjamanonline_sister_kelompok4.KafkaProducer.KafkaPembayaranProducer;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

//import java.util.Date;
//import javax.swing.JOptionPane;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 *
 * @author ACER
 */
public class Pembayaran extends javax.swing.JFrame {

    private final int iduser;
    private KafkaPembayaranProducer kafkaPembayaranProducer;

    /**
     * Creates new form Pembayaran
     *
     * @param iduser
     */
    public Pembayaran(int iduser) {
        initComponents();
        this.iduser = iduser;
        this.kafkaPembayaranProducer = new KafkaPembayaranProducer();
        loadTagihanData(); // Load tagihan saat form dibuka
//       
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtTagihan = new javax.swing.JLabel();
        txtJatuhTempo = new javax.swing.JLabel();
        btnBayar = new javax.swing.JButton();
        btnKembali = new javax.swing.JButton();
        DtTanggal = new com.toedter.calendar.JDateChooser();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(102, 102, 255));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Pembayaran");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(99, 99, 99)
                .addComponent(jLabel3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addContainerGap(7, Short.MAX_VALUE))
        );

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Tagihan :");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Tanggal :");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Jatuh Tempo :");

        txtTagihan.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtTagihan.setText("Isi");

        txtJatuhTempo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtJatuhTempo.setText("Isi");

        btnBayar.setBackground(new java.awt.Color(102, 102, 255));
        btnBayar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnBayar.setForeground(new java.awt.Color(255, 255, 255));
        btnBayar.setText("Bayar");
        btnBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBayarActionPerformed(evt);
            }
        });

        btnKembali.setBackground(new java.awt.Color(102, 102, 255));
        btnKembali.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnKembali.setForeground(new java.awt.Color(255, 255, 255));
        btnKembali.setText("Kembali");
        btnKembali.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKembaliActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtTagihan)
                            .addComponent(txtJatuhTempo)
                            .addComponent(DtTanggal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnKembali)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 79, Short.MAX_VALUE)
                        .addComponent(btnBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(27, 27, 27))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtTagihan))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(DtTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtJatuhTempo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBayar)
                    .addComponent(btnKembali))
                .addGap(22, 22, 22))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnKembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKembaliActionPerformed
        RiwayatPinjaman FormRiwayatPinjaman = new RiwayatPinjaman(iduser);
        FormRiwayatPinjaman.setVisible(true);
        this.dispose(); // Tutup form pembayaran
    }//GEN-LAST:event_btnKembaliActionPerformed

    private void btnBayarActionPerformed(java.awt.event.ActionEvent evt) {
        DtTanggal.setDateFormatString("yyyy-MM-dd");
        java.util.Date selectedDate = DtTanggal.getDate();
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Tanggal pembayaran tidak boleh kosong!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tanggalPembayaran = new java.sql.Date(selectedDate.getTime()).toString();

        // Ganti placeholder dengan nilai yang sebenarnya
        String pinjamanId = "pinjaman_id"; // Ambil dari input atau database
        String sisaTagihan = "sisa_tagihan"; // Ambil dari input atau database
        String jumlahBayar = "jumlah_bayar"; // Ambil dari input atau database

        // Format pesan sesuai kebutuhan consumer
        String message = String.format(
                "{\"iduser\":\"%s\", \"pinjaman_id\":\"%s\", \"tanggal_pembayaran\":\"%s\", \"sisa_tagihan\":\"%s\", \"jumlah_bayar\":\"%s\"}",
                iduser, pinjamanId, tanggalPembayaran, sisaTagihan, jumlahBayar
        );

        // Kirim pesan ke Kafka
        try {
            kafkaPembayaranProducer.sendMessage("notifikasi-pembayaran", message);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mengirim pesan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void dispose() {
        kafkaPembayaranProducer.close();
        super.dispose();
    }

    public static void main(String args[]) {
        // Set the Nimbus look and feel
        // ... (kode setting look and feel yang sama seperti sebelumnya) ...
        int loggedInUserId = 1;
        new Thread(new KafkaPembayaranConsumer()).start();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Pembayaran(loggedInUserId).setVisible(true);
            }
        });

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.toedter.calendar.JDateChooser DtTanggal;
    private javax.swing.JButton btnBayar;
    private javax.swing.JButton btnKembali;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel txtJatuhTempo;
    private javax.swing.JLabel txtTagihan;
    // End of variables declaration//GEN-END:variables

    private void loadTagihanData() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/loan_app", "root", "")) {
            String query = "SELECT jumlah_bayar, jatuh_tempo FROM tagihan WHERE pinjaman_id = (SELECT pinjaman_id FROM pinjaman WHERE iduser = ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, iduser);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Mengonversi double ke String
                txtTagihan.setText(String.valueOf(rs.getDouble("jumlah_bayar")));
                // Mengonversi Date ke String
                Date jatuhTempo = rs.getDate("jatuh_tempo");
                if (jatuhTempo != null) {
                    txtJatuhTempo.setText(jatuhTempo.toString()); // Anda bisa menggunakan format lain jika perlu
                } else {
                    txtJatuhTempo.setText(""); // Atau set ke string kosong jika null
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data tagihan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
