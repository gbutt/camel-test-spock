package edu.colorado.oit.test.spock.camel

import groovy.util.logging.Slf4j
import org.apache.camel.Endpoint
import org.apache.camel.component.seda.SedaComponent
import org.apache.camel.component.seda.SedaEndpoint
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.model.ModelCamelContext
import org.apache.camel.util.StopWatch
import org.apache.camel.util.TimeUtils
import spock.lang.Specification

@Slf4j
class CamelSpecSupport extends Specification {
    ModelCamelContext context = new DefaultCamelContext()

    StopWatch watch = new StopWatch()

    protected void doSetUp() throws Exception {
        log.info("********************************************************************************")
        log.info("Testing: " + specificationContext.currentIteration.name + "(" + this.class.name + ")")
        log.info("********************************************************************************")
        log.debug("setUp test")

        context = new DefaultCamelContext()

        assert context != null, "No context found!"

        // reduce default shutdown timeout to avoid waiting for 300 seconds
        context.shutdownStrategy.timeout = 10

        // speeds up unit testing for seda
        context.addComponent("seda", new SedaComponent() {
            @Override
            protected void afterConfiguration(String uri, String remaining, Endpoint endpoint, Map<String, Object> parameters) throws Exception {
                super.afterConfiguration(uri, remaining, endpoint, parameters)
                ((SedaEndpoint)endpoint).pollTimeout = 1
            }
        })

        // only start timing after all the setup
        watch.restart()
    }


    def cleanup() throws Exception {
        long time = watch.taken()

        log.info("********************************************************************************")
        log.info("Testing done: " + specificationContext.currentIteration.name + "(" + this.class.name + ")")
        log.info("Took: " + TimeUtils.printDuration(time) + " (" + time + " millis)")
        log.info("********************************************************************************")

        log.debug("tearDown test")
        context.stop()
    }
}
