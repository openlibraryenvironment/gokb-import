package de.hbznrw.ygor.export

import de.hbznrw.ygor.tools.JsonToolkit
import ygor.Enrichment

class GokbExporter {

    void extractTitles(Enrichment enrichment) {
        def titles = []
        for (def record in enrichment.dataContainer.records){
            titles << JsonToolkit.getTitleJsonFromRecord("gokb", record)
        }
        enrichment.dataContainer.titles = titles
    }


    void extractTipps(Enrichment enrichment) {
        def tipps = []
        for (def record in enrichment.dataContainer.records){
            tipps << JsonToolkit.getTippJsonFromRecord("gokb", record)
        }
        enrichment.dataContainer.tipps = tipps
    }

}
