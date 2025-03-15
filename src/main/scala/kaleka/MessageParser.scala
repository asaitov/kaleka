package kaleka

import scala.util.parsing.combinator._

case class Tag(key: String, value: String)

case class ParsedMessage(tags: Map[String, String], prefix: Option[Prefix], command: String, params: List[String], trailingParam: Option[String])

case class Prefix(nick: String, user: Option[String], host: Option[String])

trait MessageParser {
  def parse(msg: String): Either[String, ParsedMessage]
}

object MessageParser extends MessageParser {
  val impl: MessageParser = ParserCombinatorsMessageParser // TODO use faster implementation, FastParser maybe
  def parse(msg: String): Either[String, ParsedMessage] = impl.parse(msg)
}

object ParserCombinatorsMessageParser extends RegexParsers with MessageParser {
  override def skipWhitespace: Boolean = false

  def message: Parser[ParsedMessage] = (tags <~ space).? ~ (prefix <~ space).? ~ command ~ params ~ trailingParam.? ^^ {
    case tags ~ prefix ~ command ~ params ~ trailingParam => ParsedMessage(tags.getOrElse(List.empty).map(x => x.key -> x.value).toMap, prefix, command, params, trailingParam)
  }

  def prefix: Parser[Prefix] = (":" ~> "[^! ]+".r) ~ ("!" ~> "[^@ ]+".r).? ~ ("@" ~> "[^ ]+".r).? ^^ {
    case nick ~ user ~ host => Prefix(nick, user, host)
  }

  def tag: Parser[Tag] = ("[^ =]+".r ~ "=" ~ "[^ ;]*".r) <~ ";".? ^^ {
    case key ~ _ ~ value => Tag(key, value)
  }

  def tags: Parser[List[Tag]] = "@" ~> tag.*

  def command: Parser[String] = "\\w+".r

  def params: Parser[List[String]] = (space ~> "[^: ]+".r).*

  def trailingParam: Parser[String] = space ~> ":" ~> ".*".r

  def space: Parser[String] = " "

  def parse(msg: String): Either[String, ParsedMessage] = parse(message, msg) match {
    case Success(result, _) => Right(result)
    case NoSuccess(msg, _) => Left(msg)
  }
}
