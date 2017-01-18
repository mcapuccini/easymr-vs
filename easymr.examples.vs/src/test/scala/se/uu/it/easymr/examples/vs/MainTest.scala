package se.uu.it.easymr.examples.vs

import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.apache.commons.io.FileUtils
import com.google.common.io.Files
import se.uu.it.easymr.EasyDocker
import java.io.File

@RunWith(classOf[JUnitRunner])
class MainTest extends FunSuite with BeforeAndAfterAll {

  val tempDir = Files.createTempDir

  test("ensure parallel execution matches serial execution") {

    val params = MainParams(
      inputPath = getClass.getResource("molecules.sdf").getPath,
      outputPath = tempDir.getAbsolutePath + "spark_results.sdf",
      molsPerRecord = 3,
      hitSize = 3,
      localContext = true)

    Main.run(params)

    val docker = new EasyDocker()
    docker.run(
      imageName = "mcapuccini/oe-docking",
      command =
        "fred -receptor /var/openeye/hiv1_protease.oeb " +
          "-dbase /molecules.sdf " +
          "-docked_molecule_file /tempDir/serial_results.sdf " +
          s"-hitlist_size 3",
      bindFiles = Seq(
        new File(getClass.getResource("molecules.sdf").getPath),
        tempDir),
      volumeFiles = Seq(new File("/molecules.sdf"), new File("/tempDir")))

  }

  override def afterAll {
    FileUtils.deleteDirectory(tempDir)
  }

}