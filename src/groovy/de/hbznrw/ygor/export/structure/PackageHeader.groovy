package de.hbznrw.ygor.export.structure

class PackageHeader {

  def additionalProperties = []
  String breakable = FixedValues.packageHeader_breakable
  String consistent = FixedValues.packageHeader_consistent
  String fixed = FixedValues.packageHeader_fixed
  String global = FixedValues.packageHeader_global
  String isil
  String listStatus = FixedValues.packageHeader_listStatus
  String name
  PackageHeaderNominalPlatform nominalPlatform = new PackageHeaderNominalPlatform()
  String nominalProvider
  String paymentType = ""
  String scope = ""
  PackageHeaderSource source = new PackageHeaderSource()
  String userListVerifier = ""

}
