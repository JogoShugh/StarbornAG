@navigation @api
Feature: A focus as a hypermedia resource

  As an agent or the bed page moving around a bed,
  I want every focus to have its own address, a link for each possible move and forms that act
  on exactly the cells in focus,
  so that moving and tending need nothing but following links and submitting forms.

  Focus addresses: cell/B4, row/B, column/4, or bed.

  Background:
    Given a client has prepared the bed "Mars" with 4 rows, 8 columns and cell block size 1

  Scenario Outline: A focus offers a link for every possible move, and none past the edges
    When a client reads the focus "<focus>" of the bed "Mars"
    Then the response status is 200
    And the focus is labelled "<label>"
    And the move links are "<moves>"

    Examples:
      | focus    | label     | moves                                                                  |
      | cell/B2  | B2        | northwest north northeast west east southwest south southeast zoom-out |
      | cell/A1  | A1        | east southeast south zoom-out                                          |
      | row/D    | Row D     | north zoom-out                                                         |
      | column/8 | Column 8  | west zoom-out                                                          |
      | bed      | Whole bed |                                                                        |

  Scenario: Following a move link lands on the neighbor
    Given a client has read the focus "cell/B2" of the bed "Mars"
    When the client follows the "northeast" link
    Then the focus is labelled "A3"
    And the client follows the "zoom-out" link
    And the focus is labelled "Row A"

  Scenario Outline: The forms of a focus act on exactly the cells in focus
    Given a client has read the focus "<focus>" of the bed "Mars"
    When the client fills the form "water-cells" and submits it
    Then the response status is 200
    And the cells with a recorded "water" in the response are "<cells>"

    Examples:
      | focus    | cells                           |
      | cell/B2  | 2:2                             |
      | row/A    | 1:1 1:2 1:3 1:4 1:5 1:6 1:7 1:8 |
      | column/3 | 1:3 2:3 3:3 4:3                 |

  Scenario: The forms of a focus follow the state of the cells in focus
    Given a client has planted "tomato" at "B2" in the bed "Mars"
    When a client reads the focus "cell/B2" of the bed "Mars"
    Then the response's care forms are "water-cells fertilize-cells mulch-cells harvest-crop"

  Scenario Outline: A focus that is not part of the bed is not found
    When a client reads the focus "<focus>" of the bed "Mars"
    Then the response status is 404

    Examples:
      | focus     |
      | cell/E1   |
      | row/E     |
      | column/9  |
