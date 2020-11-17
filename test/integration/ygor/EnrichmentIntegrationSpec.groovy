package ygor

import grails.test.spock.IntegrationSpec

class EnrichmentIntegrationSpec extends IntegrationSpec {

  def enrichmentController
  Enrichment enrichment01

  def setup() {
    enrichmentController = new EnrichmentController()
    enrichment01 = Enrichment.fromFilename("./test/resources/KBart01.tsv")
  }

  void "test process"() {
    when:
      enrichmentController.enrichmentService.addSessionEnrichment(enrichment01)
      enrichmentController.process()
    then:
      1 == 1
  }

  def cleanup() {
    int j = 0
  }
}
