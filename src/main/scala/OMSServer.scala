import akka.Done
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future
import scala.io.StdIn

object OMSServer {

  import db.H2DB._
  import db._
  import doobie.implicits._
  import doobie.h2.implicits._


  implicit val system = ActorSystem(Behaviors.empty, "System")
  implicit val ec = system.executionContext
  var orders: List[Order] = Nil



  implicit val itemFormat = jsonFormat2(Item)
  implicit val orderFormat = jsonFormat2(Order)

  // (fake) async database query api
  def fetchItem(itemId: Long): Future[Option[Item]] = Future {
    orders.flatMap(o => o.items).find(item => item.id == itemId)
  }

  def fetchOrder(orderId: Long): Future[Option[Order]] = findAll.map(o => o.find(o => o.id == orderId))

  def saveOrder(order: Order) : Future[Done] = {
    save(order).transact(xa).as(Done).unsafeToFuture()
  }

  def main(args: Array[String]): Unit = {

    val route =
      concat(
        get {
          pathPrefix("orders"/ LongNumber) { id =>
            val mayBeOrder = fetchOrder(id)
            onSuccess(mayBeOrder) {
              case Some(order)  => complete(order)
              case None         => complete(StatusCodes.NotFound)
            }
          }
        },

        post {
          path("orders") {
            entity(as[Order]) { order =>
              val saved = saveOrder(order)
              onSuccess(saved) {
                _ => complete("order created")
              }
            }
          }
        }
      )




    val bindingFuture = Http().newServerAt("localhost",8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

  }
}
