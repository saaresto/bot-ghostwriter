package com.iissakin.ghostwriter.telegram.service

import com.iissakin.ghostwriter.telegram.api.object.Update
import groovyx.net.http.HTTPBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
/**
 * User: iissakin
 * Date: 28.02.2016.
 */
@Component
class TelegramRequester {

    @Value('${telegram.api.key}')
    private final String API_KEY
    private final String BASE_URL = 'https://api.telegram.org/bot'

    def getUpdates(Integer offset) {
        def http = new HTTPBuilder()

        http.get(uri: BASE_URL + API_KEY + '/getUpdates', query: [offset: offset]) { resp, json ->
            def updates = []
            if (json.ok) {
                json.result.each { update ->
                    updates << new Update(update as HashMap)
                }
            }

            println updates
        }
    }
}
