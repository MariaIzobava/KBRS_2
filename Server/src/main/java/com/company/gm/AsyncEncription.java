package main.java.com.company.gm;

public interface AsyncEncription<T1, T2> {
    void init();
    String getPublicKey();
    String getPrivateKey();
    String encrypt(String data);
    String decrypt(String data);
}
