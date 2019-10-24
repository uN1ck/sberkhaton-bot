package com.example.demo.interactive;

import com.example.demo.BotProvider;
import com.example.demo.RootHandler;
import com.example.demo.interactive.model.Button;
import com.google.common.base.Joiner;
import im.dlg.botsdk.domain.InteractiveEvent;
import im.dlg.botsdk.domain.Peer;
import im.dlg.botsdk.domain.interactive.InteractiveAction;
import im.dlg.botsdk.domain.interactive.InteractiveButton;
import im.dlg.botsdk.domain.interactive.InteractiveGroup;
import im.dlg.botsdk.light.InteractiveEventListener;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class Interactive implements InteractiveEventListener {

    protected static final String INVOKE_ACTION = "invoke";
    
    private final BotProvider botProvider;
    private final List<Category> categories;
    @Setter // циклические зависимости
    private RootHandler rootHandler;
    
    private final Map<String, CategoryInteractiveHandler> handlers = new HashMap<>();
    
    
    public void start(Peer peer) {
        List<Button> buttons = new ArrayList<>();
        for(Category category : categories)
            buttons.add(new Button(category.getCommandName(), category.getCommand()));
        
        renderButtons(peer, buttons);
    }

    @Override
    public void onEvent(InteractiveEvent event) {
        String value = event.getValue();
        if(value.startsWith(INVOKE_ACTION)) {
            String cmd = value.substring(value.indexOf(' ') + 1);
            rootHandler.onMessage(event.getPeer(), cmd);
            return;
        }
        
        String[] cmd = value.split(" ");
        CategoryInteractiveHandler handler = handlers.computeIfAbsent(cmd[0], a -> {
            Category category = categories.stream().filter(cat -> cat.getCommand().equals(a)).findAny().orElse(null);
            if(category == null) return null;
            return new CategoryInteractiveHandler(this, category);
        });
        
        if(handler == null)
            throw new IllegalStateException();
        
        handler.handle(event.getPeer(), Arrays.copyOfRange(cmd, 1, cmd.length));
    }
    
    protected void renderButtons(Peer peer, List<Button> buttons) {
        List<InteractiveAction> actions = new ArrayList<>();
        
        int counter = 0;
        for(Button button : buttons) {
            String cmd = Joiner.on(" ").join(button.getValue());
            actions.add(new InteractiveAction("bt" + counter, new InteractiveButton(cmd, button.getDisplayName())));
            counter++;
        }

        InteractiveGroup group = new InteractiveGroup(actions);
        botProvider.getBot().interactiveApi().send(peer, group);
    }
    
}
