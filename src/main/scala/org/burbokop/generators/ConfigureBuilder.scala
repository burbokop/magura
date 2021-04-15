package org.burbokop.generators
import org.burbokop.models.meta.RepositoryMetaData

import java.io.File

object ConfigureBuilder {

  def build(cache: List[RepositoryMetaData], inputPath: String, outputPath: String): Either[Throwable, Unit] = {
    val activeVersions = cache.map(_.currentVersion).filter(_.isDefined).map(_.get)
    println(s"build ($inputPath) activeVersions: $activeVersions")
    println(s"->: ${activeVersions.map(_.buildPath + File.separator + "headers").mkString(":")}")

    val configPath = s"$inputPath${File.separator}configure"
    if (new File(configPath).isFile) {
      val outputFolder = new File(outputPath)
      if (!outputFolder.exists()) {
        outputFolder.mkdirs();
      }
      val inputFolder = new File(inputPath)
      val permissionsChangeResult = sys.process.Process(Seq("chmod", "+x", "./configure"), inputFolder).!
      val configResult = sys.process.Process(
        Seq("./configure", s"--prefix=$outputPath"),
        inputFolder,
      ).!
      println(s"config result: $configResult (perm: $permissionsChangeResult)")

      val r0 = sys.process.Process(Seq("make"), inputFolder).!
      val r1 = sys.process.Process(Seq("make", "install"), inputFolder).!
      if (r0 == 0 && r1 == 0) {
        Right()
      } else {
        Left(CMakeBuilder.Error(s"error code: $r0, $r1"))
      }
    } else {
      Left(CMakeBuilder.Error("CMakeLists.txt not found"))
    }
  }
}

class ConfigureBuilder extends Generator {
  override def proceed(
                        cache: List[RepositoryMetaData],
                        inputPath: String,
                        outputPath: String,
                        maguraFile: MaguraFile
                      ): Either[Throwable, Boolean] = {
    ConfigureBuilder.build(cache, inputPath, outputPath).map(_ => true)
  }
}
