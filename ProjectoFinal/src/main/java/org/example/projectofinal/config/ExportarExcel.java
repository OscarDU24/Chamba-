package org.example.projectofinal.config;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

import java.io.File;

public class ExportarExcel implements IExportar {

    @Override
    public void exportar(JasperPrint reporte, File destino)
            throws JRException {

        JRXlsxExporter exportador = new JRXlsxExporter();

        exportador.setExporterInput(
                new SimpleExporterInput(reporte)
        );

        exportador.setExporterOutput(
                new SimpleOutputStreamExporterOutput(destino)
        );

        SimpleXlsxReportConfiguration configuracion =
                new SimpleXlsxReportConfiguration();

        configuracion.setOnePagePerSheet(false);
        configuracion.setDetectCellType(true);
        configuracion.setRemoveEmptySpaceBetweenRows(true);
        configuracion.setWhitePageBackground(false);

        exportador.setConfiguration(configuracion);
        exportador.exportReport();
    }
}