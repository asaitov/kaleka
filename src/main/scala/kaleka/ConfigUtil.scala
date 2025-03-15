package kaleka

import com.typesafe.config.{Config, ConfigFactory}
import scala.jdk.CollectionConverters._

object ConfigUtil {
  val config: Config = ConfigFactory.load().resolve().getConfig("kaleka")

  val clientConfig: TwitchClientConfig = {
    val bot = config.getConfig("bot")
    val connection = config.getConfig("connection")
    TwitchClientConfig(bot.getString("nick"), bot.getString("token"), connection.getString("host"), connection.getInt("port"), bot.getStringList("startup-channels").asScala.toList)
  }
}

case class TwitchClientConfig(nick: String, token: String, host: String, port: Int, startupChannels: List[String])
