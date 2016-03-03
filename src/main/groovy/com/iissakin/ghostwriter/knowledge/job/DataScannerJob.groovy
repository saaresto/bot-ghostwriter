package com.iissakin.ghostwriter.knowledge.job

import com.iissakin.ghostwriter.knowledge.service.WordService
import groovy.io.FileType
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
/**
 * User: iissakin
 * Date: 29.02.2016.
 */
@Component
@Slf4j
class DataScannerJob {

    @Autowired
    WordService wordService

    @Value('${knowledge.files.folder}')
    String rootFolder

    @Scheduled(fixedRate = 5000L)
    void scan() {
        File root = new File(rootFolder)

        root.eachFile(FileType.FILES) { file ->
            if (file.length() > 0) {
                processFile(file)
                file.delete()
            }
        }
    }

    def processFile(File file) {
        log.info("Processing ${file.name}")
        String lastWord
        def lines = file.readLines('utf-8').collect({ it.replaceAll("[^A-Za-z0-9'-.\\s]", "") })
        log.info("Total of ${lines.size()} rows")

        lines.each { line ->
            def words = line.split(" ")

            words.each { word ->
                if (!lastWord) lastWord = word // for the very first one
                def currentWord = word

                if (currentWord == lastWord) return

                wordService.newRelation(currentWord.toLowerCase(), lastWord.toLowerCase())
                lastWord = currentWord
            }
        }
        log.info("Finished processing ${file.name}")
    }
}
