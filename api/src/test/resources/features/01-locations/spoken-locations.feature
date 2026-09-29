@locations
Feature: Spoken cell locations

  As a gardener speaking with dirty hands,
  I want to name cells the way I would say them out loud,
  so that a command lands on exactly the square-foot cells I mean.

  Cells are addressed like a spreadsheet: the letter is the row and the number
  is the column, so B4 is row 2, column 4. Plain numbers work too: "3:2" is
  row 3, column 2.

  Scenario Outline: A spoken location selects cells in a bed of 4 rows and 8 columns
    When the location "<location>" is resolved in a bed of 4 rows and 8 columns
    Then the selected cells are "<cells>"

    Examples:
      | location   | cells                   |
      | A1         | 1:1                     |
      | b2         | 2:2                     |
      | 3:2        | 3:2                     |
      | A1 A2 B5   | 1:1 1:2 2:5             |
      | A1 to B2   | 1:1 1:2 2:1 2:2         |
      | 1,1 to 2,3 | 1:1 1:2 1:3 2:1 2:2 2:3 |
