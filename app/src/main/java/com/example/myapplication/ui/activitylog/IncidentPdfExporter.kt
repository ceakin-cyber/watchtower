package com.example.myapplication.ui.activitylog

import android.content.Context
import com.example.myapplication.ui.incident.Incident
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.layout.element.Cell
import com.itextpdf.kernel.colors.ColorConstants
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class IncidentPdfExporter(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val fileNameDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    fun exportIncidents(incidents: List<Incident>, startDate: Long? = null, endDate: Long? = null): File {
        val fileName = "incident_report_${fileNameDateFormat.format(Date())}.pdf"
        val file = File(context.filesDir, fileName)
        
        val writer = PdfWriter(FileOutputStream(file))
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)
        
        try {
            val font = PdfFontFactory.createFont()
            val boldFont = PdfFontFactory.createFont()
            
            // Header
            document.add(
                Paragraph("INCIDENT ACTIVITY REPORT")
                    .setFont(boldFont)
                    .setFontSize(20f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20f)
            )
            
            // Report metadata
            document.add(
                Paragraph("Generated on: ${dateFormat.format(Date())}")
                    .setFont(font)
                    .setFontSize(10f)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(10f)
            )
            
            // Add date range information if filtered
            if (startDate != null && endDate != null) {
                val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                document.add(
                    Paragraph("Date Range: ${displayDateFormat.format(Date(startDate))} - ${displayDateFormat.format(Date(endDate))}")
                        .setFont(font)
                        .setFontSize(10f)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setMarginBottom(5f)
                )
            }
            
            document.add(
                Paragraph("Total Incidents: ${incidents.size}")
                    .setFont(boldFont)
                    .setFontSize(12f)
                    .setMarginBottom(20f)
            )
            
            if (incidents.isEmpty()) {
                document.add(
                    Paragraph("No incidents to report.")
                        .setFont(font)
                        .setFontSize(12f)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(50f)
                )
            } else {
                // Summary statistics
                addSummarySection(document, incidents, font, boldFont)
                
                // Detailed incident list
                addIncidentTable(document, incidents, font, boldFont)
            }
            
        } finally {
            document.close()
        }
        
        return file
    }
    
    private fun addSummarySection(document: Document, incidents: List<Incident>, font: PdfFont, boldFont: PdfFont) {
        document.add(
            Paragraph("SUMMARY")
                .setFont(boldFont)
                .setFontSize(14f)
                .setMarginBottom(10f)
        )
        
        // Severity breakdown
        val severityCounts = incidents.groupBy { it.severity_level }.mapValues { it.value.size }
        val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f)))
            .setWidth(UnitValue.createPercentValue(50f))
        
        summaryTable.addHeaderCell(
            Cell().add(Paragraph("Severity Level").setFont(boldFont).setFontSize(10f))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
        )
        summaryTable.addHeaderCell(
            Cell().add(Paragraph("Count").setFont(boldFont).setFontSize(10f))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
        )
        
        severityCounts.forEach { (severity, count) ->
            summaryTable.addCell(Cell().add(Paragraph(severity.name).setFont(font).setFontSize(10f)))
            summaryTable.addCell(Cell().add(Paragraph(count.toString()).setFont(font).setFontSize(10f)))
        }
        
        document.add(summaryTable)
        document.add(Paragraph().setMarginBottom(20f))
    }
    
    private fun addIncidentTable(document: Document, incidents: List<Incident>, font: PdfFont, boldFont: PdfFont) {
        document.add(
            Paragraph("DETAILED INCIDENT LOG")
                .setFont(boldFont)
                .setFontSize(14f)
                .setMarginBottom(10f)
        )
        
        val table = Table(UnitValue.createPercentArray(floatArrayOf(2f, 1.5f, 1f, 3f, 1f, 1f)))
            .setWidth(UnitValue.createPercentValue(100f))
        
        // Headers
        val headers = listOf("Date/Time", "Type", "Severity", "Location", "Reported", "Case #")
        headers.forEach { header ->
            table.addHeaderCell(
                Cell().add(Paragraph(header).setFont(boldFont).setFontSize(9f))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            )
        }
        
        // Data rows
        incidents.sortedByDescending { it.timestamp }.forEach { incident ->
            table.addCell(Cell().add(Paragraph(incident.dateTime).setFont(font).setFontSize(8f)))
            table.addCell(Cell().add(Paragraph(incident.incident_type).setFont(font).setFontSize(8f)))
            table.addCell(Cell().add(Paragraph(incident.severity_level.name).setFont(font).setFontSize(8f)))
            table.addCell(Cell().add(Paragraph(incident.location).setFont(font).setFontSize(8f)))
            table.addCell(Cell().add(Paragraph(if (incident.reported_to_authorities) "Yes" else "No").setFont(font).setFontSize(8f)))
            table.addCell(Cell().add(Paragraph(incident.case_number ?: "N/A").setFont(font).setFontSize(8f)))
        }
        
        document.add(table)
        
        // Add detailed descriptions section
        if (incidents.any { it.description.isNotBlank() }) {
            document.add(Paragraph().setMarginBottom(20f))
            addDetailedDescriptions(document, incidents, font, boldFont)
        }
    }
    
    private fun addDetailedDescriptions(document: Document, incidents: List<Incident>, font: PdfFont, boldFont: PdfFont) {
        document.add(
            Paragraph("DETAILED DESCRIPTIONS")
                .setFont(boldFont)
                .setFontSize(14f)
                .setMarginBottom(10f)
        )
        
        incidents.filter { it.description.isNotBlank() }
            .sortedByDescending { it.timestamp }
            .forEach { incident ->
                document.add(
                    Paragraph()
                        .add(Text("${incident.dateTime} - ${incident.incident_type}").setFont(boldFont).setFontSize(10f))
                        .add(Text(" (${incident.severity_level.name})").setFont(font).setFontSize(9f))
                        .setMarginBottom(5f)
                )
                
                document.add(
                    Paragraph("Location: ${incident.location}")
                        .setFont(font)
                        .setFontSize(9f)
                        .setMarginLeft(10f)
                        .setMarginBottom(3f)
                )
                
                document.add(
                    Paragraph("Description: ${incident.description}")
                        .setFont(font)
                        .setFontSize(9f)
                        .setMarginLeft(10f)
                        .setMarginBottom(15f)
                )
            }
    }
}