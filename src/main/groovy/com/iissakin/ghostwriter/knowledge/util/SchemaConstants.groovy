package com.iissakin.ghostwriter.knowledge.util

/**
 * These constants are used for making OrientGraph queries consistent.
 * todo: consider inventing ORM
 *
 * User: iissakin
 * Date: 28.02.2016.
 */

class Word {
    public static final String CLASS = 'Word'
    public static final String CONTENT = 'content'
    public static final String VOWEL_COUNT = 'vowel_count'
    public static final String METAPHONE = 'metaphone'
}

class Follows {
    public static final String CLASS = 'Follows'
    public static final String COUNT = 'count'
    public static final String ARTISTS = 'artists'
}

class UpdatesTracker {
    public static final String CLASS = 'UpdatesTracker'
    public static final String LAST = 'last_update'
}

class Author {
    public static final String CLASS = 'Author'
    public static final String NAME = 'name'
}