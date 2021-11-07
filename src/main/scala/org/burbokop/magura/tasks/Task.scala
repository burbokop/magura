package org.burbokop.magura.tasks

abstract class Task {
  def exec(args: Array[String]): Unit
}
