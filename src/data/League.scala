package data

import scala.collection.mutable.ListBuffer

class League(var country: String, var name: String) {
  val matches = ListBuffer[Match]()

  def this(parsedName: String) {
    this("", "")
    val results = parsedName.split(": ")
    this.country = results(0).toLowerCase.capitalize
    this.name = results(1)
  }

  def addMatch(m: Match): Unit = {
    this.matches += m
  }

  override def toString: String = s"$country: $name"

  override def equals(obj: Any): Boolean = {
    obj match {
      case l: League => l.toString == toString
      case _ => false
    }
  }
}
