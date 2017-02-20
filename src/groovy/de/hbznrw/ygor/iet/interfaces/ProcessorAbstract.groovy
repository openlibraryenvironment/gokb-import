package de.hbznrw.ygor.iet.interfaces

import groovy.util.logging.Log4j

/**
 * Abstract class for processing input and output files
 *
 * @author David Klober
 *
 */

@Log4j
abstract class ProcessorAbstract implements ProcessorInterface {

	protected BridgeInterface bridge
	
	//

	ProcessorAbstract(BridgeInterface bridge) {
		this.bridge = bridge
	}
    ProcessorAbstract()
    {   
    }
}
