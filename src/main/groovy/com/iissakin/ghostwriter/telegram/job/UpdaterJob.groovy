package com.iissakin.ghostwriter.telegram.job

import com.iissakin.ghostwriter.telegram.service.TelegramRequester
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
/**
 * User: iissakin
 * Date: 28.02.2016.
 */
@Component
class UpdaterJob {

    @Autowired
    TelegramRequester requester

    @Scheduled(initialDelay = 3000L, fixedRate = 10000L)
    void checkForUpdates() {
        requester.getUpdates(1)
    }
}
