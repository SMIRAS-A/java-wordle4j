package ru.yandex.practicum;
/*
в главном классе нам нужно:
    создать лог-файл (он должен передаваться во все классы)
    создать загрузчик словарей WordleDictionaryLoader
    загрузить словарь WordleDictionary с помощью класса WordleDictionaryLoader
    затем создать игру WordleGame и передать ей словарь
    вызвать игровой метод в котором в цикле опрашивать пользователя и передавать информацию в игру
    вывести состояние игры и конечный результат
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Wordle {

    public static void main(String[] args) {
        PrintWriter log = null;

        try {
            log = new PrintWriter(new FileWriter("wordle.log", StandardCharsets.UTF_8), true);

            WordleDictionaryLoader loader = new WordleDictionaryLoader();
            WordleDictionary dictionary = loader.load("words_ru.txt");

            WordleGame game = new WordleGame(dictionary, log);

            Scanner scanner = new Scanner(System.in, "UTF-8");
            boolean gameRunning = true;

            System.out.println("Добро пожаловать в игру Wordle!");
            System.out.println("У вас есть 6 попыток, чтобы угадать слово из 5 букв.");
            System.out.println("Введите слово или нажмите Enter для подсказки.");

            while (gameRunning && !game.isGameOver()) {
                try {
                    System.out.print("> ");
                    String input = scanner.nextLine().trim();

                    if (input.isEmpty()) {
                        String hint = game.getHint();
                        if (hint != null) {
                            System.out.println("Подсказка: " + hint);
                        } else {
                            System.out.println("Подсказок больше нет.");
                        }
                        continue;
                    }

                    String analysis = game.makeGuess(input);
                    System.out.println("> " + analysis);

                } catch (Exception e) {
                    if (e.getMessage().contains("Поздравляем")) {
                        gameRunning = false;
                        System.out.println(e.getMessage());
                        System.out.println("Вы выиграли за " + (6 - game.getRemainingSteps()) + " попыток!");
                    } else if (e.getMessage().contains("Игра окончена")) {
                        gameRunning = false;
                        System.out.println(e.getMessage());
                        System.out.println("Загаданное слово: " + game.getAnswer());
                    } else {
                        System.out.println("Ошибка: " + e.getMessage());
                    }
                }
            }

            if (game.getRemainingSteps() == 0 && !game.getAttempts().contains(game.getAnswer())) {
                System.out.println("К сожалению, вы проиграли.");
                System.out.println("Загаданное слово: " + game.getAnswer());
            }

            scanner.close();

        } catch (IOException e) {
            System.err.println("Ошибка при работе с файлами: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Непредвиденная ошибка: " + e.getMessage());
        } finally {
            if (log != null) {
                log.close();
            }
        }
    }

}
