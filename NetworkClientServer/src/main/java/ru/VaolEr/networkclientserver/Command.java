package ru.VaolEr.networkclientserver;

import ru.VaolEr.networkclientserver.commands.*;

import java.io.Serializable;
import java.util.List;

public class Command implements Serializable {

    private CommandType commandType;
    private Object commandData;

    public CommandType getCommandType() {
        return commandType;
    }

    public Object getCommandData() {
        return commandData;
    }

    public static Command authenticationCommand(String login, String password){
        Command command = new Command();
        command.commandType = CommandType.AUTH;
        command.commandData = new AuthenticationCommandData(login,password);
        return  command;
    }

    public static Command authenticationOkCommand(String username){
        Command command = new Command();
        command.commandType = CommandType.AUTH_OK;
        command.commandData = new AuthenticationOkCommandData(username);
        return  command;
    }

    public static Command authenticationErrorCommand(String errorMessage){
        Command command = new Command();
        command.commandType = CommandType.AUTH_ERROR;
        command.commandData = new AuthenticationErrorCommandData(errorMessage);
        return  command;
    }

    public static Command errorCommand(String errorMessage) {
        Command command = new Command();
        command.commandType = CommandType.ERROR;
        command.commandData = new ErrorCommandData(errorMessage);
        return command;
    }

    public static Command messageInfoCommand(String message, String sender) {
        Command command = new Command();
        command.commandType = CommandType.SERVER_MESSAGE;
        command.commandData = new MessageInfoCommandData(message, sender);
        return command;
    }

    public static Command publicMessageCommand(String username, String message) {
        Command command = new Command();
        command.commandType  = CommandType.PUBLIC_MESSAGE;
        command.commandData = new PublicMessageCommandData(username, message);
        return command;
    }

    public static Command privateMessageCommand(String receiver, String message) {
        Command command = new Command();
        command.commandType  = CommandType.PRIVATE_MESSAGE;
        command.commandData = new PrivateMessageCommandData(receiver, message);
        return command;
    }

    public static Command updateUsersListCommand(List<String> users) {
        Command command = new Command();
        command.commandType  = CommandType.UPDATE_USERS_LIST;
        command.commandData = new UpdateUsersListCommandData(users);
        return command;
    }

    public static Command endCommand() {
        Command command = new Command();
        command.commandType  = CommandType.END;
        return command;
    }
}
