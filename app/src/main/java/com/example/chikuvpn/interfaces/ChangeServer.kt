package com.example.chikuvpn.interfaces

import com.example.chikuvpn.model.Server

interface ChangeServer {
    fun newServer(server: Server)
}