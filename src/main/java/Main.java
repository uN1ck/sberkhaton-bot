import java.util.concurrent.ExecutionException;

import im.dlg.botsdk.Bot;
import im.dlg.botsdk.BotConfig;

public class Main {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        BotConfig botConfig = BotConfig.Builder.aBotConfig()
                .withHost("grpc-test.transmit.im")
                .withPort(9443)
                .withToken("e60137c00345e62ea8a21506cfe31b2be10852ec").build();

        Bot bot = Bot.start(botConfig).get();

        bot.messaging().onMessage(message ->
                bot.users().get(message.getSender()).thenAccept(userOpt -> userOpt.ifPresent(user -> {
                            System.out.println("Got a message: " + message.getText() + " from user: " + user.getName());
                        })
                ).thenCompose(aVoid -> {
                            bot.messaging().sendText(message.getPeer(), "Reply to : " + message.getMessageContent().toString());
                            return null;
                        }
                ).thenAccept(uuid ->
                        System.out.println("Sent a message with UUID: " + uuid)));

        bot.await();
    }
}