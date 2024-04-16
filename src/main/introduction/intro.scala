object scalainto extends App {
  // everything is object
  val a = 1 + 1
  val b: Unit = println("szdf")

  val inc: Int => Int = x => x+1
  lazy val cond: Boolean = true

  val x1: String = if (cond) "1" else "2"

  val x2: Any = if (cond) println("saedfsdf") else "2"

  val x: Any = do {
    println("11111")
  } while (cond)

  val l = List(1,2,3)
  l.foreach(println)
}