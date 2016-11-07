package de.hbznrw.ygor.iet.interfaces

import de.hbznrw.ygor.iet.enums.Query;

abstract class BridgeAbstract implements BridgeInterface {

	public Query query
	public ConnectorInterface connector
	public FormatAdapterInterface formatAdapter
	public ProcessorInterface processor
	
	void go() throws Exception {
	}
	
	void go(String outputFile) throws Exception {
	}
	
	int getProgress() {
		return -1
	}
}
