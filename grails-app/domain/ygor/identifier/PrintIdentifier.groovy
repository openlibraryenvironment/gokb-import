package ygor.identifier

import ygor.field.FieldKeyMapping

@SuppressWarnings('JpaObjectClassSignatureInspection')
class PrintIdentifier extends AbstractIdentifier {

  static mapWith = "none" // disable persisting into database

  static constraints = {
    fieldKeyMapping nullable: false

    identifier validator: {
      ISSN_PATTERN.matcher(identifier).matches() ||
          ISBN13_SIMPLE_PATTERN.matcher(identifier).matches() ||
          ISBN10_SIMPLE_PATTERN.matcher(identifier).matches()
    }
  }

  PrintIdentifier(String id, FieldKeyMapping fieldKeyMapping) {
    super(fieldKeyMapping)
    identifier = id
  }
}
