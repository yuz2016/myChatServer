package com.tao.gate.core.server;

import com.tao.gate.core.handler.GateToLogicConnectionHandler;
import com.tao.protobuf.codec.ProtoPacketDecoder;
import com.tao.protobuf.codec.ProtoPacketEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


/**
 * Gate网关服务器到Logic逻辑服务器的连接。
 * @author Tao
 *
 */
public class GateToLogicConnection implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(GateToLogicConnection.class);
	
	private String logicIp;
	private int logicPort;
	
	
	public GateToLogicConnection(String logicIp, int logicPort) {

		this.logicIp = logicIp;
		this.logicPort = logicPort;
	}



	@Override
	public void run() {

		this.buildGateToLogicConnection();
	}
	
	
	/**
	 * 建立Gate网关服务器到Logic逻辑服务器的连接。
	 */
	public void buildGateToLogicConnection() {
		
		/**
		 * 使用Netty框架建立一条Gate网关服务器到Logic逻辑服务器的连接。
		 * 在这里：
		 * Gate是 客户端角色, Ligic是 服务器角色。
		 */
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		
		bootstrap.group(group)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {

					ChannelPipeline pipeline = ch.pipeline();
					
					pipeline.addLast("ProtobufPacketDecoder", new ProtoPacketDecoder());	//解码器
					pipeline.addLast("ProtobufPacketEncoder", new ProtoPacketEncoder());	//编码器
                    //处理从Logic发往Gate的消息的 handler
                    pipeline.addLast("GateToLogicConnectionHandler", new GateToLogicConnectionHandler());
				}
			});
		
		//连接Logic服务器
		ChannelFuture future = bootstrap.connect(this.logicIp, this.logicPort);	
		future.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {

				if(future.isSuccess()) {
					logger.info("[GateToLogicConnection] 连接建立成功!");
				}
				else {
					logger.error("[GateToLogicConnection] 连接建立失败!");
				}	
			}
		});
	}



	
}










