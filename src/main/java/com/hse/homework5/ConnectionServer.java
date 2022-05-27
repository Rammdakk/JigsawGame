package com.hse.homework5;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * TODO: modify the example to implement client-server calculator that operates with ints and +,-,/,*
 */

public class ConnectionServer {
    public static int active = 0;

    public static void main(String[] args) {
        int serverPortNumber = 5001;
        int playersNumber = 2;
        try {
            playersNumber = getNumberOfUsers();
            OneUserGameThread.namesNumber = playersNumber;
            OneUserGameThread.playTime = getMaxTime();
        } catch (InputMismatchException ex) {
            System.out.println("Incorrect input. Players number is integer value");
        }
        try {
            if (args.length > 0) {
                serverPortNumber = Integer.parseInt(args[0]);
            }
            ServerSocket connectionSocket = new ServerSocket(serverPortNumber);
            OneUserGameThread[] st = new OneUserGameThread[playersNumber];
            for (int i = 0; i < playersNumber; i++) {
                Socket dataSocket = connectionSocket.accept();
                st[i] = new OneUserGameThread(dataSocket, i);
                st[i].start();
            }
            if (active < 0) {
                for (int i = 0; i < playersNumber; i++) {
                    if (st[i].isAlive())
                        st[i].interrupt();
                }
            }
            System.out.println(active);
            connectionSocket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static int getNumberOfUsers() {
        int playersNumber;
        System.out.println("Enter users number. Players number must be more than zero and less than three");
        Scanner in = new Scanner(System.in);
        playersNumber = in.nextInt();
        while (playersNumber < 1 || playersNumber > 2) {
            System.out.println("Incorrect input. Players number must be more than zero and less than three");
            playersNumber = in.nextInt();
        }
        return playersNumber;
    }

    private static int getMaxTime() {
        int timeInSeconds;
        System.out.println("Enter game time in seconds. It must be more than zero and less than 86400");
        Scanner in = new Scanner(System.in);
        timeInSeconds = in.nextInt();
        while (timeInSeconds < 1 || timeInSeconds > 86400) {
            System.out.println("Incorrect input. Players number must be more than zero and less than three");
            timeInSeconds = in.nextInt();
        }
        return timeInSeconds;
    }

}

class OneUserGameThread extends Thread {
    public static final Collection<Integer> figureSequence = Collections.synchronizedCollection(new ArrayList<>());
    public static String[] names = {"", ""};
    public static PrintStream[] printStreams = {null, null};
    public static boolean[] isFinishedGame = {false, false};
    Socket dataSocket;
    static int namesNumber = 0;
    static int playTime = 0;
    int counter = 1;
    int threadNumber;
    boolean isUpdate = false;

    public OneUserGameThread(Socket s, int threadNumber) {
        this.dataSocket = s;
        this.threadNumber = threadNumber;
    }

    public void run() {
        try {
            Random rnd = new Random(System.currentTimeMillis());
            int n = 2;
            InputStream inputStream = dataSocket.getInputStream();
            Scanner in = new Scanner(inputStream);
            names[threadNumber] = in.nextLine();
            PrintStream outputStream = new PrintStream(dataSocket.getOutputStream());
            printStreams[threadNumber] = outputStream;
            if (namesNumber == 1) {
                names[1 - threadNumber] = "No enemy mod";
            }
            int reqNumber = 0;
            // Время ожидание ввода имени от второго игрока - 10 секунд, после - завершение работы.
            while (Objects.equals(names[1 - threadNumber], "") && reqNumber < 1000) {
                sleep(10);
                reqNumber++;
            }
            if (reqNumber > 999) {
                stopSocket(inputStream, in, outputStream, "App doesn't response");
                ConnectionServer.active = -3;
            }
            outputStream.println("Player name: " + names[threadNumber] + "\nEnemy name: " + names[1 - threadNumber]);
            outputStream.println(playTime);
            System.out.println((1 - threadNumber) + " user name " + names[1 - threadNumber]);
            String command = "10";
            while ("10".equals(command)) {
                ConnectionServer.active++;
                reqNumber = 0;
                // Ожидание до 10 секунд на подключение второго игрока, иначе завершение работы.
                while (ConnectionServer.active != namesNumber && reqNumber < 1000) {
                    figureSequence.clear();
                    reqNumber++;
                    sleep(10);
                }
                if (namesNumber == 1) {
                    figureSequence.clear();
                }
                if (reqNumber > 999) {
                    stopSocket(inputStream, in, outputStream, "App doesn't response");
                    return;
                }
                // n=2 - запрос на получение новой фигуры.
                while (n == 2) {
                    while (figureSequence.size() < counter) {
                        figureSequence.add(1 + rnd.nextInt(31));
                    }
                    if (counter == 2) {
                        outputStream.println(figureSequence.toArray()[counter - 2]);
                    } else {
                        outputStream.println(figureSequence.toArray()[counter - 1]);
                    }
                    System.out.println("message sent:" + threadNumber + " - thread: " + figureSequence.toArray()[counter - 1]);
                    outputStream.flush();
                    counter++;
                    n = in.nextInt();
                    // n=130 - проверка связи с сервером, происходит раз в секунду.
                    while (n == 130) {
                        System.out.println("req-res");
                        outputStream.println(130);
                        if (in.hasNext()) {
                            n = in.nextInt();
                        } else {
                            n = -1001;
                        }
                    }
                }
                isUpdate = false;
                // n=-1 - окончание одним из игроков игры.
                if (namesNumber == 2 && n == -1) {
                    finishingByOneUser(in);
                }
                // n=-2 - завершение игры.
                if (n == -2) {
                    finishingGame(in);
                }
                ConnectionServer.active--;
                while (ConnectionServer.active != 0) {
                    sleep(100);
                }
                command = in.nextLine();
                while (!("10".equals(command) || "9".equals(command))) {
                    command = in.nextLine();
                }
                // сброс счетчиков.
                n = 2;
                counter = 1;
            }
            System.out.println("command stop val = " + command);
            stopSocket(inputStream, in, outputStream, "Stopping thread");
        } catch (IOException | NoSuchElementException ex) {
            System.out.println("Socket Close Error");
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            System.out.println("Thread is interrupted");
            ex.printStackTrace();
        }
    }

    private void stopSocket(InputStream inputStream, Scanner in, PrintStream outputStream, String message) throws IOException {
        System.out.println(message);
        in.close();
        outputStream.close();
        inputStream.close();
        dataSocket.close();
    }

    /**
     * Завершение игры при закрытии приложения одним из пользователей.
     *
     * @param in scanner потока
     */
    private void finishingByOneUser(Scanner in) {
        System.out.println("code -1");
        in.nextLine();
        printStreams[1 - threadNumber].println(-1);
        printStreams[1 - threadNumber].println(in.nextLine());
        printStreams[1 - threadNumber].println(in.nextLine());
        printStreams[1 - threadNumber].flush();
    }

    /**
     * Завершение игры по обоюдному согласию, или при истечении времени.
     *
     * @param in scanner потока
     * @throws InterruptedException если поток разбудили
     */
    private void finishingGame(Scanner in) throws InterruptedException {
        if (namesNumber == 2) {
            isFinishedGame[threadNumber] = true;
            printStreams[threadNumber].println(-2);
            int x = 0;
            while (!isFinishedGame[1 - threadNumber] && x < 10000000) {
                sleep(100);
                x++;
            }
            isFinishedGame[1 - threadNumber] = false;
            in.nextLine();
            printStreams[1 - threadNumber].println(in.nextLine());
            printStreams[1 - threadNumber].println(in.nextLine());
            printStreams[1 - threadNumber].flush();
        } else {
            printStreams[threadNumber].println(-2);
            in.nextLine();
            printStreams[threadNumber].println(-1);
            printStreams[threadNumber].println(0);
            printStreams[threadNumber].flush();
        }
    }
}
