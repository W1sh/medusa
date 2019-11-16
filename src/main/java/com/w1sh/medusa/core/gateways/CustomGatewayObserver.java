package com.w1sh.medusa.core.gateways;

import discord4j.gateway.GatewayObserver;
import discord4j.gateway.IdentifyOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.netty.ConnectionObserver;

@Component
public class CustomGatewayObserver implements GatewayObserver {

    private static final Logger logger = LoggerFactory.getLogger(CustomGatewayObserver.class);

    @Override
    public void onStateChange(ConnectionObserver.State state, IdentifyOptions identifyOptions) {
        if (state ==ConnectionObserver.State.CONNECTED || state == ConnectionObserver.State.CONFIGURED
                || state == ConnectionObserver.State.DISCONNECTING){
            logger.info("WebSocket #{} has new state {}", identifyOptions.getShardIndex(), state);
        }
    }
}
