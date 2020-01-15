package ygor.identifier

import ygor.field.FieldKeyMapping

class PrintIdentifier extends AbstractIdentifier {

  static constraints = {
    fieldKeyMapping nullable: false

    identifier validator: {
      ISSN_PATTERN.matcher(identifier).matches()
    }
  }

  PrintIdentifier(String id, FieldKeyMapping fieldKeyMapping) {
    super(fieldKeyMapping)
    identifier = id
  }
}
