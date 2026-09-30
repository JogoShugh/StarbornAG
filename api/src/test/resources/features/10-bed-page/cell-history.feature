@page
Feature: A cell's history in its sheet

  As a gardener standing at one square foot of soil,
  I want to see what happened to it, newest first,
  so that I know whether it needs water or care without remembering.

  The bed cell keeps the long-lived history of its soil; the cell view reads it straight from there.

  Background:
    Given a client has prepared the bed "Mars" with 4 rows, 8 columns and cell block size 1

  Scenario: The cell view lists the cell's history, newest first, with how long ago
    Given a client has planted "tomato" at "B2" in the bed "Mars"
    And a client sends "water" at "B2" to the bed "Mars"
    When a gardener opens the focus "cell/B2" of the bed page of "Mars"
    Then the sheet's history reads:
      | what                           | when     |
      | 💧 Watered                     | just now |
      | 🍅 Planted tomato · Dark Galaxy | just now |

  Scenario: Care recorded from the sheet shows up at the top of the history
    Given a client has planted "tomato" at "B2" in the bed "Mars"
    And a gardener has opened the focus "cell/B2" of the bed page of "Mars"
    When the gardener taps "water" in the sheet
    Then the sheet's history starts with "💧 Watered"

  Scenario Outline: A cell with no history says so, and wider views show none
    When a gardener opens the focus "<focus>" of the bed page of "Mars"
    Then the sheet's history shows "<shows>"

    Examples:
      | focus    | shows              |
      | cell/C3  | Nothing yet        |
      | row/B    | no history         |
      | column/2 | no history         |
      | bed      | no history         |
