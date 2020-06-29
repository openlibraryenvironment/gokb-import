package ygor.identifier

import ygor.field.FieldKeyMapping

@SuppressWarnings('JpaObjectClassSignatureInspection')
class ZdbIdentifier extends AbstractIdentifier {

  static mapWith = "none" // disable persisting into database

  static constraints = {
    // TODO: check ZdbID format
  }

  ZdbIdentifier(String id, FieldKeyMapping fieldKeyMapping) {
    super(fieldKeyMapping)
    identifier = id
  }
}
