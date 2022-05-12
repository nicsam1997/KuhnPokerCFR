package com.poker;

public class Main {

    public static void main(String[] args) {
        SimpleCFR.GameEngine myEngine = new SimpleCFR.GameEngine(1000000);
        double[][] strategy = myEngine.play(true,true, new SimpleCFR.GameEngine.States());
        myEngine.printStrategy(strategy);
        myEngine.evaluteModelForPlayer1(strategy);
    }
}
