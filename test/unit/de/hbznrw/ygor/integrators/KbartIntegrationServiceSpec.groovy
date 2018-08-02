package de.hbznrw.ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.services.ServiceUnitTestMixin
import ygor.field.MappingsContainer
import ygor.integrators.KbartIntegrationService


/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(KbartIntegrationService)
@TestMixin(ServiceUnitTestMixin)
class KbartIntegrationServiceSpec /*extends HibernateSpec implements ServiceUnitTest<KbartIntegrationService>*/{

    String kbartFileSimple
    DataContainer data
    MappingsContainer container

    def setup() {
        kbartFileSimple = "KbartIntegrationServiceSpec.kbartFileSimple.csv"
        data = new DataContainer()
        container = new MappingsContainer()
        true
    }

    def cleanup() {
        true
    }

    void "test integrate simple"() {
        KbartIntegrationService.integrate(kbartFileSimple, data, container)
        expect:
            data.getRecordsPerEissn() == 1 // TODO this is a dummy assert
            data.getRecordsPerPissn() == 1 // TODO this is a dummy assert
            data.getRecordsPerZdbId() == 1 // TODO this is a dummy assert
    }
}
