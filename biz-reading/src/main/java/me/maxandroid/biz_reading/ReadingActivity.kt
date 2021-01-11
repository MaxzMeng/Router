package me.maxandroid.biz_reading

import androidx.appcompat.app.AppCompatActivity
import me.maxandroid.router.annotations.Destination

@Destination(
    url = "router://reading",
    description = "阅读页"
)
class ReadingActivity : AppCompatActivity() {

}