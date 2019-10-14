package de.hbznrw.ygor.export.structure

import de.hbznrw.ygor.enums.*

class TippTitle {

  Pod name = new Pod("")
  Pod type = new Pod(FixedValues.tipp_title_type, Status.HARDCODED)

  ArrayList<Identifier> identifiers = []
}