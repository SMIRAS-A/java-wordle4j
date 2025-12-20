package ru.yandex.practicum;

/*
в этом классе хранится словарь и состояние игры
    текущий шаг
    всё что пользователь вводил
    правильный ответ

в этом классе нужны методы, которые
    проанализируют совпадение слова с ответом
    предложат слово-подсказку с учётом всего, что вводил пользователь ранее

не забудьте про специальные типы исключений для игровых и неигровых ошибок
 */
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class WordleGame {

    private String answer;
    private int steps;
    private ru.yandex.practicum.WordleDictionary dictionary;
    private PrintWriter log;
    private List<Character> correctPositions;
    private List<Character> wrongPositions;
    private List<Character> absentLetters;
    private List<String> attempts;

    public WordleGame(ru.yandex.practicum.WordleDictionary dictionary, PrintWriter log) {
        this.dictionary = dictionary;
        this.log = log;
        this.answer = dictionary.getRandomWord();
        this.steps = 6;
        this.correctPositions = new ArrayList<>();
        this.wrongPositions = new ArrayList<>();
        this.absentLetters = new ArrayList<>();
        this.attempts = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            correctPositions.add(' ');
            wrongPositions.add(' ');
        }
    }

    public String makeGuess(String guess) throws Exception {
        if (steps <= 0) {
            throw new Exception("Игра окончена. Закончились попытки.");
        }

        String normalizedGuess = dictionary.normalizeWord(guess);

        if (normalizedGuess.length() != 5) {
            throw new Exception("Слово должно состоять из 5 букв");
        }

        if (!dictionary.contains(normalizedGuess)) {
            throw new Exception("Слово не найдено в словаре");
        }

        attempts.add(normalizedGuess);
        steps--;

        if (normalizedGuess.equals(answer)) {
            throw new Exception("Поздравляем! Вы угадали слово!");
        }

        String analysis = analyzeGuess(normalizedGuess);
        updateKnowledge(normalizedGuess, analysis);

        return analysis;
    }

    private String analyzeGuess(String guess) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            char guessChar = guess.charAt(i);

            if (guessChar == answer.charAt(i)) {
                result.append('+');
            } else if (answer.indexOf(guessChar) != -1) {
                result.append('^');
            } else {
                result.append('-');
            }
        }

        return result.toString();
    }

    private void updateKnowledge(String guess, String analysis) {
        for (int i = 0; i < 5; i++) {
            char guessChar = guess.charAt(i);
            char analysisChar = analysis.charAt(i);

            if (analysisChar == '+') {
                correctPositions.set(i, guessChar);
            } else if (analysisChar == '^') {
                wrongPositions.set(i, guessChar);
                if (absentLetters.contains(guessChar)) {
                    absentLetters.remove((Character) guessChar);
                }
            } else if (analysisChar == '-') {
                if (!correctPositions.contains(guessChar) && !wrongPositions.contains(guessChar)) {
                    if (!absentLetters.contains(guessChar)) {
                        absentLetters.add(guessChar);
                    }
                }
            }
        }
    }

    public String getHint() {
        return dictionary.getHint(correctPositions, wrongPositions, absentLetters);
    }

    public int getRemainingSteps() {
        return steps;
    }

    public String getAnswer() {
        return answer;
    }

    public List<String> getAttempts() {
        return attempts;
    }

    public boolean isGameOver() {
        return steps <= 0 || attempts.contains(answer);
    }

}
