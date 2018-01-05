package com.astrosoft.vok.views.utils

import com.github.vok.karibudsl.addChild
import com.jarektoro.responsivelayout.ResponsiveColumn
import com.jarektoro.responsivelayout.ResponsiveLayout
import com.jarektoro.responsivelayout.ResponsiveRow
import com.vaadin.ui.HasComponents


fun HasComponents.responsiveLayout(init: ResponsiveLayout.() -> Unit): ResponsiveLayout {
  val responsiveLayout = ResponsiveLayout()
  addChild(responsiveLayout)
  responsiveLayout.init()
  return responsiveLayout
}

fun ResponsiveLayout.row(init: ResponsiveRow.() -> Unit): ResponsiveRow {
  val row = addRow()
  row.init()
  return row
}

fun ResponsiveRow.collumn(init: ResponsiveColumn.() -> Unit): ResponsiveColumn {
  val collumn = addColumn()
  collumn.init()
  return collumn
}