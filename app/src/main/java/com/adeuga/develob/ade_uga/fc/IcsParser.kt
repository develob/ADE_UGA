package com.adeuga.develob.ade_uga.fc

import android.os.AsyncTask
import java.io.BufferedInputStream
import java.net.URL
import java.util.*
import java.net.URLConnection
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList


/**
 * ICS Parser (parse ics calendar field) to return a CalendarEvents list
 * WARNING : Not all keywords are supported
 * WARNING : Syntax errors doesn't handled
 * WARNING : Develop for a specific purpose, see "to-do" note below to improve this class and understand its limitations
 * TODO : Change the class to return an object more generic to represent the ics file, and handle the complete ics keywords / functionality
 * TODO : So probably create a new package to handle the ics parser / calendar object generator
 */
class IcsParser(val idResource:Int, val date:Date) : AsyncTask<Void, Void, ArrayList<CalendarEvent>>()  {

    private var content : String? = null

    constructor(idResource: Int, date : Date, content:String) : this(idResource, date) {
        this.content = content
    }


    /**
     * Get the content text of the ics file (from an url) and return a Calendar object (build form buildCalendar method)
     * or return null
     */
    override fun doInBackground(vararg params: Void?): ArrayList<CalendarEvent>? {
        val c : String? = this.content
        if(c == null ) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val dateChoosed:String = dateFormat.format(date)
            val urlStr = "https://ade6-ujf-ro.grenet.fr/jsp/custom/modules/plannings/anonymous_cal.jsp?resources=11608&projectId=7&calType=ical&firstDate=$dateChoosed&lastDate=$dateChoosed"
            val url = URL(urlStr)
            val conection: URLConnection = url.openConnection()

            try {
                conection.connect()
            }catch (t : Throwable) {
                return null
            }

            val lenghtOfFile:Int = conection.contentLength
            val inBuff = BufferedInputStream(url.openStream())
            var dataBuff = ByteArray(1024)

            var byteReaded = inBuff.read(dataBuff, 0, 1024)
            if(byteReaded == -1) { //error
                return null
            }
            var content = String(dataBuff,0, byteReaded)
            var tmp = 0
            while(byteReaded < lenghtOfFile) {
                tmp = inBuff.read(dataBuff, 0, 1024)
                if(tmp == -1) break
                byteReaded += tmp
                content  += String(dataBuff, 0, tmp)
            }
            this.content = content
            return this.buildCalendar(content)
        } else {
            return this.buildCalendar(c)
        }
    }


    /**
     * Build the calendar from the [ics] string (that represent an ics content)
     */
    fun buildCalendar(ics:String) :ArrayList<CalendarEvent> {
        val lines:Array<String> = ics.split("\n").toTypedArray()
        var i = 0
        val size = lines.size
        var events = ArrayList<CalendarEvent>()

        while(i<size) {

            while(i<size) {
                i+=1
                if(lines[i-1].trim() == "BEGIN:VEVENT")
                    break
            }
            if(i>=size)
                break

            var currentEvent = CalendarEvent()
            var type = ""
            var content = ""
            while(i<size && lines[i].trim() != "END:VEVENT") { // Loop over event attributes

                val l: String = lines[i]
                val lToArray: Array<String> = l.split(":", limit = 2).toTypedArray()

                when (lToArray[0].trim()) {
                    "DTSTART" ->     {type="start"
                                    content = lToArray[1].trim()}
                    "DTEND" ->      {type="end"
                                    content = lToArray[1].trim()}
                    "SUMMARY" ->    {type="title"
                                    content = lToArray[1].trim()}
                    "LOCATION" ->   {type="location"
                                    content = lToArray[1].trim()}
                    "DESCRIPTION" -> {type="description"
                                     content = lToArray[1].trim()}
                    "STATUS", "CATEGORIES", "TRANSP", "SEQUENCE", "UID", "CREATED", "LAST-MODIFIED", "DTSTAMP" -> type="unknown"

                    else -> {
                        when(type) {
                            "title", "description", "location" -> content += l
                        }
                    }
                }
                when(type) {
                    "start" -> currentEvent.begin = toDate(content)
                    "end" -> currentEvent.end = toDate(content)
                    "title" -> currentEvent.title = content
                    "description" -> currentEvent.description = content
                    "location" -> currentEvent.location = content
                }
                i+=1
            }
            events.add(currentEvent)
            i+=1
        }
        return events
    }


    /**
     * Get the date from a string
     */
    fun toDate(str:String) : Date {
        val dataFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")
        dataFormat.timeZone = TimeZone.getTimeZone("GMT")
        val date:Date = dataFormat.parse(str.trim())
        return date
    }


    /**
     * Return the ics file content (notably used to store data in database / cache)
     */
    fun getContent() : String = this.content?:""



}