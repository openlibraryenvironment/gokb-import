package de.hbznrw.ygor.processing

import de.hbznrw.ygor.readers.KbartReader
import groovy.util.logging.Log4j

@Log4j
class Stash {

  static final IGNORED_KBART_ENTRIES = 'ignoredKbartEntries'

  HashMap values
  HashMap typePerKey = [:]

  Stash() {
    values = [:]

    // api enrichment
    values[KbartReader.KBART_HEADER_ZDB_ID] = [:]
    values[KbartReader.KBART_HEADER_ONLINE_IDENTIFIER] = [:]
    values[KbartReader.KBART_HEADER_PRINT_IDENTIFIER] = [:]
    values[KbartReader.KBART_HEADER_DOI_IDENTIFIER] = [:]

    // file enrichment
    values[KbartReader.IDENTIFIER] = [:]

    // must
    values[Stash.IGNORED_KBART_ENTRIES] = []
  }

  def put(Object key, Object value) {
    if (value?.size() > 0) {
      values[key] << value
    }
  }

  def get(String key) {
    values[key]
  }

  def getKeyByValue(String type, String value) {
    def result = []
    def map = values[type]

    map?.keySet().each { k ->
      if (map.get(k) == value) {
        result << k
      }
    }
    if (result.size() > 1) {
      println "WARNING: getKeyByValue(" + type + ", " + value + ") ->> multiple matches"
    }

    result.size() == 1 ? result.get(0) : null
  }

  def putKeyType(String key, String type) {
    typePerKey.put(key, type)
  }

  def getKeyType(String key) {
    typePerKey.get(key)
  }
}
