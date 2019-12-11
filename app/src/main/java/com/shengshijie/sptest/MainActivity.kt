package com.shengshijie.sptest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.shengshijie.log.HLog

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        SPHelper.card = true
//        SPHelper.scanner = true
//        SPHelper.port = 80
//        SPHelper.person = Person("xiao ming")
        HLog.s("DD", SPHelper.card)
        HLog.s("DD", SPHelper.scanner)
        HLog.s("DD", SPHelper.port)
        HLog.s("DD", SPHelper.person)
    }
}
