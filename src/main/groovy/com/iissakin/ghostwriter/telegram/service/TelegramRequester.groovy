package com.iissakin.ghostwriter.telegram.service
import com.iissakin.ghostwriter.knowledge.service.MetadataService
import com.iissakin.ghostwriter.knowledge.service.TextWriter
import com.iissakin.ghostwriter.telegram.api.object.Update
import groovy.util.logging.Slf4j
import groovyx.net.http.HTTPBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
/**
 * User: iissakin
 * Date: 28.02.2016.
 */
@Slf4j
@Component
class TelegramRequester {

    @Autowired
    MetadataService metadataService

    @Autowired
    HTTPBuilder http

    @Autowired
    TextWriter writer

    @Value('${telegram.api.key}')
    private final String API_KEY
    private final String BASE_URL = 'https://api.telegram.org/bot'

    def getAndProcessUpdates() {
        def offset = metadataService.getLastUpdateId()
        http.get(uri: BASE_URL + API_KEY + '/getUpdates', query: [offset: offset]) { resp, json ->
            List<Update> updates = []
            if (json.ok) {
                json.result.each { update ->
                    log.info("${update}")
                    updates << new Update(update as HashMap)
                }
            }

            if (updates) {
                metadataService.setLastUpdate(updates.collect({it.updateId}).max())
                processUpdates(updates)
            }
        }
    }

    def processUpdates(List<Update> updates) {
        updates.each { update ->
            def text = writer.generate()
            http.post(uri: BASE_URL + API_KEY + '/sendMessage',
                    body: [chat_id: update.message.chat.id,
                           text: text,
                           reply_to_message_id: update.message.messageId]) { resp ->
                println "POST Success: ${resp.statusLine}"
            }
        }
    }
}
