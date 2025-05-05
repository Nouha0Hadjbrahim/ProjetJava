package service;

import model.Todo;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TodoService {
    
    public List<Todo> getAllTodos(int userId) {
        List<Todo> todos = new ArrayList<>();
        String sql = "SELECT * FROM todo WHERE user_id = ? ORDER BY id DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Todo todo = new Todo(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("description"),
                    rs.getBoolean("completed")
                );
                todos.add(todo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return todos;
    }
    
    public void addTodo(String description, int userId) {
        String sql = "INSERT INTO todo (description, completed, user_id) VALUES (?, false, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, description);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void updateTodoStatus(int id, boolean completed) {
        String sql = "UPDATE todo SET completed = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, completed);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void deleteTodo(int id) {
        String sql = "DELETE FROM todo WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 