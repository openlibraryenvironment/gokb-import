package de.hbznrw.ygor.bridges

import groovy.util.logging.Log4j
import de.hbznrw.ygor.connectors.*
import de.hbznrw.ygor.enums.*
import de.hbznrw.ygor.interfaces.*

@Log4j
class ZdbBridge extends AbstractBridge implements BridgeInterface {

  static final IDENTIFIER = 'zdb'

  Query[] tasks = [
      Query.ZDBID,
      Query.ZDB_GVKPPN,
      Query.ZDB_PISSN,
      Query.ZDB_EISSN,
      Query.ZDB_TITLE,
      Query.ZDB_PUBLISHER,
      Query.ZDB_PUBLISHED_FROM,
      Query.ZDB_PUBLISHED_TO,
      Query.ZDB_HISTORY_EVENTS
  ]

  ZdbBridge(Thread master, HashMap options) {
    this.master = master
    this.options = options
    this.processor = master.processor
    this.connector = new DnbSruPicaConnector(this)
  }

  @Override
  void go() throws Exception {
    log.info("Input:  " + options.get('inputFile'))
    master.enrichment.dataContainer.info.api << connector.getAPIQuery('<identifier>', null)
    processor.setBridge(this)
    processor.processFile(options)
  }


  @Override
  void go(String outputFile) throws Exception {
    log.warn("deprecated function call go(outputFile)")
  }


  @Override
  void finish() throws Exception {
    log.info("finish()")

    // TODO ? Close any streams / unbind any resources?
  }
}
