package me.maxandroid.router

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import me.maxandroid.router.annotations.Destination
import me.maxandroid.router_runtime.Router

@Destination(
    url = "router://page-home",
    description = "应用主页"
)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btn).setOnClickListener { v ->
            Router.go(
                v.context,
                "router://imooc/profile?name=imooc&message=hello"
            )
        }
    }
}