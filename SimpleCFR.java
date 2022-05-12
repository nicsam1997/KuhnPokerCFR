package com.poker;

import java.util.Random;

import static java.lang.Double.max;

public class SimpleCFR {
    /*
    Implement CFR for Kuhn poker
    Simple game, 3 cards (1,2,3) with one (two) betting rounds
    Can either pass or bet 1. If first player passes and second one bets,
    first one can respond.

    ALGO?
    For each node need to compute strategy according to regret minimization
    curr_strategy is gotten from cumulative regret
    avg_strategy converges
     */
    public static class GameEngine {
        private final Random random = new Random(1337);
        private final Player player1 = new Player("Niclas");
        private final Player player2 = new Player("William");
        private final int iterations;
        private final int[][] cardSets = new int[][]{{1,0},{0,1},{1,2},{2,0},{2,1},{0,2}};

        GameEngine(int iterations) {
            this.iterations = iterations;
        }

        public double[][] play(boolean updatingPlayer1, boolean updatingPLayer2, States states) {
            for (int i=0; i<iterations;i++) {
                int[] currentSet = cardSets[random.nextInt(6)];
                player1.hand = currentSet[0];
                player2.hand = currentSet[1];
                //System.out.println("Player1: " + player1.hand + " Player2: " + player2.hand);
                for (int j=0; j<states.utilityMatrix[2].length;j++) {
                    if(j == 3) states.utilityMatrix[2][j] = ((player1.hand - player2.hand) > 0) ? 2 : - 2;
                    if(j == 2) states.utilityMatrix[2][j] = -1;
                }
                for (int j=0; j<states.utilityMatrix[1].length;j++) {
                    if(j == 0) states.utilityMatrix[1][j] = ((player1.hand - player2.hand) > 0) ? -1 : 1;
                    if(j == 3) states.utilityMatrix[1][j] = ((player1.hand - player2.hand) > 0) ? -2 : 2;
                    if(j==1) states.utilityMatrix[1][j] = -states.transitionMatrix[2][player1.hand] *
                            states.utilityMatrix[2][3] - (1-states.transitionMatrix[2][player1.hand]) *
                            states.utilityMatrix[2][2];
                    if(j==2) states.utilityMatrix[1][j] = -1;
                }
                for (int j=0; j<states.utilityMatrix[0].length;j++) {
                    if(j == 1) states.utilityMatrix[0][j] = - states.transitionMatrix[1][3+player2.hand] *
                            states.utilityMatrix[1][3] -  (1-states.transitionMatrix[1][3+player2.hand]) *
                            states.utilityMatrix[1][2];
                    if(j == 0) states.utilityMatrix[0][j] = - states.transitionMatrix[1][player2.hand] *
                            states.utilityMatrix[1][1] - (1-states.transitionMatrix[1][player2.hand]) *
                            states.utilityMatrix[1][0];
                }
                double regretBet = states.utilityMatrix[2][3] + states.utilityMatrix[1][1];
                double regretCheck = states.utilityMatrix[2][2] + states.utilityMatrix[1][1];
                states.totalRegretMatrixBet[2][player1.hand] += states.transitionMatrix[1][player2.hand]*regretBet;
                states.totalRegretMatrixCheck[2][player1.hand] += states.transitionMatrix[1][player2.hand] * regretCheck;

                double probabilityOfGettingIntoState = (1-states.transitionMatrix[0][player1.hand]);
                states.summedStrategyMatrixBet[2][player1.hand] += states.transitionMatrix[2][player1.hand] * probabilityOfGettingIntoState;
                states.summedStrategyMatrixCheck[2][player1.hand] += (1 - states.transitionMatrix[2][player1.hand]) * probabilityOfGettingIntoState;


                regretBet = states.utilityMatrix[1][1] + states.utilityMatrix[0][0];
                regretCheck = states.utilityMatrix[1][0] + states.utilityMatrix[0][0];
                states.totalRegretMatrixBet[1][player2.hand] += (1-states.transitionMatrix[0][player1.hand])*regretBet;
                states.totalRegretMatrixCheck[1][player2.hand] += (1-states.transitionMatrix[0][player1.hand])*regretCheck;


                states.summedStrategyMatrixBet[1][player2.hand] +=  states.transitionMatrix[1][player2.hand];
                states.summedStrategyMatrixCheck[1][player2.hand] +=  (1-states.transitionMatrix[1][player2.hand]);


                /* A bit tricky since the below code relies on indices */
                regretBet = states.utilityMatrix[1][3] +  states.utilityMatrix[0][1];
                regretCheck = states.utilityMatrix[1][2] +  states.utilityMatrix[0][1];;
                states.totalRegretMatrixBet[1][player2.hand + 3] += states.transitionMatrix[0][player1.hand]*regretBet;
                states.totalRegretMatrixCheck[1][player2.hand + 3] += states.transitionMatrix[0][player1.hand]*regretCheck;

                states.summedStrategyMatrixBet[1][player2.hand+ 3] += states.transitionMatrix[1][player2.hand+ 3];
                states.summedStrategyMatrixCheck[1][player2.hand+ 3] += (1-states.transitionMatrix[1][player2.hand+ 3]);

                double intialNodeUtility = -(1-states.transitionMatrix[0][player1.hand]) * states.utilityMatrix[0][0]
                        - states.utilityMatrix[0][1] * states.transitionMatrix[0][player1.hand];
                regretBet = states.utilityMatrix[0][1] + intialNodeUtility;
                regretCheck = states.utilityMatrix[0][0] + intialNodeUtility;
                states.totalRegretMatrixBet[0][player1.hand] += regretBet;
                states.totalRegretMatrixCheck[0][player1.hand] += regretCheck;

                states.summedStrategyMatrixBet[0][player1.hand] += states.transitionMatrix[0][player1.hand];
                states.summedStrategyMatrixCheck[0][player1.hand] += 1-states.transitionMatrix[0][player1.hand];

                double normalizingSum = 0;
                //Update player 1
                if(updatingPlayer1) {
                    normalizingSum = max(0, states.totalRegretMatrixBet[2][player1.hand]) + max(0, states.totalRegretMatrixCheck[2][player1.hand]);
                    if (normalizingSum == 0) states.transitionMatrix[2][player1.hand] = 0.5;
                    else
                        states.transitionMatrix[2][player1.hand] = max(0, states.totalRegretMatrixBet[2][player1.hand]) / normalizingSum;

                    normalizingSum = max(0, states.totalRegretMatrixBet[0][player1.hand]) + max(0, states.totalRegretMatrixCheck[0][player1.hand]);
                    if (normalizingSum == 0) states.transitionMatrix[0][player1.hand] = 0.5;
                    else
                        states.transitionMatrix[0][player1.hand] = max(0, states.totalRegretMatrixBet[0][player1.hand]) / normalizingSum;
                }

                //Update player 2
                if(updatingPLayer2) {
                    normalizingSum = max(0, states.totalRegretMatrixBet[1][player2.hand + 3]) + max(0, states.totalRegretMatrixCheck[1][player2.hand + 3]);
                    if (normalizingSum == 0) states.transitionMatrix[1][player2.hand + 3] = 0.5;
                    else
                        states.transitionMatrix[1][player2.hand + 3] = max(0, states.totalRegretMatrixBet[1][player2.hand + 3]) / normalizingSum;

                    normalizingSum = max(0, states.totalRegretMatrixBet[1][player2.hand]) + max(0, states.totalRegretMatrixCheck[1][player2.hand]);
                    if (normalizingSum == 0) states.transitionMatrix[2][player2.hand] = 0.5;
                    else
                        states.transitionMatrix[1][player2.hand] = max(0, states.totalRegretMatrixBet[1][player2.hand]) / normalizingSum;
                }
            }
            double[][] finalStrategy = {{0,0,0}, {0,0,0,0,0,0},{0,0,0}};
            for(int i = 0; i<states.summedStrategyMatrixBet.length;i++) {
                for(int j = 0; j<states.summedStrategyMatrixBet[i].length;j++) {
                    finalStrategy[i][j] = states.summedStrategyMatrixBet[i][j]/(states.summedStrategyMatrixBet[i][j] +
                            states.summedStrategyMatrixCheck[i][j]);
                }
            }
            return finalStrategy;
        }
        
        public void printStrategy(double[][] strategy) {
            System.out.println("My strategy: ");
            for(int i = 0; i<strategy.length;i++) {
                if(i!=0) System.out.println();
                for(int j = 0; j<strategy[i].length;j++) {
                    System.out.print(strategy[i][j]);
                }
            }
        }

        public void evaluteModelForPlayer1(double[][] finalStrategy) {
            double winnings = 0;
            int rounds = 10000000;
            States states = new States(finalStrategy);
            play(false,true,states);

            for(int i = 0; i<rounds;i++) {
                int[] currentSet = cardSets[random.nextInt(6)];
                player1.hand = currentSet[0];
                player2.hand = currentSet[1];
                double prob1 = random.nextDouble();
                double prob2 = random.nextDouble();

                int player1Bet = 0;
                int player2Bet = 0;
                if(finalStrategy[0][player1.hand] > prob1) player1Bet = 1;
                if(finalStrategy[1][player2.hand+3*player1Bet] > prob2) player2Bet = 1;
                if(player1Bet == 0 && player2Bet == 1) {
                    double prob3 = random.nextDouble();
                    if(finalStrategy[2][player1.hand] > prob3) player1Bet = 1;
                }
                if(player1Bet > player2Bet) winnings += 1;
                if(player1Bet < player2Bet) winnings -= 1;
                if(player1Bet == 1 && player2Bet == 1) winnings += ((player1.hand - player2.hand)) > 0 ? 2 : -2 ;
                if(player1Bet == 0 && player2Bet == 0) winnings += ((player1.hand - player2.hand)) > 0 ? 1 : -1 ;
            }
            System.out.println();
            System.out.println("Average winnings: " + winnings/rounds);
            System.out.println("Average theoretical winnings for nash equilibrium model: " + -1.0/18.0);
        }

        public static class States {
            private static final double p1 = 0.1;
            private final double[][] transitionMatrix;
            private final double[][] totalRegretMatrixBet = new double[][] {{0,0,0},{0,0,0,0,0,0},{0,0,0}};
            private final double[][] totalRegretMatrixCheck = new double[][] {{0,0,0},{0,0,0,0,0,0},{0,0,0}};
            private final double[][] summedStrategyMatrixBet = new double[][] {{0,0,0},{0,0,0,0,0,0},{0,0,0}};
            private final double[][] summedStrategyMatrixCheck = new double[][] {{0,0,0},{0,0,0,0,0,0},{0,0,0}};
            private final double[][] utilityMatrix = new double[][] {{0,0,0,0}, {0,0,0,0},{0,0,0,0}};

            States(double[][] transitionMatrix) {
                this.transitionMatrix = transitionMatrix;
            }

            States() {
                this.transitionMatrix = new double[][] {{p1,p1,p1},{p1,p1,p1,p1,p1,p1},{p1,p1,p1}};
            }

        }

        public static class Player {
            private final String name;
            private int hand;

            public Player(String name) {
                this.name = name;
            }
        }
    }





}
