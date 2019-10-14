package de.hbznrw.ygor.readers

import ygor.field.FieldKeyMapping

abstract class AbstractReader {

  abstract def readItemData(FieldKeyMapping fieldKeyMapping, String identifier)

}
