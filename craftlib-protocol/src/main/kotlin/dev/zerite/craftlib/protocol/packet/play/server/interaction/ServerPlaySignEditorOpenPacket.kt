package dev.zerite.craftlib.protocol.packet.play.server.interaction

import dev.zerite.craftlib.protocol.Packet
import dev.zerite.craftlib.protocol.PacketIO
import dev.zerite.craftlib.protocol.ProtocolBuffer
import dev.zerite.craftlib.protocol.connection.NettyConnection
import dev.zerite.craftlib.protocol.version.ProtocolVersion

/**
 * Sent on placement of sign.
 *
 * @author Koding
 * @since  0.1.0-SNAPSHOT
 */
data class ServerPlaySignEditorOpenPacket(
    var x: Int,
    var y: Int,
    var z: Int
) : Packet() {
    companion object : PacketIO<ServerPlaySignEditorOpenPacket> {
        override fun read(
            buffer: ProtocolBuffer,
            version: ProtocolVersion,
            connection: NettyConnection
        ) = ServerPlaySignEditorOpenPacket(
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt()
        )

        override fun write(
            buffer: ProtocolBuffer,
            version: ProtocolVersion,
            packet: ServerPlaySignEditorOpenPacket,
            connection: NettyConnection
        ) {
            buffer.writeInt(packet.x)
            buffer.writeInt(packet.y)
            buffer.writeInt(packet.z)
        }
    }
}