package de.hbznrw.ygor.normalizers

import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils
import ygor.Record
import ygor.field.Field
import ygor.field.MultiField

import java.util.regex.Matcher
import java.util.regex.Pattern

@Log4j
class EditionNormalizer {

  static void normalizeEditionNumber(Record record) {
    // title.monographEdition
    // title.editionDifferentiator
    // title.editionStatement
    // ==> are already normalized as default Strings

    // title.editionNumber is derived from monographEdition
    // and needs extra normalization, as it extracts a number from text
    MultiField monographEdition = record.getMultiField("monographEdition")
    MultiField editionNumber = new MultiField(null)
    // just extract the first number (i. e. integer) from the monographEdition string value
    String stringValue = monographEdition.getFirstPrioValue()
    String numberValue = ""
    if (!StringUtils.isEmpty(stringValue)){
      Matcher matcher = Pattern.compile("\\d+").matcher(stringValue)
      if (matcher.find()){
        try{
          int i = Integer.valueOf(matcher.group())
          numberValue = String.valueOf(i)
        }
        catch(NumberFormatException | IllegalStateException exception){
          log.info("Could not derive editionNumber from monographEdition for record ${record.id} : ${record.displayTitle}")
        }
      }
    }
    if (numberValue != ""){
      editionNumber.addField(new Field("kbart", "editionNumber", numberValue))
      record.addMultiField(editionNumber)
    }
  }
}
