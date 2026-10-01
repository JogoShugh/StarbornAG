@negotiation
Feature: Forms say what they are for

  As an agent that maps what a gardener asks for onto the forms on offer,
  I want every form and every field to carry a title and a description,
  so that I choose by what the server says a form does, not by guessing from its id.

  HAL Schema Forms schemas are JSON Schema: "title" names a form or field, "description" explains it,
  including the unit of anything measured.

  Background:
    Given a client has prepared the bed "Mars" with 4 rows, 8 columns and cell block size 1
    And a client has planted "tomato" at "A1" in the bed "Mars"

  Scenario Outline: Every form is titled, and every field has a title and a description
    When a client asks for "<address>" accepting "application/hal+json"
    Then the forms are titled "<titles>"
    And every field of every form has a title and a description

    Examples:
      | address             | titles                                                                    |
      | Mars:/focus/cell/B2 | Plant a seedling, Water, Feed, Mulch, Refresh                             |
      | Mars:/focus/cell/A1 | Water, Feed, Mulch, Harvest, Refresh                                      |
      | Mars:/focus/bed     | Plant a seedling, Water, Feed, Mulch, Harvest, Go to a row, Go to a column, Go to a cell, Refresh |
      | /beds               | Prepare a bed                                                             |

  Scenario Outline: Anything measured says its unit
    When a client asks for "Mars:/focus/cell/A1" accepting "application/hal+json"
    Then the field "<field>" of the form "<form>" is described as "<description>"

    Examples:
      | form            | field  | description                                   |
      | water-cells     | volume | How much water was given, in liters           |
      | fertilize-cells | volume | How much fertilizer was given, in liters      |
      | mulch-cells     | volume | How much mulch was spread, in liters          |
      | harvest-crop    | weight | How much the harvest weighed, in kilograms    |
