package com.memo.controller;

import com.memo.model.PendingPushInfo;
import com.memo.service.MemoEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/memo")
public class MemoWebHookController {

    @Autowired
    private MemoEventService memoEventService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/push-event")
    public ResponseEntity<String> handlePushEvent(@RequestBody String payload) {

        try {
            JsonNode json = objectMapper.readTree(payload);

            // 1) Extract branch
            String ref = json.get("ref").asText();
            String branch = ref.replace("refs/heads/", "");

            // 2) Extract pusher
            String pusher = json.get("pusher").get("name").asText();

            // 3) Extract commit info
            JsonNode commits = json.get("commits");
            JsonNode lastCommit = commits.get(commits.size() - 1);

            String commitId = lastCommit.get("id").asText();
            String shortCommitId = commitId.substring(0, 7);
            String commitMessage = lastCommit.get("message").asText();

            // 4) Extract commit timestamp
            ZonedDateTime zdt = ZonedDateTime.parse(lastCommit.get("timestamp").asText());
            long pushTimestamp = zdt.toInstant().toEpochMilli();

            // 5) Store pending info
            PendingPushInfo info = new PendingPushInfo();
            info.setPending(true);
            info.setBranchName(branch);
            info.setCommitMessage(commitMessage);
            info.setShortId(shortCommitId);
            info.setCommitId(commitId);
            info.setPusherName(pusher);
            info.setPushedTime(pushTimestamp);
            info.setReceivedTime(System.currentTimeMillis());


            // 6) Initial popup
            showPopup(
                    "New push on " + branch,
                    pusher + " → " + shortCommitId + "\n\""
                            + commitMessage + "\"\nPushed at: "
                            + formatTime(pushTimestamp)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok("received");
    }

    private String formatTime(long ts) {
        return Instant.ofEpochMilli(ts)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    private void showPopup(String title, String message) {
        try {
            String command = "powershell -command \"New-BurntToastNotification -Text '"
                    + title + "', '" + message + "'\"";
            Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
