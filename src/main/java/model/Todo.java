package model;

public class Todo {
    private int id;
    private int userId;
    private String description;
    private boolean completed;

    // Constructeurs
    public Todo() {
    }

    public Todo(String description, int userId) {
        this.description = description;
        this.userId = userId;
        this.completed = false;
    }

    public Todo(int id, int userId, String description, boolean completed) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.completed = completed;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
