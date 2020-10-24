package com.example.smack_test

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.qiniu.android.http.ResponseInfo
import com.qiniu.android.storage.UpCompletionHandler
import com.qiniu.android.storage.UploadManager
import org.jivesoftware.smack.*
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.filter.StanzaFilter
import org.jivesoftware.smack.filter.StanzaTypeFilter
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.Stanza
import org.jivesoftware.smack.provider.IQProvider
import org.jivesoftware.smack.provider.ProviderManager
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.roster.RosterEntry
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jivesoftware.smackx.offline.OfflineMessageManager
import org.json.JSONObject
import org.jxmpp.jid.impl.JidCreate
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException


class MainActivity : AppCompatActivity() {










    internal class MyCustomIQ() : IQ("my", "urn:xmpp:my") {
        var token: String? = null
        var url: String? = null
        var key: String? = null
        override fun getIQChildElementBuilder(xml: IQChildElementXmlStringBuilder): IQChildElementXmlStringBuilder {
            //  String queryId = prefix + Long.toString(new AtomicLong().incrementAndGet());
            //xml.attribute("queryid", queryId)
            xml.rightAngleBracket()
            return xml
        }
    }






    private lateinit var appBarConfiguration: AppBarConfiguration



    private lateinit var cm: ChatManager

    private lateinit var offlineManager:OfflineMessageManager


    var msgListener: MessageListener = object : MessageListener {
        override fun processMessage(message: Message) {
            if (message != null && message.getBody() != null) {
                System.out.println("收到消息:" + message.getBody())
                // 可以在这进行针对这个用户消息的处理，但是这里我没做操作，看后边聊天窗口的控制
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))





        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }








    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}