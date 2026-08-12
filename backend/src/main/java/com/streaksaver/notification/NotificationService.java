package com.streaksaver.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendStreakWarningNotification(String userId) {
        log.info("NOTIFICATION_SENT userId={} type=STREAK_WARNING message='Your coding streak is at risk. No submission detected today on your active platforms.'", userId);
    }

    public void sendSubmissionSuccessNotification(String userId, String platformName) {
        log.info("NOTIFICATION_SENT userId={} type=SUBMISSION_SUCCESS message='StreakSaver made today\\'s single permitted submission on {}.'", userId, platformName);
    }

    public void sendSubmissionFailedNotification(String userId, String platformName, String reason) {
        log.info("NOTIFICATION_SENT userId={} type=SUBMISSION_FAILED message='StreakSaver attempted single permitted submission on {} but encountered error: {}. No further attempts will be made today.'", userId, platformName, reason);
    }
}
