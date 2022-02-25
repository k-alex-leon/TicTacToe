package com.example.tictactoe.models;

import java.util.List;

public class Move {

    private String idGame;
    private String Gamer1id;
    private String Gamer2id;
    private String idWinner;
    private String idLoser;
    private List<Integer> selectCells;
    private Boolean gameTurn;
    private long timestamp;
    private String idExitGame;

    // necesita costructor vacio
    public Move() {
    }

    //constructor para jugador en busca de partida


    public Move(String idGame, String gamer1id, String gamer2id, String idWinner, String idLoser,
                List<Integer> selectCells, Boolean gameTurn, long timestamp, String idExitGame) {

        this.idGame = idGame;
        Gamer1id = gamer1id;
        Gamer2id = gamer2id;
        this.idWinner = idWinner;
        this.idLoser = idLoser;
        this.selectCells = selectCells;
        this.gameTurn = gameTurn;
        this.timestamp = timestamp;
        this.idExitGame = idExitGame;
    }

    public String getIdGame() {
        return idGame;
    }

    public void setIdGame(String idGame) {
        this.idGame = idGame;
    }

    public String getGamer1id() {
        return Gamer1id;
    }

    public void setGamer1id(String gamer1id) {
        Gamer1id = gamer1id;
    }

    public String getGamer2id() {
        return Gamer2id;
    }

    public void setGamer2id(String gamer2id) {
        Gamer2id = gamer2id;
    }

    public String getIdWinner() {
        return idWinner;
    }

    public void setIdWinner(String idWinner) {
        this.idWinner = idWinner;
    }

    public String getIdLoser() {
        return idLoser;
    }

    public void setIdLoser(String idLoser) {
        this.idLoser = idLoser;
    }

    public List<Integer> getSelectCells() {
        return selectCells;
    }

    public void setSelectCells(List<Integer> selectCells) {
        this.selectCells = selectCells;
    }

    public Boolean getGameTurn() {
        return gameTurn;
    }

    public void setGameTurn(Boolean gameTurn) {
        this.gameTurn = gameTurn;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getIdExitGame() {
        return idExitGame;
    }

    public void setIdExitGame(String idExitGame) {
        this.idExitGame = idExitGame;
    }
}
