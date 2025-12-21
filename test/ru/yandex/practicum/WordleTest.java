package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    private WordleDictionary dictionary;
    private PrintWriter log;

    @BeforeEach
    void setUp() {
        List<String> words = Arrays.asList(
                "аббат", "коата", "радар", "казак", "топот",
                "шалаш", "ротор", "дедок", "заказ", "потоп"
        );
        dictionary = new WordleDictionary(words);

        try {
            log = new PrintWriter(new FileWriter("test.log", StandardCharsets.UTF_8), true);
        } catch (Exception e) {
        }
    }

    @Test
    void testDictionaryContainsWord() {
        assertTrue(dictionary.contains("аббат"));
        assertTrue(dictionary.contains("радар"));
        assertTrue(dictionary.contains("казак"));
        assertFalse(dictionary.contains("несущ"));
    }

    @Test
    void testDictionaryNormalizeWord() {
        assertEquals("ежик", dictionary.normalizeWord("ЁжиК"));
        assertEquals("казак", dictionary.normalizeWord("КАЗАК"));
        assertEquals("топот", dictionary.normalizeWord("ТоПоТ"));
    }

    @Test
    void testDictionaryGetRandomWord() {
        String word = dictionary.getRandomWord();
        assertNotNull(word);
        assertEquals(5, word.length());
        assertTrue(dictionary.getWords().contains(word));
    }

    @Test
    void testWordleDictionaryLoaderFiltersByLength(@TempDir File tempDir) throws Exception {
        File dictionaryFile = new File(tempDir, "words_ru.txt");
        try (FileWriter writer = new FileWriter(dictionaryFile, StandardCharsets.UTF_8)) {
            writer.write("аббат\n");
            writer.write("герань\n");
            writer.write("казак\n");
            writer.write("гербарий\n");
            writer.write("топот\n");
            writer.write("а\n");
            writer.write("дедок\n");
        }

        ru.yandex.practicum.WordleDictionaryLoader loader = new ru.yandex.practicum.WordleDictionaryLoader();
        ru.yandex.practicum.WordleDictionary loadedDictionary = loader.load(dictionaryFile.getAbsolutePath());

        assertNotNull(loadedDictionary);
        assertEquals(4, loadedDictionary.getWords().size());
        assertTrue(loadedDictionary.contains("аббат"));
        assertTrue(loadedDictionary.contains("казак"));
        assertTrue(loadedDictionary.contains("топот"));
        assertTrue(loadedDictionary.contains("дедок"));
        assertFalse(loadedDictionary.contains("герань"));
        assertFalse(loadedDictionary.contains("гербарий"));
    }

    @Test
    void testWordleGameInitialization() {
        ru.yandex.practicum.WordleGame game = new ru.yandex.practicum.WordleGame(dictionary, log);

        assertNotNull(game.getAnswer());
        assertEquals(5, game.getAnswer().length());
        assertEquals(6, game.getRemainingSteps());
        assertFalse(game.isGameOver());
        assertTrue(game.getAttempts().isEmpty());
    }

    @Test
    void testWordleGameMakeGuess() {
        WordleGame game = new WordleGame(dictionary, log);
        String answer = game.getAnswer();

        assertThrows(GameWonException.class, () -> {
            game.makeGuess(answer);
        });
    }

    @Test
    void testWordleGameInvalidWordLength() {
        ru.yandex.practicum.WordleGame game = new ru.yandex.practicum.WordleGame(dictionary, log);

        try {
            game.makeGuess("аб");
            fail("Expected Exception for short word");
        } catch (Exception e) {
            assertEquals("Слово должно состоять из 5 букв", e.getMessage());
        }

        try {
            game.makeGuess("длинноеслово");
            fail("Expected Exception for long word");
        } catch (Exception e) {
            assertEquals("Слово должно состоять из 5 букв", e.getMessage());
        }
    }

    @Test
    void testWordleGameWordNotInDictionary() {
        ru.yandex.practicum.WordleGame game = new ru.yandex.practicum.WordleGame(dictionary, log);

        try {
            game.makeGuess("несущ");
            fail("Expected Exception for word not in dictionary");
        } catch (Exception e) {
            assertEquals("Слово не найдено в словаре", e.getMessage());
        }
    }

    @Test
    void testWordleGameGetHint() {
        ru.yandex.practicum.WordleGame game = new ru.yandex.practicum.WordleGame(dictionary, log);

        String hint = game.getHint();
        assertNotNull(hint);
        assertEquals(5, hint.length());
        assertTrue(dictionary.contains(hint));
    }

    @Test
    void testWordleGameMultipleGuesses() {
        ru.yandex.practicum.WordleGame game = new ru.yandex.practicum.WordleGame(dictionary, log);
        String answer = game.getAnswer();

        String guess = dictionary.getWords().stream()
                .filter(word -> !word.equals(answer))
                .findFirst()
                .orElse("казак");

        try {
            String analysis = game.makeGuess(guess);
            assertEquals(5, analysis.length());
            assertEquals(5, game.getRemainingSteps());
            assertEquals(1, game.getAttempts().size());
            assertTrue(game.getAttempts().contains(guess));
        } catch (Exception e) {

            if (e.getMessage().contains("Поздравляем")) {
                return;
            }
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testWordleDictionaryLoaderWithMixedCase(@TempDir File tempDir) throws Exception {
        File dictionaryFile = new File(tempDir, "words_ru.txt");
        try (FileWriter writer = new FileWriter(dictionaryFile, StandardCharsets.UTF_8)) {
            writer.write("АББАТ\n");
            writer.write("казак\n");
            writer.write("КаЗаК\n");
            writer.write("топот\n");
        }

        ru.yandex.practicum.WordleDictionaryLoader loader = new ru.yandex.practicum.WordleDictionaryLoader();
        ru.yandex.practicum.WordleDictionary loadedDictionary = loader.load(dictionaryFile.getAbsolutePath());

        assertNotNull(loadedDictionary);
        assertTrue(loadedDictionary.contains("аббат"));
        assertTrue(loadedDictionary.contains("казак"));
        assertTrue(loadedDictionary.contains("топот"));
    }

    @Test
    void testWordleGameAnalysisFormat() {
        ru.yandex.practicum.WordleGame game = new ru.yandex.practicum.WordleGame(dictionary, log);

        String answer = game.getAnswer();
        String guess = dictionary.getWords().stream()
                .filter(word -> !word.equals(answer))
                .findFirst()
                .orElse("радар");

        try {
            String analysis = game.makeGuess(guess);
            assertEquals(5, analysis.length());

            for (char c : analysis.toCharArray()) {
                assertTrue(c == '+' || c == '^' || c == '-');
            }
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Поздравляем"));
        }
    }

    @Test
    void testWordleGameWithPalindromeWords() {
        List<String> palindromeWords = Arrays.asList("радар", "казак", "топот", "шалаш", "ротор");
        ru.yandex.practicum.WordleDictionary palindromeDict = new ru.yandex.practicum.WordleDictionary(palindromeWords);
        ru.yandex.practicum.WordleGame game = new ru.yandex.practicum.WordleGame(palindromeDict, log);

        assertNotNull(game.getAnswer());
        assertEquals(5, game.getAnswer().length());
        assertTrue(palindromeWords.contains(game.getAnswer()));
    }

    @Test
    void testWordleGameAttemptsTracking() {
        ru.yandex.practicum.WordleGame game = new ru.yandex.practicum.WordleGame(dictionary, log);
        String answer = game.getAnswer();

        assertTrue(game.getAttempts().isEmpty());

        List<String> guesses = dictionary.getWords().stream()
                .filter(word -> !word.equals(answer))
                .limit(2)
                .toList();

        if (guesses.size() < 2) {
            return;
        }

        try {
            game.makeGuess(guesses.get(0));
            assertEquals(1, game.getAttempts().size());
            assertEquals(guesses.get(0), game.getAttempts().get(0));

            game.makeGuess(guesses.get(1));
            assertEquals(2, game.getAttempts().size());
            assertEquals(guesses.get(1), game.getAttempts().get(1));
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Поздравляем"));
        }
    }

    @Test
    void testWordleGameGetAnswer() {
        ru.yandex.practicum.WordleGame game = new ru.yandex.practicum.WordleGame(dictionary, log);
        String answer = game.getAnswer();

        assertNotNull(answer);
        assertEquals(5, answer.length());
        assertTrue(dictionary.contains(answer));
    }

    @Test
    void testWordleGameRemainingSteps() {
        ru.yandex.practicum.WordleGame game = new ru.yandex.practicum.WordleGame(dictionary, log);
        String answer = game.getAnswer();

        assertEquals(6, game.getRemainingSteps());

        String guess = dictionary.getWords().stream()
                .filter(word -> !word.equals(answer))
                .findFirst()
                .orElse("заказ");

        try {
            game.makeGuess(guess);
            assertEquals(5, game.getRemainingSteps());
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Поздравляем"));
            assertEquals(5, game.getRemainingSteps());
        }
    }

}