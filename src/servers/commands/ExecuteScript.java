package servers.commands;

import common.CommandRequest;
import common.CommandResponse;
import servers.interfacess.Command;
import servers.managers.CollectionManager;
import servers.managers.FileManager;

public class ExecuteScript implements Command {
    @Override
    public CommandResponse execute(CommandRequest request,
                                   CollectionManager collectionManager,
                                   FileManager fileManager,
                                   String dataFile) {
        return new CommandResponse(false,
                "Команда 'execute_script' выполняется на стороне клиента. " +
                        "Проверьте, что файл скрипта находится на клиентской машине.");
    }

    @Override
    public String getDescription() {
        return "Считать и исполнить скрипт из указанного файла (выполняется на клиенте)";
    }

    @Override
    public String getName() {
        return "execute_script";
    }
}