package dev.zerite.craftlib.protocol.packet.login.server

import dev.zerite.craftlib.protocol.Packet
import dev.zerite.craftlib.protocol.PacketIO
import dev.zerite.craftlib.protocol.ProtocolBuffer
import dev.zerite.craftlib.protocol.connection.NettyConnection
import dev.zerite.craftlib.protocol.version.ProtocolVersion

/**
 * Used to implement a custom handshaking flow together with Login Plugin Response.
 *
 * Unlike plugin messages in "play" mode, these messages follow a lock-step request/response scheme,
 * where the client is expected to respond to a request indicating whether it understood.
 * The notchian client always responds that it hasn't understood, and sends an empty payload.
 *
 * @author Decobr
 */
data class ServerLoginPluginRequestPacket(var messageID: Int, var channel: String, var data: ByteArray) : Packet() {
    companion object : PacketIO<ServerLoginPluginRequestPacket> {
        override fun read(
            buffer: ProtocolBuffer,
            version: ProtocolVersion,
            connection: NettyConnection
        ) = ServerLoginPluginRequestPacket(
            buffer.readVarInt(),
            buffer.readString(),
            buffer.readByteArray()
        )

        override fun write(
            buffer: ProtocolBuffer,
            version: ProtocolVersion,
            packet: ServerLoginPluginRequestPacket,
            connection: NettyConnection
        ) {
            buffer.writeVarInt(packet.messageID)
            buffer.writeString(packet.channel)
            buffer.writeByteArray(packet.data)
        }

    }
}