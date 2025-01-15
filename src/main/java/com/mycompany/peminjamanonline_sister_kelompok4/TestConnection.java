/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.mycompany.peminjamanonline_sister_kelompok4;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author ACER
 */
public class TestConnection {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                System.out.println("Koneksi Berhasil!");
            }
        } catch (SQLException e) {
            System.out.println("Koneksi Gagal!");
 }

    }
    
}
