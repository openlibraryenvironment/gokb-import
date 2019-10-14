package ygor.identifier

import ygor.field.FieldKeyMapping

class EissnIdentifier extends AbstractIdentifier {

  static constraints = {
    fieldKeyMapping nullable: false

    identifier validator: {
      ISSN_PATTERN.matcher(identifier).matches()
    }
  }

  EissnIdentifier(String id, FieldKeyMapping fieldKeyMapping) {
    super(fieldKeyMapping)
    identifier = id
  }
}
