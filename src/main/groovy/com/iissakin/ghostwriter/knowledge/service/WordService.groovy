package com.iissakin.ghostwriter.knowledge.service
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

            Edge follows = wordVertex.getEdges(Direction.IN, "follows").find({it.getVertex(Direction.OUT).properties.content == follower}) as Edge
            if (!follows) follows = wordVertex.addEdge("follows", followerVertex)
            if (follows.properties.count == null)
                follows.setProperty "count", 0
            else
                follows.setProperty "count", follows.properties.count + 1

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
                    jsonWord.followsIn << [word: edge.getVertex(Direction.OUT).properties.content, count: edge.properties.count]
                }
                word.getEdges(Direction.OUT, "Follows").each { edge ->
                    if (!jsonWord.followsOut) jsonWord.followsOut = []
                    jsonWord.followsOut << [word: edge.getVertex(Direction.IN).properties.content, count: edge.properties.count]
                }

                words << jsonWord
            }

            words
        }
    }
}
