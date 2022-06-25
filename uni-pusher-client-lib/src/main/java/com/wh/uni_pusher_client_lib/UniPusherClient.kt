package com.wh.uni_pusher_client_lib

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject


class UniPusherClient private constructor(private val socket: Socket) {

    fun connect(): UniPusherClient {
        socket.connect()
        return this
    }

    fun disconnect() {
        socket.disconnect()
    }

    class ClientBuilder private constructor() {
        companion object {
            fun new(): ClientBuilder {
                return ClientBuilder()
            }
        }

        private var debug = false
        fun debugOn(): ClientBuilder {
            debug = true
            return this
        }

        private var debugTAG = TAG
        fun tag(t: String): ClientBuilder {
            this.debugTAG = t
            return this
        }

        private lateinit var url: String

        fun A_uniPusherUrl(url: String): ClientBuilder {
            this.url = url
            return this
        }

        private lateinit var registerDateJson: JSONObject

        fun B_register(puuid: String, cuuid: String): ClientBuilder {
            val j = JSONObject()
            j.put("puuid", puuid)
            j.put("cuuid", cuuid)
            registerDateJson = j
            return this
        }

        private lateinit var onConnectAction: () -> Unit

        fun C_onConnect(action: () -> Unit): ClientBuilder {
            onConnectAction = action
            return this
        }

        private lateinit var onRegisterResultAction: (Boolean, msg: String?) -> Unit

        fun D_onRegisterResult(action: (Boolean, msg: String?) -> Unit): ClientBuilder {
            onRegisterResultAction = action
            return this
        }

        private val onActionMap = mutableMapOf<String, (String) -> Unit>(
            "message" to { Log.d("$debugTAG - default event - message", it) }
        )

        fun E_on(event: String, action: (String) -> Unit): ClientBuilder {
            onActionMap[event] = action
            return this
        }

        fun F_build(): UniPusherClient {
            val socket = IO.socket(url)
            socket.on(Socket.EVENT_CONNECT) {
                onConnectAction.invoke()
                socket.emit("register", registerDateJson)
            }
            socket.on(Socket.EVENT_CONNECT_ERROR) {
                if (debug) {
                    Log.e(debugTAG, it.first().toString())
                }
            }
            socket.on(Socket.EVENT_DISCONNECT) {
                if (debug) {
                    Log.d(debugTAG, "disconnect")
                }
            }
            socket.on("register-result") {
                val aa = it.first() as JSONObject
                val b = aa.optBoolean("result", false)
                val r = aa.optString("reason", "null")
                onRegisterResultAction.invoke(b, r)
            }
            socket.on("from-project-server") {
                val jo = it.first() as JSONObject
                val event = jo.optString("event", "message")
                onActionMap[event]!!.invoke(jo.optString("data"))
            }
            return UniPusherClient(socket)
        }
    }

    companion object {

        val TAG = "UniPusherClient"
        fun newBuilder(): ClientBuilder {
            return ClientBuilder.new()
        }


        fun test() {
            Log.d("WP_", "test: ")
            val upc = newBuilder()
                .debugOn()
                .tag("WP_")
                .A_uniPusherUrl("http://192.168.50.159:52100")
                .B_register("123", "123")
                .C_onConnect { }
                .D_onRegisterResult { b, msg -> Log.d("WH_", "pass:$b $msg ") }
                .E_on("notify") { Log.d("WP_", "notify!!!") }
                .F_build()
                .connect()
        }
    }
}