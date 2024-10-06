package com.tylerkontra.chipless.util

import java.util.HashMap

class KeySet<K, E>(private val key: (E) -> K) {
    private val mutableMap: HashMap<K, E> = HashMap()

    fun add(element: E): Boolean {
        mutableMap.put(key(element), element)
        return true
    }

    fun contains(element: E): Boolean {
        var k = this.key(element)
        return mutableMap.contains(k)
    }

}