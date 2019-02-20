package de.hbznrw.ygor.bridges

import de.hbznrw.ygor.export.DataMapper
import de.hbznrw.ygor.processing.MultipleProcessingThread
import groovy.util.logging.Log4j
import de.hbznrw.ygor.connectors.*
import de.hbznrw.ygor.enums.*
import de.hbznrw.ygor.interfaces.*
import de.hbznrw.ygor.export.structure.TitleStruct
import org.codehaus.groovy.runtime.DefaultGroovyMethods

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
		this.master    = master
		this.options   = options
		this.processor = master.processor
		this.connector  = new DnbSruPicaConnector(this)
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
	void processStash() throws Exception {
		log.info("processStash()")

		def stash = processor.getStash()
		log.info("stash: " + stash)

		MultipleProcessingThread.KEY_ORDER.each { keyType ->
			stash.get(keyType).each { uid, key ->

				if (!master.isRunning) {
					log.info('Aborted by user action.')
					return
				}

				increaseProgress()

				def pollStatus = connector.poll(key, stash.getKeyType(uid),
						DefaultGroovyMethods.getAt(stash.values.kbart.get(uid), "${'publication_title'}"))

				// fallback for empty api response
				if (pollStatus == AbstractEnvelope.STATUS_NO_RESPONSE) {
					log.info("AbstractEnvelope.STATUS_NO_RESPONSE @ " + key)
					processor.processEntry(master.enrichment.dataContainer, uid, key, null)
				}

				connector.getPicaRecords().eachWithIndex { pr, i ->
					processor.processEntry(master.enrichment.dataContainer, uid, key, pr)
				}
			}
		}
	}

	@Override
	void finish() throws Exception {
		log.info("finish()")

		def stash  = processor.getStash()
		def orgMap = DataMapper.getOrganisationMap()

		master.enrichment.dataContainer.titles.each { key, value ->
			DataMapper.mapHistoryEvents(master.enrichment.dataContainer, value.v, stash)
			DataMapper.mapOrganisations(orgMap, value.v)
		}
		master.enrichment.dataContainer.pkg.tipps.each { key, value ->
			DataMapper.mapPlatform(value.v, master.enrichment.dataContainer)
		}
	}
}
