package de.hbznrw.ygor.enums

enum Status {

  // default value
  UNDEFINED("UNDEFINED"),
  // hardcoded values
  HARDCODED("HARDCODED"),
  // constants, tmp vars, etc
  IGNORE("IGNORE"),


  VALID("VALID"),
  INVALID("INVALID"),
  MISSING("MISSING"),

  // parsing crap
  REMOVE_FLAG("REMOVE_FLAG")

  private String value

  Status(String value) {
    this.value = value
  }

  @Override
  String toString() {
    return this.value
  }
}
