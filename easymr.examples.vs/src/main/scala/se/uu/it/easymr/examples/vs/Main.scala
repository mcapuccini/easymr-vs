package se.uu.it.easymr.examples.vs

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.io.Text
import se.uu.it.easymr.EasyMapReduce
import java.io.PrintWriter
import scopt.OptionParser
import se.uu.it.easymr.EasyMapParams

case class MainParams(
  inputPath: String = null,
  outputPath: String = null,
  molsPerRecord: Int = SDFRecordReader.DEFAULT_SIZE.toInt,
  hitSize: Int = 10,
  localContext: Boolean = false)

object Main {

  def run(params: MainParams) = {

    // Init context
    val conf = new SparkConf()
      .setAppName("OEDocking")
    if (params.localContext) {
      conf.setMaster("local[2]")
    }
    val sc = new SparkContext(conf)
    sc.hadoopConfiguration.set(
      SDFRecordReader.SIZE_PROPERTY_NAME,
      params.molsPerRecord.toString)

    // Read
    val defaultParallelism =
      sc.getConf.get("spark.default.parallelism", "2").toInt
    val rdd = sc.hadoopFile[LongWritable, Text, SDFInputFormat](
      params.inputPath, defaultParallelism)
      .map(_._2.toString) //convert to string RDD

    // Virtual Screening
    val topHits = new EasyMapReduce(rdd)
      .map(
        imageName = "mcapuccini/oe-docking",
        command =
          "cp /input /input.sdf " +
            "&& fred -receptor /var/openeye/hiv1_protease.oeb " +
            "-dbase /input.sdf " +
            "-docked_molecule_file /tmp/output.sdf " +
            "&& cp /tmp/output.sdf /output")
      .reduce(
        imageName = "mcapuccini/oe-docking",
        command =
          "cp /input1 /input1.sdf && cp /input2 /input2.sdf " +
            "&& scorepose -receptor /var/openeye/hiv1_protease.oeb " +
            "-dbase /input1.sdf /input2.sdf " +
            "-out /tmp/output.sdf " +
            s"-hitlist_size ${params.hitSize} " +
            "&& cp /tmp/output.sdf /output")

    // Write to file
    val pw = new PrintWriter(params.outputPath)
    pw.write(topHits)
    pw.close

    // Stop context
    sc.stop

  }

  def main(args: Array[String]) {

    val defaultParams = MainParams()

    val parser = new OptionParser[MainParams]("Main") {
      head("Proof of Concept: use EasyMapReduce for virtual screening.")
      arg[String]("inputPath")
        .required
        .text("SDF library input path")
        .action((x, c) => c.copy(inputPath = x))
      arg[String]("outputPath")
        .required
        .text("SDF hits output path")
        .action((x, c) => c.copy(outputPath = x))
      opt[Int]("molsPerRecord")
        .text(s"molecules per RDD recod (default: ${defaultParams.molsPerRecord})")
        .action((x, c) => c.copy(molsPerRecord = x))
      opt[Int]("hitSize")
        .text(s"number of hits to return (default: ${defaultParams.hitSize})")
        .action((x, c) => c.copy(hitSize = x))
      opt[Unit]("localContext")
        .text(s"set to run Spark in local mode")
        .action((x, c) => c.copy(localContext = true))
    }

    parser.parse(args, defaultParams).map { params =>
      run(params)
    } getOrElse {
      sys.exit(1)
    }

  }

}