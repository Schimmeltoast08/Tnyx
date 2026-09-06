package com.tnyx.crypto;

public class CryptoEngine {
    

    public static byte[] encrypt(byte[] plaintext, char[] password){

            for (byte b : plaintext){
            System.out.printf("%02x ", b & 0xFF); //TODO Temporary, security risk
        }





        return new byte[1]; // TEMPORARY!!! //TODO: REMOVE BEFORE USE
    }


    public static byte[] decrypt(byte[] encryptedData, char[] password){








        
        return new byte[1]; //TODO: REMOVE BEFORE USE
    }



}
