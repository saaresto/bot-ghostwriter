package com.iissakin.ghostwriter.knowledge.service

import com.tinkerpop.blueprints.impls.orient.OrientGraph
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import org.springframework.beans.factory.annotation.Autowired

/**
 * User: iissakin
 * Date: 27.02.2016.
 */
abstract class GraphTransactionalService {

    @Autowired
    OrientGraphFactory orientGraphFactory

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
            orientGraph.shutdown(false)
        }
    }

    private OrientGraph getGraph() {
        ((OrientGraph) OrientGraph.activeGraph) ?: orientGraphFactory.getTx()
    }
}
