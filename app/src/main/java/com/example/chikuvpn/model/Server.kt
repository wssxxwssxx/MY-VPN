package com.example.chikuvpn.model

class Server(
    var country: String = "",
    var flagUrl: String = "",
    var ovpn: String = "",
    var ovpnUserName: String = "",
    var ovpnUserPassword: String = ""
) {
    constructor(country: String, flagUrl: String, ovpn: String) : this(country, flagUrl, ovpn, "", "")
}
