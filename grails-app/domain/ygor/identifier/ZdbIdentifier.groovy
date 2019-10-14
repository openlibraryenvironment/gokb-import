package ygor.identifier

import ygor.field.FieldKeyMapping

class ZdbIdentifier extends AbstractIdentifier {

  static constraints = {
    // TODO: check ZdbID format
  }

  ZdbIdentifier(String id, FieldKeyMapping fieldKeyMapping) {
    super(fieldKeyMapping)
    identifier = id
  }
}
