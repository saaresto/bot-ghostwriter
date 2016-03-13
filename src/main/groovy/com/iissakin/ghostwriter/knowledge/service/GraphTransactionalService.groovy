package com.iissakin.ghostwriter.knowledge.service

import com.tinkerpop.blueprints.impls.orient.OrientGraph
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired

/**
 * User: iissakin
 * Date: 27.02.2016.
 */
@Slf4j
abstract class GraphTransactionalService {

    @Autowired
    OrientGraphFactory orientGraphFactory

    synchronized OrientGraph graph

    protected <T> T withTransaction(@ClosureParams(value = FromString, options = 'com.tinkerpop.blueprints.impls.orient.OrientGraph') Closure<T> closure) {
        OrientGraph orientGraph = getGraph()
        try {
            orientGraph.begin()
            def result = closure.call(orientGraph)
            orientGraph.commit()
            return result
        } catch (Exception e) {
            orientGraph.rollback()
            throw e
        } finally {
            log.debug("Shutting down OrientGraph for thread ${Thread.currentThread().getId()}")
            orientGraph.shutdown(false)
        }
    }

    protected <T> T withTransactionNoShutdown(@ClosureParams(value = FromString, options = 'com.tinkerpop.blueprints.impls.orient.OrientGraph') Closure<T> closure) {
        OrientGraph orientGraph = getGraph()
        try {
            orientGraph.begin()
            def result = closure.call(orientGraph)
            orientGraph.commit()
            return result
        } catch (Exception e) {
            orientGraph.rollback()
            throw e
        }
    }

    protected OrientGraph getGraph() {
        if (!graph) {
            if (OrientGraph.activeGraph) {
                graph = (OrientGraph) OrientGraph.activeGraph
            } else {
                log.debug("Creating new OrientGraph for thread ${Thread.currentThread().getId()}")
                graph =  orientGraphFactory.getTx()
            }
        }
        return graph
    }
}
