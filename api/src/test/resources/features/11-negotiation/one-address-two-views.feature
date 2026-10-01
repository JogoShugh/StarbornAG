@negotiation
Feature: One address, two views: the page for people, HAL Schema Forms for agents

  As the keeper of a garden tended by people and by agents,
  I want every bed, focus and journal to have one address that answers a browser with the page and
  an agent with HAL Schema Forms,
  so that both are offered the same moves and the same care, and neither view can drift from the other.

  Which view comes back depends only on the Accept header. Page-only state such as how far the
  journal is open stays in the page; agents navigate links to the same content instead, and get the
  most recent events embedded.

  Background:
    Given a client has prepared the bed "Mars" with 4 rows, 8 columns and cell block size 1
    And a client has planted "tomato" at "A1 to A3" in the bed "Mars"

  Scenario Outline: The same address answers each kind of client in its own media type
    When a client asks for "<address>" of the bed "Mars" accepting "<accept>"
    Then the answer is "<media type>"
    And the answer varies by "Accept"

    Examples:
      | address                 | accept                          | media type           |
      | /                       | text/html                       | text/html            |
      | /                       | application/hal+json            | application/hal+json |
      | /focus/cell/A3          | text/html,application/xhtml+xml | text/html            |
      | /focus/cell/A3          | application/hal+json            | application/hal+json |
      | /focus/cell/A3          | */*                             | application/hal+json |
      | /focus/row/B            | application/json                | application/hal+json |
      | /journal?by=row         | text/html                       | text/html            |
      | /journal?by=row         | application/hal+json            | application/hal+json |

  Scenario Outline: A gardener and an agent at the same address are offered the same care and moves
    Then a gardener and an agent at "<focus>" of the bed "Mars" are offered the same care and moves

    Examples:
      | focus     |
      | cell/A1   |
      | cell/B2   |
      | cell/D8   |
      | row/A     |
      | row/D     |
      | column/1  |
      | column/8  |
      | bed       |

  Scenario: An agent's links lead to the same addresses the page uses
    When a client asks for "/focus/cell/B2" of the bed "Mars" accepting "application/hal+json"
    Then every link of the answer leads into "/beds/"
    And the answer links "journal" to the journal of "cell/B2"

  Scenario: Agents get the most recent events embedded, newest first
    Given a client sends "water" at "A2 to B2" to the bed "Mars"
    When a client asks for "/focus/row/A" of the bed "Mars" accepting "application/hal+json"
    Then the embedded recent events read:
      | type    | cells    |
      | watered | A2       |
      | planted | A1 A2 A3 |

  Scenario Outline: Agents choose how many recent events come embedded
    Given a client sends "water" at "A1" to the bed "Mars"
    And a client sends "water" at "A2" to the bed "Mars"
    And a client sends "water" at "A3" to the bed "Mars"
    When a client asks for "/focus/row/A<query>" of the bed "Mars" accepting "application/hal+json"
    Then <count> recent events are embedded

    Examples:
      | query      | count |
      |            | 4     |
      | ?recent=2  | 2     |
      | ?recent=0  | 0     |

  Scenario Outline: Agents reach every way of reading the journal by following links
    When a client asks for "/journal?focus=<focus>" of the bed "Mars" accepting "application/hal+json"
    Then the answer links the journal folds "<folds>"
    And the page-only "size" is not part of any link

    Examples:
      | focus  | folds                         |
      | bed    | by-cell by-row by-column by-time |
      | row/A  | by-cell by-time               |
