package ru.yandex.practicum;

import java.util.List;

/*
этот класс содержит в себе список слов List<String>
    его методы похожи на методы списка, но учитывают особенности игры
    также этот класс может содержать рутинные функции по сравнению слов, букв и т.д.
 */
public class WordleDictionary {

    private List<String> words;

    public WordleDictionary(List<String> words) {
        this.words = words;
    }

    public String getRandomWord() {
        if (words.isEmpty()) {
            throw new IllegalStateException("Словарь пуст");
        }
        return words.get((int) (Math.random() * words.size()));
    }

    public boolean contains(String word) {
        String normalized = normalizeWord(word);
        return words.contains(normalized);
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

            for (int i = 0; i < 5; i++) {
                char correctChar = correctPositions.get(i);
                if (correctChar != ' ' && word.charAt(i) != correctChar) {
                    matches = false;
                    break;
                }
            }

            if (!matches) continue;

            for (int i = 0; i < 5; i++) {
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
