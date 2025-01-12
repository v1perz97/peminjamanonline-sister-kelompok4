/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.peminjamanonline_sister_kelompok4;

import java.time.LocalDate;

/**
 *
 * @author ACER
 */
public class pengajuan {
    
    String jumlah;
    String suku_bunga;
    String tenor;
    String angsuran_bulanan;
    String tanggal_cair;
    String total_cair;

    public String getJumlah(){
        return jumlah;
    }

    public void setJumlah(String jumlah) {
        this.jumlah = jumlah;
    }

    public String getSukuBunga() {
        return suku_bunga;
    }

    public void setSukuBunga(String suku_bunga) {
        this.suku_bunga = suku_bunga;
    }

    public String getTenor() {
        return tenor;
    }

    public void setTenor(String tenor) {
        this.tenor = tenor;
    }

    public String getAngsuran() {
        return angsuran_bulanan;
    }

    public void setAngsuran(String angsuran_bulanan) {
        this.angsuran_bulanan = angsuran_bulanan;
    }

    public String getTanggalLahir() {
        return tanggal_cair;
    }

    public void setTanggalLahir(String tanggal_cair) {
        this.tanggal_cair = tanggal_cair;
    }
    
    public String getTotalCair() {
        return total_cair;
    }

    public void setTotalCair(String total_cair) {
        this.total_cair = total_cair;
    }

    @Override
    
    public String toString() {
        return String.format("[{Jumlah:%s,Bunga:%s,Tenor:%s,Cicilan:%s,Tanggal Cair:%s,Total Cair:%s}]",
                jumlah, suku_bunga, tenor, angsuran_bulanan, tanggal_cair, total_cair);
    }


    
    public void toObject(String string) {
    jumlah = string.substring(string.indexOf("jumlah") + 7, string.indexOf("suku_bunga") - 1);
    suku_bunga = string.substring(string.indexOf("suku_bunga") + 11, string.indexOf("tenor") - 1);
    tenor = string.substring(string.indexOf("tenor") + 6, string.indexOf("angsuran_bulanan") - 1);
    angsuran_bulanan = string.substring(string.indexOf("angsuran_bulanan") + 17, string.indexOf("tanggal_cair") - 1);
    tanggal_cair = string.substring(string.indexOf("tanggal_cair") + 13, string.indexOf("total_cair") - 1);
    total_cair = string.substring(string.indexOf("total_cair") + 11, string.indexOf("}") - 1);
    
}

}
