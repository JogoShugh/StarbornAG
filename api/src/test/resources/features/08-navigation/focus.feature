@navigation
Feature: Moving the focus around a bed

  As a gardener tending a bed by hand or by voice,
  I want to stand on a cell, a row, a column or the whole bed and move from there,
  so that I can tend and log cell by cell, row by row, column by column or bed by bed
  without naming a location every time.

  Rows are letters and columns are numbers, as on the bed page: B4 is row B, column 4.
  Zooming out goes from a cell to its row, and from a row or a column to the whole bed.

  Scenario Outline: The moves possible from a focus stop at the edges of the bed
    Given the focus is on <focus> in a bed of 4 rows and 8 columns
    Then the possible moves are "<moves>"

    Examples:
      | focus    | moves                                                                   |
      | cell A1  | east southeast south zoom-out                                           |
      | cell A8  | west southwest south zoom-out                                           |
      | cell B2  | northwest north northeast west east southwest south southeast zoom-out  |
      | cell D1  | north northeast east zoom-out                                           |
      | cell D8  | northwest north west zoom-out                                           |
      | row A    | south zoom-out                                                          |
      | row B    | north south zoom-out                                                    |
      | row D    | north zoom-out                                                          |
      | column 1 | east zoom-out                                                           |
      | column 5 | west east zoom-out                                                      |
      | column 8 | west zoom-out                                                           |
      | the bed  |                                                                         |

  Scenario Outline: A move takes the focus to its neighbor or out to a wider view
    Given the focus is on <from> in a bed of 4 rows and 8 columns
    When the focus moves <move>
    Then the focus is on <to>

    Examples:
      | from     | move      | to       |
      | cell B2  | northeast | cell A3  |
      | cell B2  | southwest | cell C1  |
      | cell B2  | zoom-out  | row B    |
      | row B    | south     | row C    |
      | row B    | zoom-out  | the bed  |
      | column 3 | west      | column 2 |
      | column 3 | zoom-out  | the bed  |

  Scenario Outline: A focus names the cells a command applies to
    Given the focus is on <focus> in a bed of 4 rows and 8 columns
    Then the focus covers <count> cells, starting "<first cells>"

    Examples:
      | focus    | count | first cells     |
      | cell C5  | 1     | 3:5             |
      | row C    | 8     | 3:1 3:2 3:3     |
      | column 2 | 4     | 1:2 2:2 3:2 4:2 |
      | the bed  | 32    | 1:1 1:2 1:3     |
