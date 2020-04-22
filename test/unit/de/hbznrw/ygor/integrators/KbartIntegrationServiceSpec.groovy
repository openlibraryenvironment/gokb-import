package de.hbznrw.ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.readers.KbartReaderConfiguration
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
class KbartIntegrationServiceSpec /*extends HibernateSpec implements ServiceUnitTest<KbartIntegrationService>*/ {

  String kbartFileSimple
  DataContainer data
  MappingsContainer container
  KbartReaderConfiguration configuration

  def setup() {
    kbartFileSimple = "test/resources/KbartIntegrationServiceSpec.kbartFileSimple.csv"
    data = new DataContainer(new File("foo").mkdirs(), "resultHashDummy", null) // parameters to be specified
    container = new MappingsContainer()
    configuration = new KbartReaderConfiguration("comma", null, null, null)
    true
  }

  def cleanup() {
    true
  }

  void "test integrate simple"() {
    setup()
    // KbartIntegrationService.integrate(kbartFileSimple, data, container, configuration) TODO arguments
    expect:
    data.getRecordsPerEissn() == 1 // TODO this is a dummy assert
    data.getRecordsPerPissn() == 1 // TODO this is a dummy assert
    data.getRecordsPerZdbId() == 1 // TODO this is a dummy assert
  }
}
