package com.iissakin.ghostwriter.knowledge.service

import com.iissakin.ghostwriter.knowledge.util.Follows
import com.iissakin.ghostwriter.knowledge.util.Word
import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component
/**
 * User: iissakin
 * Date: 13.03.2016.
 */
@Slf4j
@Component
class TextWriter extends GraphTransactionalService {

    Iterable<Vertex> vertices

    def generate() {
        if (!vertices) vertices = graph.getVerticesOfClass(Word.CLASS)
        def start = System.currentTimeMillis()
        def lines = withTransaction { OrientGraph graph ->
                        getLinesWithAbAbRhyme()
                    }

        log.info("Time spent on generating text response: ${System.currentTimeMillis() - start}")
        return lines.join("\n")
    }

    List<String> getLinesWithAbAbRhyme() {
        def lines = []
        Vertex start = vertices[new Random().nextInt(vertices.size())]
        Vertex aVertex
        Vertex bVertex

        for (int lineNumber = 0; lineNumber < 12; lineNumber++) {
            def line
            if (lineNumber > 4 && lineNumber - 1 % 4 == 0) {
                aVertex = bVertex = null
            }

            if (lineNumber % 2 == 0) { // a
                line = getLine(start, aVertex)
                aVertex = line[-1]
            } else { // b
                line = getLine(start, bVertex)
                bVertex = line[-1]
            }

            start = getNextWord(start)

            lines << line.collect({it.properties[Word.CONTENT]}).join(" ")
        }

        lines
    }

    Vertex getNextWord(Vertex word) {
        def followers = word.getEdges(Direction.IN, Follows.CLASS).collect({it.getVertex(Direction.OUT)})
        followers[new Random().nextInt(followers.size())]
    }

    List<Vertex> getLine(Vertex start, Vertex rhymeOn) {
        def line = []
        Vertex current = start

        line << start
        for (int i = 0; i < 3; i++) {
            current = getNextWord(current)
            line << current
        }
        // get the last word
        if (rhymeOn) {
            def followers = current.getEdges(Direction.IN, Follows.CLASS).collect({it.getVertex(Direction.OUT)})
            current = findByEqualLastLetters(followers, rhymeOn)
            line << current
        } else {
            current = getNextWord(current)
            line << current
        }

        line
    }

    Vertex findByEqualLastLetters(Iterable<Vertex> words, Vertex word) {
        findByEqualLastLetters(words, word, (word.properties[Word.CONTENT] as String).length())
    }

    Vertex findByEqualLastLetters(Iterable<Vertex> words, Vertex word, int amountOfLetters) {
        if (amountOfLetters > 4) amountOfLetters = 4 // default by now

        log.info("Trying to find a rhyme by ${amountOfLetters} letters for: ${word.properties.content}")
        if (amountOfLetters == 1) {
            log.info("Couldn't find rhyme by last letters for word ${word.properties[Word.CONTENT]}")
            return findByEqualLastLetters(vertices, word, (word.properties[Word.CONTENT] as String).length())
        }

        def rhymingWords = words.findAll({
            def content = it.properties[Word.CONTENT] as String
            def wordContent = word.properties[Word.CONTENT] as String
            return !(content.length() < amountOfLetters) && (content[-amountOfLetters..-1] == wordContent[-amountOfLetters..-1])
        })

        if (!rhymingWords) {
            return findByEqualLastLetters(words, word, amountOfLetters - 1)
        } else {
            def rhyme = rhymingWords[new Random().nextInt(rhymingWords.size())]
            log.info("Found: ${rhyme.properties.content}")
            return rhyme
        }
    }
}
