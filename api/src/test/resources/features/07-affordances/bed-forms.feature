@affordances
Feature: Bed affordances as HAL Schema Forms

  As an agent or a client driving a bed through hypermedia,
  I want the bed to offer only the actions that are possible right now, each as a form with a JSON Schema,
  so that I never guess what I may do, where to send it, or what to put in it.

  The forms follow the same soil rules the bed cells enforce: planting is offered while some cell
  is empty, and a harvest is offered once something grows, only for the plant types that grow.

  Scenario: A bed declares the HAL Schema Forms profile
    Given a client has prepared the bed "Mars" with 2 rows, 4 columns and cell block size 1
    When a client reads the bed named "Mars"
    Then the response declares the profile "https://github.com/jbadeau/hal-schema-forms"

  Scenario Outline: The offered forms follow the state of the cells
    Given a client has prepared the bed "Mars" with 1 rows, 2 columns and cell block size 1
    And a client has planted "<plant>" at "<location>" in the bed "Mars"
    When a client reads the bed named "Mars"
    Then the response forms are "<forms>"
    And the form "harvest-crop" offers the plant types "<harvestable>"

    Examples:
      | plant  | location | forms                                                              | harvestable |
      | none   |          | plant-seedling water-cells fertilize-cells mulch-cells             |             |
      | tomato | A1       | plant-seedling water-cells fertilize-cells mulch-cells harvest-crop | tomato      |
      | tomato | A1 A2    | water-cells fertilize-cells mulch-cells harvest-crop                | tomato      |

  Scenario Outline: Each form says where, how and what to send
    Given a client has prepared the bed "Mars" with 2 rows, 4 columns and cell block size 1
    When a client reads the bed named "Mars"
    Then the form "<form>" posts "application/json" to the bed's "<link>" link
    And the form "<form>" requires "<required>"

    Examples:
      | form            | link      | required                                |
      | plant-seedling  | plant     | bedId plantType plantCultivar           |
      | water-cells     | water     | bedId                                   |
      | fertilize-cells | fertilize | bedId volume fertilizer                 |
      | mulch-cells     | mulch     | bedId volume material                   |

  Scenario: A client that only follows the form can water the bed
    Given a client has prepared the bed "Mars" with 2 rows, 4 columns and cell block size 1
    And a client has read the bed named "Mars"
    When the client fills the form "water-cells" with location "A1 to B2" and submits it
    Then the response status is 200
    And the cells with a recorded "water" in the response are "1:1 1:2 2:1 2:2"
