package ygor.identifier

import ygor.field.FieldKeyMapping

@SuppressWarnings('JpaObjectClassSignatureInspection')
class EzbIdentifier extends AbstractIdentifier {

  static mapWith = "none" // disable persisting into database

  static constraints = {
  }

  EzbIdentifier(String id, FieldKeyMapping fieldKeyMapping) {
    super(fieldKeyMapping)
    identifier = id
  }
}
