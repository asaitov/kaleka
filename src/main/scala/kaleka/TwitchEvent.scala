package kaleka

sealed trait TwitchEvent

object TwitchEvent {
  def parse(message: String): TwitchEvent = {
    val stripped = message.stripSuffix("\r\n")
    MessageParser.parse(stripped) match {
      case Right(result) => TwitchEvent.convert(result)
      case _ => Unparsed(stripped)
    }
  }

  def convert(parsed: ParsedMessage): TwitchEvent = parsed.command match {
    case "001" =>
      Welcome(parsed.trailingParam.getOrElse(""))
    case "002" | "003" | "004" | "375" | "372" | "376" =>
      Info(parsed.command, parsed.trailingParam.getOrElse(""))
    case "353" if parsed.params.size > 2 =>
      NamesReply(parsed.params(2), parsed.trailingParam.map(_.split(' ').toList).getOrElse(List.empty))
    case "366" if parsed.params.size > 1 =>
      EndOfNames(parsed.params(1))
    case "JOIN" if parsed.params.nonEmpty && parsed.prefix.nonEmpty =>
      Join(parsed.prefix.map(_.nick).get, parsed.params.head)
    case "WHISPER" if parsed.params.nonEmpty && parsed.prefix.nonEmpty =>
      Whisper(parsed.prefix.map(_.nick).get, parsed.trailingParam.getOrElse(""), parsed.tags)
    case "CAP" if parsed.params.size > 1 =>
      Cap(parsed.params.head, parsed.params(1), parsed.trailingParam.map(_.split(" ").toList).getOrElse(List.empty))
    case "CLEARCHAT" if parsed.params.nonEmpty =>
      ClearChat(parsed.params.head, parsed.trailingParam, parsed.tags)
    case "CLEARMSG" if parsed.params.nonEmpty =>
      ClearMsg(parsed.params.head, parsed.trailingParam.getOrElse(""), parsed.tags)
    case "GLOBALUSERSTATE" =>
      GlobalUserState(parsed.tags)
    case "NOTICE" if parsed.params.nonEmpty =>
      Notice(parsed.params.head, parsed.trailingParam.getOrElse(""), parsed.tags)
    case "PART" if parsed.params.nonEmpty =>
      Part(parsed.params.head, parsed.trailingParam.getOrElse(""))
    case "PING" =>
      Ping(parsed.trailingParam.getOrElse(""))
    case "PRIVMSG" if parsed.params.nonEmpty && parsed.prefix.nonEmpty =>
      PrivMsg(parsed.prefix.map(_.nick).get, parsed.params.head, parsed.trailingParam.getOrElse(""), parsed.tags)
    case "RECONNECT" =>
      Reconnect
    case "ROOMSTATE" if parsed.params.nonEmpty =>
      RoomState(parsed.params.head, parsed.tags)
    case "USERNOTICE" if parsed.params.nonEmpty =>
      UserNotice(parsed.params.head, parsed.trailingParam.getOrElse(""), parsed.tags)
    case "USERSTATE" if parsed.params.nonEmpty =>
      UserState(parsed.params.head, parsed.tags)
    case _ =>
      Unknown(parsed.tags, parsed.prefix.map(_.nick), parsed.command, parsed.params ++ parsed.trailingParam)
  }

  case class Welcome(msg: String) extends TwitchEvent
  case class Info(command: String, msg: String) extends TwitchEvent
  case class Cap(channel: String, result: String, caps: List[String]) extends TwitchEvent
  case class ClearChat(channel: String, user: Option[String], tags: Map[String, String]) extends TwitchEvent
  case class ClearMsg(channel: String, msg: String, tags: Map[String, String]) extends TwitchEvent
  case class GlobalUserState(tags: Map[String, String]) extends TwitchEvent
  case class Notice(channel: String, msg: String, tags: Map[String, String]) extends TwitchEvent
  case class Part(user: String, channel: String) extends TwitchEvent
  case class Ping(msg: String) extends TwitchEvent
  case class PrivMsg(user: String, channel: String, message: String, tags: Map[String, String]) extends TwitchEvent
  case object Reconnect extends TwitchEvent
  case class RoomState(channel: String, tags: Map[String, String]) extends TwitchEvent
  case class UserNotice(channel: String, msg: String, tags: Map[String, String]) extends TwitchEvent
  case class UserState(channel: String, tags: Map[String, String]) extends TwitchEvent
  case class NamesReply(channel: String, users: List[String]) extends TwitchEvent
  case class EndOfNames(channel: String) extends TwitchEvent
  case class Join(user: String, channel: String) extends TwitchEvent
  case class Whisper(from: String, message: String, tags: Map[String, String]) extends TwitchEvent

  case class Unknown(tags: Map[String, String], from: Option[String], command: String, params: List[String]) extends TwitchEvent
  case class Unparsed(msg: String) extends TwitchEvent
}
