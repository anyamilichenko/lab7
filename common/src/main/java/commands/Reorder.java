package commands;

import dataTransmission.CommandResult;
import util.DataManager;

public class Reorder extends AbstractCommand{
    public Reorder() {
        super("reorder");
    }

    @Override
    public CommandResult execute(

            DataManager dataManager,
            String username) {
        if (dataManager.getCollectionSize() == 0) return new CommandResult("Your collection is empty. The command was not executed.", true);
//        LinkedList<Dragon> dragons = dataManager.getMainData();
//        dataManager.isAscendingSort ^= true;
        try {
            dataManager.getCollectionSize();
        } catch (NumberFormatException e) {
            return new CommandResult("Your collection is empty. The command was not executed.", true);
        }

        dataManager.removeLast();

        return new CommandResult("The element was removed if it was in the data", true);
    }
}
