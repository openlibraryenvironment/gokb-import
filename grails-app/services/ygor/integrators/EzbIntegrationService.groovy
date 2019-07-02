package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import org.apache.commons.lang.StringUtils
import ygor.Record
import ygor.field.MappingsContainer
import ygor.identifier.AbstractIdentifier

import java.text.SimpleDateFormat

class EzbIntegrationService extends ExternalIntegrationService{

    EzbIntegrationService(MappingsContainer mappingsContainer){
        super(mappingsContainer)
    }


    def integrate(MultipleProcessingThread owner, DataContainer dataContainer) {
        String processStart = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS").format(new Date())
        for (Record record in dataContainer.records){
            if (isApiCallMedium(record)){
                record.ezbIntegrationDate = processStart
                Map<String, String> ezbMatch = getBestExistingMatch(owner, record)
                if (!ezbMatch.isEmpty()) {
                    integrateWithExisting(record, ezbMatch, mappingsContainer, MappingsContainer.EZB)
                }
            }
            owner.increaseProgress()
        }
    }


    private Map<String, String> getBestExistingMatch(MultipleProcessingThread owner, Record record){
        List<Map<String, String>> readData = new ArrayList<>()
        for (String key in owner.KEY_ORDER) {
            AbstractIdentifier id = record."${key}"
            if (id && !StringUtils.isEmpty(id.identifier)){
                readData = owner.ezbReader.readItemData(owner.zdbKeyMapping, id.identifier)
                if (!readData.isEmpty()){
                    break
                }
            }
        }
        return getBestMatchingData(owner, record, readData, 0, MappingsContainer.EZB, "ezbKeys")
    }
}
