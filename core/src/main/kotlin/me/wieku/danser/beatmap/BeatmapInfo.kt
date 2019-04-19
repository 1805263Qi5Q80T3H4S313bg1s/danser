package me.wieku.danser.beatmap

import org.hibernate.annotations.Type
import javax.persistence.*

@Entity(name = "BeatmapInfo")
open class BeatmapInfo(
    var fileVersion: Int = 0,
    var version: String = "",
    var onlineId: Int? = null,
    var audioLeadIn: Int = 0,
    var countdown: Boolean = false,
    var sampleSet: String = "Normal",
    var stackLeniency: Float = 0.7f,
    var mode: Int = 0,
    var letterboxInBreaks: Boolean = false,
    var widescreenStoryboard: Boolean = false,
    var md5: String = "",
    @Column(name = "breaks")
    @Type(type = "text")
    var breaksText: String = ""
) {

    @Id
    @GeneratedValue
    protected var id: Int? = null

    @Transient
    val breaks = ArrayList<Break>()

    @PostLoad
    protected fun parseBreaks() {
        breaks.clear()
        breaksText.split(",").forEach {
            if (it.isNotEmpty()) {
                val subSplit = it.split(":")
                breaks.add(Break(subSplit[0].toInt(), subSplit[1].toInt()))
            }
        }
    }

}