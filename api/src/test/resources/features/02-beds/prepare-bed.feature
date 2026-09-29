@beds
Feature: Preparing beds

  As a gardener setting up a new raised bed,
  I want the bed and its square-foot cell layout recorded as an event,
  so that it survives restarts and every later command can find its cells.

  A bed has rows and columns of feet. The cell block size groups columns into
  cells: with a block size of 2, a 10-foot row has 5 cells.

  Scenario Outline: A prepared bed has one cell per block in every row
    When the bed "<name>" is prepared with <rows> rows, <columns> columns and cell block size <block>
    Then the bed "<name>" has <rows> rows of <cells per row> cells
    And every cell of the bed "<name>" has its own id

    Examples:
      | name    | rows | columns | block | cells per row |
      | Jupiter | 4    | 8       | 1     | 8             |
      | Earth   | 5    | 10      | 2     | 5             |

  Scenario: A prepared bed is still there after the application restarts
    Given the bed "Jupiter" is prepared with 4 rows, 8 columns and cell block size 1
    When the application restarts
    Then the bed "Jupiter" has 4 rows of 8 cells

  Scenario: Preparing the same bed twice is rejected
    Given the bed "Jupiter" is prepared with 4 rows, 8 columns and cell block size 1
    When the bed "Jupiter" is prepared again
    Then the preparation is rejected because the bed already exists
