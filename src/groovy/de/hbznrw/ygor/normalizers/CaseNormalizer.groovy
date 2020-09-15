package de.hbznrw.ygor.normalizers

import ygor.field.MultiField

class CaseNormalizer{

  static void normalize(MultiField field, Case caze, Selection selection) {
    String value = field.getFirstPrioValue()
    if (value == null || value.length() < 1){
      return
    }
    if (Case.UPPER.equals(caze)){
      if (Selection.INITIAL.equals(selection)){
        field.normalized = value.substring(0, 1).toUpperCase() + (value.length() > 1 ? value.substring(1) : "" )
      }
      else if (Selection.ALL.equals(selection)){
        field.normalized = value.toUpperCase()
      }
    }
    else if (Case.LOWER.equals(caze)){
      if (Selection.INITIAL.equals(selection)){
        field.normalized = value.substring(0, 1).toLowerCase() + (value.length() > 1 ? value.substring(1) : "" )
      }
      else if (Selection.ALL.equals(selection)){
        field.normalized = value.toLowerCase()
      }
    }
  }

  enum Case{
    UPPER,
    LOWER
  }

  enum Selection{
    INITIAL,
    ALL
  }
}
