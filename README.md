# StarbornAG
Starborn Abundance Gardens Microfarm Management System

## The Story Behind StarbornAG

As a beginner, but very eager, organic gardener managing multiple raised garden beds, I kept getting frustrated with the typical ways of garden record-keeping. Paper journals get dirty or wet, and I didn't like transcribing hand-written notes into spreadsheets. OCR approaches don't work that well with dirty or wet sheets either. And, the existing mobile apps don't work well for me either - tapping through multiple screens and typing with soil-covered fingers was both tedious and impractical.

I needed something that would let me simply **speak aloud** my garden activities while I worked as well snap quick photos, something that would capture and organize my data without interrupting my flow. That's how StarbornAG was born.

## A Day in My Garden with StarbornAG

### Morning Garden Setup

When I approach my newest plot, I simply pull out my phone and open StarbornAG. "Setup the Jupiter Bed here 8 by 4," I speak while taking a quick photo of the empty bed. The app captures the GPS coordinates and creates a digital representation of my garden bed, divided into 32 square-foot cells, following the Square Foot Gardening method I use.

### Spring Planting

With my Dark Galaxy tomato seedlings ready to transplant and romaine lettuce seeds in hand, I speak naturally to the app:

"Planted Dark Galaxy seedlings in rows 1 and 3, and romaine lettuce in rows 2 and 4"

The app understands that I'm interleaving my plantings - a technique I've long wanted to validate with data. Each square foot is tracked individually, building a historical record of what's planted where.

### Daily Maintenance

Throughout the week, I use quick voice commands to log my garden care:

- "Watered Jupiter" - recording a watering event for all cells
- "Watered the lettuce rows" - the app knows to log water only for rows 2 and 4
- "Applied compost tea to the tomato rows" - tracking my organic fertilization methods
- "Observed ladybugs in cell B4" - noting beneficial insects
- "Applied shade cloth to rows 1 through 4" - protecting my tomatoes and lettuce during a heat wave

### Harvest Time

As plants mature, I continue logging with statements like these:

* "Harvested two pounds of tomatoes from row 1" – the app evenly divides the lbs amongst all tomato containing cells within the row
* “Harvested 6 oz from A1, 10 oz from A2, 4 oz from A3 and 12 oz from A4” – logs the harvest into each cell more specifically
* "Removed three bolted lettuce plants from row 4"
* "Planted succession lettuce in row 4"

I also can simply snap photosgraphs of the harvest from a bed, row, or particular cell and then say something like:

* "From row 1" -- in this case, it analyzes the number of fruits or veggies in the photo and distributes them throughout the cells of the row
* "Harvested from A1" -- it analyzes the quantity in the photo, but logs it entirely within cell A1
* <nothing> -- if I say nothing, it randomly assigns the harvested items to a plant matching the produce analyzed in the photo
  
_**Basically, it's up to me on how precise I want my records to be. The software supports me every step of the way, as lazy or as meticulous as I want to be!**_

## Beyond Personal Use

What started as a solution to my own record-keeping challenges has grown into something much more significant. StarbornAG is now contributing to a larger scientific understanding of organic growing methods. Every action logged helps validate or challenge traditional gardening wisdom, providing data-driven insights into sustainable growing techniques.

The app's machine learning algorithms correlate methods - like interplanting strategies, climate protection measures, and organic fertilization schedules - with yields and plant health outcomes.

## Community Growth

I now regularly share StarbornAG data with my community garden group, helping others see the real results of various growing methods. The app's scientific approach bridges the gap between traditional garden wisdom and data-driven validation, all while keeping the logging process as natural as speaking to a friend.

---

*StarbornAG transforms garden record-keeping from a tedious chore into a seamless part of daily garden care, while building a foundation for scientific understanding of sustainable growing methods.*

# Early Prototype

Leverage AI for building a prototype of a VUI / GUI for logging home gardening activities: planting, mulching, watering, weeding, harvesting, etc

<img width="600" alt="image" src="https://gist.github.com/user-attachments/assets/869af79e-4f8f-475c-a025-307c5a2a4856">


## Tools

* Spring Boot with Spring AI, leveraging OpenAI's chat completions API
* Kotlin language utilizing coroutines and an application-level CoroutineScope for background processing
* Spring SSE Support for sending processed result events to the GUI for screen updating
* HTMX library on the front end with its [amazingly simple SSE support](https://github.com/bigskysoftware/htmx/blob/master/www/content/extensions/sse.md)

## Challenges

* While the chat completions API supports the concept of [Function Calling](https://platform.openai.com/docs/guides/function-calling) to let you send it a JSON schema of your function signatures and it attempts to produce an array of one or more "toolCalls" results, this does increase the system propmpt payload significantly, and complicates the process of parsing the streamed results. 
* Spring's SSE Emitter does not support, out of the box, 1 to N subscriptions.

## Solutions

* I utilized the Actson library found on Github for supporting parsing JSON objects "just-in-time", and calling `emit` with them via a Kotlin flow as soon as enough of the fragment from the overall response arrives. See [this write up in Github for details so far](https://github.com/michel-kraemer/actson/issues/91).
* To support a 1 to N subcriber model, I leveraged the SSE EventBus project from Github which allows this easily.
  * But, I also wanted to allow each subscriber to specify the Media Type they wanted to receive, since some programmatic subscribers will need JSON, while the GUI will just need simple plaintext or HTML fragments.
    * Began creating this [PR](https://github.com/JogoShugh/sse-eventbus/pull/1/files#diff-b83da490a854167414f4bc48b8a5cbc6ff36c5dcb694eb1f039a993bcac27fc6) to update SSE EventBus to leverage Spring's built in MediaType formatting support and provide an option to bypass the original custom approach in the library without breaking existing behavior.
    * [See this write up](https://github.com/ralscha/sse-eventbus/issues/28#issuecomment-2418313352) for how I'm starting to use it in my project.

## Video Demonstrations

Inquire with me directly

