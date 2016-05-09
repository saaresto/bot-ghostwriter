package com.iissakin.ghostwriter

import com.iissakin.ghostwriter.knowledge.service.TextWriter
import com.iissakin.ghostwriter.knowledge.service.WordService
import com.iissakin.ghostwriter.knowledge.util.Follows
import com.iissakin.ghostwriter.knowledge.util.Word
import com.tinkerpop.blueprints.Edge
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
/**
 * This is complete BS though
 */
@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = GhostwriterApplication)
@WebAppConfiguration
class GhostwriterApplicationTests {

	@Autowired
	OrientGraphFactory orientGraphFactory

	@Autowired
	TextWriter writer

	@Autowired
	WordService service

	def "generated text exists"() {
		setup: def vertices = orientGraphFactory.getTx().getVerticesOfClass(Word.CLASS)

		when: def text = writer.generate()

		expect: text != null && text.length() > 0
	}

	def "vertices and edges are created"() {
		setup:
			Vertex followerVertex = orientGraphFactory.getTx().addVertex("class:${Word.CLASS}".toString(),
					Word.CONTENT, "follower")

			Vertex wordVertex = orientGraphFactory.getTx().addVertex("class:${Word.CLASS}".toString(),
					Word.CONTENT, word)
		when:
			Edge follows = followerVertex.addEdge(Follows.CLASS, wordVertex)

		expect:
			(orientGraphFactory.getTx().vertices.find { it.properties.content == wordVertex.properties.content } != null
			&& orientGraphFactory.getTx().vertices.find { it.properties.content == followerVertex.properties.content } != null)
	}
}
