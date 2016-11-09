package de.hbznrw.ygor.iet.interfaces

import java.util.ArrayList
import org.apache.commons.csv.CSVRecord
import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.interfaces.FormatAdapterInterface

/**
 * Abstract class for processing input and output files
 *
 * @author David Klober
 *
 */
abstract class ProcessorAbstract implements ProcessorInterface {

	protected BridgeInterface bridge
	
	//

	ProcessorAbstract(BridgeInterface bridge) {
		this.bridge = bridge
	}
		
	ArrayList processRecord(CSVRecord record, int indexOfKey, int count) {
		return new ArrayList()
	}
}
