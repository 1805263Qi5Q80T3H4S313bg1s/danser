package me.wieku.danser.beatmap.parsing

import me.wieku.danser.beatmap.Beatmap
import me.wieku.framework.resource.FileHandle
import java.util.*

class BeatmapParser {

    private val CURRENT_VERSION = 14

    var beatmap: Beatmap? = null
    var timeOffset = 0

    fun parse(file: FileHandle, beatmap: Beatmap) {
        var scanner = Scanner(file.fileURL.openStream())

        val fileVersion = scanner.nextLine().substringAfter("osu file format v").toInt()

        //Some strange osu things (see https://github.com/ppy/osu/blob/master/osu.Game/Beatmaps/Formats/LegacyBeatmapDecoder.cs#L42)
        if (fileVersion < 5) {
            timeOffset = 24
        }

        println("Parsing file version $fileVersion")

        beatmap.beatmapInfo.fileVersion = fileVersion
        beatmap.beatmapSet.directory = file.filePath!!.parent.toString()

        this.beatmap = beatmap

        var section = Section.Unknown
        while (scanner.hasNext()) {
            val line = scanner.nextLine().trim()

            if (line.isEmpty() || line.isBlank() || line.startsWith("//") || line.startsWith(" ") || line.startsWith("_")) {
                continue
            }

            if (line.startsWith("[") && line.endsWith("]")) {
                section = Section[line.substring(1, line.length-1)]
                continue
            }

            var splittedLine = line.split(section.separator).map { it.trim() }

            when(section) {
                Section.General -> parseGeneral(splittedLine)
                Section.Metadata -> parseMetadata(splittedLine)
                Section.Difficulty -> parseDifficulty(splittedLine)
            }

        }

    }

    private fun parseGeneral(line: List<String>) {
        when (line[0]) {
            "AudioFilename" -> beatmap?.beatmapMetadata?.audioFile = line[1]
            "AudioLeadIn" -> beatmap?.beatmapInfo?.audioLeadIn = getOffset(line[1].toInt())
            "PreviewTime" -> beatmap?.beatmapMetadata?.previewTime = getOffset(line[1].toInt())
            "Countdown" -> beatmap?.beatmapInfo?.countdown = line[1].toInt() == 1
            "SampleSet" -> beatmap?.beatmapInfo?.sampleSet = line[1].toString()
            "StackLeniency" -> beatmap?.beatmapInfo?.stackLeniency = line[1].toFloat()
            "Mode" -> beatmap?.beatmapInfo?.mode = line[1].toInt()
            "LetterboxInBreaks" -> beatmap?.beatmapInfo?.letterboxInBreaks = line[1].toInt() == 1
            "WidescreenStoryboard" -> beatmap?.beatmapInfo?.widescreenStoryboard = line[1].toInt() == 1
        }
    }

    private fun parseMetadata(line: List<String>) {
        when (line[0]) {
            "Title" -> beatmap?.beatmapMetadata?.title = line[1]
            "TitleUnicode" -> beatmap?.beatmapMetadata?.titleUnicode = line[1]
            "Artist" -> beatmap?.beatmapMetadata?.artist = line[1]
            "ArtistUnicode" -> beatmap?.beatmapMetadata?.artistUnicode = line[1]
            "Creator" -> beatmap?.beatmapMetadata?.creator = line[1]
            "Version" -> beatmap?.beatmapInfo?.version = line[1]
            "Source" -> beatmap?.beatmapMetadata?.source = line[1]
            "Tags" -> beatmap?.beatmapMetadata?.tags = line[1]
            "BeatmapID" -> beatmap?.beatmapInfo?.onlineId = line[1].toInt()
            "BeatmapSetID" -> beatmap?.beatmapSet?.onlineId = line[1].toInt()
        }
    }

    private fun parseDifficulty(line: List<String>) {
        when (line[0]) {
            "HPDrainRate" -> beatmap?.beatmapDifficulty?.hpDrain = line[1].toFloat()
            "CircleSize" -> beatmap?.beatmapDifficulty?.cs = line[1].toFloat()
            "OverallDifficulty" -> beatmap?.beatmapDifficulty?.od = line[1].toFloat()
            "ApproachRate" -> beatmap?.beatmapDifficulty?.ar = line[1].toFloat()
            "SliderMultiplier" -> beatmap?.beatmapDifficulty?.sliderMultiplier = line[1].toFloat()
            "SliderTickRate" -> beatmap?.beatmapDifficulty?.sliderTickRate = line[1].toFloat()
        }
    }

    private fun getOffset(time: Int): Int {
        return time+timeOffset
    }

    private fun getOffset(time: Float): Float {
        return time+timeOffset.toFloat()
    }
}