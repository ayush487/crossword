
import java.util.ArrayList;
import java.util.Arrays;
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
        int totalLength = 0;
        for (String s : words) {
            totalLength += s.length();
        }
        int[] a = getHB(totalLength, 2f, words[0].length());
        char[][] crossword = getEmptyGrid(a[0], a[1]);
        String firstWord = words[0];
        System.out.println(firstWord);
        addAtRandomPosition(crossword, a[0], a[1], firstWord);
        Random random = new Random(33);

        boolean isCrosswordCreated = createCrossword(words, 1, crossword, a[0], a[1], random.nextBoolean());

        if (isCrosswordCreated) {
            System.out.println("Crossword created!");
        } else {
            System.out.println("Crossword fail!");
        }
        printGrid(crossword);
        System.out.println("\nCopy from here");
        for (char[] c : crossword) {
            for (char cc : c) {
                System.out.print(cc);
            }
            System.out.print(":");
        }

    }

    static boolean createCrossword(String[] words, int wordIndex, char[][] grid, int height, int width, boolean isAcross) {
        if (wordIndex >= words.length) {
            return true;
        }

        String word = words[wordIndex];
        Pattern wordPattern = Pattern.compile(String.format("(?<![a-zA-Z+])%s(?![a-zA-Z+])", word));
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

    private static int[] getHB(int totalLength, float multiplier, int minSide) {
        totalLength *= multiplier;
        int sqrt = (int) Math.sqrt(totalLength);
        if (sqrt < minSide) {
            return new int[]{minSide, minSide};
        }
        if (sqrt * (sqrt + 1) >= totalLength) {
            int[] r = new int[]{sqrt, sqrt + 1};
            if (Math.random() > 0.5) {
                int temp = r[0];
                r[0] = r[1];
                r[1] = temp;
            }

            return r;
        } else {
            return new int[]{sqrt + 1, sqrt + 1};
        }
    }

    private static void addAtRandomPosition(char[][] chars, int height, int width, String word) {
        boolean isDown = Math.random() >= 0.5;
        if (isDown) {
            System.out.println("Populating Down");
            int upperBound = height - word.length();
            int posY = (int) (Math.random() * upperBound);
            int posX = (int) (Math.random() * width);
            for (int i = 0; i < word.length(); i++) {
                chars[posY + i][posX] = word.charAt(i);
            }
        } else {
            System.out.println("Populating Across");
            int upperBound = width - word.length();
            int posX = (int) (Math.random() * upperBound);
            int posY = (int) (Math.random() * height);
            for (int j = 0; j < word.length(); j++) {
                chars[posY][posX + j] = word.charAt(j);
            }
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
}
