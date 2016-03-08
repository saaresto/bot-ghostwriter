package com.iissakin.ghostwriter
import com.iissakin.ghostwriter.knowledge.util.Artist
import com.iissakin.ghostwriter.knowledge.util.Follows
import com.iissakin.ghostwriter.knowledge.util.UpdatesTracker
import com.iissakin.ghostwriter.knowledge.util.Word
import com.orientechnologies.orient.core.metadata.schema.OClass
import com.orientechnologies.orient.core.metadata.schema.OType
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory
import groovyx.net.http.HTTPBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import javax.annotation.PostConstruct
/**
 * User: iissakin
 * Date: 27.02.2016.
 */
@Configuration
class GhostwriterConfiguration {

    @Value('${orientdb.name}')
    final String DBNAME
    @Value('${orientdb.user}')
    final String DBUSER
    @Value('${orientdb.password}')
    final String DBPASSWORD

    @Bean
    OrientGraphFactory graphFactory() {
        new OrientGraphFactory(DBNAME, DBUSER, DBPASSWORD).setupPool(1, 10)
    }

    @Bean
    HTTPBuilder httpBuilder() {
        new HTTPBuilder()
    }

    @Bean
    SchemaInitializer schemaInitializer() {
        new SchemaInitializer()
    }


    class SchemaInitializer {

        @PostConstruct
        void initialize() {
            def graph = graphFactory().getNoTx()

            if (!graph.getVertexType(Word.CLASS)) {
                def wordClass = graph.createVertexType(Word.CLASS)
                wordClass.createProperty(Word.CONTENT, OType.STRING).setMandatory(true)
                wordClass.createProperty(Word.VOWEL_COUNT, OType.SHORT).setMandatory(true)

                wordClass.createIndex("wordContentIndex", OClass.INDEX_TYPE.UNIQUE, Word.CONTENT)
            }

            if (!graph.getVertexType(Artist.CLASS)) {
                def artistClass = graph.createVertexType(Artist.CLASS)
                artistClass.createProperty(Artist.NAME, OType.STRING).setMandatory(true)
            }

            if (!graph.getVertexType(UpdatesTracker.CLASS)) {
                def trackerClass = graph.createVertexType(UpdatesTracker.CLASS)
                trackerClass.createProperty(UpdatesTracker.LAST, OType.INTEGER).setMandatory(true)

                graph.addVertex("class:${UpdatesTracker.CLASS}".toString(), UpdatesTracker.LAST, 0)
            }

            if (!graph.getEdgeType(Follows.CLASS)) {
                def followsClass = graph.createEdgeType(Follows.CLASS)
                followsClass.createProperty(Follows.COUNT, OType.LONG).setMandatory(true)
                followsClass.createProperty(Follows.ARTISTS, OType.LINKSET)
            }
        }
    }
}
