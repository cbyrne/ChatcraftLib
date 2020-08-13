package dev.zerite.craftlib.protocol.packet.login.client

import dev.zerite.craftlib.protocol.Packet
import dev.zerite.craftlib.protocol.PacketIO
import dev.zerite.craftlib.protocol.ProtocolBuffer
import dev.zerite.craftlib.protocol.connection.NettyConnection
import dev.zerite.craftlib.protocol.util.Crypto
import dev.zerite.craftlib.protocol.util.asSecretKey
import dev.zerite.craftlib.protocol.version.ProtocolVersion
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey

/**
 * Sent from the client to the server to begin encryption after verification
 * using the secret key and verify token.
 *
 * @author Koding
 * @since  0.1.0-SNAPSHOT
 */
data class ClientLoginEncryptionResponsePacket(var secretKey: ByteArray, var verifyToken: ByteArray) : Packet() {

    companion object : PacketIO<ClientLoginEncryptionResponsePacket> {
        override fun read(
            buffer: ProtocolBuffer,
            version: ProtocolVersion,
            connection: NettyConnection
        ) = ClientLoginEncryptionResponsePacket(
            buffer.readByteArray { if (version >= ProtocolVersion.MC1_8) readVarInt() else readShort().toInt() },
            buffer.readByteArray { if (version >= ProtocolVersion.MC1_8) readVarInt() else readShort().toInt() }
        )

        override fun write(
            buffer: ProtocolBuffer,
            version: ProtocolVersion,
            packet: ClientLoginEncryptionResponsePacket,
            connection: NettyConnection
        ) {
            buffer.writeByteArray(packet.secretKey) {
                if (version >= ProtocolVersion.MC1_8) writeVarInt(it) else writeShort(
                    it
                )
            }
            buffer.writeByteArray(packet.verifyToken) {
                if (version >= ProtocolVersion.MC1_8) writeVarInt(it) else writeShort(
                    it
                )
            }
        }
    }

    @Suppress("UNUSED")
    constructor(publicKey: PublicKey, secretKey: SecretKey, verifyToken: ByteArray) :
            this(
                Crypto.encrypt(publicKey, secretKey.encoded),
                Crypto.encrypt(publicKey, verifyToken)
            )

    /**
     * Gets the secret key bytes and decodes it into a proper
     * secret key object which we can use.
     *
     * @param  key        The key to use to decrypt the secret.
     * @author Koding
     * @since  0.1.0-SNAPSHOT
     */
    fun getSecretKey(key: PrivateKey) = secretKey.asSecretKey(key)

    /**
     * Decrypts the verify token using our private key.
     *
     * @param  key        The key to decrypt the verify token with.
     * @author Koding
     * @since  0.1.0-SNAPSHOT
     */
    fun getVerifyToken(key: PrivateKey) = Crypto.decrypt(key, verifyToken)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientLoginEncryptionResponsePacket

        if (!secretKey.contentEquals(other.secretKey)) return false
        if (!verifyToken.contentEquals(other.verifyToken)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = secretKey.contentHashCode()
        result = 31 * result + verifyToken.contentHashCode()
        return result
    }

}
