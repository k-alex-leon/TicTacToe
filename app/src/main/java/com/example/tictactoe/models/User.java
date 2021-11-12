package com.example.tictactoe.models;

public class User {

    private String id;
    private String username;
    private String email;
    private long timestamp;
    private int games;
    private int points;

    public User() {
    }

    public User(String id, String username, String email, long timestamp, int games, int points) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.timestamp = timestamp;
        this.games = games;
        this.points = points;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getGames() {
        return games;
    }

    public void setGames(int games) {
        this.games = games;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
