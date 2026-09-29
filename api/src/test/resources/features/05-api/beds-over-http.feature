@api
Feature: Beds over HTTP

  As a client of the API (the bed page, a script, or an agent),
  I want every bed and cell request served from the stored events,
  so that what I see is what was recorded, before and after restarts.

  Scenario: Preparing a bed answers with its location and its links
    When a client prepares the bed "Mars" with 2 rows, 4 columns and cell block size 1
    Then the response status is 201
    And the response has a Location header for the bed "Mars"
    And the response links are "self plant water fertilize mulch harvest history"

  Scenario Outline: A command at a location answers with the bed's updated cells
    Given a client has prepared the bed "Mars" with 2 rows, 4 columns and cell block size 1
    When a client sends "<action>" at "<location>" to the bed "Mars"
    Then the response status is 200
    And the cells with a recorded "<action>" in the response are "<cells>"

    Examples:
      | action | location | cells       |
      | plant  | A1 A2    | 1:1 1:2     |
      | water  | B1 to B3 | 2:1 2:2 2:3 |

  Scenario Outline: A command that cannot be carried out answers with an error status
    Given a client has prepared the bed "Mars" with 2 rows, 4 columns and cell block size 1
    When a client sends "water" at "<location>" to the <bed>
    Then the response status is <status>

    Examples:
      | location | bed          | status |
      | A1       | unknown bed  | 404    |
      | A1 to C1 | bed "Mars"   | 400    |

  Scenario Outline: The demo beds are ready after startup
    When a client reads the bed <id>
    Then the response status is 200
    And the response bed is named "<name>" with <rows> rows of <cells> cells

    Examples:
      | id                                   | name    | rows | cells |
      | 2fbda883-d49d-4067-8e16-2b04cc523111 | Jupiter | 5    | 10    |
      | c0e75294-4b1e-4664-9037-3ca56f41ac5a | Earth   | 5    | 10    |
