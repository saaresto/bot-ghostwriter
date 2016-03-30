package com.iissakin.ghostwriter.knowledge.job
import com.iissakin.ghostwriter.knowledge.service.WordService
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
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

    @Value('${knowledge.files.scan}')
    Boolean scan

    @Scheduled(initialDelay = 5000L, fixedDelay = 3600000L)
    void scan() {
        if (!scan) return
        File root = new File(rootFolder)
        ArrayList<File> dirs = (root.listFiles() as ArrayList<File>).findAll({it.isDirectory()})
        GParsPool.withPool(4) {
            dirs.each { File dir ->
                log.info("STARTED PROCESSING ${dir.name} DIRECTORY")
                Arrays.asList(dir.listFiles()).each { file ->
                    if (file.length() > 0) {
                        processFile(file, dir.name)
                    } else {
                        file.delete()
                    }
                }
                dir.delete()
            }
        }
    }

    def processFile(File file, String artist) {
        log.info("Processing ${file.name}")
        def start = System.currentTimeMillis()
        String lastWord
        def lines = file.readLines('utf-8')
                .findAll({it.length() > 1 && !it.startsWith("[")})
                .collect({it.replaceAll("[^A-Za-z0-9-'.\\s]", "")})

        def relations = []

        lines.each { line ->
            def words = line.split(" ")

            words.each { word ->
                if (!lastWord) lastWord = word // for the very first one
                def currentWord = word

                if (currentWord.toLowerCase() == lastWord.toLowerCase()) return

                try {
                    relations << [follower: currentWord.toLowerCase(), word: lastWord.toLowerCase(), props: [artist: artist]]
                } catch (Exception e) {
                    e.printStackTrace()
                }
                lastWord = currentWord
            }
        }
        wordService.newRelations relations
        log.info("Finished processing ${file.name} with ${lines.size()} rows in ${(System.currentTimeMillis() - start) / 1000}s")
        file.delete()
    }
}
