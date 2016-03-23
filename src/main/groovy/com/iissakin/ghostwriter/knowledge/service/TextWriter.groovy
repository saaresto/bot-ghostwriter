package com.iissakin.ghostwriter.knowledge.service

import com.iissakin.ghostwriter.knowledge.util.Word
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component
/**
 * User: iissakin
 * Date: 13.03.2016.
 */
@Slf4j
@Component
class TextWriter extends GraphTransactionalService {

    def generate() {
        def start = System.currentTimeMillis()
        def lines = []
        def vertices = graph.getVerticesOfClass(Word.CLASS).findAll({ (it.properties[Word.METAPHONE] as String).endsWith("L") })
        8.times { // 4 rows
            def line = ""
            5.times {
                line += vertices.get(new Random().nextInt(vertices.size() - 1)).properties[Word.CONTENT] + " "
            }
            lines << line
        }
        log.info("Time spent on generating text response: ${System.currentTimeMillis() - start}")
        return lines.join("\n")
    }
}
