package db

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.h2.implicits._


final case class Item(name: String, id: Long)
final case class Order( items: List[Item], id:Long)

object H2DB {

  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  val xa = Transactor.fromDriverManager[IO](
    "org.h2.Driver", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "",
    Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
  )

  def save(o:Order) =
    sql"""
      insert into Order(id, items) values($o.id, $o.items)
    """.update
      .run

  def findByID(id: Long) =
    sql"""
         select * from Order where id = $id
         """
      .query[Order]
      .unique
      .transact(xa)
      .unsafeToFuture


  def findAll =
    sql"""
         select * from Order
         """
      .query[Order]
      .to[List]
      .transact(xa)
      .unsafeToFuture()

}
