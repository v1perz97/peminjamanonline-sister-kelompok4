/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.peminjamanonline_sister_kelompok4;

/**
 *
 * @author Zion Exeon Ch
 */
public class Main implements Runnable {
     public static void main(String[] args) {
        // Tampilkan jendela login terlebih dahulu
        
     Main obj = new Main();
    Thread thread = new Thread(obj);
    thread.start();

        MenuLogin loginWindow = new MenuLogin();
        loginWindow.setVisible(true);
        

    }

    @Override
    public void run() {
        
     while(true){
     
         
         System.out.println("Saya consume di thread");
     
     }
        //consume all topic
        
        // consume topic tambah peminjaman
        //consume {
    // insert sesuai hasil consume ke tb peminjaman
    
    //}
        
      
    }
}
