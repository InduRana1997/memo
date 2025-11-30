package com.memo.Job;

import com.memo.model.PendingPushInfo;
import com.memo.service.MemoEventService;
import com.memo.service.PendingPushInfoManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ReminderJob implements Job {

    @Autowired
    private MemoEventService memoEventService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        PendingPushInfo alert = PendingPushInfoManager.getAlert();

        if (alert == null || !alert.isPending()) {
            return; // nothing to remind
        }

        // Get developer's current branch
        String localBranch = memoEventService.getLocalBranch();

        if (!localBranch.equals(alert.getBranchName())) {
            // Developer moved to a different branch — ignore reminder
            return;
        }

        // Check if developer pulled latest commit
        String localHead = getLocalHeadCommit();

        if (localHead.equals(alert.getCommitId())) {
            // DEV HAS PULLED SUCCESSFULLY — clear pending state
            PendingPushInfoManager.clear();

            showPopup(
                    "You're up-to-date on " + alert.getBranchName(),
                    "Latest commit " + alert.getShortId() +
                            " by " + alert.getPusherName() + " is synced.\n" +
                            "Pushed at: " + formatTime(alert.getPushedTime())
            );

            return;
        }

        // STILL PENDING — show hourly reminder
        showPopup(
                "Reminder: Pull latest on " + alert.getBranchName(),
                alert.getPusherName() + " → " + alert.getShortId() + "\n\"" +
                        alert.getCommitMessage() + "\"\n" +
                        "Pending since: " + formatTime(alert.getPushedTime())
        );
    }


    private String getLocalHeadCommit() {
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "rev-parse", "HEAD");
            pb.directory(new java.io.File("."));
            Process p = pb.start();
            p.waitFor();
            return new String(p.getInputStream().readAllBytes()).trim();
        } catch (Exception e) {
            return "";
        }
    }

    private String formatTime(long ts) {
        return Instant.ofEpochMilli(ts)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    private void showPopup(String title, String message) {
        try {
            String cmd = "powershell -command \"New-BurntToastNotification -Text '"
                    + title + "', '" + message + "'\"";
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
