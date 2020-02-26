package de.hbznrw.ygor.enums

enum Status {

  // default value
  UNDEFINED("undefined"),
  // hardcoded values
  HARDCODED("hardcoded"),
  // constants, tmp vars, etc
  IGNORE("ignore"),


  VALID("valid"),
  INVALID("invalid"),
  WARNING("warning"),
  MISSING("missing"),
  MISMATCH("mismatch"),

  // parsing crap
  REMOVE_FLAG("remove_flag")

  private String value

  Status(String value) {
    this.value = value
  }

  @Override
  String toString() {
    return this.value
  }
}
