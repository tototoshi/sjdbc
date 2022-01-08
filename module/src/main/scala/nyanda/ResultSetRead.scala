package nyanda

import cats._
import cats.data.Kleisli
import cats.implicits._
import cats.effect._
import java.sql.ResultSet
import scala.collection.mutable.ListBuffer

trait ResultSetRead[F[_]: Applicative, T]:
  self =>

  def read(rs: ResultSet): F[T]

object ResultSetRead:
  def apply[F[_]: Applicative, T](k: Kleisli[F, ResultSet, T]): ResultSetRead[F, T] =
    new ResultSetRead[F, T] { def read(rs: ResultSet): F[T] = k.run(rs) }

trait ResultSetReadInstances[F[_]: Sync]:

  implicit def optionRead[T](implicit r: ResultSetRead[F, T]): ResultSetRead[F, Option[T]] =
    ResultSetRead[F, Option[T]] {
      Kleisli { rs =>
        for {
          hasNext <- Sync[F].delay(rs.next())
          result <- if (hasNext) r.read(rs).map(_.some) else Sync[F].pure(None)
        } yield result
      }
    }

  implicit def seqReader[T](implicit r: ResultSetRead[F, T]): ResultSetRead[F, Seq[T]] =
    ResultSetRead[F, Seq[T]] {
      Kleisli { rs =>
        Monad[F]
          .whileM[Seq, T](Sync[F].delay(rs.next())) {
            r.read(rs)
          }
      }
    }
