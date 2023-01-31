package dev.dialector.lsp

import dev.dialector.server.DocumentLocation
import dev.dialector.server.TextPosition
import dev.dialector.server.TextRange
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

/**
 * Converts an LSP [Position] to Dialector's [TextPosition]
 */
fun Position.asTextPosition(): TextPosition = TextPosition(this.line, this.character)

/**
 * Converts a Dialector [TextPosition] to LSP's [Position]
 */
fun TextPosition.asPosition(): Position = Position(this.line, this.column)

/**
 * Converts an LSP [Range] to Dialector's [TextRange]
 */
fun Range.asTextRange(): TextRange = TextRange(this.start.asTextPosition(), this.end.asTextPosition())

/**
 * Converts a Dialector [TextRange] to LSP's [Range]
 */
fun TextRange.asRange(): Range = Range(this.start.asPosition(), this.end.asPosition())

/**
 * Converts an LSP [Location] to Dialector's [DocumentLocation]
 */
fun Location.asDocumentLocation(): DocumentLocation = DocumentLocation(this.uri, this.range.asTextRange())

/**
 * Converts a Dialector [DocumentLocation] to LSP's [Location]
 */
fun DocumentLocation.asLocation(): Location = Location(this.uri, this.range.asRange())
