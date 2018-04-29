package com.github.ser.util

import scala.xml.XML

object XmlUtil extends Serializable {
  def optionalAttribute(line: String, attributeName: String) = {
    XML.loadString(line)
      .attribute(attributeName)
      .map(_.text)
  }

  def requiredAttribute(line: String, attributeName: String): String = {
    optionalAttribute(line, attributeName)
      .getOrElse(throw new RuntimeException(s"cannot find attribute Id in $line"))
  }
}
