/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.peminjamanonline_sister_kelompok4;

import java.time.LocalDate;
import java.util.Date;

/**
 *
 * @author ACER
 */
public class pinjaman {

    String username;
    String email;
    String password;
    String nik;
    String kontak;
    String tanggal_lahir;
    String alamat;
    String jenis_kelamin;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNik() {
        return nik;
    }

    public void setNik(String nik) {
        this.nik = nik;
    }

    public String getKontak() {
        return kontak;
    }

    public void setKontak(String kontak) {
        this.kontak = kontak;
    }
    
    public String getTanggalLahir() {
        return tanggal_lahir;
    }

//    public void setTanggalLahir(LocalDate tanggal_lahir) {
////       this.tanggal_lahir = tanggal_lahir;
//    }
    public void setTanggalLahir(LocalDate tanggal_lahir) {
        this.tanggal_lahir = tanggal_lahir != null ? tanggal_lahir.toString() : null;
    }
    
    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }
    
    public String getJenisKelamin() {
        return jenis_kelamin;
    }

    public void setJenisKelamin(String jenis_kelamin) {
        this.jenis_kelamin = jenis_kelamin;
    }

    @Override
    
    public String toString() {
        return String.format("[{username:%s,email:%s,password:%s,nik:%s,kontak:%s,tanggallahir:%s,alamat:%s,jeniskelamin:%s}]",
                username, email, password, nik, kontak, tanggal_lahir != null ? tanggal_lahir.toString() : "null", alamat, jenis_kelamin);
    }


    
    public void toObject(String string) {
    username = string.substring(string.indexOf("username") + 9, string.indexOf("email") - 1);
    email = string.substring(string.indexOf("email") + 6, string.indexOf("password") - 1);
    password = string.substring(string.indexOf("password") + 9, string.indexOf("nik") - 1);
    nik = string.substring(string.indexOf("nik") + 4, string.indexOf("kontak") - 1);
    kontak = string.substring(string.indexOf("kontak") + 7, string.indexOf("tanggallahir") - 1);
    tanggal_lahir = string.substring(string.indexOf("tanggallahir") + 13, string.indexOf("alamat") - 1);
    alamat = string.substring(string.indexOf("alamat") + 7, string.indexOf("jeniskelamin") - 1);
    jenis_kelamin = string.substring(string.indexOf("jenis_kelamin") + 14, string.indexOf("}") - 1);
}

}



