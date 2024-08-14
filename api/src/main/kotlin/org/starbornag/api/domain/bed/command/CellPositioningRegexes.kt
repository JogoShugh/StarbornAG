package org.starbornag.api.domain.bed.command

import java.util.regex.Pattern.compile

internal val intPattern = compile("""\d+""")
internal val intRegex = intPattern.toRegex()
internal val cellPattern = compile("""([A-Za-z]+|\d+)[:\s,.]*(\d+)""")
internal val cellRegex = cellPattern.toRegex()
internal val rangeJoinPattern = compile(" *(?:to|-|,|through|until) *")
internal val rangeRegex = Regex(cellPattern.pattern() + rangeJoinPattern.pattern() + cellPattern.pattern())
