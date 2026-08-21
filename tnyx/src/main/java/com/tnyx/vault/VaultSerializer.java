package com.tnyx.vault;

import java.nio.ByteBuffer;
import java.util.List;

public class VaultSerializer {
    
    public static byte[] serializeVault(List<PasswordEntry> entries){
        




        ByteBuffer buffer = ByteBuffer.allocate((4 + (entries.size() * 4))); // 4 bcs 4 bytes in an int
        byte[] serializedVault = new byte[1]; //TODO: REMOVE BEFORE USE





        return serializedVault;
    }



}
