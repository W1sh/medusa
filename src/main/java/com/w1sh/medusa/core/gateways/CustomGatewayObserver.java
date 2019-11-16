package com.w1sh.medusa.core.gateways;

import discord4j.gateway.GatewayObserver;
import discord4j.gateway.IdentifyOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.netty.ConnectionObserver;

@Slf4j
@Component
public class CustomGatewayObserver implements GatewayObserver {

    @Override
    public void onStateChange(ConnectionObserver.State state, IdentifyOptions identifyOptions) {
        if (state ==ConnectionObserver.State.CONNECTED || state == ConnectionObserver.State.CONFIGURED
                || state == ConnectionObserver.State.DISCONNECTING){
            log.info("WebSocket #{} has new state {}", identifyOptions.getShardIndex(), state);
        }
    }
}
