package com.iissakin.ghostwriter.knowledge.util

/**
 * These constants are used for making OrientGraph queries consistent.
 * todo: consider inventing ORM
 *
 * User: iissakin
 * Date: 28.02.2016.
 */

class Word {
    public static final String CLASS = "Word"
    public static final String CONTENT = "content"
}

class Follows {
    public static final String CLASS = "Follows"
    public static final String COUNT = "count"
}

class UpdatesTracker {
    public static final String CLASS = "UpdatesTracker"
    public static final String LAST = "last_update"
}