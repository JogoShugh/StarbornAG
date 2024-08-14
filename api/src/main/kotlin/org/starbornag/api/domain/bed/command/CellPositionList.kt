package org.starbornag.api.domain.bed.command

class CellPositionList(vararg initialValues: CellPosition) : List<CellPosition> by initialValues.toList()