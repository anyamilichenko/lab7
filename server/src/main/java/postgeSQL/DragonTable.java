package postgeSQL;

import data.*;

import java.sql.*;
import java.util.LinkedList;

public class DragonTable implements Table<Dragon> {
    private static final int PARAMETER_OFFSET_NAME = 1;
    private static final int PARAMETER_OFFSET_COORDINATES_X = 2;
    private static final int PARAMETER_OFFSET_COORDINATES_Y = 3;
    private static final int PARAMETER_OFFSET_TIMESTAMP = 4;
    private static final int PARAMETER_OFFSET_AGE = 5;
    private static final int PARAMETER_OFFSET_DRAGON_CHARACTER = 7;
    private static final int PARAMETER_OFFSET_WINGSPAN = 7;
    private static final int PARAMETER_OFFSET_WEIGHT = 8;
    private static final int PARAMETER_OFFSET_DEPTH = 9;
    private static final int PARAMETER_OFFSET_NUMBER_OF_TREASURES = 10;
    private static final int PARAMETER_OFFSET_AUTHOR = 14;
    private final Connection connection;


    public DragonTable(Connection connection) {
        this.connection = connection;
    }


    @Override
    public void init() throws SQLException {
        try (
                Statement statement = connection.createStatement()
        ) {
            statement.execute("CREATE TABLE IF NOT EXISTS dragon ("
                    + "id serial PRIMARY KEY,"
                    + "name varchar(100) NOT NULL,"
                    + "coordinates_x double NOT NULL,"
                    + "coordinates_y double precision NOT NULL,"
                    + "creation_date TIMESTAMP NOT NULL,"
                    + "dragons_age long,"
                    + "dragon_character varchar(100) NOT NULL,"
                    + "depth_of_cave double,"
                    + "number_of_treasures float NOT NULL,"
                    + "wingspan bigint NOT NULL,"
                    + "weight double NOT NULL,"
                    + "author_username varchar (100) NOT NULL)");
        }
    }


    @Override
    public Dragon mapRowToObject(ResultSet resultSet) throws SQLException {
        final Dragon dragon = new Dragon(
                resultSet.getString("name"),
                new Coordinates(
                        resultSet.getDouble("coordinates_x"),
                        resultSet.getDouble("coordinates_y")
                ),
                resultSet.getLong("dragons_age"),
                DragonCharacter.valueOf(resultSet.getString("dragon_character")),
                new DragonCave(
                        resultSet.getDouble("depth_of_cave"),
                        resultSet.getFloat("number_of_treasures")
                ),
                resultSet.getInt("wingspan"),
                resultSet.getDouble("weight"),
                resultSet.getTimestamp("creation_date").toLocalDateTime().toLocalDate(),
                resultSet.getString("authorName")
        );

        dragon.setId(resultSet.getInt("id"));

        return dragon;
    }

    @Override
    public int add(Dragon element) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO dragon VALUES (default, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id"
        )) {
            makePreparedStatementOfDragon(preparedStatement, element);
            try (
                    ResultSet resultSet = preparedStatement.executeQuery()
            ) {
                resultSet.next();
                return resultSet.getInt("id");
            }
        }
    }

    private void makePreparedStatementOfDragon(PreparedStatement preparedStatement, Dragon dragon) throws SQLException {
        preparedStatement.setString(PARAMETER_OFFSET_NAME, dragon.getName());
        preparedStatement.setDouble (PARAMETER_OFFSET_COORDINATES_X, dragon.getCoordinates().getX());
        preparedStatement.setDouble(PARAMETER_OFFSET_COORDINATES_Y, dragon.getCoordinates().getY());
        preparedStatement.setTimestamp(PARAMETER_OFFSET_TIMESTAMP, Timestamp.valueOf(dragon.getCreationDate().now()));
        preparedStatement.setLong(PARAMETER_OFFSET_AGE, dragon.getAge());
        preparedStatement.setString(PARAMETER_OFFSET_DRAGON_CHARACTER, dragon.getCharacter().toString());
        preparedStatement.setInt(PARAMETER_OFFSET_WINGSPAN, dragon.getWingspan());
        preparedStatement.setDouble(PARAMETER_OFFSET_WEIGHT, dragon.getWeight());
        if (dragon.getCave().getDepth() != null){
            preparedStatement.setDouble(PARAMETER_OFFSET_DEPTH, dragon.getCave().getDepth());
        } else {
            preparedStatement.setNull(PARAMETER_OFFSET_DEPTH, Types.VARCHAR);
        }
        preparedStatement.setFloat(PARAMETER_OFFSET_NUMBER_OF_TREASURES, dragon.getCave().getNumberOfTreasures());
        preparedStatement.setString(PARAMETER_OFFSET_AUTHOR, dragon.getAuthorName());
    }



    public LinkedList<Dragon> getDragonCollection() throws SQLException {
        final LinkedList<Dragon> newCollection = new LinkedList<>();

        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM study_groups")

        ) {

            while (resultSet.next()) {
                Dragon dragon = mapRowToObject(resultSet);
                newCollection.add(dragon);
            }

        }

        return newCollection;
    }

    public void clearOwnedData(String username) throws SQLException {

        String query = "DELETE FROM study_groups WHERE author_username=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.execute();
        }

    }

    public void removeById(int id) throws SQLException {
        try (
                Statement statement = connection.createStatement()
        ) {
            statement.execute("DELETE FROM study_groups WHERE id=" + id);

        }
    }

    public void removeLast() throws SQLException {
        try (
                Statement statement = connection.createStatement()
        ) {
            ResultSet resultSet = statement.executeQuery("SELECT MAX(id) FROM study_groups");
            if (resultSet.next()) {
                int lastId = resultSet.getInt(1);
                statement.execute("DELETE FROM study_groups WHERE id=" + lastId);
            }
        }
    }

    public void updateById(int id, Dragon dragon) throws SQLException {


        String query = "UPDATE study_groups SET "
                + "name=?"
                + ",coordinates_x=?"
                + ",coordinates_y=?"
                + ",creation_date=?"
                + ",dragons_age=?"
                + ",dragon_character=?"
                + ",depth_of_cave=?"
                + ",number_of_treasures=?"
                + ",wingspan=?"
                + ",weight=?"
                + ",author_username=? "
                + "WHERE id =" + id;


        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            makePreparedStatementOfDragon(preparedStatement, dragon);
            preparedStatement.execute();
        }


    }

}
