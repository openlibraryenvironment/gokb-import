package de.hbznrw.ygor.tools

import java.util.regex.Pattern

class RecordFileFilter implements java.io.FileFilter {

  Pattern pattern

  RecordFileFilter(String resultHash) {
    pattern = java.util.regex.Pattern.compile("^".concat(resultHash).concat("_"))
  }

  boolean accept(File file) {
    return pattern.matcher(file.getName()).find() && file.getName().length() == 69
  }
}
