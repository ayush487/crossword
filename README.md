# Crossword Generator

## Project Overview

The **Crossword Generator** is a Java-based application that creates a crossword grid exclusively for anagrams. This project is designed to take a list of words in CSV format and generate a corresponding crossword grid.

## Features

- Generates a crossword grid specifically for anagrams.
- Simple and efficient to use.

## Installation

To install the project, follow these steps:

1. Clone the repository:
   ```sh
   git clone https://github.com/ayush487/crossword-generator.git
   ```
2. Compile the classes:
   ```sh
    javac CrosswordGenerator.java CharData.java
   ```

## Dependencies
- Java 8 (only)

## Usage

To run the application, use the following command:
  ```sh
  java CrosswordGenerator {words in csv format}
  ```

> Note : All words should be anagrams of one word, which must present in the input, else it may go in a infinite loop.

## Examples

### Input
```sh
java CrosswordGenerator draw,drop,pass,password,road,roads,soap,swap,ward,word,wrap
```

### Output
```sh
Failed Height : 8, Width : 8                                                                                  
Failed Height : 9, Width : 8
Failed Height : 9, Width : 9
Failed Height : 10, Width : 9
Crossword created! Height : 10 Width : 10
d-p---draw
road---o-a
o-s--s-a-r
password-d
-----a-s--
--swap----
---o------
--wrap----
---d------
----------

Copy from here


d-p---draw:road---o-a:o-s--s-a-r:password-d:-----a-s--:--swap----:---o------:--wrap----:---d------:----------
```

> Notice all words in Arguements are Anagrams of the word `Password`
> Output contains the crossword display in a grid as well as in textual format each line seperated by a `:`.

### Preview

![crossword preview](https://cdn.discordapp.com/attachments/1099951887454851113/1342386418034675733/image.png?ex=67b97246&is=67b820c6&hm=ffa0611274731a567b1e9ce7a750ccc0bd7e1ff3d7c1f4f97333dd622b2dc5f3&)

## License

This project is licensed under the MIT License - see the [LICENSE](https://github.com/ayush487/crossword-generator/blob/main/LICENSE) file for details.
