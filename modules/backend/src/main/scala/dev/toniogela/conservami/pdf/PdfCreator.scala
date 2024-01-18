package dev.toniogela.conservami.pdf

import cats.effect.*
import fs2.*
import fs2.io.readOutputStream
import com.lowagie.text.*
import com.lowagie.text.pdf.*
import dev.toniogela.conservami.UserAdd
import java.time.ZonedDateTime

object PdfCreator:

  def create[F[_]: Async](userAdd: UserAdd): Stream[F, Byte] = fs2.io.readOutputStream(2048) { os =>
    Sync[F].delay {
      val document: Document = new Document(PageSize.A4, 36, 36, 65, 36)
      val writer: PdfWriter  = PdfWriter.getInstance(document, os)
      writer.setPageEvent(new HeaderAndFooterPageEventHelper())
      document.open()

      val text: Phrase =
        new Phrase("Il tal giorno alla tal ora, la seguente persona è diventata socio\n")

      text.add(s"Nome: ${userAdd.name}\n")
      text.add(s"Cognome: ${userAdd.surname}\n")
      text.add(s"Luogo di Nascita: ${userAdd.birthPlace}\n")
      text.add(s"Data di Nascita: ${userAdd.birthDate}\n")
      text.add(s"Codice Fiscale: ${userAdd.fiscalCode.value}\n")
      text.add(s"Residenza: ${userAdd.residence}\n")
      text.add(s"Numero di Telefono: ${userAdd.phoneNumber.value}\n")
      text.add(s"Email: ${userAdd.email.value}\n")
      text.add(s"Professione: ${userAdd.profession}\n")
      text.add(s"Membro Da: ${ZonedDateTime.now}\n")
      text.add(s"Numero Tessera: ${userAdd.membershipCardNumber}\n")
      text.add(s"Donazione: ${userAdd.donation}\n")

      document.add(new Paragraph(text))

      // document.newPage()
      // val page2Body: Paragraph = new Paragraph("Page two content.")
      // page2Body.setAlignment(Element.ALIGN_CENTER)
      // document.add(page2Body)

      document.close()
      writer.close()
    }
  }

class HeaderAndFooterPageEventHelper() extends PdfPageEventHelper {

  override def onStartPage(writer: PdfWriter, document: Document): Unit = {

    val table: PdfPTable = new PdfPTable(3)
    table.setTotalWidth(510)
    table.setWidths(Array[Int](38, 36, 36))
    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER)
    table.getDefaultCell().setPaddingBottom(5)
    table.getDefaultCell().setBorder(Rectangle.BOTTOM)

    val emptyCell: PdfPCell = new PdfPCell(new Paragraph(""))
    emptyCell.setBorder(Rectangle.NO_BORDER)

    // Row#1 having 1 empty cell, 1 title cell and empty cell.
    table.addCell(emptyCell)
    val title: Paragraph    = new Paragraph("Conservami", new Font(Font.COURIER, 20, Font.BOLD))
    val titleCell: PdfPCell = new PdfPCell(title)
    titleCell.setPaddingBottom(10)
    titleCell.setHorizontalAlignment(Element.ALIGN_CENTER)
    titleCell.setBorder(Rectangle.NO_BORDER)

    table.addCell(titleCell)
    table.addCell(emptyCell)

    // Row#2 having 3 cells
    val cellFont: Font = new Font(Font.HELVETICA, 8)
    table.addCell(new Paragraph("Phone Number: 888-999-0000", cellFont))
    table.addCell(new Paragraph("Address : 333, Manhattan, New York", cellFont))
    table.addCell(new Paragraph("Instagram: @conservami", cellFont))

    // write the table on PDF
    table.writeSelectedRows(0, -1, 34, 828, writer.getDirectContent())
    ()
  }

  override def onEndPage(writer: PdfWriter, document: Document): Unit = {

    /* Footer */
    val table: PdfPTable = new PdfPTable(2)
    table.setTotalWidth(510)
    table.setWidths(Array[Int](50, 50))
    /* Magic about default cell - if you add styling to default cell it will apply to all cells
     * except cells added using addCell(PdfPCell) method. */
    table.getDefaultCell().setPaddingBottom(5)
    table.getDefaultCell().setBorder(Rectangle.TOP)

    val title: Paragraph    = new Paragraph("Conservami", new Font(Font.HELVETICA, 10))
    val titleCell: PdfPCell = new PdfPCell(title)
    titleCell.setPaddingTop(4)
    titleCell.setHorizontalAlignment(Element.ALIGN_LEFT)
    titleCell.setBorder(Rectangle.TOP)
    table.addCell(titleCell)

    val pageNumberText: Paragraph =
      new Paragraph("Pagina " + document.getPageNumber(), new Font(Font.HELVETICA, 10))
    val pageNumberCell: PdfPCell  = new PdfPCell(pageNumberText)
    pageNumberCell.setPaddingTop(4)
    pageNumberCell.setHorizontalAlignment(Element.ALIGN_RIGHT)
    pageNumberCell.setBorder(Rectangle.TOP)
    table.addCell(pageNumberCell)

    // write the table on PDF
    table.writeSelectedRows(0, -1, 34, 36, writer.getDirectContent())
    ()
  }
}
