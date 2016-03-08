package com.iissakin.ghostwriter.knowledge.job

@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2' )
def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
def slurper = new XmlSlurper(tagsoupParser)
def htmlParser

def rootFolder = 'D:\\IntellijIdea\\ghostwriter-data'
File root = new File(rootFolder)

('a'..'a').each { letter ->

    // alphabetical
    // def url = "http://www.metrolyrics.com/artists-${letter}.html"
    // htmlParser = slurper.parse(url)
    // def artists = htmlParser.'**'.findAll {it.name() == 'tr'}.collect({it.td[0].a.@href}).findAll({it.toString().length() > 1}).collect({it.toString()})

    // popular
    def url = "http://www.metrolyrics.com/top-artists.html"
    htmlParser = slurper.parse(url)
    def artists = htmlParser.'**'.findAll {it.name() == 'a' && it.@class.toString() == 'image'}.collect {it.@href.toString()}

    artists.each { artist ->
        try {
            def artistName = artist.substring("http://www.metrolyrics.com/".length(), artist.lastIndexOf("-lyrics"))
            File artistFile = new File(root, artistName)
            artistFile.mkdirs()
            htmlParser = slurper.parse(artist)
            def songs = htmlParser.'**'.findAll {it.@id == 'popular'}[0].div.table.tbody.tr.collect {it.td[1].a.@href.toString()}

            songs.each { song ->
                try {
                    def songName = song.substring("http://www.metrolyrics.com/".length(), song.lastIndexOf("-${artistName}"))
                    File songFile = new File(artistFile, songName + '.txt')
                    if (songFile.length() < 1) {
                        htmlParser = slurper.parse(song)
                        def text = htmlParser.'**'.find {it.@id == 'lyrics-body-text'}.p.collect {it.text()}.join("\n")
                        songFile << text
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

}
