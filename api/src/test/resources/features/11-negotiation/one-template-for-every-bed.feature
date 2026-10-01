@negotiation
Feature: One target template for every bed, filled from the form's own fields

  As an agent, and as whoever caches or compares form definitions,
  I want a form's target to be the same template on every bed, with the bed named by a field,
  so that one form definition serves all beds and nothing about a bed is hidden inside an address.

  HAL Schema Forms fills a templated target from the form's fields, and a field may be used in both the
  address and the body. Fields the server decides are fixed the JSON Schema way: "const" (the only
  valid value), "default" (the value to start from) and "readOnly" (managed by the server alone).

  Background:
    Given a client has prepared the bed "Mars" with 4 rows, 8 columns and cell block size 1
    And a client has prepared the bed "Venus" with 4 rows, 8 columns and cell block size 1
    And a client has planted "tomato" at "A1" in the bed "Mars"
    And a client has planted "tomato" at "A1" in the bed "Venus"

  Scenario Outline: The same form has the same target template on every bed, with the bed as a fixed field
    Then the form "<form>" at "<focus>" has the same target template in "Mars" and "Venus"
    And the target template of the form "<form>" at "<focus>" of "Mars" starts with "/beds/{bedId}/"
    And at "<focus>" of "Mars" the form "<form>" fixes "bedId" to the bed's id

    Examples:
      | form           | focus   |
      | go-to-row      | bed     |
      | go-to-column   | bed     |
      | go-to-cell     | bed     |
      | go-to-cell     | row/B   |
      | refresh        | cell/A1 |
      | water-cells    | cell/A1 |
      | harvest-crop   | cell/A1 |
      | plant-seedling | cell/B2 |

  Scenario: The cells a care form acts on are fixed the same way
    Then at "cell/A1" of "Mars" the form "water-cells" fixes "location" to "A1"

  Scenario: Filled from its fields, fixed ones included, a care form posts where the page's button does
    Then at "cell/B2" of the bed "Mars" the agent's forms and the page's care buttons post to the same addresses
