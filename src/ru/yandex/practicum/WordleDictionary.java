package ru.yandex.practicum;

import java.util.List;
/*
этот класс содержит в себе список слов List<String>
    его методы похожи на методы списка, но учитывают особенности игры
    также этот класс может содержать рутинные функции по сравнению слов, букв и т.д.
 */
import java.util.Random;

public class WordleDictionary {
    private static final int WORD_LENGTH = 5; // Вынес магическое число в константу
    private final List<String> words;
    private final Random random; // Добавил поле random в классе

    public WordleDictionary(List<String> words) {
        this.words = words;
        this.random = new Random();
    }

    public String getRandomWord() {
        if (words.isEmpty()) {
            // Кидать нужно свою ошибку (Тут я не совсем понял что требуется сделать)
            throw new IllegalStateException("Словарь пуст");
        }

        // Использовал класс рандом Random и метод nextInt
        return words.get(random.nextInt(words.size()));
    }

    public boolean contains(String word) {
        return words.contains(word);
    }

    public List<String> getWords() {
        return words;
    }

    public String normalizeWord(String word) {
        return word.toLowerCase().replace('ё', 'е');
    }

    public String getHint(List<Character> correctPositions,
                          List<Character> wrongPositions,
                          List<Character> absentLetters) {
        for (String word : words) {
            boolean matches = true;

            for (int i = 0; i < WORD_LENGTH; i++) {
                char correctChar = correctPositions.get(i);
                if (correctChar != ' ' && word.charAt(i) != correctChar) {
                    matches = false;
                    break;
                }
            }

            if (!matches) continue;

            for (int i = 0; i < WORD_LENGTH; i++) {
                char wrongChar = wrongPositions.get(i);
                if (wrongChar != ' ') {
                    if (word.indexOf(wrongChar) == -1) {
                        matches = false;
                        break;
                    }
                    if (word.charAt(i) == wrongChar) {
                        matches = false;
                        break;
                    }
                }
            }

            if (!matches) continue;

            for (char absent : absentLetters) {
                if (word.indexOf(absent) != -1) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                return word;
            }
        }

        return null;
    }
}
