package de.hbznrw.ygor.interfaces

import groovy.util.logging.Log4j

/**
 * Abstract class for processing input and output files
 *
 * @author David Klober
 *
 */

@Log4j
abstract class AbstractProcessor implements ProcessorInterface {

  protected BridgeInterface bridge

  //

  AbstractProcessor(BridgeInterface bridge) {
    this.bridge = bridge
  }

  AbstractProcessor() {
  }
}
