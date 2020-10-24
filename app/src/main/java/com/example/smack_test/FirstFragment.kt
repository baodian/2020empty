package com.example.smack_test

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.qiniu.android.http.ResponseInfo
import com.qiniu.android.storage.UpCompletionHandler
import com.qiniu.android.storage.UpProgressHandler
import com.qiniu.android.storage.UploadManager
import com.qiniu.android.storage.UploadOptions
import org.jivesoftware.smack.*
import org.jivesoftware.smack.filter.StanzaFilter
import org.jivesoftware.smack.filter.StanzaTypeFilter
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.packet.Stanza
import org.jivesoftware.smack.provider.IQProvider
import org.jivesoftware.smack.provider.ProviderManager
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.roster.RosterEntry
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.json.JSONObject
import org.jxmpp.jid.impl.JidCreate
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.io.InputStream
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    var data:ByteArray? = null

    companion object {
        const val WRITE_EXTERNAL_STORAGE = 1
        const val OPEN_CAMERA = 2
        const val OPEN_ALBUM = 3
        const val CROP_IMAGE = 4
    }

    //iq解析器
    internal class NotificationIQProvider<T> : IQProvider<IQ>() {

        override fun parse(parser: XmlPullParser?, initialDepth: Int): IQ {

            var url:String = ""
            val notification = MainActivity.MyCustomIQ()
            var done = false
            while (!done) {
                val eventType: Int = parser!!.next()
                if (eventType == 2) {
                    if ("token" == parser!!.getName()) {

                        notification.token = parser!!.nextText()
                    }
                    else if ("url" == parser!!.getName()) {

                        notification.url = parser!!.nextText()
                        url = notification.url!!
                    }
                    else if ("key" == parser!!.getName()) {

                        notification.key = parser!!.nextText()

                    }

                } else if (eventType == 3 && "my" == parser!!.getName()) {
                    done = true
                }
            }





            return notification
        }
    }

    //iq监听器
    inner class NotificationPacketListener : StanzaListener {

        override fun processStanza(packet: Stanza?) {


            if (packet is MainActivity.MyCustomIQ) {
                val notification: MainActivity.MyCustomIQ = packet as MainActivity.MyCustomIQ
                if (notification.getChildElementXML().contains("urn:xmpp:my")) {
                    val token: String? = notification.token
                    val url: String? = notification.url
                    val key: String? = notification.key

                    Log.e("url",url!!)


                    val configuration: com.qiniu.android.storage.Configuration = com.qiniu.android.storage.Configuration.Builder()
                            .connectTimeout(10) //.zone(zone)
                            //.dns(buildDefaultDns())//指定dns服务器
                            .responseTimeout(60).build()
                    val uploadManager = UploadManager(configuration)

                    uploadManager.put(data, key, token, object : UpCompletionHandler {
                        override fun complete(key: String, info: ResponseInfo, res: JSONObject) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                Log.i("qiniu", "Upload Success")
                            } else {
                                Log.i("qiniu", "Upload Fail")
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                            }
                            Log.i("qiniu", "$key,\r\n $info,\r\n $res")
                        }
                    },
                            UploadOptions(null, null, false,
                                    object : UpProgressHandler {
                                        override fun progress(key: String, percent: Double) {
                                            Log.e("qiniu_procress", "$key: $percent")
                                        }
                                    }, null))




                }
            }
        }
    }


    private lateinit var xmpptcpConnection: XMPPTCPConnection

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        Thread{

            ProviderManager.addIQProvider("my", "urn:xmpp:my",
                    NotificationIQProvider<MainActivity.MyCustomIQ>())

            //val address:InetAddress = InetAddress.getByName("Const.ip");

//配置信息

            try {

                val config: XMPPTCPConnectionConfiguration = XMPPTCPConnectionConfiguration.builder()

                        .setXmppDomain(/*"192.168.199.240"*/"diandembp.lan")//设置xmpp域名

                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)//安全模式认证

                        .setHostAddress(InetAddress.getByName("192.168.199.240"))
                        .setCompressionEnabled(true)
                        .setPort(5222)
                        .setUsernameAndPassword("wangyuan","123456")
                        .build();

                xmpptcpConnection = XMPPTCPConnection(config);//连接类




                xmpptcpConnection.addConnectionListener(
                        object : ConnectionListener {
                            override fun connected(connection: XMPPConnection) {
                                Log.e("connected", "connected")
                            }

                            override fun authenticated(connection: XMPPConnection, resumed: Boolean) {
                                Log.e("authenticated", "authenticated")
                            }

                            override fun connectionClosed() {
                                Log.e("connectionClosed", "connectionClosed")
                            }

                            override fun connectionClosedOnError(e: Exception) {
                                Log.e("connectionClosedOnError", "connectionClosedOnError")
                            }

                        }
                )


                xmpptcpConnection.connect();
                xmpptcpConnection.login();

                val filter: StanzaFilter = StanzaTypeFilter.IQ

                xmpptcpConnection.addAsyncStanzaListener(NotificationPacketListener(), filter)//addPacketListener(NotificationPacketListener(), IQTypeFilter(IQ.Type.SET))


                val roster = Roster.getInstanceFor(xmpptcpConnection)

                if (!roster.isLoaded) roster.reloadAndWait()

                val entries: Collection<RosterEntry> = roster.entries

                for (entry in entries) println("Here: $entry")

/*
                val jid = JidCreate.entityBareFrom("baodian@diandembp.lan")
                val chat:Chat = cm.chatWith(jid)
                chat.send("12345")
*/





                /*

                val result = sf.get() as IQ





                val lll = result.extensions

                val aaa = result.getExtensions("token","urn:xmpp:my")





                val ret = result.toXML("")


                val child = result.getChildElementXML("").toString()





                val ggg = StringEscapeUtils.unescapeXml(child)


                val r = 6


                //result.getChildElementXML().element("token").toString()
*/
/*
                val child = result.getChildElementXML().toString()

                val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance() //取得DocumentBuilderFactory实例

                val builder: DocumentBuilder = factory.newDocumentBuilder() //从factory获取DocumentBuilder实例

                val doc: Document = builder.parse(child.byteInputStream()) //解析输入流 得到Document实例

                val rootElement: Element = doc.getDocumentElement()

                val token = rootElement.getAttribute("token")

 */

/*
                //创建xmlPull解析器

                //创建xmlPull解析器
                val parser: XmlPullParser = Xml.newPullParser()
                ///初始化xmlPull解析器
                ///初始化xmlPull解析器
                parser.setInput(child.byteInputStream(), "utf-8")
                //读取文件的类型
                //读取文件的类型
                var type: Int = parser.getEventType()
                //无限判断文件类型进行读取
                //无限判断文件类型进行读取
                while (type != XmlPullParser.END_DOCUMENT) {
                    when (type) {
                        XmlPullParser.START_TAG -> if ("students" == parser.getName()) {

                        } else if ("student" == parser.getName()) {

                        } else if ("my" == parser.getName()) {
                            //获取sex属性
                            val sex: String = parser.getAttributeValue(null, "token")

                            //获取name值
                            val name: String = parser.nextText()

                        } else if ("nickName" == parser.getName()) {
                            //获取nickName值
                            val nickName: String = parser.nextText()

                        }
                        XmlPullParser.END_TAG -> if ("student" == parser.getName()) {

                        }
                    }


                    val cc = parser.getAttributeCount();


                    //继续往下读取标签类型
                    type = parser.next()
                }


*/




                val ff = 4


            }
            catch (e: UnknownHostException)
            {
                e.printStackTrace();
            }
            catch (e: InterruptedException)
            {
                e.printStackTrace();
            }
            catch (e: IOException)
            {
                e.printStackTrace();
            }
            catch (e: SmackException)
            {
                e.printStackTrace();
            }
            catch (e: XMPPException)
            {
                e.printStackTrace();
            }
            catch (e: java.lang.Exception)
            {
                e.printStackTrace();
            }






        }.start()

        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            //findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)


            var ois: InputStream? = null
            try {
                ois =  getResources().getAssets().open("example.png")
            } catch (e: IOException) {
                e.printStackTrace()
            }
            data = ois!!.readBytes()


            val iq = MainActivity.MyCustomIQ()
            iq.type = IQ.Type.get
            iq.to = JidCreate.from("diandembp.lan")

            val sss = iq.toXML("").toString()


            val sf =  xmpptcpConnection.sendIqRequestAsync(iq)



        }


























    }
}