package de.hbznrw.ygor.export.structure

class PackageHeader {
  String isil
  String name
  PackageHeaderNominalPlatform nominalPlatform = new PackageHeaderNominalPlatform()
  PackageHeaderNominalProvider nominalProvider = new PackageHeaderNominalProvider()
  String token
  String uuid
}
