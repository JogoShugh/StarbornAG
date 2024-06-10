# June 4 2024 Plan

* Refactor BedAggregate to encapsulate N Squares 
* Make each Square be the ultimate recipient of commands
* Make BedCommandHandler be a facade over the Squares
* Handle base case of a "Bed Wide" Water command which dispatches to all N Squares
    * Ensure immediate sync "received" response
    * Asynchronously call each Square's Water handler via command disptach
      * Store the "State" as BedWatered event
* If time, implement support for a "wait" style response that collects each square's response into a response document
* If time, apply pattern to all the existing Commands / Events

## Gaps

* In this fire and forget approach there is no error handling or information available to the API caller
  * We can store the command results as SquareWatered which could have an optional Error?
  * Or, store a WaterSquare command with an error, to allow for on-demand replay?

