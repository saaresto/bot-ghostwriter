package com.iissakin.ghostwriter.knowledge.service

import com.iissakin.ghostwriter.knowledge.util.Follows
import com.iissakin.ghostwriter.knowledge.util.RandomUtils
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
        Vertex start = vertices[RandomUtils.safeRandomIndex(vertices)]
        Vertex aVertex
        Vertex bVertex

        for (int lineNumber = 0; lineNumber < 12; lineNumber++) {
            def line
            if (lineNumber > 3 && lineNumber % 4 == 0) {
                aVertex = bVertex = null
            }

            if (lineNumber % 2 == 0) { // a
                line = getLineBasedOnVowels(start, aVertex)
//                line = getLineBasedOnMetaphone(start, aVertex)
                aVertex = line[-1]
            } else { // b
                line = getLineBasedOnVowels(start, bVertex)
//                line = getLineBasedOnMetaphone(start, bVertex)
                bVertex = line[-1]
            }

            start = getNextWord(start)

            lines << line.collect({it.properties[Word.CONTENT]}).join(" ")
        }

        lines
    }

    Vertex getNextWord(Vertex word) {
        def followers = word.getEdges(Direction.IN, Follows.CLASS).collect({new WordCountPair(count: it.properties[Follows.COUNT], word:it.getVertex(Direction.OUT))})
        getNextWord(followers)
    }

    Vertex getNextWord(Iterable<WordCountPair> words) {
        // distribute
        def rangeMap = [:]
        long cursor = 1L
        words.each { pair ->
            def range = cursor..(cursor + pair.count - 1)
            rangeMap[range] = pair.word
            cursor += pair.count
        }
        def wholeCount = words*.count.sum() as Long
        def randomIndex = new Random().nextInt(wholeCount as int) + 1

        def asd = rangeMap.find {randomIndex in it.key}
        return asd.value as Vertex
    }

    List<Vertex> getLineBasedOnVowels(Vertex start, Vertex rhymeOn) {
        def vowels = 10 // default

        def line = []
        Vertex current = start

        line << start
        vowels -= start.properties[Word.VOWEL_COUNT]
        while (vowels > 3) {
            current = getNextWord(current)
            line << current
            vowels -= current.properties[Word.VOWEL_COUNT]
        }
        // get the last word
        if (rhymeOn) {
            def followers = current.getEdges(Direction.IN, Follows.CLASS).collect({new WordCountPair(count: it.properties[Follows.COUNT], word:it.getVertex(Direction.OUT))})
            current = findByEqualLastLetters(followers, rhymeOn)
            line << current
        } else {
            current = getNextWord(current)
            while ((current.properties[Word.CONTENT] as String).indexOf("'") != -1 || (current.properties[Word.CONTENT] as String).length() < 2) {
                current = getNextWord(current)
            }
            line << current
        }

        line
    }

    Vertex findByEqualLastLetters(Iterable<WordCountPair> words, Vertex word) {
        findByEqualLastLetters(words, word, (word.properties[Word.CONTENT] as String).length())
    }

    Vertex findByEqualLastLetters(Iterable<WordCountPair> words, Vertex word, int amountOfLetters) {
        if (amountOfLetters > 4) amountOfLetters = 4 // default by now

        log.info("Trying to find a rhyme by ${amountOfLetters} letters for: ${word.properties.content}")
        try {
            if (amountOfLetters == 1) {
                log.info("Couldn't find rhyme by last letters for word ${word.properties[Word.CONTENT]}")
                return findByEqualLastLetters(vertices.collect({new WordCountPair(count: 1L, word:it)}), word, (word.properties[Word.CONTENT] as String).length())
            }
        } catch (StackOverflowError sofe) {
            sofe.printStackTrace()
            return getNextWord(words)
        }

        def rhymingWords = words.findAll({
            def content = it.word.properties[Word.CONTENT] as String
            def wordContent = word.properties[Word.CONTENT] as String
            return !(content.length() < amountOfLetters) && (content[-amountOfLetters..-1] == wordContent[-amountOfLetters..-1])
        })

        if (!rhymingWords) {
            return findByEqualLastLetters(words, word, amountOfLetters - 1)
        } else {
            def rhyme = getNextWord(rhymingWords)
            log.info("Found: ${rhyme.properties.content}")
            return rhyme
        }
    }

    /*
    METAPHONE STUFF BELOW
     */

    List<Vertex> getLineBasedOnMetaphone(Vertex start, Vertex rhymeOn) {
        def metaphoneLength = 20 // default

        def line = []
        Vertex current = start

        line << start
        metaphoneLength -= (start.properties[Word.METAPHONE] as String).length()
        while (metaphoneLength > 3) {
            current = getNextWord(current)
            line << current
            metaphoneLength -= (current.properties[Word.METAPHONE] as String).length()
        }
        // get the last word
        if (rhymeOn) {
            def followers = current.getEdges(Direction.IN, Follows.CLASS).collect({it.getVertex(Direction.OUT)})
            current = findByMetaphone(followers, rhymeOn)
            line << current
        } else {
            current = getNextWord(current)
            while ((current.properties[Word.CONTENT] as String).indexOf("'") != -1
                    && (current.properties[Word.CONTENT] as String).length() < 2) {
                current = getNextWord(current)
            }
            line << current
        }

        line
    }

    Vertex findByMetaphone(Iterable<Vertex> words, Vertex word) {
        def wordMetaphone = word.properties[Word.METAPHONE] as String
        log.info("Searching for rhyme for metaphone: ${wordMetaphone}")

        def rhyme = words.find({ candidate ->
            def candidateMetaphone = candidate.properties[Word.METAPHONE] as String
            boolean threeEnd = (wordMetaphone.length() >= 3 && candidateMetaphone.length() >= 3
                    && (wordMetaphone[-3..-1] == candidateMetaphone[-3..-1]))
            boolean twoEnd = (wordMetaphone.length() >= 2 && candidateMetaphone.length() >= 2
                    && (wordMetaphone[-2..-1] == candidateMetaphone[-2..-1]))
            return (wordMetaphone.endsWith(candidateMetaphone)
                    || candidateMetaphone.endsWith(wordMetaphone)
                    || threeEnd
                    || twoEnd)
        }) as Vertex
        log.info("Found: ${rhyme?.properties?.metaphone}")
        return rhyme ?: getNextWord(word)
    }

    private class WordCountPair {
        Long count
        Vertex word
    }
}
