package de.rainerfaller.hsm.pi.lightcontrol;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.PostConstruct;

@Component
public class WebSocketManagerThread {

    private static Logger logger = Logger.getLogger(WebSocketManagerThread.class);

    @Autowired
    private WebSocketClient webSocketClient;

    @PostConstruct
    public void init() {

        StompSession stompSession = null;

        while (true) {
            if (stompSession == null || !stompSession.isConnected()) {
                try {

                    ListenableFuture<StompSession> f = webSocketClient.connect();
                    stompSession = f.get();
                    logger.info("Subscribing to topic using session " + stompSession);
                    webSocketClient.subscribeTopic(stompSession);

                    logger.info("Sending request for light inventory" + stompSession);
                    webSocketClient.requestLightInventory(stompSession);
                } catch (Throwable t) {
                    logger.error("" + t.getMessage());
                }
            }

            try {
                // try to reconnect every 60 seconds
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.debug("loop");
        }
    }
}
