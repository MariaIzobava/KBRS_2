package main.java.com.company.idea_cipher.modes;

import main.java.com.company.idea_cipher.crypto.IdeaCipher;

/**
 * Mode of operation.
 */
public abstract class OperationMode {

    public enum Mode {
        ECB, CBC, CFB, OFB
    }

    protected IdeaCipher idea;
    protected boolean encrypt;

    public OperationMode(IdeaCipher idea, boolean encrypt) {
        this.idea = idea;
        this.encrypt = encrypt;
    }

    protected abstract void crypt(byte[] data, int pos);

    void crypt(byte[] data){
        crypt(data, 0);
    }

    public boolean isEncrypt() {
        return encrypt;
    }
}
