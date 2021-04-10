package org.burbokop.tasks

abstract class Task {
  def exec(args: Array[String]): Unit
}
