package nyanda.example

import nyanda._
import nyanda.syntax._
import cats._
import cats.data.Kleisli
import cats.implicits._
import cats.effect._
import cats.effect.kernel.Resource
import cats.effect.std.Console
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource

case class Person(id: Int, name: String)

trait PersonDao[F[_]: Applicative] extends Dsl[F]:

  private val personGet = (RS.get[Int]("id"), RS.get[String]("name")).mapN((id, s) => Person(id, s))

  given ResultSetRead[F, Person] = ResultSetRead(personGet)

  private val ddl: SQL[F] =
    sql"""
      create table if not exists person(
        id integer not null,
        name varchar(32) not null,
        primary key(id)
      )
      """

  def createTable: QueryF[F, Int] = DB.update(ddl)

  def insert(p: Person): QueryF[F, Int] = DB.update(sql"insert into person (id, name) values (${p.id}, ${p.name})")

  def findById(id: Int): QueryF[F, Option[Person]] = DB.query(sql"select id, name from person where id = ${1}")

  def findAll: QueryF[F, Seq[Person]] = DB.query(sql"select id, name from person")

object Main extends IOApp:

  val dataSource =
    val ds = new JdbcDataSource()
    ds.setUrl("jdbc:h2:mem:example")
    ds.setUser("sa")
    ds.setPassword("")
    ds

  val person1 = Person(1, "Takahashi")
  val person2 = Person(2, "Suzuki")
  val person3 = Person(3, "Sato")

  val people =
    List(
      person1,
      person2,
      person3
    )

  def personDao[F[_]: Sync] = new PersonDao[F] {}

  def queryGroup[F[_]: Sync: Console]: Kleisli[F, Connection[F], (Option[Person], Seq[Person])] =
    for {
      _ <- personDao.createTable
      _ <- people.traverse(personDao.insert)
      result1 <- personDao.findById(1)
      _ <- Kleisli.liftF(Console[F].println(result1))
      result2 <- personDao.findAll
      _ <- Kleisli.liftF(Console[F].println(result2))
    } yield (result1, result2)

  override def run(args: List[String]): IO[ExitCode] =
    dataSource.transaction[IO].use(queryGroup[IO].run).map(_ => ExitCode.Success)

// sbt:root> example/run
// [info] running (fork) nyanda.example.Main
// [info] Some(Person(1,Takahashi))
// [info] List(Person(1,Takahashi), Person(2,Suzuki), Person(3,Sato))
