package com.yby.api.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.yby.api.dto.CarbonoHistoricoDTO;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PdfReportService {

    public byte[] gerarRelatorioCarbono(LocalDate dataInicio, LocalDate dataFim, List<CarbonoHistoricoDTO> historico) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, output);

            document.open();
            document.add(new Paragraph("JREDD+ Intelligence - Relatorio de Menor Emissao de Carbono",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph("Periodo: " + dataInicio + " a " + dataFim,
                FontFactory.getFont(FontFactory.HELVETICA, 11)));
            document.add(new Paragraph("Gerado em: "
                + OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                FontFactory.getFont(FontFactory.HELVETICA, 10)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.3f, 2.6f, 2f, 2f, 2f});

            addHeader(table, "ID");
            addHeader(table, "Municipio");
            addHeader(table, "Codigo IBGE");
            addHeader(table, "Emissao Total (tCO2e)");
            addHeader(table, "Media Diaria (tCO2e)");

            for (CarbonoHistoricoDTO item : historico) {
                table.addCell(text(item.municipioId() == null ? "" : String.valueOf(item.municipioId())));
                table.addCell(text(item.nome()));
                table.addCell(text(item.codigoIbge()));
                table.addCell(text(item.emissaoTotal() == null ? "-" : item.emissaoTotal().toPlainString()));
                table.addCell(text(item.mediaDiaria() == null ? "-" : item.mediaDiaria().toPlainString()));
            }

            document.add(table);
            document.close();
            return output.toByteArray();
        } catch (DocumentException ex) {
            throw new IllegalStateException("Falha ao gerar PDF", ex);
        }
    }

    private void addHeader(PdfPTable table, String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private PdfPCell text(String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, FontFactory.getFont(FontFactory.HELVETICA, 10)));
        cell.setPadding(6);
        return cell;
    }
}
