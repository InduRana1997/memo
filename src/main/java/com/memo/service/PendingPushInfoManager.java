package com.memo.service;

import com.memo.model.PendingPushInfo;
import lombok.Getter;

public class PendingPushInfoManager {

    @Getter
    private static PendingPushInfo alert;

    public static void setAlert(PendingPushInfo alert) {
        PendingPushInfoManager.alert = alert;
    }

    public static void clear() {
        alert = null;
    }
}
