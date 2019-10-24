package com.example.demo.jenkins;

import im.dlg.botsdk.Bot;
import im.dlg.botsdk.BotConfig;
import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

@Service
@Getter
public class BotProvider {
    private Bot bot;

    @PostConstruct
    private void init() throws InterruptedException, ExecutionException {
        BotConfig botConfig = BotConfig.Builder.aBotConfig()
                                               .withHost("hackathon-mob.transmit.im")
                                               .withPort(443)
                                               .withToken("").build();

        bot = Bot.start(botConfig).get();

    }

}