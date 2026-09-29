@cells
Feature: Caring for cells

  As a gardener logging work while I do it,
  I want each command recorded on exactly the cells I named,
  so that every square foot keeps its own history.

  Background:
    Given the bed "Jupiter" is prepared with 4 rows, 8 columns and cell block size 1

  Scenario Outline: A command at a spoken location is recorded on exactly those cells
    When "<action>" is done at "<location>" in the bed "Jupiter"
    Then the cells of the bed "Jupiter" with a recorded "<action>" are "<cells>"

    Examples:
      | action    | location | cells           |
      | plant     | A1       | 1:1             |
      | water     | A1 to B2 | 1:1 1:2 2:1 2:2 |
      | fertilize | B3 B5    | 2:3 2:5         |
      | mulch     | 4:8      | 4:8             |

  Scenario: A command without a location applies to every cell
    When "water" is done everywhere in the bed "Jupiter"
    Then all 32 cells of the bed "Jupiter" have a recorded "water"

  Scenario: A cell's history builds up across commands and restarts
    When tomato "Dark Galaxy" is planted at "A1" in the bed "Jupiter"
    And "A1" is watered with 2.5 in the bed "Jupiter"
    And the application restarts
    Then the cell "A1" of the bed "Jupiter" shows:
      | planting             | last watered volume |
      | tomato - Dark Galaxy | 2.5                 |

  Scenario Outline: An impossible command is rejected and records nothing
    When "water" is done at "<location>" in the <bed>
    Then the command is rejected because <reason>
    And no cell of the bed "Jupiter" has a recorded "water"

    Examples:
      | location | bed            | reason                          |
      | A1       | unknown bed    | the bed does not exist          |
      | A1 to E1 | bed "Jupiter"  | the location is outside the bed |
