package executing;

import commands.PrivateAccessedDragonCommand;
import commands.RegisterCommand;
import dataTransmission.CommandFromClient;
import dataTransmission.CommandResult;

import org.apache.logging.log4j.core.Logger;

import util.DataManager;

import java.net.SocketAddress;


import util.State;

import java.util.Queue;
import java.util.concurrent.ExecutorService;

public class CommandHandler {
    private final Queue<util.Pair<CommandFromClient, SocketAddress>> queueToBeExecuted;
    private final Queue<util.Pair<CommandResult, SocketAddress>> queueToBeSent;
    private final Logger logger;
    private final DataManager dataManager;

    public CommandHandler(
            Queue<util.Pair<CommandFromClient, SocketAddress>> queueToBeExecuted,
            Queue<util.Pair<CommandResult, SocketAddress>> queueToBeSent,
            Logger logger,
            DataManager dataManager) {
        this.queueToBeExecuted = queueToBeExecuted;
        this.queueToBeSent = queueToBeSent;
        this.logger = logger;
        this.dataManager = dataManager;

    }



    public void startToHandleCommands(
            State<Boolean> isWorkingState,
            ExecutorService threadPool
    ) {
        Runnable startCheckingForAvailableCommandsToRun = new Runnable() {
            @Override
            public void run() {
                while (isWorkingState.getValue()) {
                    if (!queueToBeExecuted.isEmpty()) {
                        util.Pair<CommandFromClient, SocketAddress> pairOfCommandAndClientAddress = queueToBeExecuted.poll();
                        Runnable executeFirstCommandTack = new Runnable() {
                            @Override
                            public void run() {
                                logger.info("Starting to execute a new command");
                                assert pairOfCommandAndClientAddress != null;
                                CommandFromClient commandFromClient = pairOfCommandAndClientAddress.getFirst();
                                SocketAddress clientAddress = pairOfCommandAndClientAddress.getSecond();
                                try {

                                    executeWithValidation(commandFromClient, clientAddress);
                                } catch (Exception e) {
                                    logger.error(e.getMessage());
                                }

                                logger.info("Successfully executed the command command");


                            }
                        };
                        threadPool.submit(executeFirstCommandTack);
                    }
                }
            }
        };
        threadPool.submit(startCheckingForAvailableCommandsToRun);
    }

    private void executeWithValidation(CommandFromClient commandFromClient, SocketAddress clientAddress) {
        if (dataManager.validateUser(commandFromClient.getLogin(), commandFromClient.getPassword()) || commandFromClient.getCommand() instanceof RegisterCommand) {
            if (commandFromClient.getCommand() instanceof PrivateAccessedDragonCommand) {
                final int id = ((PrivateAccessedDragonCommand) commandFromClient.getCommand()).getDragonId();
                if (dataManager.validateOwner(commandFromClient.getLogin(), id)) {
                    queueToBeSent.add(new util.Pair<>(commandFromClient.getCommand().execute(dataManager, commandFromClient.getLogin()), clientAddress));
                } else {
                    queueToBeSent.add(new util.Pair<>(new CommandResult("You are not the owner of the object so you can't do anything with it", true), clientAddress));
                }
            } else {
                queueToBeSent.add(new util.Pair<>(commandFromClient.getCommand().execute(dataManager, commandFromClient.getLogin()), clientAddress));
            }
        } else {
            queueToBeSent.add(new util.Pair<>(new CommandResult("Invalid login or password. Command was not executed", false), clientAddress));
        }
    }

}