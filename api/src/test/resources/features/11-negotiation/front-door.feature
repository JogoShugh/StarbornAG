@negotiation
Feature: One front door for people and agents

  As anyone arriving at the garden, person or agent,
  I want one address to start from that leads to every bed,
  so that nobody needs to know a bed's id before they can tend it.

  The front door is "/". An agent gets HAL with a link to the beds; the beds answer with each bed and
  a form to prepare a new one. A browser gets the list of beds to pick from.

  Background:
    Given a client has prepared the bed "Mars" with 4 rows, 8 columns and cell block size 1
    And a client has prepared the bed "Venus" with 2 rows, 3 columns and cell block size 1

  Scenario: An agent finds every bed from the front door
    When a client asks for "/" accepting "application/hal+json"
    Then the answer is "application/hal+json"
    And the answer links "beds" to "/beds"
    When the client follows the answer's "beds" link
    Then the answer is "application/hal+json"
    And the listed beds include:
      | name  | rows | columns |
      | Mars  | 4    | 8       |
      | Venus | 2    | 3       |
    And every listed bed links to its own address
    And the answer offers the form "prepare-bed"

  Scenario Outline: A browser at the front door gets the beds to pick from
    When a client asks for "<address>" accepting "text/html"
    Then the answer is "text/html"
    And the page links the beds "Mars Venus" to their bed pages

    Examples:
      | address |
      | /       |
      | /beds   |
