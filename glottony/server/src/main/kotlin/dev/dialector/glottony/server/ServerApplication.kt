package dev.dialector.glottony.server

import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.LanguageClient
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.Channels

class TeeOutputStream(out: OutputStream?, tee: OutputStream?) : OutputStream() {
    private val out: OutputStream
    private val tee: OutputStream

    @Throws(IOException::class)
    override fun write(b: Int) {
        out.write(b)
        tee.write(b)
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray) {
        out.write(b)
        tee.write(b)
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray, off: Int, len: Int) {
        out.write(b, off, len)
        tee.write(b, off, len)
    }

    @Throws(IOException::class)
    override fun flush() {
        out.flush()
        tee.flush()
    }

    @Throws(IOException::class)
    override fun close() {
        try {
            out.close()
        } finally {
            tee.close()
        }
    }

    init {
        if (out == null) throw NullPointerException() else if (tee == null) throw NullPointerException()
        this.out = out
        this.tee = tee
    }
}

fun main(args: Array<String>) {
    val port = /*args[0]*/ "5008"
    val socketChannel = AsynchronousServerSocketChannel.open()
    socketChannel.use {
        socketChannel.bind(InetSocketAddress("localhost", 5011))
        while (true) {
            try {
                val future = socketChannel.accept()
//                println("Creating server on port $port")
//                val serverSocket = ServerSocket(port.toInt())
                val channel: AsynchronousSocketChannel = future.get()
                val input: InputStream = Channels.newInputStream(channel)
                val output: OutputStream = Channels.newOutputStream(channel)
                val server = GlottonyLanguageServer()
                val launcher: Launcher<LanguageClient> = LSPLauncher.createServerLauncher(server, input, TeeOutputStream(System.out, output))
                val client: LanguageClient = launcher.remoteProxy
                server.connect(client)
                launcher.startListening()
                client.showMessage(MessageParams(MessageType.Info, "Server started"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}