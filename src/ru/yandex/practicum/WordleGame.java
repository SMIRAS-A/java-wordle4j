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
    private static final int MAX_ATTEMPTS = 6; // Вынес магическое число в константу
    private static final int WORD_LENGTH = 5;  // Вынес магическое число в константу

    private final String answer;
    private int steps;
    private final WordleDictionary dictionary;
    private final PrintWriter log;
    private final List<Character> correctPositions;
    private final List<Character> wrongPositions;
    private final List<Character> absentLetters;
    private final List<String> attempts;

    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this.dictionary = dictionary;
        this.log = log;
        this.answer = dictionary.getRandomWord();
        this.steps = MAX_ATTEMPTS;
        this.correctPositions = new ArrayList<>();
        this.wrongPositions = new ArrayList<>();
        this.absentLetters = new ArrayList<>();
        this.attempts = new ArrayList<>();

        for (int i = 0; i < WORD_LENGTH; i++) {
            correctPositions.add(' ');
            wrongPositions.add(' ');
        }
    }

    public String makeGuess(String guess) throws WordleGameException {
        if (steps <= 0) {
            throw new GameOverException("Игра окончена. Закончились попытки.");
        }

        String normalizedGuess = dictionary.normalizeWord(guess);

        if (normalizedGuess.length() != WORD_LENGTH) {
            throw new WordleGameException("Слово должно состоять из " + WORD_LENGTH + " букв");
        }

        if (!dictionary.contains(normalizedGuess)) {
            throw new WordleGameException("Слово не найдено в словаре");
        }

        attempts.add(normalizedGuess);
        steps--;

        if (normalizedGuess.equals(answer)) {
            throw new GameWonException("Поздравляем! Вы угадали слово!");
        }

        String analysis = analyzeGuess(normalizedGuess);
        updateKnowledge(normalizedGuess, analysis);

        log.println("Попытка: " + normalizedGuess + " -> " + analysis);

        return analysis;
    }

    private String analyzeGuess(String guess) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < WORD_LENGTH; i++) {
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
        for (int i = 0; i < WORD_LENGTH; i++) {
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
