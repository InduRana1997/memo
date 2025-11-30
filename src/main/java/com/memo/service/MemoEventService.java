package com.memo.service;

public class MemoEventService {

    public String getLocalBranch() {
        try {
            Process process = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD");
            process.waitFor();
            return new String(process.getInputStream().readAllBytes()).trim();
        } catch (Exception e) {
            return "";
        }
    }
}
