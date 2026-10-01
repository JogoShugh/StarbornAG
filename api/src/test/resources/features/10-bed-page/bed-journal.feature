@page @journal
Feature: The bed journal: every cell's history, summarized

  As a gardener looking after a whole bed,
  I want one place that sums up what happened in all of its cells,
  so that I can see at a glance what was planted, watered and fed, and where.

  The sheet's handle opens the journal for where the gardener stands. It has three stops: closed,
  half (the journal under a small map of the bed) and full (the journal alone). By cell is the
  default; by row and by column fold the cells' events back into the commands that made them, and
  the timeline lists those commands across the whole focus, newest first.

  Background:
    Given a client has prepared the bed "Mars" with 4 rows, 8 columns and cell block size 1
    And a client has planted "tomato" at "A1 to A3" in the bed "Mars"
    And a client sends "water" at "A2 to B2" to the bed "Mars"

  Scenario: The handle opens the journal half way, by cell, with the bed's totals
    Given a gardener has opened the bed page of "Mars"
    When the gardener opens the journal
    Then the journal is open "half" by "cell"
    And the journal offers the tabs "Cells Rows Columns Timeline"
    And the journal's totals read:
      | planted | empty | events | last water |
      | 3       | 29    | 5      | just now   |

  Scenario: By cell, the most recently tended cells come first
    Given a gardener has opened the journal of the bed "Mars"
    Then the journal's first cells read:
      | cell | plant                | care | events   | last              |
      | A2   | Tomato · Dark Galaxy | 💧1  | 2 events | watered just now  |
      | B2   | Empty                | 💧1  | 1 event  | watered just now  |
      | A1   | Tomato · Dark Galaxy |      | 1 event  | planted just now  |
      | A3   | Tomato · Dark Galaxy |      | 1 event  | planted just now  |
      | A4   | Empty                |      | 0 events |                   |

  Scenario: By row, each command shows once with the cells it reached in that row
    Given a gardener has opened the journal of the bed "Mars"
    When the gardener taps the journal tab "Rows"
    Then the journal is open "half" by "row"
    And the row "A" in the journal grows "3 Tomato · 5 empty"
    And the row "A" in the journal reads:
      | care       | cells | when     |
      | 💧 Watered | A2    | just now |
      | 🍅 Planted | A1–A3 | just now |
    And the row "B" in the journal reads:
      | care       | cells | when     |
      | 💧 Watered | B2    | just now |

  Scenario: By column, the same folding runs down each column
    Given a gardener has opened the journal of the bed "Mars"
    When the gardener taps the journal tab "Columns"
    Then the column "2" in the journal reads:
      | care       | cells | when     |
      | 💧 Watered | A2–B2 | just now |
      | 🍅 Planted | A2    | just now |

  Scenario: The timeline lists every command in the focus, newest first
    Given a gardener has opened the journal of the bed "Mars"
    When the gardener taps the journal tab "Timeline"
    Then the journal is open "half" by "time"
    And the journal's timeline reads:
      | care       | cells | when     |
      | 💧 Watered | A2–B2 | just now |
      | 🍅 Planted | A1–A3 | just now |

  Scenario: The journal opens fully, back to half for the map, and closes
    Given a gardener has opened the journal of the bed "Mars"
    When the gardener taps "Full" on the journal
    Then the journal is open "full" by "cell"
    And the map is hidden
    When the gardener taps "Map" on the journal
    Then the journal is open "half" by "cell"
    When the gardener taps "Close" on the journal
    Then the journal is closed
    And the breadcrumb reads "Mars"

  Scenario: Below the bed, the journal covers only the focus
    Given a gardener has opened the focus "row/A" of the bed page of "Mars"
    When the gardener opens the journal
    Then the journal offers the tabs "Cells Timeline"
    And the journal lists the cells "A2 A1 A3 A4 A5 A6 A7 A8"
    And the mini map outlines the cells "A1 A2 A3 A4 A5 A6 A7 A8"

  Scenario: Tapping a cell in the journal zooms the map there
    Given a gardener has opened the journal of the bed "Mars"
    When the gardener taps the cell "A2" in the journal
    Then the journal is closed
    And the breadcrumb reads "Mars › Row A › A2"
    And the address bar shows the focus "cell/A2"

  Scenario: Reloading a journal address shows the whole page with the journal open
    When a gardener opens the journal address "focus=bed&by=row&size=full" of the bed "Mars"
    Then the page is a whole page with the journal open "full" by "row"

  Scenario: Agents read the same journal, with a link to every line's focus
    When a client reads the journal of the bed "Mars" by "row"
    Then the journal's lines link to the focuses "row/A row/B row/C row/D"
    And the journal line "row/A" reports the commands:
      | type    | cells    |
      | watered | A2       |
      | planted | A1 A2 A3 |
