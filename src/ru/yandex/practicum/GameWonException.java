package ru.yandex.practicum;

public class GameWonException extends WordleGameException {
    public GameWonException(String message) {
        super(message);
    }
}