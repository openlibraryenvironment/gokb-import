package de.hbznrw.ygor.validators

import de.hbznrw.ygor.enums.*
import de.hbznrw.ygor.export.structure.TitleStruct
import de.hbznrw.ygor.normalizers.DateNormalizer
import de.hbznrw.ygor.readers.EzbReader
import org.apache.commons.lang.StringUtils
import org.apache.commons.validator.routines.UrlValidator
import ygor.identifier.AbstractIdentifier

import java.sql.Timestamp

// checks if given value meets the requirements

class Validator {

  static UrlValidator URL_VALIDATOR = new UrlValidator(["http", "https"] as String[])

  static validate(String type, String value, String... additionalParameters) {
    switch (type) {
      case "Title":
        return isValidString(value, 1)
        break
      case "String":
        return isValidString(value, 2)
        break
      case "Abbreviation":
        return isValidAbbreviation(value)
        break
      case "Number":
        return isValidNumber(value)
        break
      case "ID":
        return isValidIdentifier(value,
            additionalParameters[0] /* must contain ID type */,
            additionalParameters[1] /* must contain namespace */)
        break
      case "URL":
        return isValidURL(value)
        break
      case DateNormalizer.START_DATE:
        return isValidDate(value)
        break
      case DateNormalizer.END_DATE:
        return isValidDate(value)
        break
      case "Date":
        return isValidDate(value)
        break
      case "ISBN":
        return validateISBN(value)
        break
      default:
        return Status.UNDEFINED
    }
  }

  /**
   *
   * @param str
   * @return
   */
  static isValidString(String str, int minimumLength) {
    if (!str || str.trim().equals("")) {
      return Status.MISSING
    }
    else if (str.length() < minimumLength) {
      if (!StringUtils.isNumeric(str)) {
        return Status.INVALID
      }
    }
    return Status.VALID
  }

  /**
   *
   * @param abbreviation
   * @return
   */
  static isValidAbbreviation(String abbreviation) {
    if (StringUtils.isEmpty(abbreviation)) {
      return Status.MISSING
    }
    if (abbreviation.length() > 1) {
      return Status.INVALID
    }
    return Status.VALID
  }

  /**
   *
   * @param str
   * @return
   */
  static isValidNumber(String str) {
    if (!str || str.trim().equals("")) {
      return Status.MISSING
    }
    else if (str.contains("|")) {
      return Status.UNDEFINED
    }
    else if (str.isInteger()) {
      return Status.VALID
    }

    return Status.INVALID
  }

  /**
   *
   * @param str
   * @param identifierType
   * @return
   */
  static isValidIdentifier(String str, Object identifierType, String namespace) {
    if (!str || str.trim().equals("")) {
      return Status.MISSING
    }
    if (str.contains("|")) {
      return Status.UNDEFINED
    }
    if (identifierType in ["onlineIdentifier", "printIdentifier"]) {
      if (AbstractIdentifier.ISSN_PATTERN.matcher(str).matches() ||
          AbstractIdentifier.ISBN13_SIMPLE_PATTERN.matcher(str).matches() ||
          AbstractIdentifier.ISBN10_SIMPLE_PATTERN.matcher(str).matches()) {
        return Status.VALID
      }
      else {
        return Status.INVALID
      }
    }
    else if (identifierType.equals("zdbId")){
      if (str.matches("[0-9]{1,9}-[0-9Xx]")){
        // "Das Feld wird maschinell besetzt und enthält eine bis zu 11 Stellen umfassende Identifikationsnummer mit Prüfziffer."
        return Status.VALID
      }
      else{
        return Status.INVALID
      }
    }
    else if (identifierType.equals("ezbId")){
      if (str.matches("[0-9]{1,15}")){
        // "Das Feld wird maschinell besetzt und enthält eine bis zu 11 Stellen umfassende Identifikationsnummer mit Prüfziffer."
        return Status.VALID
      }
      else{
        return Status.INVALID
      }
    }
    else if (identifierType.equals("gokbUuid")){
      if (str.matches("[0-9a-zA-Z]{8}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{12}")){
        return Status.VALID
      }
      else{
        return Status.INVALID
      }
    }
    else if (identifierType.equals(TitleStruct.EISBN) || identifierType.equals(TitleStruct.PISBN)) {
      if (validateISBN(str)) {
        return Status.VALID
      }
      else {
        return Status.INVALID
      }
    }
    else if (identifierType.equals(EzbReader.IDENTIFIER)) {
      // TODO .. no valid definition
      if (str.length() > 2) {
        return Status.VALID
      }
      else {
        return Status.INVALID
      }
    }
    else if (identifierType.equals(TitleStruct.DOI)) {
      if (str.startsWith("10.")) {
        return Status.VALID
      }
      else {
        return Status.INVALID
      }
    }
    else if (identifierType.equals("inID_" + namespace)) {
      if (str) {
        return Status.VALID
      }
      else {
        return Status.INVALID
      }
    }
    else if (identifierType == namespace) {
      // TODO use identifier type in Knowledge Base (String, URL, ...) and specify here
      return Status.VALID
    }
    return Status.UNDEFINED
  }


  /**
   *
   * @param str
   * @return
   */
  static Status isValidURL(String str) {
    if (StringUtils.isEmpty(str)) {
      return Status.MISSING
    }
    if (URL_VALIDATOR.isValid(str)) {
      return Status.VALID
    }
    return Status.INVALID
  }

  /**
   *
   * @param str
   * @return
   */
  static isValidDate(String str) {
    if (!str || str.trim().equals("")) {
      return Status.MISSING
    }
    // accept "YYYY" or "YYYY-MM-DD" or "YYYY/MM/DD"
    if (str.matches("[\\d]{4}([-/][\\d]{2}){0,2}")){
      return Status.VALID
    }
    // also accept some time spans, like "YYYY-YYYY"
    if (str.matches("\\[?[\\d]{4} ?- ?[\\d]{4}]?")){
      return Status.VALID
    }
    try {
      Timestamp.valueOf(str)
      return Status.VALID
    }
    catch (Exception e) {
      return Status.INVALID
    }
  }

  /**
   * Validate ISBN 13
   * https://www.moreofless.co.uk/validate-isbn-13-java/
   *
   * @param str
   * @return
   */

  static boolean validateISBN(String str) {
    def isbn = str
    if (isbn == null) {
      return false
    }
    //remove any hyphens
    isbn = isbn.replaceAll("-", "")

    //must be a 13 digit ISBN
    if (isbn.length() != 13) {
      return false
    }
    try {
      int tot = 0;
      for (int i = 0; i < 12; i++) {
        int digit = Integer.parseInt(isbn.substring(i, i + 1))
        tot += (i % 2 == 0) ? digit * 1 : digit * 3
      }
      //checksum must be 0-9. If calculated as 10 then = 0
      int checksum = 10 - (tot % 10)
      if (checksum == 10) {
        checksum = 0
      }
      return checksum == Integer.parseInt(isbn.substring(12))
    }
    catch (NumberFormatException nfe) {
      //to catch invalid ISBNs that have non-numeric characters in them
      return false
    }
  }
}
