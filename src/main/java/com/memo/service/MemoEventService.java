package com.memo.service;

import org.springframework.stereotype.Service;

@Service
public class MemoEventService {

    public String getLocalBranch() {
        try {
            //Running the command
            Process process = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD");
            process.waitFor();
            return new String(process.getInputStream().readAllBytes()).trim();
        } catch (Exception e) {
            return "";
        }
    }

    public void showWindowsNotification(String title, String message) {
        try {
            String command = "powershell -command \"New-BurntToastNotification -Text '"
                    + title + "', '" + message + "'\"";
            Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
