package me.juneylove.shakedown.games.rapidodge;

abstract class AbstractBar {

    static final int height = 6;
    static final int maxDistance = 25;
    static final int moveInterval = 3;

    abstract void run();

    abstract void remove();
}
