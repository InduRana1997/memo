package com.memo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/memo")
public class MemoWebHookController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/push-event")
    public ResponseEntity<String> handlePushEvent(@RequestBody String payload) {
        System.out.println("Payload is " + payload);
        JsonNode json = objectMapper.readTree(payload);

        // Extract branch
        String ref = json.get("ref").asText();
        String branch = ref.replace("refs/heads/", "");

        // Extract who pushed
        String username = json.get("pusher").get("name").asText();

        System.out.println("Branch: " + branch);
        System.out.println("Pusher: " + username);
        return ResponseEntity.ok("received");
    }
}
