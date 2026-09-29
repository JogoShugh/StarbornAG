@navigation @page
Feature: Tending a bed by moving the focus on the bed page

  As a gardener at the bed with dirty hands,
  I want to tap my way around the bed, one cell, row or column at a time, and log care right where
  I am standing,
  so that I never have to type or say a location while tending.

  Tapping follows the element's own htmx attributes, as the browser would.

  Background:
    Given a client has prepared the bed "Mars" with 4 rows, 8 columns and cell block size 1

  Scenario: The bed page opens on the whole bed
    When a gardener opens the bed page of "Mars"
    Then the page shows 4 rows labelled "A B C D" and 8 columns labelled "1 2 3 4 5 6 7 8"
    And the focus panel shows "Whole bed"

  Scenario Outline: Tapping a cell, a row letter, a column number or the bed name moves the focus
    Given a gardener has opened the bed page of "Mars"
    When the gardener taps <target>
    Then the focus panel shows "<label>"
    And the highlighted cells are "<cells>"
    And the address bar shows the focus "<path>"

    Examples:
      | target          | label     | cells                   | path     |
      | the cell B2     | B2        | B2                      | cell/B2  |
      | the row C       | Row C     | C1 C2 C3 C4 C5 C6 C7 C8 | row/C    |
      | the column 5    | Column 5  | A5 B5 C5 D5             | column/5 |
      | the bed name    | Whole bed | all                     | bed      |

  Scenario Outline: The pad offers exactly the moves possible from the focus
    When a gardener opens the focus "<focus>" of the bed page of "Mars"
    Then the pad offers "<moves>"

    Examples:
      | focus   | moves                                                                  |
      | cell/A1 | east southeast south zoom-out                                          |
      | cell/B2 | northwest north northeast west east southwest south southeast zoom-out |
      | row/D   | north zoom-out                                                         |
      | bed     |                                                                        |

  Scenario Outline: The pad is shaped for the focus, with an empty slot where the bed ends
    When a gardener opens the focus "<focus>" of the bed page of "Mars"
    Then the pad is a <shape> pad reading "<slots>"

    Examples:
      | focus    | shape      | slots                                                                  |
      | cell/B2  | compass    | northwest north northeast west zoom-out east southwest south southeast |
      | cell/A1  | compass    | - - - - zoom-out east - south southeast                                |
      | row/B    | vertical   | north zoom-out south                                                   |
      | row/A    | vertical   | - zoom-out south                                                       |
      | column/1 | horizontal | - zoom-out east                                                        |
      | bed      | empty      |                                                                        |

  Scenario Outline: Zooming out says where it goes
    When a gardener opens the focus "<focus>" of the bed page of "Mars"
    Then the pad's "zoom-out" reads "<text>"

    Examples:
      | focus    | text            |
      | cell/B2  | Out to Row B    |
      | row/C    | Out to Whole bed |
      | column/4 | Out to Whole bed |

  Scenario: A harvest offers only what grows in focus, cultivar included
    Given a client has planted "tomato" at "A1" in the bed "Mars"
    When a gardener opens the focus "row/A" of the bed page of "Mars"
    Then the action "harvest" offers "tomato" to choose as "plantType"
    And the action "harvest" offers "Dark Galaxy" to choose as "plantCultivar"

  Scenario: Walking the pad moves the focus
    Given a gardener has opened the focus "cell/B2" of the bed page of "Mars"
    When the gardener taps the pad's "northeast"
    And the gardener taps the pad's "zoom-out"
    Then the focus panel shows "Row A"

  Scenario: Care from the action bar lands on exactly the cells in focus
    Given a gardener has opened the focus "row/B" of the bed page of "Mars"
    When the gardener taps "water" in the action bar
    Then the focus panel shows "Row B"
    And the cells with a recorded "water" in the bed "Mars" are "2:1 2:2 2:3 2:4 2:5 2:6 2:7 2:8"

  Scenario: The action bar asks only for what the care needs
    When a gardener opens the focus "cell/C3" of the bed page of "Mars"
    Then the action bar offers "plant water fertilize mulch"
    And the action "plant" asks for "plantType plantCultivar"
    And the action "water" asks for nothing

  Scenario: The page and its fragments declare UTF-8, so plant icons and arrows show as drawn
    When a gardener opens the focus "cell/B2" of the bed page of "Mars"
    Then the page declares the character set "UTF-8"
    And the gardener taps the pad's "north"
    And the last answer declares the character set "UTF-8"

  Scenario: Reloading a focus address shows the whole page
    When a gardener opens the focus "cell/C3" of the bed page of "Mars"
    Then the page shows 4 rows labelled "A B C D" and 8 columns labelled "1 2 3 4 5 6 7 8"
    And the focus panel shows "C3"
