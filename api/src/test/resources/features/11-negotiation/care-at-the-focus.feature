@negotiation
Feature: Care is recorded at the focus's own address, by people and agents alike

  As the keeper of a garden tended by people and by agents,
  I want care to be posted to the same address whether it comes from a button on the page or a form
  an agent filled in,
  so that the page and the agents act through one door and answer in their own view.

  The page posts its form fields; an agent posts JSON, as its form says. Each gets the focus back in
  its own view, and care the soil rules refuse is a conflict for the agent.

  Background:
    Given a client has prepared the bed "Mars" with 4 rows, 8 columns and cell block size 1
    And a client has planted "tomato" at "A1 to A3" in the bed "Mars"

  Scenario Outline: The agent's forms post where the page's care buttons post
    Then at "<focus>" of the bed "Mars" the agent's forms and the page's care buttons post to the same addresses

    Examples:
      | focus    |
      | cell/A1  |
      | cell/C3  |
      | row/B    |
      | column/8 |
      | bed      |

  Scenario: An agent records care by submitting the focus's form and gets the focus back
    Given a client has read the focus "row/B" of the bed "Mars"
    When the client fills the form "water-cells" and submits it
    Then the response status is 200
    And the response is "application/hal+json"
    And the response's most recent event is "watered" at "B1 B2 B3 B4 B5 B6 B7 B8"

  Scenario: Care the soil rules refuse is a conflict for an agent
    When an agent posts "plant" as JSON to the focus "cell/A1" of the bed "Mars"
    Then the response status is 409
    And the response is "application/problem+json"
