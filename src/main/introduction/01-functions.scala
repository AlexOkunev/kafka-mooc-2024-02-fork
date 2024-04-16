object  functions {
  def main(args: Array[String]): Unit = {
    def sum(x: Int, y:Int): Int = x+y

    sum(3,4)

    val sum2: (Int, Int) => Int = (a, b) => a+b

    val l = List((4,2), (5,0), (9,3))
    val divide: PartialFunction[(Int, Int), Int] = {
      case x if x._2 != 0 => x._1/x._2
    }

    println(l.collect(divide))


  }

}