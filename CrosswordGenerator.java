
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrosswordGenerator {

    private static List<String> wordsUsed;
    private static Set<String> wordsSet;
    private static final Pattern EXTRA_UNWANTED_TEXT_PATTERN = Pattern.compile("[a-z]{2,}");

    public static void main(String[] args) {
        String[] words = args[0].split(",");
        wordsSet = Set.of(words);
        Arrays.sort(words, (w1, w2) -> w2.length() - w1.length());
        wordsUsed = new ArrayList<>(words.length);
        Random random = new Random(33);
        int gridHeight;
        int gridWidth;
        if (random.nextBoolean()) {
            gridHeight = words[0].length();
            gridWidth = words[1].length();
        } else {
            gridHeight = words[1].length();
            gridWidth = words[0].length();
        }
        char[][] crossword;
        boolean incrementHeight = true;
        boolean isCrosswordCreated = false;
        crossword = getEmptyGrid(gridHeight, gridWidth);

        List<String> l = Arrays.asList(words);
        Collections.shuffle(l);
        words = l.toArray(String[]::new);

        while (!isCrosswordCreated) {
            isCrosswordCreated = createCrossword(words, 0, crossword, gridHeight, gridWidth, random.nextBoolean());
            if (!isCrosswordCreated) {
                wordsUsed = new ArrayList<>(words.length);
                if (incrementHeight) {
                    gridHeight++;
                } else {
                    gridWidth++;
                }
                crossword = getEmptyGrid(gridHeight, gridWidth);
                incrementHeight = !incrementHeight;
            }
        }

        System.out.println("Crossword created! Height : " + gridHeight + " Width : " + gridWidth);
        printGrid(crossword);
        System.out.println("\nCopy from here");
        printCrosswordHorizontal(crossword);

    }

    static boolean createCrossword(String[] words, int wordIndex, char[][] grid, int height, int width, boolean isAcross) {
        if (wordIndex >= words.length) {
            return true;
        }

        String word = words[wordIndex];
        Pattern wordPattern = Pattern.compile(String.format("(?<![a-zA-Z+])%s(?![a-zA-Z+])", word));
        wordsUsed.add(word);
        if (isAcross) {
            // fill across
            for (int i = 0; i < height; i++) {
                for (int j = 0; j <= width - word.length(); j++) {
                    List<CharData> charData = getGridInfo(grid, height, width);
                    populateWordInGridAcross(grid, word, i, j);
                    if (checkIfGridStillCorrect(grid, charData)
                            && checkIfWordFitWell(grid, wordPattern, true, i)
                            && checkIfOtherWordsAlsoFine(grid)
                            && createCrossword(words, wordIndex + 1, grid, height, width, false)) {
                        return true;
                    } else {
                        setDataInGridFromCharData(grid, charData);
                        wordsUsed.remove(word);
                    }
                }
            }

            // fill down
            for (int i = 0; i <= height - word.length(); i++) {
                for (int j = 0; j < width; j++) {
                    List<CharData> charData = getGridInfo(grid, height, width);
                    populateWordInGridDown(grid, word, i, j);
                    if (checkIfGridStillCorrect(grid, charData)
                            && checkIfWordFitWell(grid, wordPattern, false, j)
                            && checkIfOtherWordsAlsoFine(grid)
                            && createCrossword(words, wordIndex + 1, grid, height, width, true)) {
                        return true;
                    } else {
                        setDataInGridFromCharData(grid, charData);
                    }
                }
            }
        } else {

            // fill down
            for (int i = 0; i <= height - word.length(); i++) {
                for (int j = 0; j < width; j++) {
                    List<CharData> charData = getGridInfo(grid, height, width);
                    populateWordInGridDown(grid, word, i, j);
                    if (checkIfGridStillCorrect(grid, charData)
                            && checkIfWordFitWell(grid, wordPattern, false, j)
                            && checkIfOtherWordsAlsoFine(grid)
                            && createCrossword(words, wordIndex + 1, grid, height, width, true)) {
                        return true;
                    } else {
                        setDataInGridFromCharData(grid, charData);
                    }
                }
            }

            // fill across
            for (int i = 0; i < height; i++) {
                for (int j = 0; j <= width - word.length(); j++) {
                    List<CharData> charData = getGridInfo(grid, height, width);
                    populateWordInGridAcross(grid, word, i, j);
                    if (checkIfGridStillCorrect(grid, charData)
                            && checkIfWordFitWell(grid, wordPattern, true, i)
                            && checkIfOtherWordsAlsoFine(grid)
                            && createCrossword(words, wordIndex + 1, grid, height, width, false)) {
                        return true;
                    } else {
                        setDataInGridFromCharData(grid, charData);
                    }
                }
            }
        }

        return false;
    }

    private static boolean checkIfOtherWordsAlsoFine(char[][] grid) {
        String[] downStrings = getDownString(grid);
        String[] acrossStrings = getAcrossString(grid);
        for (String w : wordsUsed) {
            boolean isFound = false;
            Pattern wordPattern = Pattern.compile(String.format("(?<![a-zA-Z+])%s(?![a-zA-Z+])", w));
            // check if present across
            for (String acrossStr : acrossStrings) {
                var matcher = wordPattern.matcher(acrossStr);
                if (matcher.find()) {
                    isFound = true;
                    break;
                }
            }
            // check if present down
            if (!isFound) {
                for (String downStr : downStrings) {
                    var matcher = wordPattern.matcher(downStr);
                    if (matcher.find()) {
                        isFound = true;
                        break;
                    }
                }
            }
            if (!isFound) {
                return false;
            }
        }
        for (String d : downStrings) {
            Matcher matcher = EXTRA_UNWANTED_TEXT_PATTERN.matcher(d);
            while (matcher.find()) {
                String matchedText = matcher.group();
                if (!wordsSet.contains(matchedText)) {
                    return false;
                }
            }
        }
        for (String a : acrossStrings) {
            Matcher matcher = EXTRA_UNWANTED_TEXT_PATTERN.matcher(a);
            while (matcher.find()) {
                String matchedText = matcher.group();
                if (!wordsSet.contains(matchedText)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String[] getAcrossString(char[][] grid) {
        int height = grid.length;
        int width = grid[0].length;
        String[] arr = new String[height];
        for (int i = 0; i < height; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < width; j++) {
                sb.append(grid[i][j]);
            }
            arr[i] = sb.toString();
        }
        return arr;
    }

    private static String[] getDownString(char[][] grid) {
        int height = grid.length;
        int width = grid[0].length;
        String[] arr = new String[width];
        for (int j = 0; j < width; j++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < height; i++) {
                sb.append(grid[i][j]);
            }
            arr[j] = sb.toString();
        }
        return arr;
    }

    private static void populateWordInGridAcross(char[][] grid, String word, int i, int j) {
        for (int jl = 0; jl < word.length(); jl++) {
            grid[i][j + jl] = word.charAt(jl);
        }
    }

    private static void populateWordInGridDown(char[][] grid, String word, int i, int j) {
        for (int il = 0; il < word.length(); il++) {
            grid[i + il][j] = word.charAt(il);
        }
    }

    private static boolean checkIfWordFitWell(char[][] gridTemp, Pattern wordPattern, boolean isAcross, int index) {
        if (isAcross) {
            int gridWidth = gridTemp[0].length;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < gridWidth; i++) {
                sb.append(gridTemp[index][i]);
            }
            var matcher = wordPattern.matcher(sb.toString());
            return matcher.find();
        } else {
            int gridHeight = gridTemp.length;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < gridHeight; i++) {
                sb.append(gridTemp[i][index]);
            }
            var matcher = wordPattern.matcher(sb.toString());
            return matcher.find();
        }
    }

    private static boolean checkIfGridStillCorrect(char[][] grid, List<CharData> charDataList) {
        for (CharData cd : charDataList) {
            if (cd.character() != grid[cd.y()][cd.x()]) {
                return false;
            }
        }
        return true;
    }

    private static List<CharData> getGridInfo(char[][] grid, int h, int w) {
        List<CharData> list = new ArrayList<>();
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (grid[i][j] != '-') {
                    list.add(new CharData(grid[i][j], j, i));
                }
            }
        }
        return list;
    }

    private static void setDataInGridFromCharData(char[][] grid, List<CharData> charDatas) {
        for (char[] cArr : grid) {
            for (int i = 0; i < cArr.length; i++) {
                cArr[i] = '-';
            }
        }
        for (CharData cd : charDatas) {
            grid[cd.y()][cd.x()] = cd.character();
        }
    }

    private static char[][] getEmptyGrid(int h, int w) {
        char[][] grid = new char[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                grid[i][j] = '-';
            }
        }
        return grid;
    }

    private static void printGrid(char[][] grid) {
        int y = grid.length;
        int x = grid[0].length;
        for (int i = 0; i < y; i++) {
            for (int j = 0; j < x; j++) {
                System.out.print(grid[i][j]);
            }
            System.out.println();
        }
    }

    private static void printCrosswordHorizontal(char[][] crossword) {
        System.out.println("\n");
        int height = crossword.length;
        for (int i=0;i<height;i++) {
            for (char c : crossword[i]) {
                System.out.print(c);
            }
            if(i!=height-1) {
                System.out.print(":");
            }
        }
    }
}
