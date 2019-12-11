package com.shengshijie.sptest

import android.content.Context
import android.content.SharedPreferences
import com.shengshijie.preference.*
import com.shengshijie.sp.*

object SPHelper {

    private var sp: SharedPreferences = App.instance!!.getSharedPreferences("pay", Context.MODE_PRIVATE).apply {
        key = "2222222222222222".toByteArray()
    }

    var heartBeat by sp.string()

    var port by sp.int()

    var obj by sp.obj<String>()

    var person by sp.obj<Person>(encrypt = true)

    var scanner by sp.boolean(encrypt = true)

    var card by sp.boolean()


}