@announcements
Feature: Announcing recorded events

  As a gardener watching the bed on my phone,
  I want every recorded event announced as soon as it is stored,
  so that the bed view updates while I work, and never shows work that was not recorded.

  Background:
    Given the bed "Jupiter" is prepared with 4 rows, 8 columns and cell block size 1
    And a listener is following the bed "Jupiter"

  Scenario Outline: Each cell's recorded event is announced once
    When "<action>" is done at "<location>" in the bed "Jupiter"
    Then the listener heard "<event>" for the cells "<cells>" of the bed "Jupiter"

    Examples:
      | action    | location | event      | cells           |
      | plant     | A1 A2    | planted    | 1:1 1:2         |
      | water     | A1 to B2 | watered    | 1:1 1:2 2:1 2:2 |
      | fertilize | D8       | fertilized | 4:8             |

  Scenario: A rejected command announces nothing
    When "water" is done at "A1 to E1" in the bed "Jupiter"
    Then the listener heard nothing
