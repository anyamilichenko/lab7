package postgeSQL;

import data.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public interface Table<T> {
    void init() throws SQLException;

    //LinkedList<User> getCollection() throws SQLException;

    T mapRowToObject(ResultSet resultSet) throws SQLException;

    int add(T element) throws SQLException;
}
