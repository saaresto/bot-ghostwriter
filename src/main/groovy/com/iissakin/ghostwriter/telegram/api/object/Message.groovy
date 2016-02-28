package com.iissakin.ghostwriter.telegram.api.object

/**
 * User: iissakin
 * Date: 28.02.2016.
 */
class Message extends UnderscoreNotationParceable {
    Integer messageId
    Integer date
    User from
    Chat chat
    String text
}
