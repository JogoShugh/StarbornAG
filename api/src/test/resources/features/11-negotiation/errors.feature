@negotiation
Feature: Errors an agent can act on, as vnd.error documents

  As an agent that only knows what the server tells it,
  I want every refusal to say what went wrong in the garden's own words, which fields to fix, and
  which resource it is about,
  so that I can re-read that resource and offer the gardener what is possible instead of guessing.

  HAL Schema Forms says error responses must be vnd.error documents: a message, one embedded error
  per field with its JSON pointer path, and links to what the error is about.

  Background:
    Given a client has prepared the bed "Mars" with 4 rows, 8 columns and cell block size 1
    And a client has planted "tomato" at "A1 to A3" in the bed "Mars"

  Scenario Outline: Care the soil rules refuse names the cells, and points back to the focus
    When an agent posts '<body>' to "<target>" of the bed "Mars"
    Then the response status is 409
    And the response is "application/vnd.error+json"
    And the error message is "<message>"
    And the error is about "<about>"

    Examples:
      | target                | body                                                   | message                                   | about         |
      | focus/cell/A1/plant   | {"plantType":"tomato","plantCultivar":"Dark Galaxy"}   | A1 is already planted                     | focus/cell/A1 |
      | focus/row/A/plant     | {"plantType":"tomato","plantCultivar":"Dark Galaxy"}   | A1, A2 and A3 are already planted         | focus/row/A   |
      | focus/cell/B2/harvest | {"plantType":"tomato","plantCultivar":"Dark Galaxy"}   | None of the named cells is growing tomato | focus/cell/B2 |

  Scenario Outline: A form sent without its required fields names each one
    When an agent posts '<body>' to "focus/cell/B2/plant" of the bed "Mars"
    Then the response status is 400
    And the response is "application/vnd.error+json"
    And the error is about "focus/cell/B2"
    And the field errors are "<paths>"

    Examples:
      | body                  | paths                       |
      | {"plantType":"tomato"} | /plantCultivar              |
      | {}                    | /plantCultivar /plantType   |

  Scenario Outline: An address outside the bed is not found, and points back to the bed
    When a client asks for "<address>" of the bed "Mars" expecting an error
    Then the response is "application/vnd.error+json"
    And the error message is "<message>"
    And the error is about the bed "Mars"

    Examples:
      | address        | message                        |
      | /focus/cell/Z9 | No focus 'cell/Z9' in this bed |
      | /focus/row/Q   | No focus 'row/Q' in this bed   |
