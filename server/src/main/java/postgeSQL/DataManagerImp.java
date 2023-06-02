package postgeSQL;

import data.Dragon;
import data.User;
import org.apache.logging.log4j.core.Logger;
import util.DataManager;
import util.Encryptor;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class DataManagerImp implements DataManager {
    private final Database database;
    private LinkedList<Dragon> mainData = new LinkedList<>();
    private LinkedList<User> users = new LinkedList<>();
    private final Logger logger;
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final Date dateOfInitialization = new Date();


    public DataManagerImp(Database database, Logger logger) {
        this.database = database;
        this.logger = logger;


    }

    @Override
    public void addUser(User user) {
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            final User encryptedUser = new User(user.getId(), Encryptor.encryptThisString(user.getPassword()), user.getName());

            final int generatedId;

            try {
                generatedId = database.getUsersTable().add(encryptedUser);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            encryptedUser.setId(generatedId);
            users.add(encryptedUser);

            logger.info("Successfully registered a new user: " + encryptedUser);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void addDragon(Dragon dragon) {
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            final int generatedId = database.getDragonTable().add(dragon);
            dragon.setId(generatedId);
            mainData.add(dragon);
            logger.info("Successfully added a dragon: " + dragon);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            writeLock.unlock();
        }

    }

    @Override
    public boolean validateUser(String username, String password) {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            return users.stream().anyMatch(it -> it.getName().equals(username) && it.getPassword().equals(Encryptor.encryptThisString(password)));
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean checkIfUsernameUnique(String username) {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            return users.stream().noneMatch(it -> it.getName().matches(username));
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean checkIfMin(Dragon dragon) {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            return mainData.isEmpty() || dragon.compareTo(mainData.peekFirst()) < 0;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clearOwnedData(String username) {
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            database.getDragonTable().clearOwnedData(username);
            mainData.removeIf(it -> it.getAuthorName().equals(username));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            writeLock.unlock();
        }

    }

//    @Override
//    public String filterLessThanSemesterEnumToString(DragonCharacter inpEnum) {
//        Lock readLock = lock.readLock();
//        try {
//            readLock.lock();
//            StringJoiner output = new StringJoiner("\n\n");
//            mainData.stream().filter(it -> it.getCharacter().compareTo(inpEnum) < 0).forEach(it -> output.add(it.toString()));
//            return output.toString();
//        } finally {
//            readLock.unlock();
//        }
//    }

    @Override
    public int getCollectionSize() {
        return mainData.size();
    }


    @Override
    public String getCollectionInfo() {
        return "Collection type: " + mainData.getClass().getName() + "\n"
                + "Date of initialization: " + dateOfInitialization + "\n"
                + "Collection size: " + mainData.size();
    }
//    @Override
//    public InfoCommand.InfoCommandResult getInfoAboutCollections() {
//        Lock readLock = lock.readLock();
//        try {
//            readLock.lock();
//            if (mainData.isEmpty()) {
//                return new InfoCommand.InfoCommandResult(
//                        0,
//                        0
//                );
//            }
//            return new InfoCommand.InfoCommandResult(
//                    mainData.size());
//        } finally {
//            readLock.unlock();
//        }
//    }


    @Override
    public Dragon getMinByIdDragon() {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            return mainData.stream().min(Comparator.comparingLong(Dragon::getId)).orElse(null);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String ascendingDataToString() {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            return mainData.toString();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void removeDragonById(int id) {
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            database.getDragonTable().removeById(id);
            mainData.removeIf(it -> it.getId() == id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void removeLast() {
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            database.getDragonTable().removeLast();
            mainData.removeLast();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public String showSortedByName() {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            return mainData
                    .stream()
                    .sorted(Comparator.comparing(Dragon::getName))
                    .collect(Collectors.toList()).toString();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void updateDragonById(int id, Dragon dragon) {
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            database.getDragonTable().updateById(id, dragon);
            mainData.removeIf(it -> it.getId() == id);
            mainData.add(dragon);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            writeLock.unlock();
        }
    }
//    @Override
//    public void getDragonCollection(Dragon dragon){
//        Lock writeLock = lock.writeLock();
//        try {
//            writeLock.lock();
//            return mainData;
//        }catch (SQLException e) {
//    }


    @Override
    public void removeGreaterIfOwned(Dragon dragon, String username) {
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            final Set<Integer> idsToRemove = new HashSet<>();
            Iterator<Dragon> iterator = mainData.descendingIterator();
            while (iterator.hasNext()) {
                Dragon currentDragon = iterator.next();
                if (currentDragon.compareTo(dragon) > 0 && currentDragon.getAuthorName().equals(username)) {
                    idsToRemove.add(Math.toIntExact(currentDragon.getId()));
                    iterator.remove();
                }
            }
            for (int id : idsToRemove) {
                database.getDragonTable().removeById(id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }
//    @Override
//    public void removeGreaterIfOwned(Dragon dragon, String username) {
//        Lock writeLock = lock.writeLock();
//        try {
//            writeLock.lock();
//            final Set<Integer> idsToRemove =
//                    mainData.chooseLast(dragon)
//                            .stream()
//                            .filter(it -> it.getAuthorName().equals(username))
//                            .map(Dragon::getId)
//                            .collect(Collectors.toSet());
//            for (int id : idsToRemove) {
//                database.getDragonTable().removeById(id);
//                mainData.removeIf(it -> idsToRemove.contains(it.getId()));
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            writeLock.unlock();
//        }
//    }


//    public void removeGreaterIfOwned(Dragon dragon, String username) {
//        Lock writeLock = lock.writeLock();
//        try {
//            writeLock.lock();
//            final Set<Integer> idsToRemove =
//                    mainData.tailSet(dragon)
//                            .stream()
//                            .filter(it -> it.getAuthorName().equals(username))
//                            .map(Dragon::getId)
//                            .collect(Collectors.toSet());
//            for (int id : idsToRemove) {
//                database.getDragonTable().removeById(id);
//                mainData.removeIf(it -> idsToRemove.contains(it.getId()));
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            writeLock.unlock();
//        }
//    }

    @Override
    public boolean validateOwner(String username, int dragonId) {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            return mainData.stream().anyMatch(it -> it.getId() == dragonId && it.getAuthorName().equals(username));
        } finally {
            readLock.unlock();
        }
    }


    @Override
    public void initialiseData() {
        try {
            this.mainData = database.getDragonTable().getDragonCollection();
            this.users = database.getUsersTable().getUserCollection();
            logger.info("Made a data manager with initialised collections:\n"
                    + mainData + "\n\n" + users);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
