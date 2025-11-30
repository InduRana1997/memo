package com.memo.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class PendingPushInfo {
    private boolean pending;
    private String pusherName;
    private String branchName;
    private String shortId;
    private String commitMessage;
    private String commitId;
    private Long receivedTime;
    private Long pushedTime;
}
