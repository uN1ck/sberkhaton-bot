package com.example.demo;

import im.dlg.botsdk.Bot;
import im.dlg.botsdk.BotConfig;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

@Service
public class MainService {
    @PostConstruct
    public void main() throws InterruptedException, ExecutionException {
        BotConfig botConfig = BotConfig.Builder.aBotConfig()
                                               .withHost("hackathon-mob.transmit.im")
                                               .withPort(443)
                                               .withToken("").build();

        Bot bot = Bot.start(botConfig).get();

        bot.messaging().onMessage(message -> {
            bot.messaging().sendText(message.getPeer(), "REPLIED! " + message.getText());
        });

        // bot.await();
    }


}
