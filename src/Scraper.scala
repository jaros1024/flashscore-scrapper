import data.{League, Match}
import org.jsoup.Jsoup
import scala.jdk.CollectionConverters._
import scala.collection.immutable.HashMap
import scala.collection.mutable.ListBuffer


object Scraper {
  private final val BASE_URL = "http://m.flashscore.pl/"

  private def groupElementsByLeague(content: String): List[League] = {
    val elements = content.split("<br>")
    val leagues = ListBuffer[League]()

    for (i <- 0 until elements.length - 1) {
      val el = Jsoup.parse(elements(i))
      val titles = el.getElementsByTag("h4")
      if (!titles.isEmpty) {
        val leagueName = titles.first().text()
        leagues += new League(leagueName)
        titles.first().remove()
      }
      val time = el.getElementsByTag("span").first().text()
      el.getElementsByTag("span").first().remove()

      val scoreTag = el.getElementsByTag("a").first()
      val score = scoreTag.text()
      val live = scoreTag.className() == "live"

      scoreTag.remove()
      val teams = el.text().split(" - ")
      val m = new Match(teams(0), teams(1), time, score, live)
      leagues.last.addMatch(m)
    }

    return leagues.toList
  }

  private def getWebContent(args: HashMap[String, String]): String = {
    var argString = ""
    if (args.nonEmpty) {
      argString = "?"
      argString = argString.concat((args.view.map{case(k,v) => s"$k=$v"} toList).mkString("="))
    }

    val doc = Jsoup.connect(BASE_URL.concat(argString)).get()
    return doc.getElementById("score-data").html()
  }

  private def generatePlainString(leagues: List[League]): String = {
    val builder = new StringBuilder()

    for (league <- leagues) {
      builder ++= s"${league.country}: ${league.name}\n"

      for (m <- league.matches) {
        builder ++= "    "
        if (m.live) {
          builder ++= "(Live) "
        }
        builder ++= s"${m.time} ${m.team1} - ${m.team2} ${m.score}\n"
      }

      builder ++ "\n\n"
    }

    return builder.mkString
  }

  private def generateJsonString(leagues: List[League]): String = {
    return Serializer.gson.toJson(leagues.asJava)
  }

  def main(args: Array[String]): Unit = {
    val usage = ""
    if (args.length == 0) {
      println(usage)
    }
    val arglist = args.toList
    type OptionMap = HashMap[String, String]

    def nextOption(map : OptionMap, list: List[String]) : OptionMap = {
      def isSwitch(s : String) = (s(0) == '-')
      list match {
        case Nil => map
        case "--yesterday" :: tail =>
          nextOption(map ++ HashMap("d" -> "-1"), tail)
        case "--tomorrow" :: tail =>
          nextOption(map ++ HashMap("d" -> "1"), tail)
        case "--live-only" :: tail =>
          nextOption(map ++ HashMap("s" -> "2"), tail)
        case "--finished-only" :: tail =>
          nextOption(map ++ HashMap("s" -> "3"), tail)
        case "--json" :: tail =>
          nextOption(map ++ HashMap("json" -> "true"), tail)
        case option :: tail => throw new Exception("Unknown option "+ option)
      }
    }
    var options: OptionMap = null
    try {
      options = nextOption(HashMap(), arglist)
    }
    catch {
      case x: Exception =>
        println(x)
        System.exit(1)
    }
    var json = false
    if (options.contains("json")) {
      json = true
      options = options - "json"
    }
    val webContent = getWebContent(options)
    val leagues = groupElementsByLeague(webContent)

    var result: String = ""
    if (json) {
      result = generateJsonString(leagues)
    }
    else {
      result = generatePlainString(leagues)
    }

    println(result)
  }
}

