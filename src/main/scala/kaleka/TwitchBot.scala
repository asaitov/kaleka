package kaleka

import kaleka.TwitchEvent.Welcome
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

object TwitchBot {
  def apply(): Behavior[TwitchEvent] = Behaviors.setup { ctx =>
    val client = ctx.spawn(TwitchClient(ConfigUtil.clientConfig, ctx.self), "TwitchClient")
    client ! TwitchClient.Connect

    def connecting(): Behavior[TwitchEvent] = Behaviors.receiveMessagePartial {
      case Welcome(_) =>
        client ! TwitchClient.Send(Join(ConfigUtil.clientConfig.startupChannels))
        active()
    }

    def active(): Behavior[TwitchEvent] = Behaviors.receiveMessagePartial {
      case event =>
        Behaviors.same
    }

    connecting()
  }
}
