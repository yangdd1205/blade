package com.blade.server.netty;

import com.blade.Blade;
import com.blade.mvc.Const;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.concurrent.ScheduledExecutorService;

/**
 * HttpServerInitializer
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext               sslCtx;
    private final Blade                    blade;
    private final boolean                  enableGzip;
    private final boolean                  enableCors;
    private final ScheduledExecutorService service;

    public HttpServerInitializer(SslContext sslCtx, Blade blade, ScheduledExecutorService service) {
        this.sslCtx = sslCtx;
        this.blade = blade;
        this.service = service;
        this.enableGzip = blade.environment().getBoolean(Const.ENV_KEY_GZIP_ENABLE, false);
        this.enableCors = blade.environment().getBoolean(Const.ENV_KEY_CORS_ENABLE, false);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }
        if (enableGzip) {
            p.addLast(new HttpContentCompressor());
        }
        p.addLast(new HttpServerCodec(36192 * 2, 36192 * 8, 36192 * 16, false));
        p.addLast(new HttpServerExpectContinueHandler());
        p.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
        p.addLast(new ChunkedWriteHandler());
        if (enableCors) {
            CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials().build();
            p.addLast(new CorsHandler(corsConfig));
        }
        if (null != blade.webSocketPath()) {
            p.addLast(new WebSocketServerProtocolHandler(blade.webSocketPath(), null, true));
            p.addLast(new WebSockerHandler(blade));
        }
        p.addLast(new HttpServerHandler(blade, service));
    }
}
