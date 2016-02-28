package com.iissakin.ghostwriter.telegram.api.object

/**
 * User: iissakin
 * Date: 28.02.2016.
 */
abstract class UnderscoreNotationParceable {

    /*
    Had to manually make transformations like "message_id" -> "messageId" due to the way objects are parsed from JSON.
     */
    def propertyMissing(String name, Object value) {
        while (name.indexOf("_") != -1) {
            StringBuilder sb = new StringBuilder(name)
            sb.setCharAt(name.indexOf("_") + 1, (sb.charAt(name.indexOf("_") + 1) as String).toUpperCase().toCharacter())
            sb.deleteCharAt(name.indexOf("_"))

            name = sb.toString()
        }

        if (this.hasProperty(name)) this[name] = value
    }
}
