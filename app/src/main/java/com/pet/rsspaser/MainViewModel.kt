package com.pet.rsspaser

import android.util.Log
import android.util.Xml
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


/**
 * Created by tam.hs on 1/19/2024.
 */
class MainViewModel : ViewModel() {
    fun readRss(urlRss: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val url = URL(urlRss)
            val urlConnection = url.openConnection() as HttpURLConnection
            try {
                val `in`: InputStream = BufferedInputStream(urlConnection.inputStream)
                parseFeed(`in`)
            } finally {
                urlConnection.disconnect()
            }
        }
    }

    private fun parseFeed(inputStream: InputStream): List<RssNews> {
        var title = ""
        var link = ""
        var description = ""
        var publicDate = ""
        var image = ""
        var isItem = false
        val items = mutableListOf<RssNews>()
        try {
            val xmlPullParser = Xml.newPullParser()
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            xmlPullParser.setInput(inputStream, null)
            xmlPullParser.nextTag()
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                val eventType = xmlPullParser.eventType
                val name = xmlPullParser.name ?: continue
                if (eventType == XmlPullParser.END_TAG) {
                    if (name.equals("item", true)) {
                        isItem = false
                    }
                    continue
                }
                if (eventType == XmlPullParser.START_TAG) {
                    if (name.equals("item", true)) {
                        isItem = true
                        continue
                    }
                }

                Log.d("XmlParser", "Parsing name ==> $name")
                var result = ""
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.text
                    xmlPullParser.nextTag()
                }

                if (name.equals("title", true)) {
                    title = result
                } else if (name.equals("link", true)) {
                    link = result
                } else if (name.equals("description", true)) {
                    description = result
                } else if (name.equals("pubDate", true)) {
                    publicDate = result
                } else if (name.equals("media:content", true)) {
                    image = xmlPullParser.getAttributeValue(null, "url")
                    Log.d("XmlParser", "Parsing image==> $image")
                }

                if (title.isNotEmpty() && link.isNotEmpty() && description.isNotEmpty()) {
                    if (isItem) {
                        items.add(RssNews(title, description, link, publicDate, image))
                    }

                    title = ""
                    link = ""
                    description = ""
                    publicDate = ""
                    image = ""
                    isItem = false
                }
            }

            return items
        } catch (ex: Exception) {
            ex.printStackTrace()
            return listOf()
        } finally {
            inputStream.close()
        }
    }
}

data class RssNews(
    val title: String,
    val description: String,
    val link: String,
    val publicDate: String,
    val image: String
)