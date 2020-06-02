package de.hbznrw.ygor.normalizers

import ygor.Record
import ygor.field.Field
import ygor.field.MultiField

class EditionNormalizer {

  static void normalizeEditionNumber(Record record) {
    // title.monographEdition
    // title.editionDifferentiator
    // title.editionStatement
    // ==> are already normalized as default Strings

    // title.editionNumber
    // needs extra normalization, as it extracts a number from text

    MultiField editionNumber = record.getMultiField("editionNumber")
    // re-set first number for now
    editionNumber.addField(new Field("kbart", "editionNumber", editionNumber.getFirstPrioValue()))
  }
}