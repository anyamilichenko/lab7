package util;

import data.Dragon;
import data.User;

/**
 * This interface does everything but giving the collection itself, so no SOLID principles should be
 * violated :)
 */
public interface DataManager {

    void addUser(User user);

    void addDragon(Dragon dragon);

    boolean validateUser(String username, String password);

    boolean checkIfUsernameUnique(String username);

    boolean checkIfMin(Dragon dragon);

    void clearOwnedData(String username);

//    String filterLessThanSemesterEnumToString(Semester inpEnum);
//
    String getCollectionInfo();
    //InfoCommand.InfoCommandResult getInfoAboutCollections();

    void removeLast();
    int getCollectionSize();

    Dragon getMinByIdDragon();

    String ascendingDataToString();

    void removeDragonById(int id);

    String showSortedByName();

    void updateDragonById(int id, Dragon dragon);

    void removeGreaterIfOwned(Dragon dragon, String username);

    boolean validateOwner(String username, int dragonId);

    void initialiseData();
}