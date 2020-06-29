package ygor.identifier

import ygor.field.FieldKeyMapping

@SuppressWarnings('JpaObjectClassSignatureInspection')
class DoiIdentifier extends AbstractIdentifier {

  static mapWith = "none" // disable persisting into database

  static constraints = {
    fieldKeyMapping nullable: false

    // TODO: check Doi format
  }

  DoiIdentifier(String id, FieldKeyMapping fieldKeyMapping) {
    super(fieldKeyMapping)
    identifier = id
  }
}
