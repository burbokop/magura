import routes.GithubRoutes
import utils.ZipUtils

import java.io.{BufferedOutputStream, ByteArrayInputStream, File, FileOutputStream, FileWriter}
import java.util.zip.{ZipInputStream, ZipOutputStream}



object Main extends App {


  def downloadRepo(user: String, repo: String) = {
    GithubRoutes.downloadRepositoryZip(user, repo, "")
      .body.fold(
      Left(_),
      { data =>
        println(s"data received: ${data.length}, $data")
        ZipUtils.unzipToFolder(new ByteArrayInputStream(data), s"./result_repos/$user/$repo")
        val bos = new FileOutputStream(s"./result_repos/$user/$repo.zip")
        bos.write(data)
        bos.close()
      }
    )
  }

  downloadRepo("burbokop", "SPM")
}
