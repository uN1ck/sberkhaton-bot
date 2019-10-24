package com.example.demo.csm.service;

import com.example.demo.csm.data.CsmCommand;
import im.dlg.botsdk.domain.Message;
import im.dlg.botsdk.domain.Peer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CsmRoutingService {


    private CsmRequestService csmRequestService;

    @Autowired
    public CsmRoutingService(CsmRequestService csmRequestService) {
        this.csmRequestService = csmRequestService;
    }

    public String onMessage(Peer peer, String text) {
        if (text.equals("/csm")) {
            String res = "```Вам доступны следующие команды:\n" +
                    "   /csm status - вывести статус модуля csm\n" +
                    "   /csm list - вывести список всех доступных серверов\n" +
                    "   /csm server *hostname:port* - вывести основную информацию о сервере\n" +
                    "   /csm server *hostname:port* options - вывести информацию о java input arguements\n" +
                    "   /csm server *hostname:port* stop/restart - оставновить/перезапустить jvm выбранного сервера\n" +
                    "   /csm server *hostname:port* modules - вывести основную информацию о модулях, которые расположены на данном сервере\n" +
                    "   /csm server *hostname:port* modules *moduleName* - вывести отдельную справку о интересующем вас модуле\n" +
                    "   /csm server *hostname:port* modules *moduleName* module_start/module_stop/module_restart - запустить, остановить и перезапустить модуль\n" +
                    "  ```";
            return res;
        }
        CsmCommand csmCommand = new CsmCommand(text);
        if (csmCommand.getMainCommand().equals(CsmCommand.CommandTypes.STATUS.getCommandText())) {
            return csmRequestService.getCsmStatus();
        }
        if (csmCommand.getMainCommand().equals(CsmCommand.CommandTypes.LIST.getCommandText())) {
            return csmRequestService.getAllServers();
        }
        if (csmCommand.getMainCommand().equals(CsmCommand.CommandTypes.SERVER.getCommandText())) {
            if (csmCommand.getMainArguement() == null) {
                return "Необходимо указать host:port";
            }
            CsmCommand subCommand = csmCommand.getSubCommand();
            if (subCommand == null) {
                return csmRequestService.getSingleHostData(csmCommand.getMainArguement(), false);
            } else {
                if (subCommand.getMainCommand().equals(CsmCommand.SubCommandTypes.SERVER_MODULES.getCommandText())) {
                    if (subCommand.getSubCommand() == null) {
                        if (subCommand.getMainArguement() == null) {
                            return csmRequestService.getModules(csmCommand.getMainArguement());
                        } else {
                            return csmRequestService.getSingleModule(csmCommand.getMainArguement(),subCommand.getMainArguement());
                        }
                    } else {
                        if (subCommand.getSubCommand().getMainCommand().equals(CsmCommand.SubCommandTypes.MODULE_START.getCommandText())) {
                            return csmRequestService.startModule(csmCommand.getMainArguement(), csmCommand.getSubCommand().getMainArguement(), peer);
                        }
                        if (subCommand.getSubCommand().getMainCommand().equals(CsmCommand.SubCommandTypes.MODULE_STOP.getCommandText())) {
                            return csmRequestService.stopModule(csmCommand.getMainArguement(), csmCommand.getSubCommand().getMainArguement(), peer);
                        }
                        if (subCommand.getSubCommand().getMainCommand().equals(CsmCommand.SubCommandTypes.MODULE_RESTART.getCommandText())) {
                            return csmRequestService.restartModule(csmCommand.getMainArguement(), csmCommand.getSubCommand().getMainArguement(), peer);
                        }
                        return "Такой команды нет";
                    }
                }
                if (subCommand.getMainCommand().equals(CsmCommand.SubCommandTypes.SERVER_STATUS.getCommandText())) {
                    return csmRequestService.getSingleHostData(csmCommand.getMainArguement(), true);
                }
                if (subCommand.getMainCommand().equals(CsmCommand.SubCommandTypes.SERVER_OPTIONS.getCommandText())) {
                    return csmRequestService.getServerOptions(csmCommand.getMainArguement());
                }
                if (subCommand.getMainCommand().equals(CsmCommand.SubCommandTypes.SERVER_STOP.getCommandText())) {
                    return csmRequestService.manageNode(csmCommand.getMainArguement(), false, peer);
                }
                if (subCommand.getMainCommand().equals(CsmCommand.SubCommandTypes.SERVER_RESTART.getCommandText())) {
                    return csmRequestService.manageNode(csmCommand.getMainArguement(), true, peer);
                }
                return "Такой команды нет";
            }
        }
        return "Такой команды нет";
    }
}
