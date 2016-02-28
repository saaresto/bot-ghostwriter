package com.iissakin.ghostwriter

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory
import groovyx.net.http.HTTPBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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
}
