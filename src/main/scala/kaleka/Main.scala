package kaleka

import org.apache.pekko.actor.typed.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {
  val system = ActorSystem(TwitchBot(), "TwitchBot")
  Await.ready(system.whenTerminated, Duration.Inf)
}
