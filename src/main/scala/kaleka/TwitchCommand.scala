package kaleka

import scala.language.implicitConversions

object TwitchCommand {
  implicit def asString(command: TwitchCommand): String = command.render
}

sealed trait TwitchCommand {
  def render: String
}

case class Nick(nick: String) extends TwitchCommand {
  override def render: String = s"NICK $nick\r\n"
}

case class Pass(token: String) extends TwitchCommand {
  override def render: String = s"PASS oauth:$token\r\n"
}

case class Join(channels: List[String]) extends TwitchCommand {
  override def render: String = {
    val channelString = channels.mkString("#", ",#", "")
    s"JOIN $channelString\r\n"
  }
}

case class Pong(msg: String) extends TwitchCommand {
  override def render: String = s"PONG :$msg\r\n"
}


case class Misc(s: String) extends TwitchCommand {
  override def render: String = s"$s\r\n"
}
