package me.yattaw.project.plproject.data;

public class ObfData {

    private boolean xorObfuscation;
    private boolean nameObfuscation;
    private boolean stringObfuscation;

    public boolean isXorObfuscation() {
        return xorObfuscation;
    }

    public void setXorObfuscation(boolean xorObfuscation) {
        this.xorObfuscation = xorObfuscation;
    }

    public boolean isNameObfuscation() {
        return nameObfuscation;
    }

    public void setNameObfuscation(boolean nameObfuscation) {
        this.nameObfuscation = nameObfuscation;
    }

    public boolean isStringObfuscation() {
        return stringObfuscation;
    }

    public void setStringObfuscation(boolean stringObfuscation) {
        this.stringObfuscation = stringObfuscation;
    }
}
