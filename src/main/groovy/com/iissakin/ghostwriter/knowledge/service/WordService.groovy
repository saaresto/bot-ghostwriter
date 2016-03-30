package com.iissakin.ghostwriter.knowledge.service

import com.iissakin.ghostwriter.knowledge.entity.WordEntity
import com.iissakin.ghostwriter.knowledge.util.Follows
import com.iissakin.ghostwriter.knowledge.util.Word
import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Edge
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import groovy.util.logging.Slf4j
import org.apache.commons.codec.language.Metaphone
import org.springframework.stereotype.Component

import java.util.regex.Matcher
import java.util.regex.Pattern
/**
 * User: iissakin
 * Date: 27.02.2016.
 */
@Component
@Slf4j
class WordService extends GraphTransactionalService {

    static Set<WordEntity> cachedVertices = new HashSet<>()

    /*@PostConstruct
    def initializeCache() {
        // TODO investigate necessity of such kind of caching
        log.info("Cache initialization started.")
        def start = System.currentTimeMillis()
        withTransaction { OrientGraph graph ->
            def wordVertices = graph.getVerticesOfClass(Word.CLASS)
            cachedVertices = wordVertices.collect({ new WordEntity(it.properties) })

            wordVertices.each { Vertex word ->
                WordEntity entity = cachedVertices.find({it.content == word.properties[Word.CONTENT]}) ?: new WordEntity(word.properties)
                if (!entity.followers) entity.followers = []

                def followers = word.getEdges(Direction.IN, Follows.CLASS).collect({it.getVertex(Direction.OUT)})
                followers.each { Vertex follower ->
                    entity.followers << cachedVertices.find({it.content == follower.properties[Word.CONTENT]}) ?: new WordEntity(follower.properties)
                }

                cachedVertices << entity
                log.info("Cache size: ${cachedVertices.size()}")
            }

            graph.getEdgesOfClass(Follows.CLASS).each { edge ->
                def baseWord = cachedVertices.find{ word -> word.content == edge.getVertex(Direction.IN).properties[Word.CONTENT]}
                def follower = cachedVertices.find{ word -> word.content == edge.getVertex(Direction.OUT).properties[Word.CONTENT]}

                if (baseWord && follower) {
                    if (!baseWord.followers) baseWord.followers = []
                    baseWord.followers << follower
                }
            }
        }
        log.info("Cache initialization finished in ${(System.currentTimeMillis() - start) / 1000}s")
    }*/

    def newRelations(relations) {
        withTransaction { OrientGraph graph ->
            relations.each { relation ->
                try {
                    newRelation(graph, relation.follower, relation.word, relation.props)
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }

    def newRelation(OrientGraph graph, String follower, String word, Map props) {
        def metaphone = new Metaphone()
        def vertices = graph.getVerticesOfClass("Word")
        Vertex followerVertex = vertices.find {
            it.properties[Word.CONTENT] == follower
        }
        Vertex wordVertex = vertices.find {
            it.properties[Word.CONTENT] == word
        }

        if (!followerVertex) {
            followerVertex = graph.addVertex("class:${Word.CLASS}".toString(),
                    Word.CONTENT, follower,
                    Word.METAPHONE, metaphone.metaphone(follower),
                    Word.VOWEL_COUNT, countVowelsRegex(follower))
        }
        if (!wordVertex) {
            wordVertex = graph.addVertex("class:${Word.CLASS}".toString(),
                    Word.CONTENT, word,
                    Word.METAPHONE, metaphone.metaphone(word),
                    Word.VOWEL_COUNT, countVowelsRegex(follower))
        }

        Edge follows = wordVertex.getEdges(Direction.IN, Follows.CLASS).find({it.getVertex(Direction.OUT).properties[Word.CONTENT] == follower}) as Edge
        if (!follows) follows = followerVertex.addEdge(Follows.CLASS, wordVertex)
        if (follows.properties[Follows.COUNT] == null)
            follows.setProperty Follows.COUNT, 1
        else
            follows.setProperty Follows.COUNT, follows.properties[Follows.COUNT] + 1


        // get the artist
        if (props.artist) {
            Vertex v
            def artists = graph.getVerticesOfClass("Author")
            if (!artists || !artists.find({it.properties.name == props.artist})) {
                v = graph.addVertex("class:Author", "name", props.artist)
            } else {
                v = artists.find({it.properties.name == props.artist}) as Vertex
            }

            Set<Vertex> set
            if (follows.properties.artists) {
                set = follows.properties.artists
            } else {
                set = new HashSet<>()
            }
            set.add(v)
            follows.setProperty("artists", set)
        }
    }

    int countVowelsRegex(String str) {
        int count = 0;

        if (str.length() > 0) {
            // Create a pattern that detects vowels.
            Pattern vowelPattern = Pattern.compile("[aeiou]");
            Matcher vowelMatcher = vowelPattern.matcher(str);

            // Look for the next match and if found, add to count and repeat.
            while (vowelMatcher.find())
                count++;
        }

        return count;
    }

    def getWords() {
        withTransaction { OrientGraph graph ->
            def words = []
            def vertices = graph.getVerticesOfClass(Word.CLASS)
            vertices.each { word ->
                def jsonWord = [:]
                jsonWord.rid = word.id.toString()
                jsonWord.content = word.properties[Word.CONTENT]

                word.getEdges(Direction.IN, Follows.CLASS).each { edge ->
                    if (!jsonWord.followsIn) jsonWord.followsIn = []
                    jsonWord.followsIn << [word: edge.getVertex(Direction.OUT).properties[Word.CONTENT], count: edge.properties[Follows.COUNT]]
                }
                word.getEdges(Direction.OUT, Follows.CLASS).each { edge ->
                    if (!jsonWord.followsOut) jsonWord.followsOut = []
                    jsonWord.followsOut << [word: edge.getVertex(Direction.IN).properties[Word.CONTENT], count: edge.properties[Follows.COUNT]]
                }

                words << jsonWord
            }

            [words: words]
        }
    }
}
