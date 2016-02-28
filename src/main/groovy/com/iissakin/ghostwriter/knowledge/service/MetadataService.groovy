package com.iissakin.ghostwriter.knowledge.service

import com.iissakin.ghostwriter.knowledge.util.UpdatesTracker
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import org.springframework.stereotype.Component

/**
 * User: iissakin
 * Date: 28.02.2016.
 */
@Component
class MetadataService extends GraphTransactionalService{

    Integer getLastUpdateId() {
        withTransaction { OrientGraph graph ->
            return graph.getVerticesOfClass(UpdatesTracker.CLASS)[0].properties[UpdatesTracker.LAST] as Integer
        }
    }

    Integer setLastUpdate(Integer id) {
        withTransaction { OrientGraph graph ->
            Vertex updater = graph.getVerticesOfClass(UpdatesTracker.CLASS)[0]
            updater.setProperty(UpdatesTracker.LAST, id + 1)
        }
    }
}
