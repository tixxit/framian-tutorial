package framian

import org.joda.time._

import spire.implicits._

import framian.csv.Csv

object tutorial {

  /**
   * Parses a value with an optional "scale" suffix at the end, such as 123K,
   * 24M, or 3.12B. This will return the scaled `BigDecimal`. Valid scales are
   * currently K, M, B, or T, for 1,000, 1,000,000, 1,000,000,000, and
   * 1,000,000,000,000 respectively.
   *
   * @param value the scaled value string
   */
  def parseScaledAmount(value: String): Cell[BigDecimal] = value.toUpperCase match {
    case ScaledAmount(value, scale) => Value(BigDecimal(value) * getScale(scale))
    case _ => NM
  }

  /**
   * Fetch some basic company info for the given IDs. This returns a frame with
   * the following columns: Name, Stock Exchange, Ticker, Currency, and Market
   * Cap. There is 1 row per company.
   *
   * @param ids a list of Yahoo! IDs to fetch company information for
   */
  def fetchCompanyInfo(ids: String*): Frame[Int, String] = {
    val idList = ids.mkString(",")
    val fields = "n0x0s0c4j1"
    val csv = fetchCsv(s"http://download.finance.yahoo.com/d/quotes.csv?s=$idList&f=$fields&e=.csv")
    csv.unlabeled.toFrame.withColIndex(Index.fromKeys("Name", "Stock Exchange", "Ticker", "Currency", "Market Cap"))
  }

  /**
   * Fetches the last 5 years of share price data, given a id, from Yahoo!
   * Finance.
   *
   * @param id a valid Yahoo! Finance ID
   */
  def fetchSharePriceData(id: String): Frame[Int, String] =
    fetchSharePriceData(id, LocalDate.now().minusYears(5), LocalDate.now())

  def fetchSharePriceData(id1: String, id2: String, id3: String*): Frame[Int, String] = {
    val ids = id1 :: id2 :: id3.toList
    ids.map { id =>
      val frame = fetchSharePriceData(id)
      val names = Series(frame.rowIndex, Column.value(id))
      frame.join("Ticker", names)(Join.Inner)
    }.reduceLeft(_ appendRows _)
  }

  /**
   * Fetches some historical share price data from Yahoo! Finance. The result
   * is returned as a CSV file, so we use Framian's CSV parser to convert it to
   * a frame.
   *
   * @param id     a valid Yahoo! Finance ID
   * @param start  the date to start fetching data from
   * @param end    the date to fetch data to (inclusive)
   */
  def fetchSharePriceData(id: String, start: LocalDate, end: LocalDate): Frame[Int, String] = {
    val paramMap = Map(
      "s" -> id,
      "a" -> (start.getMonthOfYear - 1),
      "b" -> start.getDayOfMonth,
      "c" -> start.getYear,
      "d" -> (end.getMonthOfYear - 1),
      "e" -> end.getDayOfMonth,
      "f" -> end.getYear,
      "ignore" -> ".csv")
    val queryString = paramMap.map { case (k, v) => s"$k=$v" }.mkString("&")
    val csv = fetchCsv(s"http://real-chart.finance.yahoo.com/table.csv?$queryString")
    csv.labeled.toFrame
  }

  /**
   * Fetches the exchange rate data from Yahoo! Finance. All rates are for
   * conversions to USD.
   *
   * @param currencies a list of currencies (eg. CAD, EUR, etc)
   */
  def fetchExchangeRate(currencies: String*): Frame[Int, String] = {
    val ids = currencies.map(_ + "USD=X").mkString(",")
    val fields = "c4l1"
    val csv = fetchCsv(s"http://download.finance.yahoo.com/d/quotes.csv?s=$ids&f=$fields&e=.csv")
    csv.unlabeled.toFrame.withColIndex(Index.fromKeys("Currency", "Rate"))
  }

  private def fetchCsv(url: String): Csv = {
    val csv = scala.io.Source.fromURL(url).mkString
    Csv.parseString(csv)
  }

  private val ScaledAmount = """(\d+(?:\.\d+))([kKmMbB])?""".r

  private def getScale(suffix: String): BigDecimal = suffix match {
    case "k" | "K" => BigDecimal(1000)
    case "m" | "M" => BigDecimal(1000000)
    case "b" | "B" => BigDecimal(1000000000)
    case "t" | "T" => BigDecimal("1000000000000")
  }
}
