package com.saucelabs.bamboo.sod;

/**
 * @author Ross Rowe
 */
public enum SeleniumVersion {
    ONE("1"), TWO("2");
    private String versionNumber;

    SeleniumVersion(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getVersionNumber() {
        return versionNumber;
    }
}