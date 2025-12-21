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
import java.util.stream.Collectors;

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

        WordleDictionaryLoader loader = new WordleDictionaryLoader();
        WordleDictionary loadedDictionary = loader.load(dictionaryFile.getAbsolutePath());

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
        WordleGame game = new WordleGame(dictionary, log);

        assertNotNull(game.getAnswer());
        assertEquals(5, game.getAnswer().length());
        assertEquals(6, game.getRemainingSteps());
        assertFalse(game.isGameOver());
        assertTrue(game.getAttempts().isEmpty());
    }

    @Test
    void testWordleGameMakeCorrectGuess() {
        WordleGame game = new WordleGame(dictionary, log);
        String answer = game.getAnswer();

        try {
            boolean isCorrect = game.makeGuess(answer);
            assertTrue(isCorrect);
            assertTrue(game.isGameOver());
            assertEquals(5, game.getRemainingSteps());
        } catch (WordleException e) {
            fail("Не должно быть исключения при правильной догадке: " + e.getMessage());
        }
    }

    @Test
    void testWordleGameInvalidWordLength() {
        WordleGame game = new WordleGame(dictionary, log);

        assertThrows(WordleGameException.class, () -> {
            game.makeGuess("аб");
        });

        assertThrows(WordleGameException.class, () -> {
            game.makeGuess("длинноеслово");
        });
    }

    @Test
    void testWordleGameWordNotInDictionary() {
        WordleGame game = new WordleGame(dictionary, log);

        assertThrows(WordleGameException.class, () -> {
            game.makeGuess("несущ");
        });
    }

    @Test
    void testWordleGameGetHint() {
        WordleGame game = new WordleGame(dictionary, log);

        String hint = game.getHint();
        assertNotNull(hint);
        assertEquals(5, hint.length());
        assertTrue(dictionary.contains(hint));
    }

    @Test
    void testWordleGameMultipleGuesses() {
        WordleGame game = new WordleGame(dictionary, log);
        String answer = game.getAnswer();

        String wrongGuess = dictionary.getWords().stream()
                .filter(word -> !word.equals(answer))
                .findFirst()
                .orElse("казак");

        try {
            boolean isCorrect = game.makeGuess(wrongGuess);
            assertFalse(isCorrect);
            assertEquals(5, game.getRemainingSteps());
            assertEquals(1, game.getAttempts().size());
            assertTrue(game.getAttempts().contains(wrongGuess));

            String analysis = game.getLastAnalysis();
            assertNotNull(analysis);
            assertEquals(5, analysis.length());
        } catch (WordleException e) {
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

        WordleDictionaryLoader loader = new WordleDictionaryLoader();
        WordleDictionary loadedDictionary = loader.load(dictionaryFile.getAbsolutePath());

        assertNotNull(loadedDictionary);
        assertTrue(loadedDictionary.contains("аббат"));
        assertTrue(loadedDictionary.contains("казак"));
        assertTrue(loadedDictionary.contains("топот"));
    }

    @Test
    void testWordleGameAnalysisFormat() {
        WordleGame game = new WordleGame(dictionary, log);

        String answer = game.getAnswer();
        String wrongGuess = dictionary.getWords().stream()
                .filter(word -> !word.equals(answer))
                .findFirst()
                .orElse("радар");

        try {
            boolean isCorrect = game.makeGuess(wrongGuess);
            assertFalse(isCorrect);

            String analysis = game.getLastAnalysis();
            assertNotNull(analysis);
            assertEquals(5, analysis.length());

            for (char c : analysis.toCharArray()) {
                assertTrue(c == '+' || c == '^' || c == '-');
            }
        } catch (WordleException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testWordleGameWithPalindromeWords() {
        List<String> palindromeWords = Arrays.asList("радар", "казак", "топот", "шалаш", "ротор");
        WordleDictionary palindromeDict = new WordleDictionary(palindromeWords);
        WordleGame game = new WordleGame(palindromeDict, log);

        assertNotNull(game.getAnswer());
        assertEquals(5, game.getAnswer().length());
        assertTrue(palindromeWords.contains(game.getAnswer()));
    }

    @Test
    void testWordleGameAttemptsTracking() {
        WordleGame game = new WordleGame(dictionary, log);
        String answer = game.getAnswer();

        assertTrue(game.getAttempts().isEmpty());

        List<String> wrongGuesses = dictionary.getWords().stream()
                .filter(word -> !word.equals(answer))
                .limit(2)
                .collect(Collectors.toList());

        if (wrongGuesses.size() < 2) {
            return;
        }

        try {
            boolean isCorrect1 = game.makeGuess(wrongGuesses.get(0));
            assertFalse(isCorrect1);
            assertEquals(1, game.getAttempts().size());
            assertEquals(wrongGuesses.get(0), game.getAttempts().get(0));

            boolean isCorrect2 = game.makeGuess(wrongGuesses.get(1));
            assertFalse(isCorrect2);
            assertEquals(2, game.getAttempts().size());
            assertEquals(wrongGuesses.get(1), game.getAttempts().get(1));
        } catch (WordleException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testWordleGameGetAnswer() {
        WordleGame game = new WordleGame(dictionary, log);
        String answer = game.getAnswer();

        assertNotNull(answer);
        assertEquals(5, answer.length());
        assertTrue(dictionary.contains(answer));
    }

    @Test
    void testWordleGameRemainingSteps() {
        WordleGame game = new WordleGame(dictionary, log);
        String answer = game.getAnswer();

        assertEquals(6, game.getRemainingSteps());

        String wrongGuess = dictionary.getWords().stream()
                .filter(word -> !word.equals(answer))
                .findFirst()
                .orElse("заказ");

        try {
            boolean isCorrect = game.makeGuess(wrongGuess);
            assertFalse(isCorrect);
            assertEquals(5, game.getRemainingSteps());
        } catch (WordleException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testWordleGameGameOverException() {
        WordleGame game = new WordleGame(dictionary, log);

        String answer = game.getAnswer();
        List<String> wrongGuesses = dictionary.getWords().stream()
                .filter(word -> !word.equals(answer))
                .limit(6)
                .collect(Collectors.toList());

        if (wrongGuesses.size() < 6) {
            return;
        }

        for (String guess : wrongGuesses) {
            try {
                boolean isCorrect = game.makeGuess(guess);
                assertFalse(isCorrect, "Попытка должна быть неправильной");
            } catch (WordleException e) {
                fail("Неожиданное исключение при попытке: " + guess + " - " + e.getMessage());
            }
        }

        assertThrows(GameOverException.class, () -> {
            game.makeGuess("любое_слово");
        });

        assertEquals(0, game.getRemainingSteps());
        assertTrue(game.isGameOver());
    }

    @Test
    void testWordleGameLastAnalysis() {
        WordleGame game = new WordleGame(dictionary, log);

        assertNull(game.getLastAnalysis(), "До первой попытки getLastAnalysis() должен возвращать null");

        String answer = game.getAnswer();
        String wrongGuess = dictionary.getWords().stream()
                .filter(word -> !word.equals(answer))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Не найдено неправильное слово для теста"));

        try {
            game.makeGuess(wrongGuess);
            assertNotNull(game.getLastAnalysis(), "После попытки должен быть анализ");
            assertEquals(5, game.getLastAnalysis().length());
        } catch (WordleException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
}