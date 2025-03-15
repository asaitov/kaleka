package kaleka

import org.apache.pekko.actor.{Actor, ActorRef, Props}
import org.apache.pekko.io.{IO, Tcp}
import org.apache.pekko.util.ByteString
import kaleka.TwitchClient.Message

import java.net.InetSocketAddress
import scala.annotation.tailrec
import Tcp._

object TcpClient {
  sealed trait Command
  case object Connect extends Command
  case object Disconnect extends Command
  case class Send(data: String) extends Command

  def props(remote: InetSocketAddress, replies: org.apache.pekko.actor.typed.ActorRef[Message]): Props =
    Props(new TcpClient(remote, replies))
}

class TcpClient(remote: InetSocketAddress, client: org.apache.pekko.actor.typed.ActorRef[Message]) extends Actor {
  @tailrec
  private def splitReversed(buf: ByteString, result: List[ByteString], delim: Byte = '\n'): List[ByteString] = {
    val i = buf.indexOf(delim)
    if (i < 0) {
      buf :: result
    } else {
      val (item, rest) = buf.splitAt(i + 1)
      splitReversed(rest, item :: result, delim)
    }
  }

  private def split(buf: ByteString, delim: Byte = '\n'): List[ByteString] = {
    splitReversed(buf, List.empty, delim).reverse
  }

  def connected(connection: ActorRef, buf: ByteString): Receive = {
    case TcpClient.Send(s) =>
      print(s">> $s")
      connection ! Write(ByteString.fromString(s))
    case cf @ CommandFailed(w: Write) =>
      client ! TwitchClient.SendFailed(w.data.utf8String, cf.cause)
      // TODO change behavior
    case Received(data) =>
      val parts = split(buf ++ data)
      parts.dropRight(1).foreach { part =>
        client ! TwitchClient.Received(part.utf8String)
      }
      context.become(connected(connection, parts.last))
    case TcpClient.Disconnect =>
      connection ! Close
      context.become(disconnecting)
    case _: ConnectionClosed =>
      // TODO reconnect
  }

  val connecting: Receive = {
    case cf @ CommandFailed(_: Connect) =>
      client ! TwitchClient.ConnectFailed(cf.cause)
      context.stop(self)

    case Connected(_, _) =>
      client ! TwitchClient.Connected
      val connection = sender()
      connection ! Register(self)
      context.become(connected(connection, ByteString.empty))
  }

  val disconnecting: Receive = {
    case _: ConnectionClosed =>
      client ! TwitchClient.Disconnected
      context.stop(self)
  }

  def receive: Receive = {
    case TcpClient.Connect =>
      IO(Tcp)(context.system) ! Connect(remote)
      context.become(connecting)
  }
}
