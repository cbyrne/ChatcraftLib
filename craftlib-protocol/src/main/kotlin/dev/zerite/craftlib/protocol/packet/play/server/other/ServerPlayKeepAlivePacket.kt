package dev.zerite.craftlib.protocol.packet.play.server.other

import dev.zerite.craftlib.protocol.Packet
import dev.zerite.craftlib.protocol.PacketIO
import dev.zerite.craftlib.protocol.ProtocolBuffer
import dev.zerite.craftlib.protocol.connection.NettyConnection
import dev.zerite.craftlib.protocol.version.ProtocolVersion

/**
 * Sent by the server to indicate that the connection is still
 * alive and valid.
 *
 * @author Koding
 * @since  0.1.0-SNAPSHOT
 */
data class ServerPlayKeepAlivePacket(var id: Long) : Packet() {
    companion object : PacketIO<ServerPlayKeepAlivePacket> {
        override fun read(
            buffer: ProtocolBuffer,
            version: ProtocolVersion,
            connection: NettyConnection
        ): ServerPlayKeepAlivePacket {
            val keepAliveId = when {
                version >= ProtocolVersion.MC1_12_2 -> {
                    buffer.readLong()
                }
                version >= ProtocolVersion.MC1_8 -> {
                    buffer.readVarInt().toLong()
                }
                else -> {
                    buffer.readInt().toLong()
                }
            }

            return ServerPlayKeepAlivePacket(keepAliveId)
        }

        override fun write(
            buffer: ProtocolBuffer,
            version: ProtocolVersion,
            packet: ServerPlayKeepAlivePacket,
            connection: NettyConnection
        ) {
            when {
                version >= ProtocolVersion.MC1_12_2 -> {
                    buffer.writeLong(packet.id)
                }
                version >= ProtocolVersion.MC1_8 -> {
                    buffer.writeVarInt(packet.id.toInt())
                }
                else -> {
                    buffer.writeInt(packet.id.toInt())
                }
            }
        }
    }
}
