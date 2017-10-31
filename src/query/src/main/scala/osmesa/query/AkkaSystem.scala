package osmesa.query

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer


object AkkaSystem {
  implicit val system = ActorSystem("osmesa-query")
  implicit val materializer = ActorMaterializer()
}

