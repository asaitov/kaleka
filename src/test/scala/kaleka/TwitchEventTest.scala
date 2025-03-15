package kaleka

import org.scalatest.funsuite.AnyFunSuite
import TwitchEvent._

class TwitchEventTest extends AnyFunSuite {
    test("TwitchEvent should be parsed") {
      assert(TwitchEvent.parse(":user!user@user.tmi.twitch.tv JOIN #channel") == Join("user", "#channel"))
    }
}
