@rules
Feature: Soil rules for bed cells

  As a gardener keeping honest records,
  I want each cell to accept only what can really happen in that square foot of soil,
  so that its history never shows two crops sharing a cell or a harvest from bare soil.

  A cell holds one planting at a time. Harvesting does not empty a cell.

  Background:
    Given the bed "Jupiter" is prepared with 4 rows, 8 columns and cell block size 1
    And tomato "Dark Galaxy" is planted at "A1 A3" in the bed "Jupiter"
    And lettuce "Romaine" is planted at "A2" in the bed "Jupiter"

  Scenario: Planting is rejected when any named cell is already planted
    When lettuce "Romaine" is planted at "A3 A4" in the bed "Jupiter"
    Then the command is rejected because a cell is already planted
    And the cells of the bed "Jupiter" with a recorded "plant" are "1:1 1:2 1:3"

  Scenario Outline: A harvest is recorded only on the named cells growing that plant
    When <plant> is harvested at "<location>" in the bed "Jupiter"
    Then the cells of the bed "Jupiter" with a recorded "harvest" are "<cells>"

    Examples:
      | plant   | location | cells   |
      | tomato  | A1 to A4 | 1:1 1:3 |
      | lettuce | A1 to A4 | 1:2     |
      | tomato  | A3       | 1:3     |

  Scenario: A harvest where none of the named cells grows that plant is rejected
    When tomato is harvested at "A2 B1" in the bed "Jupiter"
    Then the command is rejected because nothing there can be harvested
    And no cell of the bed "Jupiter" has a recorded "harvest"

  Scenario: A cell keeps its planting after a harvest
    When tomato is harvested at "A1" in the bed "Jupiter"
    Then the cell "A1" of the bed "Jupiter" is still growing "tomato - Dark Galaxy"
