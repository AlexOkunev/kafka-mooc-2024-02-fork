trait Show[A] {
  def show(a: A): String
}

object Show{

  def show[A](a:A)(implicit sh: Show[A]): String = sh.show(a)

  implicit val intCanShow: Show[Int] =
    new Show[Int] {
      def show(int: Int): String = s"int $int"
    }

  implicit val stringCanShow: Show[String] =
    new Show[String] {
      def show(str: String): String = s"str $str"
    }

  def main(args: Array[String]): Unit = {
    println(show(20))
    println(show("20"))

  }
}