package com.iissakin.ghostwriter.knowledge.job

@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2' )
def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
def slurper = new XmlSlurper(tagsoupParser)
def htmlParser

def goodArtists = [
        "http://www.metrolyrics.com/depeche-mode-lyrics.html",
        "http://www.metrolyrics.com/muse-lyrics.html",
        "http://www.metrolyrics.com/arctic-monkeys-lyrics.html",
        "http://www.metrolyrics.com/hurts-lyrics.html",
        "http://www.metrolyrics.com/lana-del-rey-lyrics.html",
        "http://www.metrolyrics.com/disturbed-lyrics.html",
        "http://www.metrolyrics.com/coldplay-lyrics.html",
        "http://www.metrolyrics.com/metallica-lyrics.html",
        "http://www.metrolyrics.com/nightwish-lyrics.html",
        "http://www.metrolyrics.com/queen-lyrics.html",
        "http://www.metrolyrics.com/papa-roach-lyrics.html",
        "http://www.metrolyrics.com/poets-of-the-fall-lyrics.html",
        "http://www.metrolyrics.com/linkin-park-lyrics.html",
        "http://www.metrolyrics.com/fall-out-boy-lyrics.html",
        "http://www.metrolyrics.com/three-days-grace-lyrics.html",
        "http://www.metrolyrics.com/johnny-cash-lyrics.html",
        "http://www.metrolyrics.com/30-seconds-to-mars-lyrics.html",
        "http://www.metrolyrics.com/beatles-lyrics.html",
        "http://www.metrolyrics.com/the-scorpions-lyrics.html",
        "http://www.metrolyrics.com/skillet-lyrics.html"
]

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

    goodArtists.each { artist ->
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
