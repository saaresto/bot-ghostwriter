package com.iissakin.ghostwriter.knowledge.service

import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Edge
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * User: iissakin
 * Date: 27.02.2016.
 */
@Component
class WordService extends GraphTransactionalService {

    @Autowired
    OrientGraphFactory orientGraphFactory

    def newWord(String content) {
        withTransaction { OrientGraph graph ->
            Vertex word = graph.addVertex("class:Word")
            word.setProperty("content", content)

            "done"
        }
    }

    def newRelation(String follower, String word) {
        withTransaction { OrientGraph graph ->
            def vertices = graph.getVerticesOfClass("Word")
            Vertex followerVertex = vertices.find {
                it.properties.content == follower
            }
            Vertex wordVertex = vertices.find {
                it.properties.content == word
            }

            if (!followerVertex) {
                followerVertex = graph.addVertex("class:Word", "content", follower)
            }
            if (!wordVertex) {
                wordVertex = graph.addVertex("class:Word", "content", word)
            }

            Edge follows = graph.addEdge("class:Follows", followerVertex, wordVertex, "follows")

            "done"
        }
    }

    def getWords() {
        withTransaction { OrientGraph graph ->
            def words = []
            def vertices = graph.getVerticesOfClass("Word")
            vertices.each { word ->
                def jsonWord = [:]
                jsonWord.rid = word.id.toString()
                jsonWord.content = word.properties.content

                word.getEdges(Direction.IN, "Follows").each { edge ->
                    if (!jsonWord.followsIn) jsonWord.followsIn = []
                    jsonWord.followsIn << edge.getVertex(Direction.OUT).properties.content
                }
                word.getEdges(Direction.OUT, "Follows").each { edge ->
                    if (!jsonWord.followsOut) jsonWord.followsOut = []
                    jsonWord.followsOut << edge.getVertex(Direction.IN).properties.content
                }

                words << jsonWord
            }

            words
        }
    }
}
