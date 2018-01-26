package edu.colorado.oit.test.spock.camel

import groovy.util.logging.Slf4j
import org.apache.camel.Endpoint
import org.apache.camel.ProducerTemplate
import org.apache.camel.RoutesBuilder
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.component.seda.SedaComponent
import org.apache.camel.component.seda.SedaEndpoint
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.model.ModelCamelContext
import org.apache.camel.util.StopWatch
import org.apache.camel.util.TimeUtils
import spock.lang.Specification

@Slf4j("logger")
class CamelSpecSupport extends Specification {

    ModelCamelContext context = new DefaultCamelContext()
    ProducerTemplate template
    StopWatch watch = new StopWatch()

    def setup() {
        doSetUp()
        context.start()
    }

    def cleanup() {
        doCleanup()
    }

    RoutesBuilder[] createRouteBuilders() {
        return []
    }

    MockEndpoint getMockEndpoint(String uri) {
        return context.hasEndpoint(uri)
    }

    protected void doSetUp() throws Exception {
        logger.info("********************************************************************************")
        logger.info("Testing: " + specificationContext.currentIteration.name + "(" + this.class.name + ")")
        logger.info("********************************************************************************")
        logger.debug("setUp test")

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
        template = context.createProducerTemplate()

        createRouteBuilders().each {
            context.addRoutes(it)
        }

        // only start timing after all the setup
        watch.restart()
    }

    protected void doCleanup() throws Exception {
        template.stop()
        long time = watch.taken()

        logger.info("********************************************************************************")
        logger.info("Testing done: " + specificationContext.currentIteration.name + "(" + this.class.name + ")")
        logger.info("Took: " + TimeUtils.printDuration(time) + " (" + time + " millis)")
        logger.info("********************************************************************************")

        logger.debug("tearDown test")
        context.stop()
    }
}
