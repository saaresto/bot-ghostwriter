package com.iissakin.ghostwriter.knowledge.util

/**
 * User: iissakin
 * Date: 31.03.2016.
 */
class RandomUtils {

    static int safeRandomIndex(Iterable iterable) {
        int index = -1
        while (index < 0 || index > iterable.size() - 1) index = new Random().nextInt(iterable.size())
        return index
    }
}
