package dev.zerite.craftlib.protocol.version

import dev.zerite.craftlib.protocol.connection.NettyConnection
import dev.zerite.craftlib.protocol.connection.NettyServer
import dev.zerite.craftlib.protocol.connection.PacketHandler
import dev.zerite.craftlib.protocol.connection.ServerHandler
import dev.zerite.craftlib.protocol.connection.io.LengthCodec
import dev.zerite.craftlib.protocol.connection.io.PacketCodec
import dev.zerite.craftlib.protocol.packet.handshake.client.ClientHandshakePacket
import dev.zerite.craftlib.protocol.packet.login.client.ClientLoginEncryptionResponsePacket
import dev.zerite.craftlib.protocol.packet.login.client.ClientLoginStartPacket
import dev.zerite.craftlib.protocol.packet.login.server.ServerLoginDisconnectPacket
import dev.zerite.craftlib.protocol.packet.login.server.ServerLoginEncryptionRequestPacket
import dev.zerite.craftlib.protocol.packet.login.server.ServerLoginSetCompressionPacket
import dev.zerite.craftlib.protocol.packet.login.server.ServerLoginSuccessPacket
import dev.zerite.craftlib.protocol.packet.play.client.display.ClientPlayChatMessagePacket
import dev.zerite.craftlib.protocol.packet.play.client.other.ClientPlayClientStatusPacket
import dev.zerite.craftlib.protocol.packet.play.client.other.ClientPlayKeepAlivePacket
import dev.zerite.craftlib.protocol.packet.play.server.display.ServerPlayChatMessagePacket
import dev.zerite.craftlib.protocol.packet.play.server.other.ServerPlayDisconnectPacket
import dev.zerite.craftlib.protocol.packet.play.server.other.ServerPlayKeepAlivePacket
import dev.zerite.craftlib.protocol.packet.play.server.other.ServerPlaySetCompressionPacket
import dev.zerite.craftlib.protocol.packet.status.client.ClientStatusPingPacket
import dev.zerite.craftlib.protocol.packet.status.client.ClientStatusRequestPacket
import dev.zerite.craftlib.protocol.packet.status.server.ServerStatusPingPacket
import dev.zerite.craftlib.protocol.packet.status.server.ServerStatusResponsePacket
import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ServerChannel
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.ServerSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.timeout.ReadTimeoutHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import java.net.InetAddress

/**
 * Contains the mappings for packet identifiers.
 *
 * @author Koding
 * @since  0.1.0-SNAPSHOT
 */
object MinecraftProtocol : AbstractProtocol() {
    /**
     * The initial state for all new connections, only listening
     * for a packet from the client.
     */
    @JvmField
    val HANDSHAKE = protocol("Handshake", -1) {
        serverbound {
            ClientHandshakePacket {
                ProtocolVersion.MC1_7_2 to 0x00
            }
        }
    }

    /**
     * State for when the player is successfully authenticated and should
     * be receiving game updates.
     */
    @JvmField
    val PLAY = protocol("Play", 0) {
        serverbound {
            ClientPlayKeepAlivePacket {
                ProtocolVersion.MC1_7_2 to 0x00
                ProtocolVersion.MC1_9 to 0x0B
                ProtocolVersion.MC1_13 to 0x0E
                ProtocolVersion.MC1_14 to 0x0F
                ProtocolVersion.MC1_16 to 0x10
            }
            ClientPlayChatMessagePacket {
                ProtocolVersion.MC1_7_2 to 0x01
                ProtocolVersion.MC1_9 to 0x02
                ProtocolVersion.MC1_13 to 0x01
                ProtocolVersion.MC1_14 to 0x03
            }
            ClientPlayClientStatusPacket {
                ProtocolVersion.MC1_7_2 to 0x16
                ProtocolVersion.MC1_9 to 0x03
                ProtocolVersion.MC1_13 to 0x02
            }
        }
        clientbound {
            ServerPlayKeepAlivePacket {
                ProtocolVersion.MC1_7_2 to 0x00
                ProtocolVersion.MC1_9 to 0x1F
                ProtocolVersion.MC1_13 to 0x21
                ProtocolVersion.MC1_14 to 0x20
                ProtocolVersion.MC1_15 to 0x21
                ProtocolVersion.MC1_16 to 0x20
            }
            ServerPlayChatMessagePacket {
                ProtocolVersion.MC1_7_2 to 0x02
                ProtocolVersion.MC1_9 to 0x0F
                ProtocolVersion.MC1_13 to 0x0E
                ProtocolVersion.MC1_14 to 0x0F
                ProtocolVersion.MC1_16 to 0x0E
            }
            // TODO
            /*ServerPlayRespawnPacket {
                ProtocolVersion.MC1_7_2 to 0x07
                ProtocolVersion.MC1_9 to 0x33
                ProtocolVersion.MC1_14 to 0x3A
                ProtocolVersion.MC1_15 to 0x3B
            }*/
            ServerPlayDisconnectPacket {
                ProtocolVersion.MC1_7_2 to 0x40
                ProtocolVersion.MC1_9 to 0x1A
                ProtocolVersion.MC1_13 to 0x1B
                ProtocolVersion.MC1_14 to 0x1A
                ProtocolVersion.MC1_15 to 0x1B
                ProtocolVersion.MC1_16 to 0x1A
            }
            ServerPlaySetCompressionPacket {
                ProtocolVersion.MC1_8 to 0x46
                ProtocolVersion.MC1_9 to -1
            }
        }
    }

    /**
     * Handles packets relating to providing server list info.
     */
    @JvmField
    val STATUS = protocol("Status", 1) {
        serverbound {
            ClientStatusRequestPacket {
                ProtocolVersion.MC1_7_2 to 0x00
                ProtocolVersion.MC1_9 to 0x00
            }
            ClientStatusPingPacket {
                ProtocolVersion.MC1_7_2 to 0x01
                ProtocolVersion.MC1_9 to 0x01
            }
        }
        clientbound {
            ServerStatusResponsePacket {
                ProtocolVersion.MC1_7_2 to 0x00
                ProtocolVersion.MC1_9 to 0x00
            }
            ServerStatusPingPacket {
                ProtocolVersion.MC1_7_2 to 0x01
                ProtocolVersion.MC1_9 to 0x01
            }
        }
    }

    /**
     * First state after handshake to begin authenticating with the server and
     * start play.
     */
    @JvmField
    val LOGIN = protocol("Login", 2) {
        serverbound {
            ClientLoginStartPacket {
                ProtocolVersion.MC1_7_2 to 0x00
            }
            ClientLoginEncryptionResponsePacket {
                ProtocolVersion.MC1_7_2 to 0x01
            }
        }
        clientbound {
            ServerLoginDisconnectPacket {
                ProtocolVersion.MC1_7_2 to 0x00
            }
            ServerLoginEncryptionRequestPacket {
                ProtocolVersion.MC1_7_2 to 0x01
            }
            ServerLoginSuccessPacket {
                ProtocolVersion.MC1_7_2 to 0x02
            }
            ServerLoginSetCompressionPacket {
                ProtocolVersion.MC1_8 to 0x03
            }
        }
    }

    /**
     * Connect to a socket with the given address and port.
     *
     * @param  address     The address to connect to.
     * @param  port        The port of the remote address.
     * @param  build       Builds the config values.
     *
     * @author Koding
     * @since  0.1.0-SNAPSHOT
     */
    @Suppress("UNUSED")
    suspend fun connect(address: InetAddress, port: Int, build: ConnectConfig.() -> Unit = {}): NettyConnection {
        val config = ConnectConfig(address, port).apply(build)
        val connection = config.connectionFactory().apply { handler = config.handler }

        // Configure and connect
        withContext(Dispatchers.IO) {
            Bootstrap()
                .group(config.eventLoopGroup)
                .channel(config.channel)
                .handler(object : ChannelInitializer<SocketChannel>() {
                    /**
                     * Initialize the channel by registering the pipeline.
                     *
                     * @param  ch      The channel being initialized.
                     * @author Koding
                     * @since  0.1.0-SNAPSHOT
                     */
                    override fun initChannel(ch: SocketChannel) {
                        config.build(ch)
                        ch.config().isTcpNoDelay = config.noDelay
                        config.timeout.takeIf { it > 0 }
                            ?.let { ch.pipeline().addLast("readTimeout", ReadTimeoutHandler(it)) }

                        ch.pipeline()
                            .addLast("length", LengthCodec(connection))
                            .addLast("packet", PacketCodec(connection))
                            .apply { if (config.debug) addLast("logger", LoggingHandler(LogLevel.INFO)) }
                            .addLast("connection", connection)
                    }
                })
                .connect(config.address, config.port)
                .let { if (config.connectSync) it.channel().closeFuture().syncUninterruptibly() }
        }

        return connection
    }

    /**
     * Connect to a socket with the given address and port which returns
     * a future.
     *
     * @param  address     The address to connect to.
     * @param  port        The port of the remote address.
     * @param  build       Builds the config values.
     *
     * @author Koding
     * @since  0.1.2
     */
    @JvmStatic
    @Suppress("UNUSED")
    fun connectFuture(address: InetAddress, port: Int, build: ConnectConfig.() -> Unit = {}) =
        GlobalScope.future { connect(address, port, build) }


    /**
     * Listen on a socket with the given address and port.
     *
     * @param  port        The port to bind to.
     * @param  build       Builds the config values.
     *
     * @author Koding
     * @since  0.1.0-SNAPSHOT
     */
    @Suppress("UNUSED")
    suspend fun listen(port: Int, build: ListenConfig.() -> Unit = {}): NettyServer {
        val config = ListenConfig(port).apply(build)
        val server = config.serverFactory().apply { handler = config.handler }

        // Configure and listen
        withContext(Dispatchers.IO) {
            ServerBootstrap()
                .group(config.eventLoopGroup, config.eventLoopGroup)
                .channel(config.channel)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    /**
                     * Initialize the channel by registering the pipeline.
                     *
                     * @param  ch      The channel being initialized.
                     * @author Koding
                     * @since  0.1.0-SNAPSHOT
                     */
                    override fun initChannel(ch: SocketChannel) {
                        config.build(ch)
                        ch.config().isTcpNoDelay = config.noDelay

                        config.timeout.takeIf { it > 0 }
                            ?.let { ch.pipeline().addLast("readTimeout", ReadTimeoutHandler(it)) }

                        val connection = config.connectionFactory().apply {
                            this.server = server
                            this.handler = config.packetHandler
                        }

                        ch.pipeline()
                            .addLast("length", LengthCodec(connection))
                            .addLast("packet", PacketCodec(connection))
                            .apply { if (config.debug) addLast("logger", LoggingHandler(LogLevel.INFO)) }
                            .addLast("connection", connection)
                    }
                })
                .bind(config.port)
                .apply {
                    addListener { server.open(channel() as ServerChannel) }
                }
                .let { if (config.listenSync) it.syncUninterruptibly() }
        }

        return server
    }

    /**
     * Listen on a socket with the given address and port, returning
     * a future.
     *
     * @param  port        The port to bind to.
     * @param  build       Builds the config values.
     *
     * @author Koding
     * @since  0.1.2
     */
    @JvmStatic
    @Suppress("UNUSED")
    fun listenFuture(port: Int, build: ListenConfig.() -> Unit = {}) =
        GlobalScope.future { listen(port, build) }

    /**
     * Configuration for a connection being built.
     *
     * @author Koding
     * @since  0.1.0-SNAPSHOT
     */
    data class ConnectConfig(@JvmField var address: InetAddress, @JvmField var port: Int) {
        /**
         * Defines the initial packet handler for the connection.
         */
        @JvmField
        var handler: PacketHandler? = null

        /**
         * Default timeout for inactivity, causing termination of the pipeline.
         */
        @JvmField
        var timeout = 30

        /**
         * Whether we should block the current thread of the connection
         * whilst it is being initialized.
         */
        @JvmField
        var connectSync = true

        /**
         * Set the TCP No Delay value in the builder.
         */
        @JvmField
        var noDelay = true

        /**
         * Configures if we should add the logging handler to the pipeline.
         */
        @JvmField
        var debug = false

        /**
         * The event loop group we should be using as a parent for all our
         * event processing for this connection.
         */
        @JvmField
        var eventLoopGroup = if (Epoll.isAvailable()) EpollEventLoopGroup() else NioEventLoopGroup()

        /**
         * The channel type which should be associated with the event loop
         * group type.
         */
        @JvmField
        var channel = if (Epoll.isAvailable()) EpollSocketChannel::class.java else NioSocketChannel::class.java

        /**
         * The connection factory which builds a default connection.
         */
        @JvmField
        var connectionFactory = { NettyConnection(PacketDirection.SERVERBOUND) }

        /**
         * Sets the connection factory variable.
         *
         * @param  build       The connection builder.
         * @author Koding
         * @since  0.1.0-SNAPSHOT
         */
        @Suppress("UNUSED")
        fun connectionFactory(build: () -> NettyConnection) {
            // Set the factory
            connectionFactory = build
        }

        /**
         * The builder for adding additional calls to the channel.
         */
        @JvmField
        var build: SocketChannel.() -> Unit = {}

        /**
         * Sets the build variable.
         *
         * @param  build      The new build function.
         * @author Koding
         * @since  0.1.0-SNAPSHOT
         */
        @Suppress("UNUSED")
        fun build(build: SocketChannel.() -> Unit) {
            this.build = build
        }
    }

    /**
     * Configuration for a server listener.
     *
     * @author Koding
     * @since  0.1.0-SNAPSHOT
     */
    data class ListenConfig(@JvmField var port: Int) {
        /**
         * Defines the initial server handler for the connection.
         */
        @JvmField
        var handler: ServerHandler? = null

        /**
         * Defines the initial packet handler for all new connections.
         */
        @JvmField
        var packetHandler: PacketHandler? = null

        /**
         * Default timeout for inactivity, causing termination of the pipeline.
         */
        @JvmField
        var timeout = 30

        /**
         * Whether we should block the current thread of the connection
         * whilst it is being initialized.
         */
        @JvmField
        var listenSync = true

        /**
         * Set the TCP No Delay value in the builder.
         */
        @JvmField
        var noDelay = true

        /**
         * Configures if we should add the logging handler to the pipeline.
         */
        @JvmField
        var debug = false

        /**
         * The event loop group we should be using as a parent for all our
         * event processing for this connection.
         */
        @JvmField
        var eventLoopGroup = if (Epoll.isAvailable()) EpollEventLoopGroup() else NioEventLoopGroup()

        /**
         * The channel type which should be associated with the event loop
         * group type.
         */
        @JvmField
        var channel: Class<out ServerSocketChannel> =
            if (Epoll.isAvailable()) EpollServerSocketChannel::class.java else NioServerSocketChannel::class.java

        /**
         * The connection factory which builds a default connection.
         */
        @JvmField
        var serverFactory = { NettyServer() }

        /**
         * Sets the server factory variable.
         *
         * @param  build       The server builder.
         * @author Koding
         * @since  0.1.0-SNAPSHOT
         */
        @Suppress("UNUSED")
        fun serverFactory(build: () -> NettyServer) {
            // Set the factory
            serverFactory = build
        }

        /**
         * The connection factory which builds a default connection.
         */
        @JvmField
        var connectionFactory = { NettyConnection(PacketDirection.CLIENTBOUND) }

        /**
         * Sets the connection factory variable.
         *
         * @param  build       The connection builder.
         * @author Koding
         * @since  0.1.0-SNAPSHOT
         */
        @Suppress("UNUSED")
        fun connectionFactory(build: () -> NettyConnection) {
            // Set the factory
            connectionFactory = build
        }

        /**
         * The builder for adding additional calls to the channel.
         */
        @JvmField
        var build: SocketChannel.() -> Unit = {}

        /**
         * Sets the build variable.
         *
         * @param  build      The new build function.
         * @author Koding
         * @since  0.1.0-SNAPSHOT
         */
        @Suppress("UNUSED")
        fun build(build: SocketChannel.() -> Unit) {
            this.build = build
        }
    }
}
