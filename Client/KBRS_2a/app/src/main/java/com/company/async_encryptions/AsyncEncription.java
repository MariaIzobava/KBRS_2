package com.company.async_encryptions;

public interface AsyncEncription<T1, T2> {
    void init();
    String getPublicKey();
    String getPrivateKey();
    String encrypt(String data);
    String decrypt(String data);
}
