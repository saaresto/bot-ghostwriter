package com.iissakin.ghostwriter.knowledge.service

import com.iissakin.ghostwriter.knowledge.util.Follows
import com.iissakin.ghostwriter.knowledge.util.Word
import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Edge
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import org.springframework.stereotype.Component
/**
 * User: iissakin
 * Date: 27.02.2016.
 */
@Component
class WordService extends GraphTransactionalService {

    def newRelation(String follower, String word) {
        withTransaction { OrientGraph graph ->
            def vertices = graph.getVerticesOfClass("Word")
            Vertex followerVertex = vertices.find {
                it.properties[Word.CONTENT] == follower
            }
            Vertex wordVertex = vertices.find {
                it.properties[Word.CONTENT] == word
            }

            if (!followerVertex) {
                followerVertex = graph.addVertex("class:${Word.CLASS}", Word.CONTENT, follower)
            }
            if (!wordVertex) {
                wordVertex = graph.addVertex("class:${Word.CLASS}", Word.CONTENT, word)
            }

            Edge follows = wordVertex.getEdges(Direction.IN, Follows.CLASS).find({it.getVertex(Direction.OUT).properties[Word.CONTENT] == follower}) as Edge
            if (!follows) follows = wordVertex.addEdge(Follows.CLASS, followerVertex)
            if (follows.properties[Follows.COUNT] == null)
                follows.setProperty Follows.COUNT, 0
            else
                follows.setProperty Follows.COUNT, follows.properties[Follows.COUNT] + 1

            "done"
        }
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
