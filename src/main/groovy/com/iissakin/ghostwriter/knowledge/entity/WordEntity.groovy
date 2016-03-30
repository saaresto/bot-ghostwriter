package com.iissakin.ghostwriter.knowledge.entity

import com.iissakin.ghostwriter.telegram.api.object.UnderscoreNotationParceable

/**
 * User: iissakin
 * Date: 28.03.2016.
 */
class WordEntity extends UnderscoreNotationParceable {
    String content
    String metaphone
    Integer vowelCount
    ArrayList<WordEntity> followers
}