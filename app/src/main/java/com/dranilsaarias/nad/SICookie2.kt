/**
 * Taken from https://github.com/loopj/android-async-http
 */
package com.dranilsaarias.nad

import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.net.HttpCookie

/**
 * A simple wrapper for [HttpCookie] to work with [SiCookieStore2]
 * Gives power of serialization-deserialization to [HttpCookie]
 *
 * @author Manish
 */
class SICookie2
/**
 *
 */
(cookie: HttpCookie) : Serializable {
    var cookie: HttpCookie? = null
        private set

    init {
        this.cookie = cookie
    }

    @Throws(IOException::class)
    private fun writeObject(out: ObjectOutputStream) {
        out.writeObject(cookie!!.name)
        out.writeObject(cookie!!.value)
        out.writeObject(cookie!!.comment)
        out.writeObject(cookie!!.commentURL)
        out.writeBoolean(cookie!!.discard)
        out.writeObject(cookie!!.domain)
        out.writeLong(cookie!!.maxAge)
        out.writeObject(cookie!!.path)
        out.writeObject(cookie!!.portlist)
        out.writeBoolean(cookie!!.secure)
        out.writeInt(cookie!!.version)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(`in`: ObjectInputStream) {
        val name = `in`.readObject() as String
        val value = `in`.readObject() as String
        cookie = HttpCookie(name, value)
        cookie!!.comment = `in`.readObject() as String
        cookie!!.commentURL = `in`.readObject() as String
        cookie!!.discard = `in`.readBoolean()
        cookie!!.domain = `in`.readObject() as String
        cookie!!.maxAge = `in`.readLong()
        cookie!!.path = `in`.readObject() as String
        cookie!!.portlist = `in`.readObject() as String
        cookie!!.secure = `in`.readBoolean()
        cookie!!.version = `in`.readInt()
    }

    companion object {

        /**
         *
         */
        private const val serialVersionUID = 2532101328282342578L
    }

}
