package com.example.demo;

import com.example.demo.stash.StashService;
import com.example.demo.stash.util.Pretty;
import im.dlg.botsdk.Bot;
import im.dlg.botsdk.BotConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class MainService {
    private final StashService stashService;

    @PostConstruct
    public void main() throws InterruptedException, ExecutionException {
        BotConfig botConfig = BotConfig.Builder.aBotConfig()
                .withHost("hackathon-mob.transmit.im")
                .withPort(443)
                .withToken("d33eadd7fa9460116631493b5076c2965d614443").build();

        Bot bot = Bot.start(botConfig).get();

        bot.messaging().onMessage(
                message -> {
                    String result;
                    try {
                        result = Pretty.toString(stashService.listRepositories("kirekov", "1234", "TEST-PROJECT"));
                    } catch (Exception e) {
                        result = e.getMessage();
                    }
                    bot.messaging().sendText(message.getPeer(), result);
                }
        );

        bot.await();
    }
}
