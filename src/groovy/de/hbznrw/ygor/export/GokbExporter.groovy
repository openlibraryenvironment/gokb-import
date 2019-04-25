package de.hbznrw.ygor.export

import com.fasterxml.jackson.databind.node.ArrayNode
import de.hbznrw.ygor.format.GokbFormatter
import de.hbznrw.ygor.tools.JsonToolkit
import ygor.Enrichment
import ygor.Record

class GokbExporter {

    static GokbFormatter formatter = new GokbFormatter()

    static void extractTitles(Enrichment enrichment) {
        def titles = []
        for (def record in enrichment.dataContainer.records){
            titles << JsonToolkit.getTitleJsonFromRecord("gokb", record, formatter)
        }
        enrichment.dataContainer.titles = titles
    }


    static void extractTipps(Enrichment enrichment) {
        def tipps = []
        for (def record in enrichment.dataContainer.records){
            tipps << JsonToolkit.getTippJsonFromRecord("gokb", record, formatter)
        }
        enrichment.dataContainer.tipps = tipps
    }


    static void removeInvalidFields(Enrichment enrichment){
        for (def title in enrichment.dataContainer.titles){
            removeEmptyIdentifiers(title.identifiers)
        }
        for (def tipp in enrichment.dataContainer.tipps){
            removeEmptyIdentifiers(tipp.title.identifiers)
        }
    }


    static private void removeEmptyIdentifiers(ArrayNode identifiers){
        def iter = identifiers.iterator()
        def count = 0
        def idsToBeRemoved = []
        while (iter.hasNext()){
            if (iter.next().value.toString() == "\"\""){
                idsToBeRemoved << count
            }
            count++
        }
        for (int i=idsToBeRemoved.size()-1; i>-1; i--){
            identifiers.remove(idsToBeRemoved[i])
        }
    }

}
