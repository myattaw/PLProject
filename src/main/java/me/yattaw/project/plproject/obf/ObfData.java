package me.yattaw.project.plproject.obf;

import lombok.Data;

@Data
public class ObfData {

    private boolean xorObfuscation;
    private boolean nameObfuscation;
    private boolean stringObfuscation;

    // Store the obfuscated name
    private String obfuscatedName;

}
