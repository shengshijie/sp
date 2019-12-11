package com.shengshijie.sp

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private inline fun <T> SharedPreferences.proxy(
    key: String? = null,
    default: T,
    encrypt: Boolean = false,
    crossinline getter: SharedPreferences.(String, T, Boolean) -> T,
    crossinline setter: Editor.(String, T, Boolean) -> Editor
): ReadWriteProperty<Any, T> =
    object : ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T =
            getter(key ?: property.name, default, encrypt)!!

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
            edit().setter(key ?: property.name, value, encrypt).apply()
    }

fun SharedPreferences.int(
    key: String? = null,
    default: Int = 0,
    encrypt: Boolean = false
): ReadWriteProperty<Any, Int> {
    return proxy(key, default, encrypt, SharedPreferences::getWrapInt, Editor::putWrapInt)
}

fun SharedPreferences.long(
    key: String? = null,
    default: Long = 0,
    encrypt: Boolean = false
): ReadWriteProperty<Any, Long> {
    return proxy(key, default, encrypt, SharedPreferences::getWrapLong, Editor::putWrapLong)
}

fun SharedPreferences.float(
    key: String? = null,
    default: Float = 0f,
    encrypt: Boolean = false
): ReadWriteProperty<Any, Float> {
    return proxy(key, default, encrypt, SharedPreferences::getWrapFloat, Editor::putWrapFloat)
}

fun SharedPreferences.boolean(
    key: String? = null,
    default: Boolean = false,
    encrypt: Boolean = false
): ReadWriteProperty<Any, Boolean> {
    return proxy(key, default, encrypt, SharedPreferences::getWrapBoolean, Editor::putWrapBoolean)
}

fun SharedPreferences.string(
    key: String? = null,
    default: String = "",
    encrypt: Boolean = false
): ReadWriteProperty<Any, String> {
    return proxy(key, default, encrypt, SharedPreferences::getWrapString, Editor::putWrapString)
}

fun SharedPreferences.stringSet(
    key: String? = null,
    default: Set<String> = emptySet(),
    encrypt: Boolean = false
): ReadWriteProperty<Any, Set<String>> {
    return proxy(key, default, encrypt, SharedPreferences::getWrapStringSet, Editor::putWrapStringSet)
}

inline fun <reified T> SharedPreferences.obj(key: String? = null, encrypt: Boolean = false) =
    object : ReadWriteProperty<Any, T?> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T? {
            val s = getWrapString(key ?: property.name, "", encrypt)
            return if (s.isBlank()) null else Gson().fromJson(s, T::class.java)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) =
            edit().putWrapString(key ?: property.name, Gson().toJson(value), encrypt).apply()
    }

inline fun <reified T> SharedPreferences.list(key: String? = null, encrypt: Boolean = false) =
    object : ReadWriteProperty<Any, List<T>> {
        override fun getValue(thisRef: Any, property: KProperty<*>): List<T> {
            val s = getWrapString(key ?: property.name, "", encrypt)
            return if (s.isBlank()) emptyList() else Gson().fromJson<List<T>>(
                s,
                object : TypeToken<List<T>>() {}.type
            )
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: List<T>) =
            edit().putWrapString(key ?: property.name, Gson().toJson(value), encrypt).apply()
    }

fun SharedPreferences.getWrapInt(key: String, default: Int, encrypt: Boolean): Int {
    return unwrap(encrypt, getString(wrap(encrypt, key), null) ?: return default).toInt()
}

fun Editor.putWrapInt(key: String, value: Int, encrypt: Boolean): Editor {
    putString(wrap(encrypt, key), wrap(encrypt, value.toString()))
    return this
}

fun SharedPreferences.getWrapLong(key: String, default: Long, encrypt: Boolean): Long {
    return unwrap(encrypt, getString(wrap(encrypt, key), null) ?: return default).toLong()
}

fun Editor.putWrapLong(key: String, value: Long, encrypt: Boolean): Editor {
    putString(wrap(encrypt, key), wrap(encrypt, value.toString()))
    return this
}

fun SharedPreferences.getWrapFloat(key: String, default: Float, encrypt: Boolean): Float {
    return unwrap(encrypt, getString(wrap(encrypt, key), null) ?: return default).toFloat()
}

fun Editor.putWrapFloat(key: String, value: Float, encrypt: Boolean): Editor {
    putString(wrap(encrypt, key), wrap(encrypt, value.toString()))
    return this
}

fun SharedPreferences.getWrapBoolean(key: String, default: Boolean, encrypt: Boolean): Boolean {
    return unwrap(encrypt, getString(wrap(encrypt, key), null) ?: return default).toBoolean()
}

fun Editor.putWrapBoolean(key: String, value: Boolean, encrypt: Boolean): Editor {
    putString(wrap(encrypt, key), wrap(encrypt, value.toString()))
    return this
}

fun SharedPreferences.getWrapString(key: String, default: String, encrypt: Boolean): String {
    return unwrap(encrypt, getString(wrap(encrypt, key), null) ?: return default)
}

fun Editor.putWrapString(key: String, value: String, encrypt: Boolean): Editor {
    putString(wrap(encrypt, key), wrap(encrypt, value))
    return this
}

fun SharedPreferences.getWrapStringSet(key: String, defaults: Set<String>, encrypt: Boolean): Set<String> {
    val encryptSet = getStringSet(wrap(encrypt, key), null) ?: return defaults
    val decryptSet = HashSet<String>()
    for (encryptValue in encryptSet) {
        decryptSet.add(unwrap(encrypt, encryptValue))
    }
    return decryptSet
}

fun Editor.putWrapStringSet(key: String, values: Set<String>, encrypt: Boolean): Editor {
    val encryptSet = HashSet<String>()
    for (value in values) {
        encryptSet.add(wrap(encrypt, value))
    }
    putStringSet(wrap(encrypt, key), encryptSet)
    return this
}

fun wrap(encrypt: Boolean, plain: String): String {
    return if (encrypt) aesEncrypt(plain, key) else plain
}

fun unwrap(encrypt: Boolean, chip: String): String {
    return if (encrypt) aesDecrypt(chip, key) else chip
}

var key: ByteArray = "1111111111111111".toByteArray()

fun aesEncrypt(dataByteArray: String, keyByteArray: ByteArray): String {
    return try {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyByteArray, "AES"))
        Base64.encodeToString(cipher.doFinal(dataByteArray.toByteArray()), Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun aesDecrypt(cipherText: String, keyByteArray: ByteArray): String {
    return try {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(keyByteArray, "AES"))
        String(cipher.doFinal(Base64.decode(cipherText, Base64.DEFAULT)))
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}
