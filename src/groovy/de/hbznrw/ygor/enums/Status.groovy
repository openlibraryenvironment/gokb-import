package de.hbznrw.ygor.enums

enum Status {

  // default value
  UNDEFINED("UNDEFINED"),
  // hardcoded values
  HARDCODED("HARDCODED"),
  // constants, tmp vars, etc
  IGNORE("IGNORE"),


  // use for valid date values
  DATE_IS_VALID("DATE_IS_VALID"),
  // use for non conform date values
  DATE_IS_INVALID("DATE_IS_INVALID"),
  // no value given
  DATE_IS_MISSING("DATE_IS_MISSING"),

  // given identifier seems to be valid
  IDENTIFIER_IS_VALID("IDENTIFIER_IS_VALID"),
  // given identifier is not valid
  IDENTIFIER_IS_INVALID("IDENTIFIER_IS_INVALID"),
  // given identifier seems to contain multiple matches
  IDENTIFIER_IS_NOT_ATOMIC("IDENTIFIER_IS_NOT_ATOMIC"),
  // no given identifier
  IDENTIFIER_IS_MISSING("IDENTIFIER_IS_MISSING"),
  // e.g. not implemented yet
  IDENTIFIER_IN_UNKNOWN_STATE("IDENTIFIER_IN_UNKNOWN_STATE"),

  // given title seems to be valid
  STRING_IS_VALID("STRING_IS_VALID"),
  // given title is given but not valid
  STRING_IS_INVALID("STRING_IS_INVALID"),
  // given title seems to contain multiple matches
  STRING_IS_NOT_ATOMIC("STRING_IS_NOT_ATOMIC"),
  // no given title
  STRING_IS_MISSING("STRING_IS_MISSING"),

  // given url seems to be valid
  URL_IS_VALID("URL_IS_VALID"),
  // given url is invalid
  URL_IS_INVALID("URL_IS_INVALID"),
  // given url seems to contain multiple matches
  URL_IS_NOT_ATOMIC("URL_IS_NOT_ATOMIC"),
  // no given url
  URL_IS_MISSING("URL_IS_MISSING"),

  // given number seems to be valid
  NUMBER_IS_VALID("NUMBER_IS_VALID"),
  // given number is not valid
  NUMBER_IS_INVALID("NUMBER_IS_INVALID"),
  // given number seems to contain multiple matches
  NUMBER_IS_NOT_ATOMIC("NUMBER_IS_NOT_ATOMIC"),
  // no given number
  NUMBER_IS_MISSING("NUMBER_IS_MISSING"),

  // advanced
  PUBLISHER_NOT_MATCHING("PUBLISHER_NOT_MATCHING"),
  TIPPURL_NOT_MATCHING("TIPPURL_NOT_MATCHING"),

  // parsing crap
  REMOVE_FLAG("REMOVE_FLAG"),

  // given coverage seems to be valid
  COVERAGE_IS_VALID("COVERAGE_IS_VALID"),
  // given coverage is invalid
  COVERAGE_IS_INVALID("COVERAGE_IS_INVALID"),
  // default case
  COVERAGE_IS_UNDEF("COVERAGE_IS_UNDEF"),

  // given history event seems to be valid
  HISTORYEVENT_IS_VALID("HISTORYEVENT_IS_VALID"),
  // given history event is invalid
  HISTORYEVENT_IS_INVALID("HISTORYEVENT_IS_INVALID"),
  // default case
  HISTORYEVENT_IS_UNDEF("HISTORYEVENT_IS_UNDEF"),

  // given publisher history seems to be valid
  PUBLISHERHISTORY_IS_VALID("PUBLISHERHISTORY_IS_VALID"),
  // given publisher history is invalid
  PUBLISHERHISTORY_IS_INVALID("PUBLISHERHISTORY_IS_INVALID"),
  // default case
  PUBLISHERHISTORY_IS_UNDEF("PUBLISHERHISTORY_IS_UNDEF")

  private String value

  Status(String value) {
    this.value = value
  }

  @Override
  String toString() {
    return this.value
  }
}
