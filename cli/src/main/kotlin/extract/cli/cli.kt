package extract.cli

import java.io.File
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option

data class Options(
        @set:Option(
                name = "-extracts", aliases = ["-f"],
                usage = "Path to extracts file. \"repository/.extracts\" path is used by default.")
        var extracts: File? = null,

        @set:Option(name = "-number", aliases = ["-n"], usage = "Limit the number of commits to output. Default is 50.")
        var number: Int = 50,

        @set:Option(name = "-open", usage = "Is result history should be open automatically. Default is TRUE.")
        var boolean: Boolean = true,

        @set:Option(
                name = "-html-log",
                usage = "Output html file. \"extracts.html\" in working directory will be generated by default.")
        var output: File = File("extracts.html"),

        @set:Option(name = "-help", help = true, usage = "Get the program options help")
        var help: Boolean = false,

        @set:Option(name = "-repository", aliases = ["-r"])
        var repository: File = File(".git")

)

class ParserException(message: String?, val usage: String, cause: Throwable) : Exception(message, cause)

object Runner {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val (options, usage) = parseArguments(args)
            if (options.help) {
                println(usage)
            }

            println(options)
        } catch (ex: ParserException) {
            System.err.println(ex.message)
            System.err.println(ex.usage)
        }
    }

    data class OptionsParseResult(val options: Options, val usage: String)

    @Throws(exceptionClasses = [(ParserException::class)])
    fun parseArguments(args: Array<String>): OptionsParseResult {
        val options = Options()
        val cmdLineParser = CmdLineParser(options)

        val usage = ByteOutputStream().use { outStream ->
            cmdLineParser.printUsage(outStream)
            outStream.toString()
        }

        try {
            cmdLineParser.parseArgument(*args)
            return OptionsParseResult(options, usage)
        } catch (ex: CmdLineException) {
            throw ParserException(ex.message, usage, ex)
        }
    }
}