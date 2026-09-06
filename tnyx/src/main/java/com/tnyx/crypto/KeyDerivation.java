package com.tnyx.crypto;

import java.security.SecureRandom;

public class KeyDerivation {

    public static byte[] generateSalt() {
        byte[] salt = new byte[CryptoConstants.SALT_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return salt;

    }


    //TODO: Step 6 in developement plan // plan is NOT public, sorry :(

}
