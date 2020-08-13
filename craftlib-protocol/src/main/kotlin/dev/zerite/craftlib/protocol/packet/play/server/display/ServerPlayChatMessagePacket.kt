package dev.zerite.craftlib.protocol.packet.play.server.display

import dev.zerite.craftlib.chat.component.BaseChatComponent
import dev.zerite.craftlib.protocol.Packet
import dev.zerite.craftlib.protocol.PacketIO
import dev.zerite.craftlib.protocol.ProtocolBuffer
import dev.zerite.craftlib.protocol.connection.NettyConnection
import dev.zerite.craftlib.protocol.data.registry.RegistryEntry
import dev.zerite.craftlib.protocol.data.registry.impl.MagicChatPosition
import dev.zerite.craftlib.protocol.version.ProtocolVersion
import java.util.*

/**
 * Sent by the server to display a chat message in-game.
 *
 * @author Koding
 * @since  0.1.0-SNAPSHOT
 */
data class ServerPlayChatMessagePacket @JvmOverloads constructor(
    var message: BaseChatComponent,
    var position: RegistryEntry = MagicChatPosition.CHAT,
    var sender: UUID? = null
) : Packet() {
    companion object : PacketIO<ServerPlayChatMessagePacket> {
        override fun read(
            buffer: ProtocolBuffer,
            version: ProtocolVersion,
            connection: NettyConnection
        ) = ServerPlayChatMessagePacket(
            buffer.readChat(),
            if (version >= ProtocolVersion.MC1_8) MagicChatPosition[version, buffer.readByte()
                .toInt()] else MagicChatPosition.CHAT,
            if(version >= ProtocolVersion.MC1_16) buffer.readUUID(ProtocolBuffer.UUIDMode.RAW) else null
        )

        override fun write(
            buffer: ProtocolBuffer,
            version: ProtocolVersion,
            packet: ServerPlayChatMessagePacket,
            connection: NettyConnection
        ) {
            buffer.writeChat(packet.message)
            if (version >= ProtocolVersion.MC1_8)
                buffer.writeByte(MagicChatPosition[version, packet.position, Int::class.java] ?: 0)
            if(version >= ProtocolVersion.MC1_16 && packet.sender != null)
                buffer.writeUUID(mode = ProtocolBuffer.UUIDMode.RAW, value = packet.sender!!)
        }
    }
}
