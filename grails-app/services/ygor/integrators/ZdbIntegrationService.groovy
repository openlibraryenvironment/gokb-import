package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import grails.transaction.Transactional
import org.slf4j.Logger
import ygor.field.MappingsContainer
import ygor.identifier.ZdbIdentifier

@Transactional
class ZdbIntegrationService {

    static def integrate(def owner, DataContainer dataContainer, MappingsContainer mappingsContainer) {
        for (ZdbIdentifier zdbId : dataContainer.recordsPerZdbId.keySet()){
            Logger.info(zdbId.id)
            Logger.info(dataContainer.recordsPerZdbId.get(zdbId).multiFields)
        }
        Logger.info(dataContainer.recordsPerPissn)
        Logger.info(dataContainer.recordsPerEissn)
        Logger.info(dataContainer.titles)
        Logger.info(dataContainer.pkg)

        Logger.info(mappingsContainer.ygorMappings)
        Logger.info(mappingsContainer.zdbMappings)
        Logger.info(mappingsContainer.ezbMappings)
    }

}
