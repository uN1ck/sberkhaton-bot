package com.example.demo.csm.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.List;

@Data
@EqualsAndHashCode
public class CsmCommand {
    //  private String prefix;
    private String mainCommand;
    private String mainArguement;
    private CsmCommand subCommand;

    public CsmCommand(String rawCommand) {
        if (rawCommand.startsWith("/csm")) {
            rawCommand = rawCommand.substring(4).trim();
        }
        List<String> parts = Arrays.asList(rawCommand.split(" "));
        setMainCommand(parts.get(0));

        if (parts.size() > 1)
            setMainArguement(parts.get(1));
        if (parts.size() > 2) {
            String rawSubCommand = "";
            for (int i = 2; i < parts.size(); i++) {
                rawSubCommand += parts.get(i) + " ";
            }
            rawSubCommand.trim();
            setSubCommand(new CsmCommand(rawSubCommand));
        }
    }

    public enum CommandTypes {
        STATUS("status"), LIST("list"), SERVER("server");
        private String commandText;

        CommandTypes(String command) {
            commandText = command;
        }

        public String getCommandText() {
            return commandText;
        }
    }

    public enum SubCommandTypes {
        SERVER_STATUS("status"), SERVER_OPTIONS("options"), SERVER_STOP("stop"),
        SERVER_RESTART("restart"), SERVER_FAV("fav"), SERVER_UNFAV("unfav"),
        SERVER_MODULES("modules"),
        MODULE_START("module_start"), MODULE_STOP("module_stop"),MODULE_RESTART("module_restart");
        private String commandText;

        SubCommandTypes(String commandText) {
            this.commandText = commandText;
        }

        public String getCommandText() {
            return commandText;
        }
    }
}
