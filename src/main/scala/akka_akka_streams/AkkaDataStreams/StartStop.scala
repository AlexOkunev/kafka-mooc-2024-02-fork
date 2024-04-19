package akka_akka_streams.AkkaDataStreams

import akka.NotUsed
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

sealed trait Command
case class StartChild(name: String) extends Command
case class SendMessageToChild(name: String, msg: String, num: Int) extends  Command
case class StopChild(name: String) extends  Command
case object Stop extends Command

object Parent {
  def apply(): Behavior[Command] = withChildren(Map())

  def withChildren(children: Map[String, ActorRef[Command]]): Behavior[Command] =
    Behaviors.setup{ ctx =>
      Behaviors.receiveMessage{
        case StartChild(name) =>
          ctx.log.info(s"start child $name")
          val newChild = ctx.spawn(Child(), name)
          withChildren(children + (name -> newChild))
        case msg@SendMessageToChild(name, _, i) =>
          ctx.log.info(s"Send message to child $name")
          val childOption = children.get(name)
          childOption.foreach(childref => childref ! msg)
          Behaviors.same
        case StopChild(name) =>
          ctx.log.info(s"Stopping child with name $name")
          val childOption = children.get(name)
          childOption match {
            case Some(childRef) =>
              ctx.stop(childRef)
              Behaviors.same
          }
      }
    }
}

object Child {
  def apply(): Behavior[Command] = Behaviors.setup { ctx =>
    Behaviors.receiveMessage{msg =>
      ctx.log.info(s"Child got message $msg")
      Behaviors.same
    }
  }
}

object StartStopSpec extends App {
  def apply(): Behavior[NotUsed] = {
    Behaviors.setup{ctx =>
      val parent = ctx.spawn(Parent(), "parent")
      parent ! StartChild("child1")
      parent ! SendMessageToChild("child1", "1111111", 0)
      parent ! StopChild("child1")

      for (i <- 1 to 15) parent ! SendMessageToChild("child1", "222222", i)
      Behaviors.same
    }
  }

  val value = StartStopSpec()
  implicit val system = ActorSystem(value, "akka")
  Thread.sleep(5000)
  system.terminate()
}
