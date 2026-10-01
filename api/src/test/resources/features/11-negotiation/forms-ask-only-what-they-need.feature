@negotiation
Feature: Forms ask only for what the gardener has to decide

  As an agent that must never invent a value,
  I want a form to require only what the gardener actually decides,
  so that I never have to make up a time, an id or anything else the server can work out itself.

  Background:
    Given a client has prepared the bed "Mars" with 4 rows, 8 columns and cell block size 1
    And a client has planted "tomato" at "A1" in the bed "Mars"

  Scenario Outline: When care happened is optional: left out, the server records the moment it arrives
    Given a client has read the focus "<focus>" of the bed "Mars"
    Then the form "<form>" requires "<required>"

    Examples:
      | focus   | form            | required                     |
      | cell/B2 | plant-seedling  | bedId plantCultivar plantType |
      | cell/B2 | water-cells     | bedId                        |
      | cell/B2 | fertilize-cells | bedId fertilizer volume      |
      | cell/B2 | mulch-cells     | bedId material volume        |
      | cell/A1 | harvest-crop    | bedId plantCultivar plantType |

  Scenario: Care sent without a time is recorded as happening now
    When an agent posts '{"volume":1.0}' to "focus/cell/B2/water" of the bed "Mars"
    Then the response status is 200
    And the response's most recent event is "watered" at "B2"
