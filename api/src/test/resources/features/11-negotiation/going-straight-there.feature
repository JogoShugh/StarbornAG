@negotiation
Feature: Going straight to a row, column or cell with GET forms

  As an agent asked to tend one named place in a bed,
  I want forms that take me straight there, with the valid places stated in their schemas,
  so that I never build an address myself and never walk the bed one step at a time.

  HAL Schema Forms lets a GET form have a templated target: its fields fill the template (RFC 6570),
  and its JSON Schema says which values are valid. The whole bed offers one form per way in; a row or
  a column offers a form for its own cells. A cell needs none: its neighbors are links.

  Background:
    Given a client has prepared the bed "Mars" with 4 rows, 8 columns and cell block size 1

  Scenario: The whole bed offers a GET form for each way in, bounded by the bed's size
    When a client asks for "/focus/bed" of the bed "Mars" accepting "application/hal+json"
    Then the answer offers these GET forms:
      | form         | target                    | fields     |
      | go-to-row    | /focus/row/{row}          | row        |
      | go-to-column | /focus/column/{column}    | column     |
      | go-to-cell   | /focus/cell/{row}{column} | row column |
    And the form "go-to-row" lets "row" be one of "A B C D"
    And the form "go-to-column" lets "column" run from 1 to 8
    And the form "go-to-cell" lets "row" be one of "A B C D"
    And the form "go-to-cell" lets "column" run from 1 to 8

  Scenario Outline: A row or a column offers its own cells, and only those
    When a client asks for "/focus/<focus>" of the bed "Mars" accepting "application/hal+json"
    Then the form "go-to-cell" lets "row" be one of "<rows>"
    And the form "go-to-cell" lets "column" run from <first> to <last>

    Examples:
      | focus    | rows    | first | last |
      | row/B    | B       | 1     | 8    |
      | column/3 | A B C D | 3     | 3    |

  Scenario Outline: An agent fills a GET form and lands on that focus
    When an agent fills the GET form "<form>" at "<from>" of the bed "Mars" with "<values>"
    Then the answer is "application/hal+json"
    And the agent stands on "<label>"

    Examples:
      | from     | form         | values          | label    |
      | bed      | go-to-row    | row=C           | Row C    |
      | bed      | go-to-column | column=7        | Column 7 |
      | bed      | go-to-cell   | row=B column=4  | B4       |
      | row/D    | go-to-cell   | row=D column=8  | D8       |
      | column/2 | go-to-cell   | row=A column=2  | A2       |

  Scenario Outline: Every place a gardener can tap, an agent can reach there, and the other way round
    Then at "<focus>" of the bed "Mars" a gardener and an agent can reach the same places

    Examples:
      | focus    |
      | bed      |
      | row/B    |
      | column/3 |
      | cell/B2  |
      | cell/D8  |
