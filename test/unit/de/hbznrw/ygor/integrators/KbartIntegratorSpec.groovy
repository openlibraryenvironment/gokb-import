package de.hbznrw.ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import grails.test.mixin.TestFor
import spock.lang.Specification
import ygor.field.MappingsContainer


/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(KbartIntegrator)
class KbartIntegratorSpec extends Specification{

    def setup() {
        String kbartFileSimple = "KbartIntegratorSpec.kbartFileSimple.csv"
        DataContainer data = new DataContainer()
        MappingsContainer container = new MappingsContainer()
        true
    }

    def cleanup() {
        true
    }

    void "test integrate simple"() {
        KbartIntegrator.integrate(kbartFileSimple, data, container)
        expect:
            data.getRecordsPerEissn() == 1 // TODO this is a dummy assert
            data.getRecordsPerPissn() == 1 // TODO this is a dummy assert
            data.getRecordsPerZdbId() == 1 // TODO this is a dummy assert
    }
}
