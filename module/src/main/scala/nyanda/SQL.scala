package nyanda

case class SQL[F[_]](statement: String, params: Seq[ParameterBinder[F]])

trait ParameterBinder[F[_]]:
  def bind(statement: PreparedStatement[F], index: Int): F[Unit]

object ParameterBinder:

  def apply[F[_], T](value: T)(using b: ParameterBind[F, T]) =
    new ParameterBinder[F]:
      def bind(statement: PreparedStatement[F], index: Int): F[Unit] = b.bind(statement, index, value)
