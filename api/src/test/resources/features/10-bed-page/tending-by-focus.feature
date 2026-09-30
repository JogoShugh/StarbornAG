@navigation @page
Feature: Tending a bed zoomed to where the gardener stands

  As a gardener at the bed with dirty hands,
  I want the screen to show just where I stand: the whole bed, one row, one column or one cell among
  its neighbors, and to log care right there,
  so that I never have to type or say a location while tending.

  The view is the focus. Tapping a row letter, a column number, a neighboring row or column, or a
  neighboring cell moves there; the breadcrumb zooms back out. The map turns with the phone: in
  portrait the row letters run across the top and the column numbers down the side.

  Tapping follows the element's own htmx attributes, as the browser would.

  Background:
    Given a client has prepared the bed "Mars" with 4 rows, 8 columns and cell block size 1

  Scenario: The bed view shows the whole bed, and every row, column and cell is a way in
    When a gardener opens the bed page of "Mars"
    Then the view shows 4 rows labelled "A B C D" and 8 columns labelled "1 2 3 4 5 6 7 8"
    And the breadcrumb reads "Mars"
    And tapping these opens their focus:
      | tap      | opens    |
      | row A    | row/A    |
      | column 3 | column/3 |
      | cell B2  | cell/B2  |

  Scenario Outline: A row or a column view shows only its cells, with its neighbors peeking in
    When a gardener opens the focus "<focus>" of the bed page of "Mars"
    Then the view shows the cells "<cells>"
    And "<before>" peeks in before and "<after>" after
    And the breadcrumb reads "<crumbs>"

    Examples:
      | focus    | cells                   | before   | after    | crumbs           |
      | row/A    | A1 A2 A3 A4 A5 A6 A7 A8 | edge     | row/B    | Mars › Row A     |
      | row/B    | B1 B2 B3 B4 B5 B6 B7 B8 | row/A    | row/C    | Mars › Row B     |
      | row/D    | D1 D2 D3 D4 D5 D6 D7 D8 | row/C    | edge     | Mars › Row D     |
      | column/1 | A1 B1 C1 D1             | edge     | column/2 | Mars › Column 1  |
      | column/8 | A8 B8 C8 D8             | column/7 | edge     | Mars › Column 8  |

  Scenario Outline: The cell view is the cell among its eight neighbors, and the neighbors are the moves
    When a gardener opens the focus "<focus>" of the bed page of "Mars"
    Then the neighborhood reads "<slots>"
    And the breadcrumb reads "<crumbs>"

    Examples:
      | focus   | slots                              | crumbs             |
      | cell/A3 | edge edge edge A2 A3 A4 B2 B3 B4   | Mars › Row A › A3  |
      | cell/B2 | A1 A2 A3 B1 B2 B3 C1 C2 C3         | Mars › Row B › B2  |
      | cell/D8 | C7 C8 edge D7 D8 edge edge edge edge | Mars › Row D › D8 |

  Scenario: Tapping a neighbor steps onto it, and the breadcrumb zooms back out
    Given a gardener has opened the focus "cell/B2" of the bed page of "Mars"
    When the gardener taps the neighbor "A3"
    Then the breadcrumb reads "Mars › Row A › A3"
    And the address bar shows the focus "cell/A3"
    And the gardener taps the breadcrumb "Row A"
    And the breadcrumb reads "Mars › Row A"

  Scenario Outline: The map turns with the phone
    When a gardener opens the focus "cell/A3" of the bed page of "Mars"
    Then the neighbor "<cell>" sits <landscape> of the cell in landscape and <portrait> of it in portrait

    Examples:
      | cell | landscape | portrait |
      | A2   | left      | above    |
      | A4   | right     | below    |
      | B3   | below     | right    |

  Scenario: Care from the sheet lands on exactly the cells in view
    Given a gardener has opened the focus "row/B" of the bed page of "Mars"
    When the gardener taps "water" in the sheet
    Then the breadcrumb reads "Mars › Row B"
    And the cells with a recorded "water" in the bed "Mars" are "2:1 2:2 2:3 2:4 2:5 2:6 2:7 2:8"

  Scenario: The sheet asks only for what the care needs
    When a gardener opens the focus "cell/C3" of the bed page of "Mars"
    Then the sheet offers "plant water fertilize mulch"
    And the action "plant" asks for "plantType plantCultivar"
    And the action "water" asks for nothing

  Scenario: A harvest offers only what grows in view, cultivar included
    Given a client has planted "tomato" at "A1" in the bed "Mars"
    When a gardener opens the focus "row/A" of the bed page of "Mars"
    Then the action "harvest" offers "tomato" to choose as "plantType"
    And the action "harvest" offers "Dark Galaxy" to choose as "plantCultivar"

  Scenario: The page can be switched between light and dark
    When a gardener opens the bed page of "Mars"
    Then the page offers a light and dark toggle

  Scenario: The page and its fragments declare UTF-8, so plant icons and arrows show as drawn
    When a gardener opens the focus "cell/B2" of the bed page of "Mars"
    Then the page declares the character set "UTF-8"
    And the gardener taps the neighbor "B3"
    And the last answer declares the character set "UTF-8"

  Scenario: Reloading a focus address shows the whole page
    When a gardener opens the focus "cell/C3" of the bed page of "Mars"
    Then the page is a whole page with the breadcrumb "Mars › Row C › C3"
