package kaleka

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.scaladsl.adapter._
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import TwitchCommand._
import kaleka.TwitchEvent.{Cap, Ping}

import java.net.InetSocketAddress

object TwitchClient {
  sealed trait Message
  sealed trait Event extends Message
  sealed trait Command extends Message

  case object Connect extends Command
  case class ConnectFailed(cause: Option[Throwable]) extends Command
  case object Disconnect extends Command
  case object Disconnected extends Event
  case object Connected extends Event
  case class Received(message: String) extends Event
  case class Send(command: TwitchCommand) extends Command
  case class SendFailed(data: String, cause: Option[Throwable]) extends Event

  def apply(config: TwitchClientConfig, logic: ActorRef[TwitchEvent]): Behavior[Message] = Behaviors.setup { ctx =>
    val tcp = ctx.actorOf(TcpClient.props(new InetSocketAddress(config.host, config.port), ctx.self))
    ctx.watch(tcp)

    def disconnected(): Behavior[Message] = Behaviors.receiveMessage {
      case Connect =>
        tcp ! TcpClient.Connect
        connecting()
      case _ =>
        Behaviors.same
    }

    def connecting(): Behavior[Message] = Behaviors.receiveMessage {
      case Connected =>
        tcp ! TcpClient.Send(Misc("CAP REQ :twitch.tv/commands twitch.tv/commands twitch.tv/tags"))
        caps()
      case _ =>
        Behaviors.same
    }

    def caps(): Behavior[Message] = Behaviors.receiveMessage {
      case Received(message) =>
        val event = TwitchEvent.parse(message)
        print(message)
        println(s">> $event")
        event match {
          case Cap(_, _, _) =>
            tcp ! TcpClient.Send(Pass(config.token))
            tcp ! TcpClient.Send(Nick(config.nick))
            auth()
          case _ =>
            Behaviors.same
        }
      case _ =>
        Behaviors.same
    }

    def auth(): Behavior[Message] = Behaviors.receiveMessage {
      case Received(message) =>
        val event = TwitchEvent.parse(message)
        print(message)
        println(s">> $event")
        logic ! event
        event match {
          case TwitchEvent.Welcome(_) => connected()
          case TwitchEvent.Notice("*", "Login authentication failed" | "Improperly formatted auth", _) =>
            tcp ! TcpClient.Disconnect
            disconnecting()
          case _ => Behaviors.same
        }
      case _ =>
        Behaviors.same
    }

    def connected(): Behavior[Message] = Behaviors.receiveMessage {
      case Send(command) =>
        tcp ! TcpClient.Send(command)
        Behaviors.same
      case SendFailed(data, cause) =>
        cause.foreach(_.printStackTrace)
        Behaviors.same
      case Received(message) =>
        val event = TwitchEvent.parse(message)
        print(message)
        println(s"<< $event")
        event match {
          case Ping(msg) =>
            tcp ! TcpClient.Send(Pong(msg))
            Behaviors.same
          case _ =>
            logic ! event
            Behaviors.same
        }
      case Disconnect =>
        tcp ! TcpClient.Disconnect
        disconnecting()
      case _ =>
        Behaviors.same
    }

    def disconnecting(): Behavior[Message] = Behaviors.receiveMessage {
      case Disconnected =>
        Behaviors.stopped
      case _ =>
        Behaviors.same
    }

    disconnected()
  }
}
