package de.hbznrw.ygor.export

import de.hbznrw.ygor.format.GokbFormatter
import de.hbznrw.ygor.tools.JsonToolkit
import ygor.Enrichment

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

}
