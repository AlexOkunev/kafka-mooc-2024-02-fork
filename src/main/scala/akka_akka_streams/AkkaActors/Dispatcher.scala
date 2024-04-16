package akka_akka_streams.AkkaActors

import akka.NotUsed
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka_akka_streams.AkkaActors.Dispatcher.JsonParser.{Parse, ParseResponse}
import akka_akka_streams.AkkaActors.Dispatcher.LogWorker.{LogRequest, LogResponse}
import akka_akka_streams.AkkaActors.Dispatcher.TaskDispatcher.{LogWork, ParseJson}

import java.util.UUID

object Dispatcher extends App {
  //main actor
  object TaskDispatcher {
    sealed trait CommandDispatcher

    case class ParseJson(json: String) extends CommandDispatcher
    case class LogWork(msg: String) extends CommandDispatcher

    case class LogResponseWrapper(msg: LogResponse) extends CommandDispatcher
    case class ParseResponseWrapper(msg: ParseResponse) extends CommandDispatcher

    def apply(): Behavior[CommandDispatcher] = Behaviors.setup{ctx =>
      val logAdapter: ActorRef[LogResponse] = ctx.messageAdapter[LogResponse](rs => LogResponseWrapper(rs))
      val parseAdapter: ActorRef[ParseResponse] = ctx.messageAdapter[ParseResponse](rs => ParseResponseWrapper(rs))

      Behaviors.receiveMessage{
        case LogWork(work) =>
          val logWorker: ActorRef[LogRequest] = ctx.spawn(LogWorker(), s"LogWorker${UUID.randomUUID()}")
          ctx.log.info(s"Dispatcher receive log $work")
          logWorker ! LogWorker.Log(work, logAdapter)
          Behaviors.same
        case ParseJson(json) =>
          val jsonParser = ctx.spawn(JsonParser(), s"JsonParser${UUID.randomUUID()}")
          ctx.log.info(s"Dispatcher receive json $json")
          jsonParser ! Parse(json, parseAdapter)

          Behaviors.same
        case LogResponseWrapper(msg) =>
          ctx.log.info("Log done")
          Behaviors.same
        case ParseResponseWrapper(msg) =>
          ctx.log.info("Parse done")
          Behaviors.same

      }


    }

  }

  //child actor 1
  object LogWorker{
    sealed trait LogRequest
    case class Log(l: String, replyTo: ActorRef[LogResponse]) extends  LogRequest

    sealed trait LogResponse
    case class LogDone() extends LogResponse

    def apply(): Behavior[LogRequest] = Behaviors.setup{ctx =>
      Behaviors.receiveMessage {
        case Log(l, replyTo) =>
          ctx.log.info("log work in progress")
          replyTo ! LogDone()
          Behaviors.stopped
      }
    }
  }

  //child actor 2
  object JsonParser {
    sealed trait ParseCommand
    case class Parse(json: String, replyTo: ActorRef[ParseResponse]) extends ParseCommand


    sealed trait ParseResponse
    case class ParseDone() extends  ParseResponse

    def apply(): Behavior[ParseCommand] = Behaviors.setup{ctx =>
      Behaviors.receiveMessage{
        case Parse(json, replyTo) =>
          ctx.log.info(s"parsing $json done")
          replyTo ! ParseDone()
          Behaviors.stopped
      }
    }

  }

  def apply(): Behavior[NotUsed] =
    Behaviors.setup{ctx =>
      val dispatcherActorRef = ctx.spawn(TaskDispatcher(), "dispatcher")
      dispatcherActorRef ! LogWork("bla bla bla")
      dispatcherActorRef ! ParseJson("url url url")
      Behaviors.same
    }

  implicit  val actorSystem = ActorSystem(Dispatcher(), "dispatcher")
  Thread.sleep(3000)
  actorSystem.terminate()


}