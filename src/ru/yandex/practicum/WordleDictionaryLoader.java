package ru.yandex.practicum;

/*
этот класс содержит в себе всю рутину по работе с файлами словарей и с кодировками
    ему нужны методы по загрузке списка слов из файла по имени файла
    на выходе должен быть класс WordleDictionary
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {
    private static final int WORD_LENGTH = 5; // Вынес магическое число в константу

    public WordleDictionary load(String filename) throws IOException, WordleDictionaryException {
        List<String> words = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new FileReader(filename, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toLowerCase().replace('ё', 'е');

                if (word.length() == WORD_LENGTH) {
                    boolean valid = true;
                    for (char c : word.toCharArray()) {
                        if (c < 'а' || c > 'я') {
                            valid = false;
                            break;
                        }
                    }
                    if (valid) {
                        words.add(word);
                    }
                }
            }
        }

        if (words.isEmpty()) {
            throw new WordleDictionaryException("Словарь пуст");
        }

        return new WordleDictionary(words);
    }
}
