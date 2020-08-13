package dev.zerite.craftlib.protocol.packet.login.client

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
data class ClientLoginPluginResponsePacket(var messageID: Int, var successful: Boolean, var data: ByteArray? = null) : Packet() {
    companion object : PacketIO<ClientLoginPluginResponsePacket> {
        override fun read(
            buffer: ProtocolBuffer,
            version: ProtocolVersion,
            connection: NettyConnection
        ) = ClientLoginPluginResponsePacket (
            buffer.readVarInt(),
            buffer.readBoolean(),
            buffer.readByteArray()
        )

        override fun write(
            buffer: ProtocolBuffer,
            version: ProtocolVersion,
            packet: ClientLoginPluginResponsePacket,
            connection: NettyConnection
        ) {
            buffer.writeVarInt(packet.messageID)
            buffer.writeBoolean(packet.successful)

            if (packet.data != null) {
                buffer.writeByteArray(packet.data!!)
            }
        }

    }
}