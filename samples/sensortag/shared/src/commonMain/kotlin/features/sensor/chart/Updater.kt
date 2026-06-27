package com.juul.sensortag.features.sensor.chart

import com.juul.krayon.axis.axisBottom
import com.juul.krayon.axis.axisLeft
import com.juul.krayon.axis.call
import com.juul.krayon.color.limeGreen
import com.juul.krayon.color.orange
import com.juul.krayon.color.steelBlue
import com.juul.krayon.color.white
import com.juul.krayon.element.PathElement
import com.juul.krayon.element.RootElement
import com.juul.krayon.element.TransformElement
import com.juul.krayon.element.withKind
import com.juul.krayon.kanvas.Paint
import com.juul.krayon.kanvas.Transform
import com.juul.krayon.scale.ContinuousScale
import com.juul.krayon.scale.domain
import com.juul.krayon.scale.extent
import com.juul.krayon.scale.max
import com.juul.krayon.scale.min
import com.juul.krayon.scale.range
import com.juul.krayon.scale.scale
import com.juul.krayon.selection.append
import com.juul.krayon.selection.asSelection
import com.juul.krayon.selection.data
import com.juul.krayon.selection.each
import com.juul.krayon.selection.join
import com.juul.krayon.selection.selectAll
import com.juul.krayon.shape.line

private val xLinePaint = Paint.Stroke(steelBlue, 1f)
private val yLinePaint = Paint.Stroke(limeGreen, 1f)
private val zLinePaint = Paint.Stroke(orange, 1f)

private val insets = Bounds(left = 40f, bottom = 25f)
private val bounds = Bounds()

internal fun update(root: RootElement, width: Float, height: Float, data: List<Sample>) {
    if (data.count() < 2) return

    bounds.set(width, height).inset(insets)
    val (x, y) = scale(data)
    root.xAxis(x)
    root.yAxis(y)
    root.xLine(data, x, y)
    root.yLine(data, x, y)
    root.zLine(data, x, y)
}

private fun RootElement.xAxis(x: ContinuousScale<Float, Float>) {
    asSelection()
        .selectAll(TransformElement.withKind("x-axis"))
        .data(listOf(null))
        .join { append(TransformElement).each { kind = "x-axis" } }
        .each { transform = Transform.Translate(vertical = bounds.bottom) }
        .call(axisBottom(x).apply {
            lineColor = white
            textColor = white
        })
}

private fun RootElement.yAxis(y: ContinuousScale<Float, Float>) {
    asSelection()
        .selectAll(TransformElement.withKind("y-axis"))
        .data(listOf(null))
        .join { append(TransformElement).each { kind = "y-axis" } }
        .each { transform = Transform.Translate(horizontal = bounds.left) }
        .call(axisLeft(y).apply {
            lineColor = white
            textColor = white
        })
}

private fun RootElement.xLine(
    data: List<Sample>,
    x: ContinuousScale<Float, Float>,
    y: ContinuousScale<Float, Float>,
) {
    val line = line<Sample>()
        .x { (p) -> x.scale(p.t) }
        .y { (p) -> y.scale(p.x) }

    asSelection()
        .selectAll(PathElement.withKind("x-line"))
        .data(listOf(data, data))
        .join {
            append(PathElement).each {
                kind = "x-line"
                paint = xLinePaint
            }
        }.each { (datum) ->
            path = line.render(datum)
        }
}

private fun RootElement.yLine(
    data: List<Sample>,
    x: ContinuousScale<Float, Float>,
    y: ContinuousScale<Float, Float>,
) {
    val line = line<Sample>()
        .x { (p) -> x.scale(p.t) }
        .y { (p) -> y.scale(p.y) }

    asSelection()
        .selectAll(PathElement.withKind("y-line"))
        .data(listOf(data, data))
        .join {
            append(PathElement).each {
                kind = "y-line"
                paint = yLinePaint
            }
        }.each { (datum) ->
            path = line.render(datum)
        }
}

private fun RootElement.zLine(
    data: List<Sample>,
    x: ContinuousScale<Float, Float>,
    y: ContinuousScale<Float, Float>,
) {
    val line = line<Sample>()
        .x { (p) -> x.scale(p.t) }
        .y { (p) -> y.scale(p.z) }

    asSelection()
        .selectAll(PathElement.withKind("z-line"))
        .data(listOf(data, data))
        .join {
            append(PathElement).each {
                kind = "z-line"
                paint = zLinePaint
            }
        }.each { (datum) ->
            path = line.render(datum)
        }
}

private fun scale(data: List<Sample>) = scaleX(data) to scaleY(data)

private fun scaleX(data: List<Sample>) = scale()
    .domain(data.extent(Sample::t))
    .range(bounds.x)

private fun scaleY(data: List<Sample>) = scale()
    .domain(
        data.min { minOf(it.x, it.y, it.z) },
        data.max { maxOf(it.x, it.y, it.z) },
    )
    .range(bounds.y)
