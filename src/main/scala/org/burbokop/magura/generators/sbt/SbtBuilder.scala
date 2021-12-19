package org.burbokop.magura.generators.sbt

import org.burbokop.magura.generators.{Generator, MaguraFile}
import org.burbokop.magura.models.meta.RepositoryMetaData
import org.burbokop.magura.utils.FileUtils
import org.burbokop.magura.utils.FileUtils.RichFile

import java.io.File

class SbtBuilder extends Generator {
  override def proceed(
                        cache: List[RepositoryMetaData],
                        inputPath: String,
                        outputPath: String,
                        options: Generator.Options,
                        maguraFile: MaguraFile
                      ): Either[Throwable, Boolean] = {
    val env = sys.env.toSeq
    val r0 = sys.process.Process(Seq("sbt", "assembly"), Some(new File(inputPath)), env:_*).!
    if(r0 != 0) {
      println(s"sbt finished with code: ${r0}")
    }

    FileUtils.recursiveListFiles(new File(inputPath) / "target").map[Either[Throwable, Unit]] { file =>
      val path: String = file.getPath
      if (path.endsWith(".jar")) {
        FileUtils.copyFile(path, (new File(outputPath) / file.getName).getPath)
      } else {
        Right()
      }
    }
      .find(_.isLeft).getOrElse(Right()).map(_ => true)
  }
}
